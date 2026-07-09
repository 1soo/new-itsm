package com.itsm.problem.application;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.TicketLink;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.repository.IncidentRepository;
import com.itsm.problem.application.dto.ActionCreateRequest;
import com.itsm.problem.application.dto.ActionResponse;
import com.itsm.problem.application.dto.ActionStatusRequest;
import com.itsm.problem.application.dto.CloseRequest;
import com.itsm.problem.application.dto.CloseResponse;
import com.itsm.problem.application.dto.CreateProblemRequest;
import com.itsm.problem.application.dto.KnownErrorCreateRequest;
import com.itsm.problem.application.dto.KnownErrorCreatedResponse;
import com.itsm.problem.application.dto.KnownErrorSearchResponse;
import com.itsm.problem.application.dto.LinkRequest;
import com.itsm.problem.application.dto.LinkResponse;
import com.itsm.problem.application.dto.ProblemCreatedResponse;
import com.itsm.problem.application.dto.ProblemDetailResponse;
import com.itsm.problem.application.dto.ProblemSummaryResponse;
import com.itsm.problem.application.dto.RcaRequest;
import com.itsm.problem.application.dto.RcaResponse;
import com.itsm.problem.application.dto.StatusResponse;
import com.itsm.problem.application.dto.StatusTransitionRequest;
import com.itsm.problem.application.dto.WorkaroundRequest;
import com.itsm.problem.application.dto.WorkaroundResponse;
import com.itsm.problem.domain.ActionStatus;
import com.itsm.problem.domain.KnownError;
import com.itsm.problem.domain.LinkTargetType;
import com.itsm.problem.domain.Level;
import com.itsm.problem.domain.Problem;
import com.itsm.problem.domain.ProblemAction;
import com.itsm.problem.domain.ProblemFiveWhy;
import com.itsm.problem.domain.ProblemOrigin;
import com.itsm.problem.domain.ProblemPriority;
import com.itsm.problem.domain.ProblemStatus;
import com.itsm.problem.domain.repository.KnownErrorRepository;
import com.itsm.problem.domain.repository.ProblemActionRepository;
import com.itsm.problem.domain.repository.ProblemFiveWhyRepository;
import com.itsm.problem.domain.repository.ProblemRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.List;

/**
 * 문제 유스케이스: 등록·조회·상태전이(6단계)·RCA·워크어라운드·알려진오류(KEDB)·연계·후속조치·종료.
 * RBAC(problem_manager.md): 모든 문제 API는 PROBLEM_MANAGER 전용. 인시던트 연계 시 대상 존재를 검증한다.
 * 인시던트→문제 연계(API-INC-012)에서 재사용되는 내부 메서드(createReactiveProblem/assertExists)는 역할 검사를 하지 않는다.
 */
@Service
public class ProblemService {

    private static final String PM = "PROBLEM_MANAGER";
    private static final TicketType TT = TicketType.PROBLEM;

    private final ProblemRepository problemRepository;
    private final ProblemFiveWhyRepository fiveWhyRepository;
    private final KnownErrorRepository knownErrorRepository;
    private final ProblemActionRepository actionRepository;
    private final TimelineEventRepository timelineRepository;
    private final TicketLinkRepository ticketLinkRepository;
    private final IncidentRepository incidentRepository;

    public ProblemService(ProblemRepository problemRepository,
                          ProblemFiveWhyRepository fiveWhyRepository,
                          KnownErrorRepository knownErrorRepository,
                          ProblemActionRepository actionRepository,
                          TimelineEventRepository timelineRepository,
                          TicketLinkRepository ticketLinkRepository,
                          IncidentRepository incidentRepository) {
        this.problemRepository = problemRepository;
        this.fiveWhyRepository = fiveWhyRepository;
        this.knownErrorRepository = knownErrorRepository;
        this.actionRepository = actionRepository;
        this.timelineRepository = timelineRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.incidentRepository = incidentRepository;
    }

    // ---------- create (API-PRB-002) ----------

    @Transactional
    public ProblemCreatedResponse create(CreateProblemRequest request) {
        requireRole(PM);
        Problem saved = problemRepository.save(new Problem(
                nextTicketKey(), request.summary(), request.description(), request.origin(),
                request.investigationReason(), request.impact(), request.urgency(), request.component()));
        timelineRepository.save(TimelineEvent.of(TT, saved.getId(), "CREATE", "문제가 등록되었습니다."));
        return new ProblemCreatedResponse(saved.getId(), saved.getTicketKey(), saved.getStatus().name(),
                saved.getPriority() != null ? saved.getPriority().name() : null);
    }

    // ---------- list (API-PRB-001) ----------

    @Transactional(readOnly = true)
    public PageResponse<ProblemSummaryResponse> list(ProblemStatus status, ProblemPriority priority,
                                                     ProblemOrigin origin, String assignee,
                                                     OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        requireRole(PM);
        OffsetDateTime fromV = from != null ? from : OffsetDateTime.parse("1970-01-01T00:00:00Z");
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        String assigneeV = StringUtils.hasText(assignee) ? assignee : null;
        return PageResponse.from(
                problemRepository.search(status, priority, origin, assigneeV, fromV, toV, pageable),
                this::toSummary);
    }

    // ---------- detail (API-PRB-003) ----------

    @Transactional(readOnly = true)
    public ProblemDetailResponse detail(Long id) {
        requireRole(PM);
        Problem problem = findProblem(id);
        return toDetail(problem);
    }

    // ---------- status transition (API-PRB-004) ----------

    @Transactional
    public StatusResponse transition(Long id, StatusTransitionRequest request) {
        requireRole(PM);
        Problem problem = findProblem(id);
        ProblemStatus target = request.targetStatus();
        if (!ProblemStateMachine.isAllowed(problem.getStatus(), target)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        problem.changeStatus(target);
        problemRepository.save(problem);
        timelineRepository.save(TimelineEvent.of(TT, id, "STATUS_" + target.name(),
                StringUtils.hasText(request.note()) ? request.note() : "상태가 " + target.name() + "로 변경되었습니다."));
        return new StatusResponse(id, target.name());
    }

    // ---------- RCA (API-PRB-005) ----------

    @Transactional
    public RcaResponse saveRca(Long id, RcaRequest request) {
        requireRole(PM);
        Problem problem = findProblem(id);
        problem.updateRca(request.rootCause(), request.category());
        problemRepository.save(problem);

        fiveWhyRepository.deleteByProblemId(id);
        if (request.fiveWhys() != null) {
            short step = 1;
            for (String content : request.fiveWhys()) {
                if (StringUtils.hasText(content)) {
                    fiveWhyRepository.save(new ProblemFiveWhy(id, step++, content));
                }
            }
        }
        timelineRepository.save(TimelineEvent.of(TT, id, "RCA", "근본 원인 분석이 기록되었습니다."));
        return new RcaResponse(problem.getRootCause(), currentFiveWhys(id), problem.getRootCauseCategory());
    }

    // ---------- workaround (API-PRB-006) ----------

    @Transactional
    public WorkaroundResponse addWorkaround(Long id, WorkaroundRequest request) {
        requireRole(PM);
        if (!StringUtils.hasText(request.content())) {
            throw new BusinessException(ErrorCode.WORKAROUND_CONTENT_REQUIRED);
        }
        Problem problem = findProblem(id);
        problem.updateWorkaround(request.content());
        problemRepository.save(problem);
        if (request.linkedArticleId() != null) {
            saveLinkOnce(TT, id, TicketType.KNOWLEDGE, request.linkedArticleId(), "WORKAROUND");
        }
        timelineRepository.save(TimelineEvent.of(TT, id, "WORKAROUND", "워크어라운드가 등록되었습니다."));
        return new WorkaroundResponse(id, problem.getWorkaround());
    }

    // ---------- known error create (API-PRB-007) ----------

    @Transactional
    public KnownErrorCreatedResponse createKnownError(Long id, KnownErrorCreateRequest request) {
        requireRole(PM);
        findProblem(id);
        KnownError saved = knownErrorRepository.save(
                new KnownError(id, request.title(), request.rootCause(), request.workaround()));
        timelineRepository.save(TimelineEvent.of(TT, id, "KNOWN_ERROR",
                "알려진 오류(KEDB)가 등록되었습니다: " + request.title()));
        return new KnownErrorCreatedResponse(saved.getId(), saved.getTitle());
    }

    // ---------- KEDB search (API-PRB-008) ----------

    @Transactional(readOnly = true)
    public PageResponse<KnownErrorSearchResponse> searchKnownErrors(String keyword, Pageable pageable) {
        requireRole(PM);
        String kw = StringUtils.hasText(keyword) ? keyword : null;
        return PageResponse.from(knownErrorRepository.searchByKeyword(kw, pageable), this::toKnownErrorSearch);
    }

    // ---------- link (API-PRB-009) ----------

    @Transactional
    public LinkResponse link(Long id, LinkRequest request) {
        requireRole(PM);
        Problem problem = findProblem(id);

        if (request.targetType() == LinkTargetType.CHANGE) {
            // TODO(change 도메인): createNewChange 생성 또는 기존 변경 연계 후 ticket_link(PROBLEM↔CHANGE) 저장.
            throw new BusinessException(ErrorCode.CHANGE_LINK_UNAVAILABLE);
        }

        // INCIDENT 연계: 기존 인시던트 존재 검증 후 양방향 링크.
        Long incidentId = request.targetId();
        if (incidentId == null) {
            throw new BusinessException(ErrorCode.LINK_TARGET_REQUIRED);
        }
        Incident incident = incidentRepository.findById(incidentId)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.LINK_TARGET_NOT_FOUND));
        saveLinkOnce(TT, problem.getId(), TicketType.INCIDENT, incident.getId(), "RELATED");
        saveLinkOnce(TicketType.INCIDENT, incident.getId(), TT, problem.getId(), "RELATED");
        timelineRepository.save(TimelineEvent.of(TT, id, "LINK",
                "인시던트 " + incident.getTicketKey() + " 와 연계되었습니다."));
        return new LinkResponse(problem.getId(), TicketType.INCIDENT.name(), incident.getId());
    }

    // ---------- action register (API-PRB-010) ----------

    @Transactional
    public ActionResponse addAction(Long id, ActionCreateRequest request) {
        requireRole(PM);
        findProblem(id);
        ProblemAction saved = actionRepository.save(
                new ProblemAction(id, request.description(), request.owner(), request.dueDate()));
        timelineRepository.save(TimelineEvent.of(TT, id, "ACTION_ADD", "후속 조치가 등록되었습니다."));
        return new ActionResponse(saved.getId(), saved.getStatus().name());
    }

    // ---------- action status (API-PRB-011) ----------

    @Transactional
    public ActionResponse changeActionStatus(Long id, Long actionId, ActionStatusRequest request) {
        requireRole(PM);
        findProblem(id);
        ProblemAction action = actionRepository.findById(actionId)
                .filter(a -> !a.isDeleted() && a.getProblemId().equals(id))
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_ACTION_NOT_FOUND));
        action.changeStatus(request.status());
        actionRepository.save(action);
        return new ActionResponse(action.getId(), action.getStatus().name());
    }

    // ---------- close (API-PRB-012) ----------

    @Transactional
    public CloseResponse close(Long id, CloseRequest request) {
        requireRole(PM);
        Problem problem = findProblem(id);
        long openActions = actionRepository.countByProblemIdAndStatusNot(id, ActionStatus.DONE);

        if (openActions > 0 && !request.force()) {
            return new CloseResponse(id, problem.getStatus().name(),
                    "미해결 후속 조치 " + openActions + "건이 있습니다. 종료하려면 force=true로 재요청하세요.");
        }
        problem.changeStatus(ProblemStatus.RESOLVED_CLOSED);
        problemRepository.save(problem);
        String warning = openActions > 0
                ? "미해결 후속 조치 " + openActions + "건이 남아있으나 강제 종료되었습니다."
                : null;
        timelineRepository.save(TimelineEvent.of(TT, id, "CLOSE",
                warning != null ? warning : "문제가 종료되었습니다."));
        return new CloseResponse(id, problem.getStatus().name(), warning);
    }

    // ---------- 인시던트→문제 연계(API-INC-012) 재사용 ----------

    /** 인시던트에서 신규 문제(REACTIVE)를 생성한다. 역할 검사는 호출측(IncidentService)에서 수행. */
    @Transactional
    public Long createReactiveProblem(String summary, String description) {
        Problem saved = problemRepository.save(new Problem(
                nextTicketKey(), summary, description, ProblemOrigin.REACTIVE,
                null, null, null, null));
        timelineRepository.save(TimelineEvent.of(TT, saved.getId(), "CREATE",
                "인시던트 연계로 문제가 생성되었습니다."));
        return saved.getId();
    }

    /** 기존 문제 존재 여부. 인시던트 연계(API-INC-012)에서 부재를 400으로 매핑하기 위해 boolean으로 반환. */
    @Transactional(readOnly = true)
    public boolean existsProblem(Long problemId) {
        return problemId != null && problemRepository.findById(problemId)
                .filter(p -> !p.isDeleted()).isPresent();
    }

    // ---------- helpers ----------

    private ProblemSummaryResponse toSummary(Problem p) {
        return new ProblemSummaryResponse(p.getId(), p.getTicketKey(), p.getSummary(), p.getStatus().name(),
                p.getPriority() != null ? p.getPriority().name() : null,
                p.getOrigin() != null ? p.getOrigin().name() : null,
                p.getCreatedBy(), p.getUpdatedAt());
    }

    private ProblemDetailResponse toDetail(Problem p) {
        Long id = p.getId();
        ProblemDetailResponse.Rca rca = new ProblemDetailResponse.Rca(
                p.getRootCause(), currentFiveWhys(id), p.getRootCauseCategory());

        List<ProblemDetailResponse.LinkRef> linkedIncidents = ticketLinkRepository
                .findBySourceTypeAndSourceId(TT, id).stream()
                .filter(l -> l.getTargetType() == TicketType.INCIDENT)
                .map(l -> new ProblemDetailResponse.LinkRef(l.getTargetId(), incidentKey(l.getTargetId())))
                .toList();
        List<ProblemDetailResponse.LinkRef> linkedChanges = ticketLinkRepository
                .findBySourceTypeAndSourceId(TT, id).stream()
                .filter(l -> l.getTargetType() == TicketType.CHANGE)
                .map(l -> new ProblemDetailResponse.LinkRef(l.getTargetId(), null))
                .toList();
        List<ProblemDetailResponse.ActionDto> actions = actionRepository.findByProblemId(id).stream()
                .map(a -> new ProblemDetailResponse.ActionDto(a.getId(), a.getDescription(), a.getStatus().name()))
                .toList();
        List<String> allowed = ProblemStateMachine.allowedTargets(p.getStatus()).stream()
                .map(ProblemStatus::name).sorted().toList();

        return new ProblemDetailResponse(id, p.getTicketKey(), p.getSummary(), p.getDescription(),
                p.getStatus().name(), p.getPriority() != null ? p.getPriority().name() : null,
                p.getImpact() != null ? p.getImpact().name() : null,
                p.getUrgency() != null ? p.getUrgency().name() : null,
                rca, p.getWorkaround(), linkedIncidents, linkedChanges, actions, allowed);
    }

    private KnownErrorSearchResponse toKnownErrorSearch(KnownError k) {
        String problemKey = problemRepository.findById(k.getProblemId())
                .map(Problem::getTicketKey).orElse(null);
        return new KnownErrorSearchResponse(k.getId(), k.getTitle(), k.getRootCause(),
                k.getWorkaround(), problemKey);
    }

    private List<String> currentFiveWhys(Long problemId) {
        return fiveWhyRepository.findByProblemIdOrderByStepNoAsc(problemId).stream()
                .map(ProblemFiveWhy::getContent).toList();
    }

    private String incidentKey(Long incidentId) {
        return incidentRepository.findById(incidentId).map(Incident::getTicketKey).orElse(null);
    }

    private void saveLinkOnce(TicketType sourceType, Long sourceId, TicketType targetType, Long targetId,
                              String linkType) {
        if (!ticketLinkRepository.existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
                sourceType, sourceId, targetType, targetId)) {
            ticketLinkRepository.save(new TicketLink(sourceType, sourceId, targetType, targetId, linkType));
        }
    }

    private String nextTicketKey() {
        String prefix = "PRB-" + Year.now().getValue() + "-";
        long seq = problemRepository.countByTicketKeyStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private Problem findProblem(Long id) {
        return problemRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));
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
}
