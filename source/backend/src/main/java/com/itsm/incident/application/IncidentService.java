package com.itsm.incident.application;

import com.itsm.asset.application.AssetService;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.change.application.ChangeService;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.application.TicketCreationGateSupport;
import com.itsm.common.approval.domain.ApprovalRequest;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.TicketLink;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
import com.itsm.common.ticket.TimelineMessages;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.problem.application.ProblemService;
import com.itsm.incident.application.dto.ActionItemDto;
import com.itsm.incident.application.dto.AssignRoleRequest;
import com.itsm.incident.application.dto.CreateIncidentRequest;
import com.itsm.incident.application.dto.EscalateRequest;
import com.itsm.incident.application.dto.EscalateResponse;
import com.itsm.incident.application.dto.IncidentCreatedResponse;
import com.itsm.incident.application.dto.IncidentDetailResponse;
import com.itsm.incident.application.dto.IncidentMetrics;
import com.itsm.incident.application.dto.IncidentMetricsResponse;
import com.itsm.incident.application.dto.IncidentSummaryResponse;
import com.itsm.incident.application.dto.LinkProblemRequest;
import com.itsm.incident.application.dto.LinkResponse;
import com.itsm.incident.application.dto.PostmortemRequest;
import com.itsm.incident.application.dto.PostmortemResponse;
import com.itsm.incident.application.dto.ResolveRequest;
import com.itsm.incident.application.dto.ResolveResponse;
import com.itsm.incident.application.dto.ResponderDto;
import com.itsm.incident.application.dto.SeverityChangeRequest;
import com.itsm.incident.application.dto.SeverityChangeResponse;
import com.itsm.incident.application.dto.StatusResponse;
import com.itsm.incident.application.dto.StatusTransitionRequest;
import com.itsm.incident.application.dto.TimelineUpdateRequest;
import com.itsm.incident.application.dto.TimelineUpdateResponse;
import com.itsm.incident.domain.ActionItemStatus;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.IncidentResponder;
import com.itsm.incident.domain.IncidentSeverityHistory;
import com.itsm.incident.domain.IncidentStatus;
import com.itsm.incident.domain.Postmortem;
import com.itsm.incident.domain.PostmortemActionItem;
import com.itsm.incident.domain.PostmortemFiveWhy;
import com.itsm.incident.domain.ResponseRole;
import com.itsm.incident.domain.Severity;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.incident.domain.repository.IncidentResponderRepository;
import com.itsm.incident.domain.repository.IncidentSeverityHistoryRepository;
import com.itsm.incident.domain.repository.PostmortemActionItemRepository;
import com.itsm.incident.domain.repository.PostmortemFiveWhyRepository;
import com.itsm.incident.domain.repository.PostmortemRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 인시던트 유스케이스: 등록·조회·심각도/우선순위·상태전이·역할배정·에스컬레이션·타임라인·해결(MTTx)·포스트모템·지표.
 * RBAC(role 정의서): 역할 배정(INC-006)/심각도(INC-004)/해결(INC-009)/포스트모템 작성(INC-011)은 INCIDENT_MANAGER 전용,
 * 상태전이(INC-005)/에스컬레이션(INC-007)/타임라인(INC-008)은 SERVICE_DESK_AGENT 또는 INCIDENT_MANAGER.
 */
@Service
public class IncidentService {

    private static final String IM = "INCIDENT_MANAGER";
    private static final String AGENT = "SERVICE_DESK_AGENT";
    private static final TicketType TT = TicketType.INCIDENT;
    private static final String DOMAIN = "INCIDENT";

    private final IncidentRepository incidentRepository;
    private final IncidentResponderRepository responderRepository;
    private final IncidentSeverityHistoryRepository severityHistoryRepository;
    private final PostmortemRepository postmortemRepository;
    private final PostmortemFiveWhyRepository fiveWhyRepository;
    private final PostmortemActionItemRepository actionItemRepository;
    private final TimelineEventRepository timelineRepository;
    private final TicketLinkRepository ticketLinkRepository;
    private final AppUserRepository appUserRepository;
    private final ProblemService problemService;
    private final ChangeService changeService;
    private final AssetService assetService;
    private final ApprovalGateService approvalGateService;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final TicketCreationGateSupport ticketCreationGateSupport;

    public IncidentService(IncidentRepository incidentRepository,
                           IncidentResponderRepository responderRepository,
                           IncidentSeverityHistoryRepository severityHistoryRepository,
                           PostmortemRepository postmortemRepository,
                           PostmortemFiveWhyRepository fiveWhyRepository,
                           PostmortemActionItemRepository actionItemRepository,
                           TimelineEventRepository timelineRepository,
                           TicketLinkRepository ticketLinkRepository,
                           AppUserRepository appUserRepository,
                           ProblemService problemService,
                           ChangeService changeService,
                           AssetService assetService,
                           ApprovalGateService approvalGateService,
                           ApprovalRequestRepository approvalRequestRepository,
                           TicketCreationGateSupport ticketCreationGateSupport) {
        this.incidentRepository = incidentRepository;
        this.responderRepository = responderRepository;
        this.severityHistoryRepository = severityHistoryRepository;
        this.postmortemRepository = postmortemRepository;
        this.fiveWhyRepository = fiveWhyRepository;
        this.actionItemRepository = actionItemRepository;
        this.timelineRepository = timelineRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.appUserRepository = appUserRepository;
        this.problemService = problemService;
        this.changeService = changeService;
        this.assetService = assetService;
        this.approvalGateService = approvalGateService;
        this.approvalRequestRepository = approvalRequestRepository;
        this.ticketCreationGateSupport = ticketCreationGateSupport;
    }

    // ---------- create (API-INC-002) ----------

    @Transactional
    public IncidentCreatedResponse create(CreateIncidentRequest request) {
        Long requesterId = SecurityUtils.currentPrincipal().userId();
        Incident saved = ticketCreationGateSupport.createThenGate(
                () -> {
                    Incident incident = incidentRepository.save(new Incident(
                            nextTicketKey(), request.summary(), request.description(), request.severity(),
                            request.affectedService(), request.affectedProduct()));
                    timelineRepository.save(TimelineEvent.of(TT, incident.getId(), "CREATE", "인시던트가 등록되었습니다."));
                    return incident;
                },
                Incident::getId,
                DOMAIN, null, requesterId, TT, IncidentStatus.NEW.name());
        return new IncidentCreatedResponse(saved.getId(), saved.getTicketKey(), saved.getStatus().name());
    }

    // ---------- list (API-INC-001) ----------

    @Transactional(readOnly = true)
    public PageResponse<IncidentSummaryResponse> list(IncidentStatus status, Severity severity, Long assignee,
                                                      String keyword, OffsetDateTime from, OffsetDateTime to,
                                                      Pageable pageable) {
        SecurityUtils.currentPrincipal();
        OffsetDateTime fromV = from != null ? from : OffsetDateTime.parse("1970-01-01T00:00:00Z");
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        String kw = StringUtils.hasText(keyword) ? keyword : null;
        var page = incidentRepository.search(status, severity, assignee, kw, fromV, toV, pageable);
        Map<Long, String> pendingTargetStates = approvalGateService.pendingApprovalTargetStatesOf(
                TT, page.getContent().stream().map(Incident::getId).toList());
        return PageResponse.from(page, i -> toSummary(i, pendingTargetStates.get(i.getId())));
    }

    // ---------- detail (API-INC-003) ----------

    @Transactional(readOnly = true)
    public IncidentDetailResponse detail(Long id) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        Incident inc = findIncident(id);
        if (!SecurityUtils.hasAnyRole(AGENT, IM)
                && !approvalGateService.canApproverView(DOMAIN, null, requesterIdOf(inc))) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        List<ResponderDto> responders = responderRepository.findByIncidentId(id).stream()
                .map(r -> new ResponderDto(r.getUserId(), userName(r.getUserId()), r.getResponseRole().name()))
                .toList();
        List<IncidentDetailResponse.LinkDto> links = ticketLinkRepository.findBySourceTypeAndSourceId(TT, id).stream()
                .map(l -> new IncidentDetailResponse.LinkDto(l.getTargetType().name(), linkedTicketKey(l.getTargetType(), l.getTargetId())))
                .toList();
        Map<String, String> actorCache = new HashMap<>();
        List<IncidentDetailResponse.TimelineEntry> timeline =
                timelineRepository.findByTicketTypeAndTicketIdOrderByOccurredAtAsc(TT, id).stream()
                        .map(t -> new IncidentDetailResponse.TimelineEntry(
                                t.getEventType(), t.getVisibility().name(), t.getMessage(), t.getOccurredAt(),
                                actorCache.computeIfAbsent(t.getCreatedBy(), appUserRepository::resolveDisplayName)))
                        .toList();

        ApprovalRequest latestApproval = approvalRequestRepository
                .findTopByTicketTypeAndTicketIdOrderByIdDesc(TT, id).orElse(null);
        IncidentDetailResponse.ApprovalInfo approvalInfo = new IncidentDetailResponse.ApprovalInfo(
                latestApproval != null ? latestApproval.getId() : null,
                latestApproval != null ? latestApproval.getStatus().name() : null,
                latestApproval != null ? latestApproval.getTargetState() : null);

        return new IncidentDetailResponse(
                inc.getId(), inc.getTicketKey(), inc.getSummary(), inc.getDescription(),
                inc.getSeverity().name(), inc.getPriority() != null ? inc.getPriority().name() : null,
                inc.getStatus().name(), inc.getAffectedService(), inc.getAffectedProduct(),
                responders, metricsOf(inc), approvalInfo, links, timeline, allowedTransitions(principal, inc));
    }

    private List<String> allowedTransitions(AuthPrincipal principal, Incident inc) {
        if (!SecurityUtils.hasAnyRole(AGENT, IM)) {
            return List.of();
        }
        return IncidentStateMachine.allowedTargets(inc.getStatus()).stream()
                .map(IncidentStatus::name).sorted().toList();
    }

    // ---------- severity/priority (API-INC-004, IM 전용) ----------

    @Transactional
    public SeverityChangeResponse changeSeverity(Long id, SeverityChangeRequest request) {
        requireAnyRole(IM);
        Incident inc = findIncident(id);
        String oldSev = inc.getSeverity().name();
        String oldPri = inc.getPriority() != null ? inc.getPriority().name() : null;
        inc.changeSeverityAndPriority(request.severity(), request.priority());
        incidentRepository.save(inc);
        severityHistoryRepository.save(new IncidentSeverityHistory(id, oldSev, inc.getSeverity().name(),
                oldPri, inc.getPriority() != null ? inc.getPriority().name() : null));
        timelineRepository.save(TimelineEvent.of(TT, id, "SEVERITY_CHANGE",
                "심각도/우선순위가 " + inc.getSeverity().name() + "/"
                        + (inc.getPriority() != null ? inc.getPriority().name() : "-") + "로 변경되었습니다."));
        return new SeverityChangeResponse(id, inc.getSeverity().name(),
                inc.getPriority() != null ? inc.getPriority().name() : null);
    }

    // ---------- status transition (API-INC-005) ----------

    @Transactional
    public StatusResponse transition(Long id, StatusTransitionRequest request) {
        requireAnyRole(AGENT, IM);
        Incident inc = findIncident(id);
        IncidentStatus target = request.targetStatus();
        if (!IncidentStateMachine.isAllowed(inc.getStatus(), target)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        approvalGateService.checkGate(DOMAIN, null, SecurityUtils.currentPrincipal().userId(), TT, id, target.name());
        inc.changeStatus(target);
        incidentRepository.save(inc);
        timelineRepository.save(TimelineEvent.of(TT, id, "STATUS_" + target.name(),
                StringUtils.hasText(request.note()) ? request.note()
                        : "상태가 " + TimelineMessages.quotedWithParticle(target.label()) + " 변경되었습니다."));
        return new StatusResponse(id, target.name());
    }

    // ---------- responder assignment (API-INC-006, IM 전용) ----------

    @Transactional
    public IncidentDetailResponse assignRole(Long id, AssignRoleRequest request) {
        requireAnyRole(IM);
        findIncident(id);
        if (appUserRepository.findById(request.userId()).isEmpty()) {
            throw new BusinessException(ErrorCode.ASSIGNEE_NOT_FOUND);
        }
        if (!responderRepository.existsByIncidentIdAndUserIdAndResponseRole(id, request.userId(), request.role())) {
            responderRepository.save(new IncidentResponder(id, request.userId(), request.role()));
            timelineRepository.save(TimelineEvent.of(TT, id, "ROLE_ASSIGN",
                    userName(request.userId()) + "님이 " + request.role().name() + "로 배정되었습니다."));
        }
        return detail(id);
    }

    // ---------- escalation (API-INC-007) ----------

    @Transactional
    public EscalateResponse escalate(Long id, EscalateRequest request) {
        requireAnyRole(AGENT, IM);
        findIncident(id);
        if (appUserRepository.findById(request.targetUserId()).isEmpty()) {
            throw new BusinessException(ErrorCode.ESCALATION_TARGET_NOT_FOUND);
        }
        OffsetDateTime at = OffsetDateTime.now();
        String reason = StringUtils.hasText(request.reason()) ? " · 사유: " + request.reason() : "";
        timelineRepository.save(TimelineEvent.of(TT, id, "ESCALATE",
                request.type().name() + " 에스컬레이션: " + userName(request.targetUserId()) + reason));
        return new EscalateResponse(id, request.targetUserId(), request.type().name(), at);
    }

    // ---------- timeline update (API-INC-008) ----------

    @Transactional
    public TimelineUpdateResponse addUpdate(Long id, TimelineUpdateRequest request) {
        requireAnyRole(AGENT, IM);
        findIncident(id);
        TimelineEvent saved = timelineRepository.save(
                TimelineEvent.of(TT, id, "UPDATE", request.message(), request.visibility()));
        return new TimelineUpdateResponse(saved.getId(), saved.getOccurredAt());
    }

    // ---------- resolve + metrics (API-INC-009, IM 전용) ----------

    @Transactional
    public ResolveResponse resolve(Long id, ResolveRequest request) {
        requireAnyRole(IM);
        Incident inc = findIncident(id);
        approvalGateService.checkGate(DOMAIN, null, SecurityUtils.currentPrincipal().userId(), TT, id, IncidentStatus.RESOLVED.name());
        OffsetDateTime impactStart = request.impactStartAt() != null ? request.impactStartAt() : inc.getImpactStartAt();
        OffsetDateTime detected = request.detectedAt() != null ? request.detectedAt() : inc.getDetectedAt();
        OffsetDateTime impactEnd = request.impactEndAt() != null ? request.impactEndAt() : inc.getImpactEndAt();

        Integer mttd = minutesBetween(impactStart, detected);
        Integer mtta = minutesBetween(detected, impactEnd);
        Integer mttr = minutesBetween(impactStart, impactEnd);

        inc.resolve(impactStart, detected, impactEnd, OffsetDateTime.now(), mttd, mtta, mttr);
        incidentRepository.save(inc);
        timelineRepository.save(TimelineEvent.of(TT, id, "RESOLVE",
                StringUtils.hasText(request.resolutionNote()) ? request.resolutionNote() : "인시던트가 해결되었습니다."));
        return new ResolveResponse(id, inc.getStatus().name(), new IncidentMetrics(mttd, mtta, mttr));
    }

    // ---------- postmortem (API-INC-010 / 011) ----------

    @Transactional(readOnly = true)
    public PostmortemResponse getPostmortem(Long id) {
        SecurityUtils.currentPrincipal();
        findIncident(id);
        Postmortem pm = postmortemRepository.findByIncidentId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POSTMORTEM_NOT_FOUND));
        return toPostmortemResponse(pm);
    }

    @Transactional
    public PostmortemResponse savePostmortem(Long id, PostmortemRequest request) {
        requireAnyRole(IM);
        findIncident(id);
        if (!StringUtils.hasText(request.rootCause())) {
            throw new BusinessException(ErrorCode.ROOT_CAUSE_REQUIRED);
        }
        Postmortem pm = postmortemRepository.findByIncidentId(id).orElse(null);
        if (pm == null) {
            pm = postmortemRepository.save(
                    new Postmortem(id, request.summary(), request.timeline(), request.rootCause()));
        } else {
            pm.update(request.summary(), request.timeline(), request.rootCause());
            pm = postmortemRepository.save(pm);
            fiveWhyRepository.deleteByPostmortemId(pm.getId());
            actionItemRepository.deleteByPostmortemId(pm.getId());
        }
        Long pmId = pm.getId();
        if (request.fiveWhys() != null) {
            short step = 1;
            for (String content : request.fiveWhys()) {
                if (StringUtils.hasText(content)) {
                    fiveWhyRepository.save(new PostmortemFiveWhy(pmId, step++, content));
                }
            }
        }
        if (request.actionItems() != null) {
            for (ActionItemDto item : request.actionItems()) {
                actionItemRepository.save(new PostmortemActionItem(pmId, item.description(), item.owner(),
                        item.dueDate(), parseActionStatus(item.status())));
            }
        }
        return toPostmortemResponse(pm);
    }

    // ---------- link (API-INC-012) — 기존 문제 연계 또는 신규 문제 생성 후 양방향 링크 ----------

    @Transactional
    public LinkResponse linkProblem(Long id, LinkProblemRequest request) {
        requireAnyRole(IM);
        Incident inc = findIncident(id);
        Long problemId;
        if (request.createNewProblem()) {
            problemId = problemService.createReactiveProblem(inc.getSummary(), inc.getDescription());
        } else if (request.problemId() != null) {
            if (!problemService.existsProblem(request.problemId())) {
                throw new BusinessException(ErrorCode.LINK_TARGET_NOT_FOUND);
            }
            problemId = request.problemId();
        } else {
            throw new BusinessException(ErrorCode.LINK_TARGET_REQUIRED);
        }
        saveLinkOnce(TicketType.INCIDENT, id, TicketType.PROBLEM, problemId);
        saveLinkOnce(TicketType.PROBLEM, problemId, TicketType.INCIDENT, id);
        timelineRepository.save(TimelineEvent.of(TT, id, "LINK", "문제 연계가 생성되었습니다."));
        return new LinkResponse(id, problemId);
    }

    private void saveLinkOnce(TicketType sourceType, Long sourceId, TicketType targetType, Long targetId) {
        if (!ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
                sourceType, sourceId, targetType, targetId)) {
            ticketLinkRepository.save(new TicketLink(sourceType, sourceId, targetType, targetId, "RELATED"));
        }
    }

    // ---------- metrics (API-INC-013) ----------

    @Transactional(readOnly = true)
    public IncidentMetricsResponse metrics(OffsetDateTime from, OffsetDateTime to) {
        SecurityUtils.currentPrincipal();
        OffsetDateTime fromV = from != null ? from : OffsetDateTime.parse("1970-01-01T00:00:00Z");
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        List<Incident> incidents = incidentRepository.findByCreatedAtBetween(fromV, toV);
        long sev1 = incidents.stream().filter(i -> i.getSeverity() == Severity.SEV1).count();
        long sev2 = incidents.stream().filter(i -> i.getSeverity() == Severity.SEV2).count();
        long sev3 = incidents.stream().filter(i -> i.getSeverity() == Severity.SEV3).count();
        double avgMttr = incidents.stream()
                .map(Incident::getMttrMinutes).filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue).average().orElse(0.0);
        return new IncidentMetricsResponse(incidents.size(),
                new IncidentMetricsResponse.SeverityDistribution(sev1, sev2, sev3), avgMttr);
    }

    // ---------- helpers ----------

    private IncidentSummaryResponse toSummary(Incident inc, String pendingApprovalTargetState) {
        boolean hasPostmortem = postmortemRepository.findByIncidentId(inc.getId()).isPresent();
        return new IncidentSummaryResponse(inc.getId(), inc.getTicketKey(), inc.getSummary(),
                inc.getSeverity().name(), inc.getStatus().name(), primaryResponderName(inc.getId()),
                postmortemRequired(inc, hasPostmortem), inc.getUpdatedAt(), pendingApprovalTargetState);
    }

    /** SEV1·SEV2 인시던트가 해결/종료되었으나 포스트모템이 아직 없으면 필요 표시(TC-INC-037). */
    private boolean postmortemRequired(Incident inc, boolean hasPostmortem) {
        boolean major = inc.getSeverity() == Severity.SEV1 || inc.getSeverity() == Severity.SEV2;
        boolean resolved = inc.getStatus() == IncidentStatus.RESOLVED || inc.getStatus() == IncidentStatus.CLOSED;
        return major && resolved && !hasPostmortem;
    }

    private String primaryResponderName(Long incidentId) {
        List<IncidentResponder> responders = responderRepository.findByIncidentId(incidentId);
        return responders.stream()
                .filter(r -> r.getResponseRole() == ResponseRole.TECH_LEAD)
                .findFirst()
                .or(() -> responders.stream().findFirst())
                .map(r -> userName(r.getUserId()))
                .orElse(null);
    }

    private IncidentMetrics metricsOf(Incident inc) {
        return new IncidentMetrics(inc.getMttdMinutes(), inc.getMttaMinutes(), inc.getMttrMinutes());
    }

    private PostmortemResponse toPostmortemResponse(Postmortem pm) {
        List<String> fiveWhys = fiveWhyRepository.findByPostmortemIdOrderByStepNoAsc(pm.getId()).stream()
                .map(PostmortemFiveWhy::getContent).toList();
        List<ActionItemDto> actionItems = actionItemRepository.findByPostmortemId(pm.getId()).stream()
                .map(a -> new ActionItemDto(a.getDescription(), a.getOwner(), a.getDueDate(), a.getStatus().name()))
                .toList();
        return new PostmortemResponse(pm.getIncidentId(), pm.getSummary(), pm.getTimelineSummary(),
                pm.getRootCause(), fiveWhys, actionItems);
    }

    private ActionItemStatus parseActionStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return ActionItemStatus.OPEN;
        }
        try {
            return ActionItemStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "정의되지 않은 조치항목 상태입니다: " + status);
        }
    }

    private Integer minutesBetween(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        long minutes = Duration.between(start, end).toMinutes();
        return minutes < 0 ? null : (int) minutes;
    }

    private String nextTicketKey() {
        String prefix = "INC-" + Year.now().getValue() + "-";
        long seq = incidentRepository.countByTicketKeyStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private String userName(Long id) {
        return id == null ? null : appUserRepository.findById(id).map(AppUser::getName).orElse(null);
    }

    /** 승인 게이트의 "요청자"는 인시던트를 등록한 사용자(created_by, 이메일)로 판정한다. */
    private Long requesterIdOf(Incident inc) {
        return appUserRepository.findByEmail(inc.getCreatedBy()).map(AppUser::getId).orElse(null);
    }

    /** 연계 대상 ticketKey 조회(API-INC-003 links). PROBLEM은 INC-012로 직접 연계, CHANGE는 API-CHG-009(change→incident)
     * 양방향 링크로 인해 인시던트 상세에도 노출된다. ASSET은 API-ITAM-007(자산→인시던트) 양방향 링크로 노출된다(REQ-ITAM-006). */
    private String linkedTicketKey(TicketType targetType, Long targetId) {
        if (targetType == TicketType.PROBLEM) {
            return problemService.ticketKeyOf(targetId);
        }
        if (targetType == TicketType.CHANGE) {
            return changeService.ticketKeyOf(targetId);
        }
        if (targetType == TicketType.ASSET) {
            return assetService.assetKeyOf(targetId);
        }
        return null;
    }

    private Incident findIncident(Long id) {
        return incidentRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.INCIDENT_NOT_FOUND));
    }

    private void requireAnyRole(String... roles) {
        if (!SecurityUtils.hasAnyRole(roles)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}
