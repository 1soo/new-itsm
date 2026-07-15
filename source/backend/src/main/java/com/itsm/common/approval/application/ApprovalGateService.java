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
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.TicketType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
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

    /**
     * 승인 대상자 역할 기반 동적 상세조회 권한(common.md 0-1절). 티켓의 (도메인, 요청유형, 요청자 역할)로 매칭되는
     * 규칙의 전체 차수(현재 차수만이 아님 — 인스턴스 생성 전에도 조회 가능해야 하므로)에 지정된 승인자 역할 중
     * 로그인 사용자(조회 요청자)가 하나라도 보유하면 true.
     */
    public boolean canApproverView(String domain, String requestSubtypeKey, Long requesterId) {
        ApprovalProcess matched = matchProcess(domain, requestSubtypeKey, requesterId);
        if (matched == null) {
            return false;
        }
        List<ApprovalProcessStep> steps = processStepRepository.findByApprovalProcessIdOrderByStepNoAsc(matched.getId());
        Set<Long> approverRoleIds = new HashSet<>();
        for (ApprovalProcessStep step : steps) {
            processStepRoleRepository.findByStepId(step.getId())
                    .forEach(role -> approverRoleIds.add(role.getRoleId()));
        }
        if (approverRoleIds.isEmpty()) {
            return false;
        }
        Set<Long> viewerRoleIds = new HashSet<>(
                roleResolver.roleIdsOf(SecurityUtils.currentPrincipal().roles()));
        return !Collections.disjoint(approverRoleIds, viewerRoleIds);
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

    /**
     * KNOWLEDGE처럼 "전이 요청은 항상 성공(200)하되 게이트 매칭 여부로 결과 상태만 갈리는" 도메인을 위한
     * 논-스로잉 게이트 평가. {@link #checkGate}와 달리 이전 인스턴스 상태를 재사용하지 않고, 매칭 규칙이
     * 있으면(1차 이상) 항상 새 인스턴스를 생성한다(반려 후 재검토 요청 시에도 신규 인스턴스, common.md 0절).
     * 별도 트랜잭션이 필요 없다 — 호출측 전이 자체가 이 결과와 함께 정상 커밋되는 구조이기 때문이다.
     */
    public GateDecision evaluateAndCreateIfNeeded(String domain, String requestSubtypeKey, Long requesterId,
                                                  TicketType ticketType, Long ticketId) {
        ApprovalProcess matched = matchProcess(domain, requestSubtypeKey, requesterId);
        if (matched == null) {
            return new GateDecision(true, null);
        }
        List<ApprovalProcessStep> steps = processStepRepository.findByApprovalProcessIdOrderByStepNoAsc(matched.getId());
        if (steps.isEmpty()) {
            return new GateDecision(true, null);
        }
        ApprovalRequest created = createInstance(matched, steps, ticketType, ticketId);
        return new GateDecision(false, created.getId());
    }

    /** {@link #evaluateAndCreateIfNeeded} 판정 결과(통과 여부 + 생성된 인스턴스 id, 통과 시 null). */
    public record GateDecision(boolean passed, Long approvalRequestId) {
    }
}
