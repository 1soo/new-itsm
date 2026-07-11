package com.itsm.common.approval.application;

import com.itsm.common.approval.domain.ApprovalProcess;
import com.itsm.common.approval.domain.ApprovalProcessRequesterRole;
import com.itsm.common.approval.domain.ApprovalProcessStep;
import com.itsm.common.approval.domain.ApprovalProcessStepRole;
import com.itsm.common.approval.domain.ApprovalRequest;
import com.itsm.common.approval.domain.ApprovalRequestStatus;
import com.itsm.common.approval.domain.ApprovalRequestStep;
import com.itsm.common.approval.domain.ApprovalRequestStepRole;
import com.itsm.common.approval.domain.repository.ApprovalProcessRepository;
import com.itsm.common.approval.domain.repository.ApprovalProcessRequesterRoleRepository;
import com.itsm.common.approval.domain.repository.ApprovalProcessStepRepository;
import com.itsm.common.approval.domain.repository.ApprovalProcessStepRoleRepository;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.approval.domain.repository.ApprovalRequestStepRepository;
import com.itsm.common.approval.domain.repository.ApprovalRequestStepRoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.ticket.TicketType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 전 도메인 공용 승인 게이트 체크(common.md 0절). 각 도메인의 상태 전이 서비스가 게이트가 걸린 target
 * 전이를 처리하기 전에 호출한다. 통과하면 조용히 반환하고, 막히면 {@link BusinessException}(APPROVAL_PENDING, 409)을
 * 던진다(응답에 진행 중 인스턴스의 approvalRequestId를 함께 싣는다).
 */
@Service
public class ApprovalGateService {

    private final ApprovalProcessRepository approvalProcessRepository;
    private final ApprovalProcessRequesterRoleRepository requesterRoleRepository;
    private final ApprovalProcessStepRepository processStepRepository;
    private final ApprovalProcessStepRoleRepository processStepRoleRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalRequestStepRepository requestStepRepository;
    private final ApprovalRequestStepRoleRepository requestStepRoleRepository;
    private final ApprovalRoleResolver roleResolver;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public ApprovalGateService(ApprovalProcessRepository approvalProcessRepository,
                               ApprovalProcessRequesterRoleRepository requesterRoleRepository,
                               ApprovalProcessStepRepository processStepRepository,
                               ApprovalProcessStepRoleRepository processStepRoleRepository,
                               ApprovalRequestRepository approvalRequestRepository,
                               ApprovalRequestStepRepository requestStepRepository,
                               ApprovalRequestStepRoleRepository requestStepRoleRepository,
                               ApprovalRoleResolver roleResolver,
                               PlatformTransactionManager transactionManager) {
        this.approvalProcessRepository = approvalProcessRepository;
        this.requesterRoleRepository = requesterRoleRepository;
        this.processStepRepository = processStepRepository;
        this.processStepRoleRepository = processStepRoleRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.requestStepRepository = requestStepRepository;
        this.requestStepRoleRepository = requestStepRoleRepository;
        this.roleResolver = roleResolver;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * 게이트 체크. 매칭 규칙이 없거나 0차 승인이면 조용히 통과한다.
     * 매칭 규칙이 있으면 티켓의 가장 최근 인스턴스 상태에 따라 통과(APPROVED)하거나 409(그 외)를 던진다.
     * 인스턴스 생성은 별도 트랜잭션(REQUIRES_NEW)으로 커밋한다 — 호출측(도메인 상태 전이 서비스)이 이 메서드의
     * 예외로 자신의 트랜잭션을 롤백하더라도, 방금 생성된 승인 인스턴스는 그대로 남아야 하기 때문이다.
     */
    public void checkGate(String domain, String requestSubtypeKey, Long requesterId, TicketType ticketType, Long ticketId) {
        ApprovalProcess matched = matchProcess(domain, requestSubtypeKey, requesterId);
        if (matched == null) {
            return;
        }
        List<ApprovalProcessStep> steps = processStepRepository.findByApprovalProcessIdOrderByStepNoAsc(matched.getId());
        if (steps.isEmpty()) {
            return;
        }
        ApprovalRequest latest = approvalRequestRepository
                .findTopByTicketTypeAndTicketIdOrderByIdDesc(ticketType, ticketId).orElse(null);
        if (latest == null) {
            ApprovalRequest created = requiresNewTransactionTemplate.execute(
                    status -> createInstance(matched, steps, ticketType, ticketId));
            throw new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), created.getId());
        }
        if (latest.getStatus() == ApprovalRequestStatus.APPROVED) {
            return;
        }
        throw new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), latest.getId());
    }

    private ApprovalProcess matchProcess(String domain, String requestSubtypeKey, Long requesterId) {
        List<ApprovalProcess> candidates = approvalProcessRepository.findByDomain(domain);
        if (candidates.isEmpty()) {
            return null;
        }
        Set<Long> requesterRoleIds = new HashSet<>(
                roleResolver.roleIdsOf(roleResolver.roleCodesOfUser(requesterId)));
        ApprovalProcess best = null;
        for (ApprovalProcess candidate : candidates) {
            if (candidate.getRequestSubtypeKey() != null && !candidate.getRequestSubtypeKey().equals(requestSubtypeKey)) {
                continue;
            }
            List<ApprovalProcessRequesterRole> scopeRoles = requesterRoleRepository.findByApprovalProcessId(candidate.getId());
            if (!scopeRoles.isEmpty()) {
                boolean anyMatch = scopeRoles.stream()
                        .map(ApprovalProcessRequesterRole::getRoleId)
                        .anyMatch(requesterRoleIds::contains);
                if (!anyMatch) {
                    continue;
                }
            }
            if (best == null || candidate.getPriorityTier() > best.getPriorityTier()) {
                best = candidate;
            }
        }
        return best;
    }

    private ApprovalRequest createInstance(ApprovalProcess process, List<ApprovalProcessStep> steps,
                                            TicketType ticketType, Long ticketId) {
        ApprovalRequest request = approvalRequestRepository.save(
                new ApprovalRequest(ticketType, ticketId, process.getId(), steps.get(0).getStepNo()));
        for (ApprovalProcessStep step : steps) {
            ApprovalRequestStep requestStep = requestStepRepository.save(
                    new ApprovalRequestStep(request.getId(), step.getStepNo(), step.getDecisionMode()));
            for (ApprovalProcessStepRole role : processStepRoleRepository.findByStepId(step.getId())) {
                requestStepRoleRepository.save(new ApprovalRequestStepRole(requestStep.getId(), role.getRoleId()));
            }
        }
        return request;
    }
}
