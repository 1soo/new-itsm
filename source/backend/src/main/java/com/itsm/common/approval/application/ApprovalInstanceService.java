package com.itsm.common.approval.application;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.dto.ApprovalDecisionResultResponse;
import com.itsm.common.approval.application.dto.ApprovalDetailResponse;
import com.itsm.common.approval.application.dto.ApprovalInboxItemResponse;
import com.itsm.common.approval.domain.ApprovalDecision;
import com.itsm.common.approval.domain.ApprovalProcess;
import com.itsm.common.approval.domain.ApprovalRequest;
import com.itsm.common.approval.domain.ApprovalRequestStatus;
import com.itsm.common.approval.domain.ApprovalRequestStep;
import com.itsm.common.approval.domain.ApprovalRequestStepRole;
import com.itsm.common.approval.domain.ApprovalStepStatus;
import com.itsm.common.approval.domain.DecisionMode;
import com.itsm.common.approval.domain.DecisionType;
import com.itsm.common.approval.domain.repository.ApprovalDecisionRepository;
import com.itsm.common.approval.domain.repository.ApprovalProcessRepository;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.approval.domain.repository.ApprovalRequestStepRepository;
import com.itsm.common.approval.domain.repository.ApprovalRequestStepRoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.TicketType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * 전 도메인 공용 승인 대기함·상세·결정 유스케이스(API-COM-003~005, common.md 0절).
 */
@Service
public class ApprovalInstanceService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalRequestStepRepository requestStepRepository;
    private final ApprovalRequestStepRoleRepository requestStepRoleRepository;
    private final ApprovalDecisionRepository decisionRepository;
    private final ApprovalProcessRepository approvalProcessRepository;
    private final TargetStateLabelResolver targetStateLabelResolver;
    private final ApprovalRoleResolver roleResolver;
    private final AppUserRepository appUserRepository;
    private final Map<TicketType, ApprovalTicketSummaryProvider> summaryProviders;
    private final Map<TicketType, ApprovalDecisionCallback> decisionCallbacks;

    public ApprovalInstanceService(ApprovalRequestRepository approvalRequestRepository,
                                   ApprovalRequestStepRepository requestStepRepository,
                                   ApprovalRequestStepRoleRepository requestStepRoleRepository,
                                   ApprovalDecisionRepository decisionRepository,
                                   ApprovalProcessRepository approvalProcessRepository,
                                   TargetStateLabelResolver targetStateLabelResolver,
                                   ApprovalRoleResolver roleResolver,
                                   AppUserRepository appUserRepository,
                                   List<ApprovalTicketSummaryProvider> summaryProviders,
                                   List<ApprovalDecisionCallback> decisionCallbacks) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.requestStepRepository = requestStepRepository;
        this.requestStepRoleRepository = requestStepRoleRepository;
        this.decisionRepository = decisionRepository;
        this.approvalProcessRepository = approvalProcessRepository;
        this.targetStateLabelResolver = targetStateLabelResolver;
        this.roleResolver = roleResolver;
        this.appUserRepository = appUserRepository;
        this.summaryProviders = summaryProviders.stream()
                .collect(java.util.stream.Collectors.toMap(ApprovalTicketSummaryProvider::supportedType, Function.identity()));
        this.decisionCallbacks = decisionCallbacks.stream()
                .collect(java.util.stream.Collectors.toMap(ApprovalDecisionCallback::supportedType, Function.identity()));
    }

    // ---------- API-COM-003 ----------

    @Transactional(readOnly = true)
    public PageResponse<ApprovalInboxItemResponse> inbox(String domain, int page, int size) {
        boolean admin = SecurityUtils.isSystemAdmin();
        Set<Long> myRoleIds = admin ? Set.of()
                : new HashSet<>(roleResolver.roleIdsOf(SecurityUtils.currentPrincipal().roles()));

        List<ApprovalInboxItemResponse> matched = new ArrayList<>();
        for (ApprovalRequest ar : approvalRequestRepository.findByStatusAndDomain(ApprovalRequestStatus.IN_PROGRESS, domain)) {
            if (ar.getCurrentStepNo() == null) {
                continue;
            }
            ApprovalRequestStep step = requestStepRepository
                    .findByApprovalRequestIdAndStepNo(ar.getId(), ar.getCurrentStepNo()).orElse(null);
            if (step == null) {
                continue;
            }
            List<ApprovalRequestStepRole> stepRoles = requestStepRoleRepository.findByStepId(step.getId());
            boolean eligible = admin || stepRoles.stream().anyMatch(r ->
                    myRoleIds.contains(r.getRoleId()) && !decisionRepository.existsByStepIdAndRoleId(step.getId(), r.getRoleId()));
            if (eligible) {
                matched.add(toInboxItem(ar));
            }
        }
        int total = matched.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        return new PageResponse<>(matched.subList(from, to), page, size, total);
    }

    // ---------- API-COM-004 ----------

    @Transactional(readOnly = true)
    public ApprovalDetailResponse detail(Long id) {
        ApprovalRequest ar = findRequest(id);
        List<ApprovalDetailResponse.StepDto> steps = requestStepRepository
                .findByApprovalRequestIdOrderByStepNoAsc(id).stream()
                .map(this::toStepDto)
                .toList();
        TicketSummary summary = summaryOf(ar);
        return new ApprovalDetailResponse(ar.getId(), ar.getTicketType().name(), ar.getTicketId(),
                summary != null ? summary.ticketKey() : null, ar.getTargetState(), targetStateLabelOf(ar),
                ar.getStatus().name(), ar.getCurrentStepNo(), steps);
    }

    // ---------- API-COM-005 ----------

    @Transactional
    public ApprovalDecisionResultResponse decide(Long id, DecisionType decision, String reason) {
        if (decision == DecisionType.REJECT && !StringUtils.hasText(reason)) {
            throw new BusinessException(ErrorCode.REJECT_REASON_REQUIRED);
        }
        ApprovalRequest ar = findRequest(id);
        if (!ar.isInProgress()) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY_DECIDED);
        }
        ApprovalRequestStep step = requestStepRepository.findByApprovalRequestIdAndStepNo(ar.getId(), ar.getCurrentStepNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NOT_FOUND));

        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        List<ApprovalRequestStepRole> stepRoles = requestStepRoleRepository.findByStepId(step.getId());
        List<Long> myRoleIds;
        if (SecurityUtils.isSystemAdmin()) {
            myRoleIds = stepRoles.stream().map(ApprovalRequestStepRole::getRoleId).toList();
        } else {
            Set<Long> mine = new HashSet<>(roleResolver.roleIdsOf(principal.roles()));
            myRoleIds = stepRoles.stream().map(ApprovalRequestStepRole::getRoleId).filter(mine::contains).toList();
        }
        if (myRoleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        List<Long> undecidedRoleIds = myRoleIds.stream()
                .filter(roleId -> !decisionRepository.existsByStepIdAndRoleId(step.getId(), roleId))
                .toList();
        if (undecidedRoleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY_DECIDED);
        }
        for (Long roleId : undecidedRoleIds) {
            decisionRepository.save(new ApprovalDecision(step.getId(), roleId, principal.userId(), decision, reason));
        }

        ApprovalStepStatus outcome = resolveStepOutcome(step, decision);
        if (outcome == ApprovalStepStatus.PENDING) {
            return new ApprovalDecisionResultResponse(ar.getId(), step.getStepNo(), outcome.name(), ar.getStatus().name());
        }
        if (outcome == ApprovalStepStatus.APPROVED) {
            step.approve();
        } else {
            step.reject();
        }
        requestStepRepository.save(step);

        if (outcome == ApprovalStepStatus.REJECTED) {
            ar.reject();
            approvalRequestRepository.save(ar);
            skipRemainingSteps(ar.getId(), step.getStepNo());
            notifyFinalized(ar, false, reason);
        } else {
            Optional<ApprovalRequestStep> next = requestStepRepository.findByApprovalRequestIdOrderByStepNoAsc(ar.getId())
                    .stream().filter(s -> s.getStepNo() > step.getStepNo()).findFirst();
            if (next.isPresent()) {
                ar.advanceTo(next.get().getStepNo());
                approvalRequestRepository.save(ar);
            } else {
                ar.approve();
                approvalRequestRepository.save(ar);
                notifyFinalized(ar, true, null);
            }
        }
        return new ApprovalDecisionResultResponse(ar.getId(), step.getStepNo(), outcome.name(), ar.getStatus().name());
    }

    // ---------- helpers ----------

    private void notifyFinalized(ApprovalRequest ar, boolean approved, String reason) {
        ApprovalDecisionCallback callback = decisionCallbacks.get(ar.getTicketType());
        if (callback == null) {
            return;
        }
        if (approved) {
            callback.onApproved(ar.getTicketId());
        } else {
            callback.onRejected(ar.getTicketId(), reason);
        }
    }

    private ApprovalStepStatus resolveStepOutcome(ApprovalRequestStep step, DecisionType justDecided) {
        if (step.getDecisionMode() == DecisionMode.OR) {
            return justDecided == DecisionType.APPROVE ? ApprovalStepStatus.APPROVED : ApprovalStepStatus.REJECTED;
        }
        List<ApprovalRequestStepRole> requiredRoles = requestStepRoleRepository.findByStepId(step.getId());
        List<ApprovalDecision> decisions = decisionRepository.findByStepId(step.getId());
        if (decisions.stream().anyMatch(d -> d.getDecision() == DecisionType.REJECT)) {
            return ApprovalStepStatus.REJECTED;
        }
        boolean allApproved = requiredRoles.stream().allMatch(r -> decisions.stream()
                .anyMatch(d -> d.getRoleId().equals(r.getRoleId()) && d.getDecision() == DecisionType.APPROVE));
        return allApproved ? ApprovalStepStatus.APPROVED : ApprovalStepStatus.PENDING;
    }

    private void skipRemainingSteps(Long approvalRequestId, short fromStepNoExclusive) {
        for (ApprovalRequestStep s : requestStepRepository.findByApprovalRequestIdOrderByStepNoAsc(approvalRequestId)) {
            if (s.getStepNo() > fromStepNoExclusive && s.getStatus() == ApprovalStepStatus.PENDING) {
                s.skip();
                requestStepRepository.save(s);
            }
        }
    }

    private ApprovalDetailResponse.StepDto toStepDto(ApprovalRequestStep step) {
        List<ApprovalDetailResponse.RoleDto> roles = requestStepRoleRepository.findByStepId(step.getId()).stream()
                .map(stepRole -> {
                    ApprovalDecision decision = decisionRepository
                            .findByStepIdAndRoleId(step.getId(), stepRole.getRoleId()).orElse(null);
                    Role role = roleResolver.findById(stepRole.getRoleId()).orElse(null);
                    return new ApprovalDetailResponse.RoleDto(
                            role != null ? role.getRoleCode() : null,
                            role != null ? role.getRoleName() : null,
                            decision != null ? decision.getDecision().name() : "PENDING",
                            decision != null ? userName(decision.getDecidedById()) : null,
                            decision != null ? decision.getReason() : null,
                            decision != null ? decision.getDecidedAt() : null);
                })
                .toList();
        return new ApprovalDetailResponse.StepDto(step.getStepNo(), step.getDecisionMode().name(), step.getStatus().name(), roles);
    }

    private ApprovalInboxItemResponse toInboxItem(ApprovalRequest ar) {
        TicketSummary summary = summaryOf(ar);
        return new ApprovalInboxItemResponse(ar.getId(), ar.getTicketType().name(), ar.getTicketId(),
                summary != null ? summary.ticketKey() : null,
                summary != null ? summary.title() : null,
                ar.getTargetState(), targetStateLabelOf(ar),
                summary != null ? summary.requesterName() : null,
                ar.getCurrentStepNo(), ar.getCreatedAt());
    }

    /** targetState 표시 라벨(도메인은 인스턴스가 참조하는 규칙(approvalProcessId)에서 역조회, 2026-07-22 신규). */
    private String targetStateLabelOf(ApprovalRequest ar) {
        String domain = approvalProcessRepository.findById(ar.getApprovalProcessId())
                .map(ApprovalProcess::getDomain).orElse(null);
        return targetStateLabelResolver.label(domain, ar.getTargetState());
    }

    private TicketSummary summaryOf(ApprovalRequest ar) {
        ApprovalTicketSummaryProvider provider = summaryProviders.get(ar.getTicketType());
        return provider != null ? provider.summaryOf(ar.getTicketId()) : null;
    }

    private ApprovalRequest findRequest(Long id) {
        return approvalRequestRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NOT_FOUND));
    }

    private String userName(Long id) {
        return id == null ? null : appUserRepository.findById(id).map(AppUser::getName).orElse(null);
    }
}
