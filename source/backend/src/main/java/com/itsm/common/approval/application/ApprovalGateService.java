package com.itsm.common.approval.application;

import com.itsm.common.approval.application.dto.ApprovalResubmitResponse;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
     * 매칭 규칙이 있으면 티켓의 이 targetState로의 가장 최근 인스턴스 상태에 따라 통과(APPROVED)하거나
     * 409(IN_PROGRESS→APPROVAL_PENDING, REJECTED→APPROVAL_REJECTED)를 던진다(common.md 0절, 2026-07-22
     * 상태별 승인자 지정 확장 — REJECTED는 자동으로 새 인스턴스를 만들지 않고 {@link #resubmit}로만 재개된다).
     * 인스턴스 생성은 별도 트랜잭션(REQUIRES_NEW)으로 커밋한다 — 호출측(도메인 상태 전이 서비스)이 이 메서드의
     * 예외로 자신의 트랜잭션을 롤백하더라도, 방금 생성된 승인 인스턴스는 그대로 남아야 하기 때문이다.
     */
    public void checkGate(String domain, String requestSubtypeKey, Long requesterId, TicketType ticketType,
                           Long ticketId, String targetState) {
        ApprovalProcess matched = matchProcess(domain, requestSubtypeKey, requesterId, targetState);
        if (matched == null) {
            return;
        }
        List<ApprovalProcessStep> steps = processStepRepository.findByApprovalProcessIdOrderByStepNoAsc(matched.getId());
        if (steps.isEmpty()) {
            return;
        }
        ApprovalRequest latest = approvalRequestRepository
                .findTopByTicketTypeAndTicketIdAndTargetStateOrderByIdDesc(ticketType, ticketId, targetState).orElse(null);
        if (latest == null) {
            ApprovalRequest created = requiresNewTransactionTemplate.execute(
                    status -> createInstance(matched, steps, ticketType, ticketId, targetState));
            throw new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), created.getId());
        }
        if (latest.getStatus() == ApprovalRequestStatus.APPROVED) {
            return;
        }
        if (latest.getStatus() == ApprovalRequestStatus.REJECTED) {
            throw new BusinessException(ErrorCode.APPROVAL_REJECTED, ErrorCode.APPROVAL_REJECTED.getDefaultMessage(), latest.getId());
        }
        throw new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), latest.getId());
    }

    /**
     * 승인 대상자 역할 기반 동적 상세조회 권한(common.md 0-1절, 2026-07-22 집계 로직 변경). 티켓의
     * (도메인, 요청유형, 요청자 역할)로 매칭되는 **targetState 무관 전체 후보 규칙**(전체 상태 공통 1건 +
     * 상태별 N건) 각각의 전체 차수(현재 차수만이 아님 — 인스턴스 생성 전에도 조회 가능해야 하므로)에 지정된
     * 승인자 역할을 합집합해, 로그인 사용자(조회 요청자)가 하나라도 보유하면 true.
     */
    public boolean canApproverView(String domain, String requestSubtypeKey, Long requesterId) {
        Set<Long> approverRoleIds = collectApproverRoleCandidates(domain, requestSubtypeKey, requesterId);
        if (approverRoleIds.isEmpty()) {
            return false;
        }
        Set<Long> viewerRoleIds = new HashSet<>(
                roleResolver.roleIdsOf(SecurityUtils.currentPrincipal().roles()));
        return !Collections.disjoint(approverRoleIds, viewerRoleIds);
    }

    /**
     * 반려(REJECTED) 후 재승인요청(API-COM-006, common.md 2절). targetState 무관 "티켓 전체의 최신 인스턴스"
     * 기준으로 반려 여부를 판정하고, 그 인스턴스의 도메인/요청유형/targetState + 지금 호출한 사용자의
     * requesterId로 현재 시점 규칙을 재매칭해 새 인스턴스를 REQUIRES_NEW로 생성한다. 매칭 규칙이 사라졌다면
     * 인스턴스를 생성하지 않고 상태 "NO_RULE_MATCHED"로 응답한다. 이 메서드는 던지지 않는다(명시적 사용자
     * 액션이므로 정상 응답으로 결과를 전달) — 단, 최신 인스턴스가 없거나(404) REJECTED가 아니면(400) 예외.
     */
    public ApprovalResubmitResponse resubmit(TicketType ticketType, Long ticketId, Long requesterId) {
        ApprovalRequest latest = approvalRequestRepository
                .findTopByTicketTypeAndTicketIdOrderByIdDesc(ticketType, ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NOT_FOUND));
        if (latest.getStatus() != ApprovalRequestStatus.REJECTED) {
            throw new BusinessException(ErrorCode.APPROVAL_RESUBMIT_NOT_ALLOWED);
        }
        ApprovalProcess previous = approvalProcessRepository.findById(latest.getApprovalProcessId())
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_PROCESS_NOT_FOUND));
        String targetState = latest.getTargetState();
        ApprovalProcess matched = matchProcess(previous.getDomain(), previous.getRequestSubtypeKey(), requesterId, targetState);
        List<ApprovalProcessStep> steps = matched == null ? List.of()
                : processStepRepository.findByApprovalProcessIdOrderByStepNoAsc(matched.getId());
        if (matched == null || steps.isEmpty()) {
            return new ApprovalResubmitResponse(null, ticketType.name(), ticketId, targetState, "NO_RULE_MATCHED", null);
        }
        ApprovalProcess finalMatched = matched;
        ApprovalRequest created = requiresNewTransactionTemplate.execute(
                status -> createInstance(finalMatched, steps, ticketType, ticketId, targetState));
        return new ApprovalResubmitResponse(created.getId(), ticketType.name(), ticketId, targetState,
                created.getStatus().name(), created.getCurrentStepNo());
    }

    private ApprovalProcess matchProcess(String domain, String requestSubtypeKey, Long requesterId, String targetState) {
        List<ApprovalProcess> candidates = approvalProcessRepository.findByDomain(domain);
        if (candidates.isEmpty()) {
            return null;
        }
        Set<Long> requesterRoleIds = new HashSet<>(
                roleResolver.roleIdsOf(roleResolver.roleCodesOfUser(requesterId)));
        ApprovalProcess best = null;
        for (ApprovalProcess candidate : candidates) {
            if (candidate.getTargetState() != null && !candidate.getTargetState().equals(targetState)) {
                continue;
            }
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

    /**
     * {@link #canApproverView}용 헬퍼(common.md 0-1절). {@link #matchProcess}("tier 최고 1건 선택")와 달리
     * targetState 축을 필터링하지 않고, (도메인, 요청유형, 요청자 역할)에 매칭되는 모든 후보 규칙 각각의
     * 전체 차수 승인자 역할을 합집합으로 모은다.
     */
    private Set<Long> collectApproverRoleCandidates(String domain, String requestSubtypeKey, Long requesterId) {
        List<ApprovalProcess> candidates = approvalProcessRepository.findByDomain(domain);
        Set<Long> requesterRoleIds = new HashSet<>(
                roleResolver.roleIdsOf(roleResolver.roleCodesOfUser(requesterId)));
        Set<Long> approverRoleIds = new HashSet<>();
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
            for (ApprovalProcessStep step : processStepRepository.findByApprovalProcessIdOrderByStepNoAsc(candidate.getId())) {
                processStepRoleRepository.findByStepId(step.getId())
                        .forEach(role -> approverRoleIds.add(role.getRoleId()));
            }
        }
        return approverRoleIds;
    }

    private ApprovalRequest createInstance(ApprovalProcess process, List<ApprovalProcessStep> steps,
                                            TicketType ticketType, Long ticketId, String targetState) {
        ApprovalRequest request = approvalRequestRepository.save(
                new ApprovalRequest(ticketType, ticketId, process.getId(), targetState, steps.get(0).getStepNo()));
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
                                                  TicketType ticketType, Long ticketId, String targetState) {
        ApprovalProcess matched = matchProcess(domain, requestSubtypeKey, requesterId, targetState);
        if (matched == null) {
            return new GateDecision(true, null);
        }
        List<ApprovalProcessStep> steps = processStepRepository.findByApprovalProcessIdOrderByStepNoAsc(matched.getId());
        if (steps.isEmpty()) {
            return new GateDecision(true, null);
        }
        ApprovalRequest created = createInstance(matched, steps, ticketType, ticketId, targetState);
        return new GateDecision(false, created.getId());
    }

    /**
     * 도메인 목록 API의 pendingApprovalTargetState 배치 조회(N+1 방지, 2026-07-22 신규 — 8개 도메인
     * 서비스에 동일 코드가 중복돼 있던 것을 공용화, code review 지적 반영). ticketId → 최신 IN_PROGRESS
     * 인스턴스의 targetState. COMPLIANCE는 게이트가 요구사항이 아니라 시정조치 단위라 이 헬퍼 대상이 아니다.
     */
    public Map<Long, String> pendingApprovalTargetStatesOf(TicketType ticketType, List<Long> ticketIds) {
        if (ticketIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        for (ApprovalRequest ar : approvalRequestRepository.findByTicketTypeAndTicketIdInAndStatusOrderByIdDesc(
                ticketType, ticketIds, ApprovalRequestStatus.IN_PROGRESS)) {
            result.putIfAbsent(ar.getTicketId(), ar.getTargetState());
        }
        return result;
    }

    /** {@link #evaluateAndCreateIfNeeded} 판정 결과(통과 여부 + 생성된 인스턴스 id, 통과 시 null). */
    public record GateDecision(boolean passed, Long approvalRequestId) {
    }
}
