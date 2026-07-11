package com.itsm.change.application;

import com.itsm.asset.application.AssetService;
import com.itsm.auth.application.dto.PageResponse;
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
import com.itsm.change.application.dto.ResultRequest;
import com.itsm.change.application.dto.ResultResponse;
import com.itsm.change.application.dto.ScheduleItemResponse;
import com.itsm.change.application.dto.StatusResponse;
import com.itsm.change.application.dto.StatusTransitionRequest;
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
import com.itsm.common.approval.domain.ApprovalRequest;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.TicketLink;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
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
 * 변경(change) 유스케이스: RFC 등록·조회·6단계 전이·분류·구현결과·
 * 인시던트/문제 연계·일정·표준 변경 템플릿·지표.
 * RBAC(change_manager.md/approver.md): 대부분 CHANGE_MANAGER 전용이며, 상세 조회는 APPROVER도 가능.
 * 승인 경로 자동 라우팅·승인 결정·대기 목록은 승인 프로세스 커스텀 기능(2026-07-11)으로 제거되었다(공용 승인 엔진이 대체).
 * Stage 1(공용 엔진 도입)에서는 컴파일 유지를 위해 구 승인 코드만 제거했으며, IMPLEMENTATION 전이의 실제 게이트 연동은
 * Stage 2에서 진행한다(그때까지 게이트 없이 통과).
 */
@Service
public class ChangeService {

    private static final String CM = "CHANGE_MANAGER";
    private static final String APPROVER = "APPROVER";
    private static final TicketType TT = TicketType.CHANGE;

    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeTemplateRepository templateRepository;
    private final ChangeAffectedSystemRepository affectedSystemRepository;
    private final TicketLinkRepository ticketLinkRepository;
    private final TimelineEventRepository timelineRepository;
    private final IncidentRepository incidentRepository;
    private final ProblemRepository problemRepository;
    private final AssetService assetService;
    private final ComplianceRequirementRepository complianceRequirementRepository;
    private final ApprovalRequestRepository approvalRequestRepository;

    public ChangeService(ChangeRequestRepository changeRequestRepository,
                         ChangeTemplateRepository templateRepository,
                         ChangeAffectedSystemRepository affectedSystemRepository,
                         TicketLinkRepository ticketLinkRepository,
                         TimelineEventRepository timelineRepository,
                         IncidentRepository incidentRepository,
                         ProblemRepository problemRepository,
                         AssetService assetService,
                         ComplianceRequirementRepository complianceRequirementRepository,
                         ApprovalRequestRepository approvalRequestRepository) {
        this.changeRequestRepository = changeRequestRepository;
        this.templateRepository = templateRepository;
        this.affectedSystemRepository = affectedSystemRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.timelineRepository = timelineRepository;
        this.incidentRepository = incidentRepository;
        this.problemRepository = problemRepository;
        this.assetService = assetService;
        this.complianceRequirementRepository = complianceRequirementRepository;
        this.approvalRequestRepository = approvalRequestRepository;
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
        ChangeRequest saved = changeRequestRepository.save(new ChangeRequest(
                nextTicketKey(), request.summary(), request.description(), request.type(), request.risk(),
                request.implementationPlan(), request.rollbackPlan(), request.scheduledAt(),
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
        change.changeStatus(target);
        changeRequestRepository.save(change);
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
        changeRequestRepository.save(change);
        return new ClassificationResponse(id, change.getType().name(),
                change.getRisk() != null ? change.getRisk().name() : null);
    }

    // ---------- result (API-CHG-008) ----------

    @Transactional
    public ResultResponse recordResult(Long id, ResultRequest request) {
        requireRole(CM);
        ChangeRequest change = findChange(id);
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
        ChangeRequest saved = changeRequestRepository.save(new ChangeRequest(
                nextTicketKey(), summary, description, ChangeType.NORMAL, null,
                null, null, null, null));
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
        if (!SecurityUtils.hasAnyRole(roles)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
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

        ApprovalRequest latestApproval = approvalRequestRepository
                .findTopByTicketTypeAndTicketIdOrderByIdDesc(TT, id).orElse(null);
        ChangeDetailResponse.ApprovalInfo approvalInfo = new ChangeDetailResponse.ApprovalInfo(
                latestApproval != null ? latestApproval.getId() : null,
                latestApproval != null ? latestApproval.getStatus().name() : null);

        return new ChangeDetailResponse(id, c.getTicketKey(), c.getSummary(), c.getDescription(),
                c.getType().name(), c.getRisk() != null ? c.getRisk().name() : null, c.getStatus().name(),
                c.getImplementationPlan(), c.getRollbackPlan(), result, approvalInfo, links, allowed);
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

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
