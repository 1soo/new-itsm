package com.itsm.change.application;

import com.itsm.asset.application.AssetService;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.change.application.dto.ChangeApprovalDecision;
import com.itsm.change.application.dto.ChangeApprovalRequest;
import com.itsm.change.application.dto.ChangeApprovalResponse;
import com.itsm.change.application.dto.ChangeCreatedResponse;
import com.itsm.change.application.dto.ChangeDetailResponse;
import com.itsm.change.application.dto.ChangeMetricsResponse;
import com.itsm.change.application.dto.ChangeSummaryResponse;
import com.itsm.change.application.dto.ChangeTemplateResponse;
import com.itsm.change.application.dto.ClassificationRequest;
import com.itsm.change.application.dto.ClassificationResponse;
import com.itsm.change.application.dto.CreateChangeRequest;
import com.itsm.change.application.dto.LinkRequest;
import com.itsm.change.application.dto.LinkResponse;
import com.itsm.change.application.dto.PendingChangeApprovalResponse;
import com.itsm.change.application.dto.ResultRequest;
import com.itsm.change.application.dto.ResultResponse;
import com.itsm.change.application.dto.ScheduleItemResponse;
import com.itsm.change.application.dto.StatusResponse;
import com.itsm.change.application.dto.StatusTransitionRequest;
import com.itsm.change.domain.ApprovalRoute;
import com.itsm.change.domain.ChangeAffectedSystem;
import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.ChangeRisk;
import com.itsm.change.domain.ChangeStatus;
import com.itsm.change.domain.ChangeType;
import com.itsm.change.domain.LinkTargetType;
import com.itsm.change.domain.Outcome;
import com.itsm.change.domain.repository.ChangeAffectedSystemRepository;
import com.itsm.change.domain.repository.ChangeRequestRepository;
import com.itsm.change.domain.repository.ChangeTemplateRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.Approval;
import com.itsm.common.ticket.ApprovalStatus;
import com.itsm.common.ticket.TicketLink;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
import com.itsm.common.ticket.repository.ApprovalRepository;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.compliance.domain.ComplianceRequirement;
import com.itsm.compliance.domain.repository.ComplianceRequirementRepository;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.problem.domain.Problem;
import com.itsm.problem.domain.repository.ProblemRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * 변경(change) 유스케이스: RFC 등록·조회·6단계 전이·분류(승인경로)·승인/반려·구현결과·
 * 인시던트/문제 연계·일정·표준 변경 템플릿·지표.
 * RBAC(change_manager.md/approver.md): 대부분 CHANGE_MANAGER 전용이며, 상세 조회는 APPROVER도 가능.
 * 승인 결정/대기 목록은 approval.approver_role(CAB/동료검토→APPROVER) 보유자가 처리(역할 기반 공유 대기함).
 */
@Service
public class ChangeService {

    private static final String CM = "CHANGE_MANAGER";
    private static final String APPROVER = "APPROVER";
    private static final TicketType TT = TicketType.CHANGE;

    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeTemplateRepository templateRepository;
    private final ChangeAffectedSystemRepository affectedSystemRepository;
    private final ApprovalRepository approvalRepository;
    private final TicketLinkRepository ticketLinkRepository;
    private final TimelineEventRepository timelineRepository;
    private final IncidentRepository incidentRepository;
    private final ProblemRepository problemRepository;
    private final AppUserRepository appUserRepository;
    private final AssetService assetService;
    private final ComplianceRequirementRepository complianceRequirementRepository;

    public ChangeService(ChangeRequestRepository changeRequestRepository,
                         ChangeTemplateRepository templateRepository,
                         ChangeAffectedSystemRepository affectedSystemRepository,
                         ApprovalRepository approvalRepository,
                         TicketLinkRepository ticketLinkRepository,
                         TimelineEventRepository timelineRepository,
                         IncidentRepository incidentRepository,
                         ProblemRepository problemRepository,
                         AppUserRepository appUserRepository,
                         AssetService assetService,
                         ComplianceRequirementRepository complianceRequirementRepository) {
        this.changeRequestRepository = changeRequestRepository;
        this.templateRepository = templateRepository;
        this.affectedSystemRepository = affectedSystemRepository;
        this.approvalRepository = approvalRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.timelineRepository = timelineRepository;
        this.incidentRepository = incidentRepository;
        this.problemRepository = problemRepository;
        this.appUserRepository = appUserRepository;
        this.assetService = assetService;
        this.complianceRequirementRepository = complianceRequirementRepository;
    }

    // ---------- create (API-CHG-002) ----------

    @Transactional
    public ChangeCreatedResponse create(CreateChangeRequest request) {
        requireRole(CM);
        if (request.templateId() != null) {
            templateRepository.findById(request.templateId())
                    .filter(t -> !t.isDeleted())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "유효하지 않은 템플릿입니다."));
        }
        ApprovalRoute route = computeApprovalRoute(request.type(), request.risk(), request.templateId());
        ChangeRequest saved = changeRequestRepository.save(new ChangeRequest(
                nextTicketKey(), request.summary(), request.description(), request.type(), request.risk(),
                route, request.implementationPlan(), request.rollbackPlan(), request.scheduledAt(),
                request.templateId()));
        if (request.affectedSystems() != null) {
            for (String systemName : request.affectedSystems()) {
                if (StringUtils.hasText(systemName)) {
                    affectedSystemRepository.save(new ChangeAffectedSystem(saved.getId(), systemName));
                }
            }
        }
        timelineRepository.save(TimelineEvent.of(TT, saved.getId(), "CREATE", "변경 요청(RFC)이 등록되었습니다."));
        return new ChangeCreatedResponse(saved.getId(), saved.getTicketKey(), saved.getStatus().name(),
                saved.getType().name());
    }

    // ---------- list (API-CHG-001) ----------

    @Transactional(readOnly = true)
    public PageResponse<ChangeSummaryResponse> list(ChangeType type, ChangeStatus status, ChangeRisk risk,
                                                    OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        requireRole(CM);
        OffsetDateTime fromV = from != null ? from : OffsetDateTime.parse("1970-01-01T00:00:00Z");
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        return PageResponse.from(
                changeRequestRepository.search(type, status, risk, fromV, toV, pageable), this::toSummary);
    }

    // ---------- detail (API-CHG-003) ----------

    @Transactional(readOnly = true)
    public ChangeDetailResponse detail(Long id) {
        requireRole(CM, APPROVER);
        return toDetail(findChange(id));
    }

    // ---------- status transition (API-CHG-004) ----------

    @Transactional
    public StatusResponse transition(Long id, StatusTransitionRequest request) {
        requireRole(CM);
        ChangeRequest change = findChange(id);
        ChangeStatus target = request.targetStatus();
        if (!ChangeStateMachine.isAllowed(change.getStatus(), target)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        if (target == ChangeStatus.IMPLEMENTATION && !isApproved(change, id)) {
            throw new BusinessException(ErrorCode.APPROVAL_PENDING);
        }
        change.changeStatus(target);
        changeRequestRepository.save(change);
        if (target == ChangeStatus.APPROVAL && change.getApprovalRoute() != ApprovalRoute.AUTO
                && approvalRepository.findByTicketTypeAndTicketId(TT, id).isEmpty()) {
            approvalRepository.save(new Approval(TT, id, APPROVER));
        }
        timelineRepository.save(TimelineEvent.of(TT, id, "STATUS_" + target.name(),
                StringUtils.hasText(request.note()) ? request.note() : "상태가 " + target.name() + "로 변경되었습니다."));
        return new StatusResponse(id, target.name());
    }

    // ---------- classification (API-CHG-005) ----------

    @Transactional
    public ClassificationResponse classify(Long id, ClassificationRequest request) {
        requireRole(CM);
        ChangeRequest change = findChange(id);
        change.updateClassification(request.type(), request.risk());
        ApprovalRoute route = computeApprovalRoute(request.type(), request.risk(), change.getTemplateId());
        change.updateApprovalRoute(route);
        changeRequestRepository.save(change);
        return new ClassificationResponse(id, change.getType().name(),
                change.getRisk() != null ? change.getRisk().name() : null, route.name());
    }

    // ---------- approval (API-CHG-006) ----------

    @Transactional
    public ChangeApprovalResponse decideApproval(Long id, ChangeApprovalRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        ChangeRequest change = findChange(id);
        Approval approval = approvalRepository.findByTicketTypeAndTicketId(TT, id)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NOT_FOUND));
        if (!principal.roles().contains(approval.getApproverRole())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED); // 승인 담당 역할 미보유
        }
        if (!approval.isPending()) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY_DECIDED); // 이미 결정된 건 재처리 차단
        }
        if (request.decision() == ChangeApprovalDecision.REJECT
                && !StringUtils.hasText(request.opinion())) {
            throw new BusinessException(ErrorCode.REJECT_REASON_REQUIRED);
        }
        if (request.decision() == ChangeApprovalDecision.APPROVE) {
            approval.approve(principal.userId(), request.opinion());
            timelineRepository.save(TimelineEvent.of(TT, id, "APPROVAL_APPROVED", "승인되었습니다."));
        } else {
            approval.reject(principal.userId(), request.opinion());
            timelineRepository.save(TimelineEvent.of(TT, id, "APPROVAL_REJECTED", "반려되었습니다: " + request.opinion()));
        }
        approvalRepository.save(approval);
        return new ChangeApprovalResponse(id, change.getStatus().name());
    }

    // ---------- pending approvals (API-CHG-007) ----------

    @Transactional(readOnly = true)
    public List<PendingChangeApprovalResponse> pendingApprovals() {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        if (principal.roles().isEmpty()) {
            return List.of();
        }
        return approvalRepository.findByTicketTypeAndStatusAndApproverRoleIn(TT, ApprovalStatus.PENDING, principal.roles())
                .stream()
                .map(a -> {
                    ChangeRequest c = changeRequestRepository.findById(a.getTicketId()).orElse(null);
                    return new PendingChangeApprovalResponse(
                            a.getTicketId(),
                            c != null ? c.getTicketKey() : null,
                            c != null && c.getType() != null ? c.getType().name() : null,
                            c != null && c.getRisk() != null ? c.getRisk().name() : null,
                            c != null ? c.getCreatedBy() : null);
                })
                .toList();
    }

    // ---------- result (API-CHG-008) ----------

    @Transactional
    public ResultResponse recordResult(Long id, ResultRequest request) {
        requireRole(CM);
        ChangeRequest change = findChange(id);
        if (!isApproved(change, id)) {
            throw new BusinessException(ErrorCode.CHANGE_NOT_APPROVED);
        }
        change.recordResult(request.outcome(), request.rolledBack(), request.note());
        changeRequestRepository.save(change);
        timelineRepository.save(TimelineEvent.of(TT, id, "RESULT",
                "구현 결과가 기록되었습니다: " + request.outcome()));
        return new ResultResponse(id, change.getOutcome() != null ? change.getOutcome().name() : null,
                change.getRolledBack(), change.getResultNote());
    }

    // ---------- links (API-CHG-009) ----------

    @Transactional
    public LinkResponse link(Long id, LinkRequest request) {
        requireRole(CM);
        ChangeRequest change = findChange(id);
        TicketType targetType = request.targetType() == LinkTargetType.INCIDENT
                ? TicketType.INCIDENT : TicketType.PROBLEM;
        boolean exists = request.targetType() == LinkTargetType.INCIDENT
                ? incidentRepository.findById(request.targetId()).filter(i -> !i.isDeleted()).isPresent()
                : problemRepository.findById(request.targetId()).filter(p -> !p.isDeleted()).isPresent();
        if (!exists) {
            throw new BusinessException(ErrorCode.LINK_TARGET_NOT_FOUND);
        }
        saveLinkOnce(TT, change.getId(), targetType, request.targetId());
        saveLinkOnce(targetType, request.targetId(), TT, change.getId());
        timelineRepository.save(TimelineEvent.of(TT, id, "LINK",
                targetType.name() + " " + request.targetId() + " 와 연계되었습니다."));
        return new LinkResponse(change.getId(), targetType.name(), request.targetId());
    }

    // ---------- schedule (API-CHG-010) ----------

    @Transactional(readOnly = true)
    public List<ScheduleItemResponse> schedule(OffsetDateTime from, OffsetDateTime to, ChangeType type) {
        requireRole(CM);
        OffsetDateTime fromV = from != null ? from : OffsetDateTime.parse("1970-01-01T00:00:00Z");
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        return changeRequestRepository.findSchedule(type, fromV, toV).stream()
                .map(c -> new ScheduleItemResponse(c.getId(), c.getTicketKey(), c.getSummary(),
                        c.getType().name(), c.getScheduledAt()))
                .toList();
    }

    // ---------- templates (API-CHG-011) ----------

    @Transactional(readOnly = true)
    public List<ChangeTemplateResponse> listTemplates() {
        requireRole(CM);
        return templateRepository.findActive().stream()
                .map(t -> new ChangeTemplateResponse(t.getId(), t.getName(), t.getDescription()))
                .toList();
    }

    // ---------- metrics (API-CHG-012) ----------

    @Transactional(readOnly = true)
    public ChangeMetricsResponse metrics(OffsetDateTime from, OffsetDateTime to) {
        requireRole(CM);
        OffsetDateTime fromV = from != null ? from : OffsetDateTime.parse("1970-01-01T00:00:00Z");
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        List<ChangeRequest> changes = changeRequestRepository.findByCreatedAtBetween(fromV, toV);
        long total = changes.size();
        if (total == 0) {
            return new ChangeMetricsResponse(0, 0, 0, 0);
        }
        long withOutcome = changes.stream().filter(c -> c.getOutcome() != null).count();
        long success = changes.stream().filter(c -> c.getOutcome() == Outcome.SUCCESS).count();
        long failure = changes.stream().filter(c -> c.getOutcome() == Outcome.FAILURE).count();
        long emergency = changes.stream().filter(c -> c.getType() == ChangeType.EMERGENCY).count();
        double successRate = withOutcome == 0 ? 0 : (double) success / withOutcome * 100.0;
        double failureRate = withOutcome == 0 ? 0 : (double) failure / withOutcome * 100.0;
        double emergencyRate = (double) emergency / total * 100.0;
        return new ChangeMetricsResponse(round(successRate), round(failureRate), round(emergencyRate), total);
    }

    // ---------- 문제→변경 연계(API-PRB-009) 재사용 ----------

    /** 문제에서 신규 변경(RFC)을 생성한다. 역할 검사는 호출측(ProblemService)에서 수행. */
    @Transactional
    public Long createLinkedChange(String summary, String description) {
        ApprovalRoute route = computeApprovalRoute(ChangeType.NORMAL, null, null);
        ChangeRequest saved = changeRequestRepository.save(new ChangeRequest(
                nextTicketKey(), summary, description, ChangeType.NORMAL, null,
                route, null, null, null, null));
        timelineRepository.save(TimelineEvent.of(TT, saved.getId(), "CREATE", "문제 연계로 변경 요청이 생성되었습니다."));
        return saved.getId();
    }

    /** 기존 변경 존재 여부. 문제 연계(API-PRB-009)에서 부재를 400으로 매핑하기 위해 boolean으로 반환. */
    @Transactional(readOnly = true)
    public boolean existsChange(Long changeId) {
        return changeId != null && changeRequestRepository.findById(changeId)
                .filter(c -> !c.isDeleted()).isPresent();
    }

    /** 변경 ticketKey 조회(없으면 null). 문제 상세(linkedChanges) 노출에 사용. */
    @Transactional(readOnly = true)
    public String ticketKeyOf(Long changeId) {
        return changeRequestRepository.findById(changeId).map(ChangeRequest::getTicketKey).orElse(null);
    }

    // ---------- helpers ----------

    private ChangeRequest findChange(Long id) {
        return changeRequestRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANGE_NOT_FOUND));
    }

    private void requireRole(String... roles) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        for (String role : roles) {
            if (principal.roles().contains(role)) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    private ApprovalRoute computeApprovalRoute(ChangeType type, ChangeRisk risk, Long templateId) {
        if (type == ChangeType.STANDARD && templateId != null) {
            return ApprovalRoute.AUTO; // 사전 승인된 표준 변경은 위험도 평가 여부와 무관하게 재승인 없이 진행(REQ-CHG-006)
        }
        if (risk == null || risk == ChangeRisk.HIGH) {
            return ApprovalRoute.CAB;
        }
        return ApprovalRoute.PEER_REVIEW;
    }

    private boolean isApproved(ChangeRequest change, Long id) {
        if (change.getApprovalRoute() == ApprovalRoute.AUTO) {
            return true;
        }
        return approvalRepository.findByTicketTypeAndTicketId(TT, id)
                .map(a -> a.getStatus() == ApprovalStatus.APPROVED).orElse(false);
    }

    private void saveLinkOnce(TicketType sourceType, Long sourceId, TicketType targetType, Long targetId) {
        if (!ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
                sourceType, sourceId, targetType, targetId)) {
            ticketLinkRepository.save(new TicketLink(sourceType, sourceId, targetType, targetId, "RELATED"));
        }
    }

    private String nextTicketKey() {
        String prefix = "CHG-" + Year.now().getValue() + "-";
        long seq = changeRequestRepository.countByTicketKeyStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private ChangeSummaryResponse toSummary(ChangeRequest c) {
        return new ChangeSummaryResponse(c.getId(), c.getTicketKey(), c.getSummary(), c.getType().name(),
                c.getStatus().name(), c.getRisk() != null ? c.getRisk().name() : null,
                c.getScheduledAt(), c.getUpdatedAt());
    }

    private ChangeDetailResponse toDetail(ChangeRequest c) {
        Long id = c.getId();
        ChangeDetailResponse.Result result = new ChangeDetailResponse.Result(
                c.getOutcome() != null ? c.getOutcome().name() : null, c.getRolledBack(), c.getResultNote());

        List<ChangeDetailResponse.ApprovalDto> approvals = approvalRepository.findByTicketTypeAndTicketId(TT, id)
                .filter(a -> !a.isPending())
                .map(a -> new ChangeDetailResponse.ApprovalDto(userName(a.getDecidedById()),
                        a.getStatus().name(), a.getDecisionReason(), a.getDecidedAt()))
                .map(List::of)
                .orElse(List.of());

        List<ChangeDetailResponse.LinkRef> links = new ArrayList<>(
                ticketLinkRepository.findBySourceTypeAndSourceId(TT, id).stream()
                        .filter(l -> l.getTargetType() == TicketType.INCIDENT || l.getTargetType() == TicketType.PROBLEM
                                || l.getTargetType() == TicketType.ASSET)
                        .map(l -> new ChangeDetailResponse.LinkRef(l.getTargetType().name(), linkedTicketKey(l.getTargetType(), l.getTargetId())))
                        .toList());
        ticketLinkRepository.findByTargetTypeAndTargetId(TT, id).stream()
                .filter(l -> l.getSourceType() == TicketType.COMPLIANCE_REQUIREMENT)
                .map(l -> new ChangeDetailResponse.LinkRef(TicketType.COMPLIANCE_REQUIREMENT.name(), complianceRequirementKeyOf(l.getSourceId())))
                .forEach(links::add);

        List<String> allowed = ChangeStateMachine.allowedTargets(c.getStatus()).stream()
                .map(ChangeStatus::name).sorted().toList();

        return new ChangeDetailResponse(id, c.getTicketKey(), c.getSummary(), c.getDescription(),
                c.getType().name(), c.getRisk() != null ? c.getRisk().name() : null, c.getStatus().name(),
                c.getApprovalRoute() != null ? c.getApprovalRoute().name() : null,
                c.getImplementationPlan(), c.getRollbackPlan(), result, approvals, links, allowed);
    }

    private String linkedTicketKey(TicketType type, Long targetId) {
        if (type == TicketType.INCIDENT) {
            return incidentRepository.findById(targetId).map(Incident::getTicketKey).orElse(null);
        }
        if (type == TicketType.ASSET) {
            return assetService.assetKeyOf(targetId);
        }
        return problemRepository.findById(targetId).map(Problem::getTicketKey).orElse(null);
    }

    private String complianceRequirementKeyOf(Long requirementId) {
        return complianceRequirementRepository.findById(requirementId)
                .map(ComplianceRequirement::getRequirementKey).orElse(null);
    }

    private String userName(Long id) {
        return id == null ? null : appUserRepository.findById(id).map(AppUser::getName).orElse(null);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
