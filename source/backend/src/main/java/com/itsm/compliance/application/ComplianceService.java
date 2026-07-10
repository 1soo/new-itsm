package com.itsm.compliance.application;

import com.itsm.auth.application.AuditLogService;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.AuditLog;
import com.itsm.auth.domain.AuditResult;
import com.itsm.auth.domain.EventType;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.repository.ChangeRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.TicketLink;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.compliance.application.dto.ComplianceAuditLogResponse;
import com.itsm.compliance.application.dto.ComplianceMetricsResponse;
import com.itsm.compliance.application.dto.CorrectiveActionCreateRequest;
import com.itsm.compliance.application.dto.CorrectiveActionCreatedResponse;
import com.itsm.compliance.application.dto.CorrectiveActionStatusResponse;
import com.itsm.compliance.application.dto.CorrectiveActionStatusTransitionRequest;
import com.itsm.compliance.application.dto.CreateRequirementRequest;
import com.itsm.compliance.application.dto.LinkRequest;
import com.itsm.compliance.application.dto.OwnerRequest;
import com.itsm.compliance.application.dto.OwnerResponse;
import com.itsm.compliance.application.dto.RequirementCreatedResponse;
import com.itsm.compliance.application.dto.RequirementDetailResponse;
import com.itsm.compliance.application.dto.RequirementSummaryResponse;
import com.itsm.compliance.application.dto.UpdateRequirementRequest;
import com.itsm.compliance.domain.ComplianceRequirement;
import com.itsm.compliance.domain.ComplianceStatus;
import com.itsm.compliance.domain.CorrectiveAction;
import com.itsm.compliance.domain.CorrectiveActionStatus;
import com.itsm.compliance.domain.repository.ComplianceRequirementRepository;
import com.itsm.compliance.domain.repository.CorrectiveActionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 컴플라이언스 요구사항 유스케이스: 등록·조회·수정·변경연계·책임자지정·시정조치(등록/전이)·감사로그조회·준수현황.
 * RBAC(compliance_officer.md): 전 API가 COMPLIANCE_OFFICER 전용(그 외 역할 403).
 */
@Service
public class ComplianceService {

    private static final String CO = "COMPLIANCE_OFFICER";
    private static final OffsetDateTime EPOCH = OffsetDateTime.parse("1970-01-01T00:00:00Z");
    private static final Set<EventType> COMPLIANCE_EVENT_TYPES = Set.of(
            EventType.COMPLIANCE_REQ_CREATE, EventType.COMPLIANCE_REQ_UPDATE, EventType.COMPLIANCE_ACTION_STATUS_CHANGE);
    private static final Set<CorrectiveActionStatus> UNRESOLVED = Set.of(
            CorrectiveActionStatus.DETECTED, CorrectiveActionStatus.IN_PROGRESS);

    private final ComplianceRequirementRepository requirementRepository;
    private final CorrectiveActionRepository correctiveActionRepository;
    private final TicketLinkRepository ticketLinkRepository;
    private final AppUserRepository appUserRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final AuditLogService auditLogService;

    public ComplianceService(ComplianceRequirementRepository requirementRepository,
                              CorrectiveActionRepository correctiveActionRepository,
                              TicketLinkRepository ticketLinkRepository,
                              AppUserRepository appUserRepository,
                              ChangeRequestRepository changeRequestRepository,
                              AuditLogService auditLogService) {
        this.requirementRepository = requirementRepository;
        this.correctiveActionRepository = correctiveActionRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.appUserRepository = appUserRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.auditLogService = auditLogService;
    }

    // ---------- create (API-COMP-002) ----------

    @Transactional
    public RequirementCreatedResponse create(CreateRequirementRequest request) {
        requireRole();
        ComplianceRequirement saved = requirementRepository.save(
                new ComplianceRequirement(nextRequirementKey(), request.name(), request.basis(), request.scope()));
        recordAudit(EventType.COMPLIANCE_REQ_CREATE, requirementTarget(saved.getId()));
        return new RequirementCreatedResponse(saved.getId(), saved.getRequirementKey());
    }

    // ---------- list (API-COMP-001) ----------

    @Transactional(readOnly = true)
    public PageResponse<RequirementSummaryResponse> list(ComplianceStatus complianceStatus, Boolean ownerAssigned,
                                                          String keyword, Pageable pageable) {
        requireRole();
        var page = requirementRepository.search(
                complianceStatus != null ? complianceStatus.name() : null, ownerAssigned, keyword, pageable);
        Map<Long, ComplianceStatus> statusById = batchComplianceStatus(
                page.getContent().stream().map(ComplianceRequirement::getId).toList());
        return PageResponse.from(page, r -> new RequirementSummaryResponse(
                r.getId(), r.getRequirementKey(), r.getName(), r.getBasis(), userName(r.getOwnerId()),
                statusById.get(r.getId()).name(), r.getUpdatedAt()));
    }

    // ---------- detail (API-COMP-003) ----------

    @Transactional(readOnly = true)
    public RequirementDetailResponse detail(Long id) {
        requireRole();
        return toDetail(findRequirement(id));
    }

    // ---------- update (API-COMP-004) ----------

    @Transactional
    public void update(Long id, UpdateRequirementRequest request) {
        requireRole();
        ComplianceRequirement requirement = findRequirement(id);
        requirement.updateDetails(request.name(), request.basis(), request.scope());
        requirementRepository.save(requirement);
        recordAudit(EventType.COMPLIANCE_REQ_UPDATE, requirementTarget(id));
    }

    // ---------- change link (API-COMP-005) ----------

    @Transactional
    public void link(Long id, LinkRequest request) {
        requireRole();
        findRequirement(id);
        boolean exists = changeRequestRepository.findById(request.changeId())
                .filter(c -> !c.isDeleted()).isPresent();
        if (!exists) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "존재하지 않는 변경 요청입니다.");
        }
        if (!ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
                TicketType.COMPLIANCE_REQUIREMENT, id, TicketType.CHANGE, request.changeId())) {
            ticketLinkRepository.save(new TicketLink(
                    TicketType.COMPLIANCE_REQUIREMENT, id, TicketType.CHANGE, request.changeId(), "RELATED"));
        }
    }

    // ---------- owner (API-COMP-006) ----------

    @Transactional
    public OwnerResponse assignOwner(Long id, OwnerRequest request) {
        requireRole();
        ComplianceRequirement requirement = findRequirement(id);
        AppUser owner = appUserRepository.findById(request.ownerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "유효하지 않은 사용자입니다."));
        requirement.assignOwner(owner.getId());
        requirementRepository.save(requirement);
        return new OwnerResponse(id, owner.getName());
    }

    // ---------- corrective action create (API-COMP-007) ----------

    @Transactional
    public CorrectiveActionCreatedResponse addCorrectiveAction(Long id, CorrectiveActionCreateRequest request) {
        requireRole();
        findRequirement(id);
        CorrectiveAction saved = correctiveActionRepository.save(new CorrectiveAction(id, request.description()));
        return new CorrectiveActionCreatedResponse(saved.getId(), saved.getStatus().name());
    }

    // ---------- corrective action status transition (API-COMP-008) ----------

    @Transactional
    public CorrectiveActionStatusResponse transitionCorrectiveAction(Long actionId,
                                                                      CorrectiveActionStatusTransitionRequest request) {
        requireRole();
        CorrectiveAction action = correctiveActionRepository.findById(actionId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.CORRECTIVE_ACTION_NOT_FOUND));
        CorrectiveActionStatus target = request.targetStatus();
        if (!CorrectiveActionStateMachine.isAllowed(action.getStatus(), target)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        action.changeStatus(target);
        correctiveActionRepository.save(action);
        recordAudit(EventType.COMPLIANCE_ACTION_STATUS_CHANGE, correctiveActionTarget(actionId));
        return new CorrectiveActionStatusResponse(actionId, target.name());
    }

    // ---------- compliance audit logs (API-COMP-009) ----------

    @Transactional(readOnly = true)
    public List<ComplianceAuditLogResponse> auditLogs(Long requirementId, OffsetDateTime from, OffsetDateTime to) {
        requireRole();
        OffsetDateTime fromV = from != null ? from : EPOCH;
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        List<AuditLog> logs = auditLogService.findByEventTypes(COMPLIANCE_EVENT_TYPES, fromV, toV);
        if (requirementId != null) {
            findRequirement(requirementId);
            Set<String> allowedTargets = correctiveActionRepository.findByRequirementId(requirementId).stream()
                    .map(a -> correctiveActionTarget(a.getId()))
                    .collect(Collectors.toCollection(java.util.HashSet::new));
            allowedTargets.add(requirementTarget(requirementId));
            logs = logs.stream().filter(l -> allowedTargets.contains(l.getTarget())).toList();
        }
        return logs.stream()
                .map(l -> new ComplianceAuditLogResponse(
                        l.getEventType().name(), l.getActorEmail(), l.getTarget(), l.getResult().name(), l.getOccurredAt()))
                .toList();
    }

    // ---------- metrics (API-COMP-010) ----------

    @Transactional(readOnly = true)
    public ComplianceMetricsResponse metrics(OffsetDateTime from, OffsetDateTime to) {
        requireRole();
        OffsetDateTime fromV = from != null ? from : EPOCH;
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        List<ComplianceRequirement> requirements = requirementRepository.findByCreatedAtBetween(fromV, toV);
        if (requirements.isEmpty()) {
            return new ComplianceMetricsResponse(0, 0, 0, 0, 0);
        }
        List<Long> ids = requirements.stream().map(ComplianceRequirement::getId).toList();
        List<CorrectiveAction> actions = correctiveActionRepository.findByRequirementIdIn(ids);
        Map<Long, List<CorrectiveAction>> byRequirement = actions.stream()
                .collect(Collectors.groupingBy(CorrectiveAction::getRequirementId));

        long total = requirements.size();
        long nonCompliant = requirements.stream()
                .filter(r -> hasUnresolved(byRequirement.getOrDefault(r.getId(), List.of())))
                .count();
        long compliant = total - nonCompliant;
        long openActions = actions.stream().filter(a -> UNRESOLVED.contains(a.getStatus())).count();
        double rate = total == 0 ? 0 : round((double) compliant / total);
        return new ComplianceMetricsResponse(total, compliant, nonCompliant, openActions, rate);
    }

    // ---------- helpers ----------

    private void requireRole() {
        if (!SecurityUtils.hasRole(CO)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private ComplianceRequirement findRequirement(Long id) {
        return requirementRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPLIANCE_REQUIREMENT_NOT_FOUND));
    }

    private Map<Long, ComplianceStatus> batchComplianceStatus(List<Long> requirementIds) {
        Map<Long, List<CorrectiveAction>> byRequirement = correctiveActionRepository.findByRequirementIdIn(requirementIds)
                .stream().collect(Collectors.groupingBy(CorrectiveAction::getRequirementId));
        return requirementIds.stream().collect(Collectors.toMap(id -> id,
                id -> hasUnresolved(byRequirement.getOrDefault(id, List.of()))
                        ? ComplianceStatus.NON_COMPLIANT : ComplianceStatus.COMPLIANT));
    }

    private boolean hasUnresolved(List<CorrectiveAction> actions) {
        return actions.stream().anyMatch(a -> !a.isDeleted() && UNRESOLVED.contains(a.getStatus()));
    }

    private RequirementDetailResponse toDetail(ComplianceRequirement r) {
        List<CorrectiveAction> actions = correctiveActionRepository.findByRequirementId(r.getId());
        List<RequirementDetailResponse.CorrectiveActionDto> actionDtos = actions.stream()
                .map(a -> new RequirementDetailResponse.CorrectiveActionDto(
                        a.getId(), a.getDescription(), a.getStatus().name(), a.getUpdatedAt()))
                .toList();

        List<RequirementDetailResponse.LinkedChange> linkedChanges = ticketLinkRepository
                .findBySourceTypeAndSourceId(TicketType.COMPLIANCE_REQUIREMENT, r.getId()).stream()
                .filter(l -> l.getTargetType() == TicketType.CHANGE)
                .map(l -> new RequirementDetailResponse.LinkedChange(l.getTargetId(), changeTicketKeyOf(l.getTargetId())))
                .toList();

        ComplianceStatus status = hasUnresolved(actions) ? ComplianceStatus.NON_COMPLIANT : ComplianceStatus.COMPLIANT;
        return new RequirementDetailResponse(r.getId(), r.getRequirementKey(), r.getName(), r.getBasis(), r.getScope(),
                userName(r.getOwnerId()), status.name(), actionDtos, linkedChanges);
    }

    private void recordAudit(EventType eventType, String target) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        auditLogService.record(eventType, principal.userId(), principal.email(), target, AuditResult.SUCCESS);
    }

    private String requirementTarget(Long id) {
        return "COMPLIANCE_REQUIREMENT:" + id;
    }

    private String correctiveActionTarget(Long id) {
        return "CORRECTIVE_ACTION:" + id;
    }

    private String userName(Long id) {
        return id == null ? null : appUserRepository.findById(id).map(AppUser::getName).orElse(null);
    }

    private String changeTicketKeyOf(Long changeId) {
        return changeRequestRepository.findById(changeId).map(ChangeRequest::getTicketKey).orElse(null);
    }

    private String nextRequirementKey() {
        String prefix = "COMP-" + Year.now().getValue() + "-";
        long seq = requirementRepository.countByRequirementKeyStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
