package com.itsm.srm.application;

import com.itsm.asset.application.AssetService;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.domain.ApprovalRequest;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.Comment;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
import com.itsm.common.ticket.TimelineMessages;
import com.itsm.common.ticket.repository.CommentRepository;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.srm.application.dto.AssignRequest;
import com.itsm.srm.application.dto.AssigneeCandidateResponse;
import com.itsm.srm.application.dto.CommentCreateRequest;
import com.itsm.srm.application.dto.CommentResponse;
import com.itsm.srm.application.dto.CreateRequestRequest;
import com.itsm.srm.application.dto.CsatRequest;
import com.itsm.srm.application.dto.CsatResponse;
import com.itsm.srm.application.dto.RequestCreatedResponse;
import com.itsm.srm.application.dto.RequestDetailResponse;
import com.itsm.srm.application.dto.RequestSummaryResponse;
import com.itsm.srm.application.dto.StatusResponse;
import com.itsm.srm.application.dto.StatusTransitionRequest;
import com.itsm.srm.domain.Csat;
import com.itsm.srm.domain.Queue;
import com.itsm.srm.domain.RequestStatus;
import com.itsm.srm.domain.ServiceCatalogItem;
import com.itsm.srm.domain.ServiceRequest;
import com.itsm.srm.domain.ServiceRequestFormValue;
import com.itsm.srm.domain.SlaStatus;
import com.itsm.srm.domain.repository.CatalogFormFieldRepository;
import com.itsm.srm.domain.repository.CsatRepository;
import com.itsm.srm.domain.repository.QueueRepository;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import com.itsm.srm.domain.repository.ServiceRequestFormValueRepository;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 서비스 요청 유스케이스: 제출·조회·배정·상태전이·코멘트·CSAT.
 * RBAC: scope mine/all, Agent 배정·이행, 요청자 CSAT.
 * 승인은 전 도메인 공용 승인 엔진(common.approval)이 담당하며, IN_FULFILLMENT 전이 시 게이트를 통과해야 한다
 * (docs/02_plan/api_spec/service-request.md API-SRM-010, docs/02_plan/api_spec/common.md 0절).
 */
@Service
public class ServiceRequestService {

    private static final String AGENT = "SERVICE_DESK_AGENT";
    private static final String PROCESS_OWNER = "PROCESS_OWNER";
    private static final TicketType TT = TicketType.SERVICE_REQUEST;
    private static final String DOMAIN = "SERVICE_REQUEST";

    private final ServiceRequestRepository requestRepository;
    private final ServiceRequestFormValueRepository formValueRepository;
    private final ServiceCatalogItemRepository catalogItemRepository;
    private final CatalogFormFieldRepository formFieldRepository;
    private final QueueRepository queueRepository;
    private final CsatRepository csatRepository;
    private final CommentRepository commentRepository;
    private final TimelineEventRepository timelineRepository;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final TicketLinkRepository ticketLinkRepository;
    private final AssetService assetService;
    private final ApprovalGateService approvalGateService;
    private final ApprovalRequestRepository approvalRequestRepository;

    public ServiceRequestService(ServiceRequestRepository requestRepository,
                                 ServiceRequestFormValueRepository formValueRepository,
                                 ServiceCatalogItemRepository catalogItemRepository,
                                 CatalogFormFieldRepository formFieldRepository,
                                 QueueRepository queueRepository,
                                 CsatRepository csatRepository,
                                 CommentRepository commentRepository,
                                 TimelineEventRepository timelineRepository,
                                 AppUserRepository appUserRepository,
                                 RoleRepository roleRepository,
                                 TicketLinkRepository ticketLinkRepository,
                                 AssetService assetService,
                                 ApprovalGateService approvalGateService,
                                 ApprovalRequestRepository approvalRequestRepository) {
        this.requestRepository = requestRepository;
        this.formValueRepository = formValueRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.formFieldRepository = formFieldRepository;
        this.queueRepository = queueRepository;
        this.csatRepository = csatRepository;
        this.commentRepository = commentRepository;
        this.timelineRepository = timelineRepository;
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.assetService = assetService;
        this.approvalGateService = approvalGateService;
        this.approvalRequestRepository = approvalRequestRepository;
    }

    // ---------- create ----------

    @Transactional
    public RequestCreatedResponse create(CreateRequestRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        ServiceCatalogItem item = catalogItemRepository.findById(request.catalogItemId())
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "유효하지 않은 카탈로그 항목입니다."));

        validateRequiredFields(item.getId(), request.formValues());

        Long queueId = item.getQueueId() != null ? item.getQueueId()
                : queueRepository.findFirstByIsDefaultTrue().map(Queue::getId).orElse(null);
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime responseDue = item.getSlaResponseMinutes() != null
                ? now.plusMinutes(item.getSlaResponseMinutes()) : null;
        OffsetDateTime resolveDue = item.getSlaResolveMinutes() != null
                ? now.plusMinutes(item.getSlaResolveMinutes()) : null;

        ServiceRequest saved = requestRepository.save(new ServiceRequest(
                nextTicketKey(), item.getId(), principal.userId(), queueId, responseDue, resolveDue));

        if (request.formValues() != null) {
            request.formValues().forEach((key, value) -> formValueRepository.save(
                    new ServiceRequestFormValue(saved.getId(), key, value == null ? null : String.valueOf(value))));
        }
        timelineRepository.save(TimelineEvent.of(TT, saved.getId(), "SUBMIT", "요청이 제출되었습니다."));
        return new RequestCreatedResponse(saved.getId(), saved.getTicketKey(), saved.getStatus().name(), saved.getCreatedAt());
    }

    // ---------- list ----------

    @Transactional(readOnly = true)
    public PageResponse<RequestSummaryResponse> list(String scope, Long queueId, RequestStatus status,
                                                     OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        Long requesterFilter;
        if (scope == null || scope.equalsIgnoreCase("mine")) {
            requesterFilter = principal.userId();
        } else {
            if (!SecurityUtils.hasAnyRole(AGENT, PROCESS_OWNER)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
            requesterFilter = null;
        }
        OffsetDateTime fromV = from != null ? from : OffsetDateTime.parse("1970-01-01T00:00:00Z");
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        return PageResponse.from(
                requestRepository.search(requesterFilter, queueId, status, fromV, toV, pageable),
                this::toSummary);
    }

    // ---------- detail ----------

    @Transactional(readOnly = true)
    public RequestDetailResponse detail(Long id) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        ServiceRequest request = findRequest(id);
        assertCanView(principal, request);

        ServiceCatalogItem item = catalogItemRepository.findById(request.getCatalogItemId()).orElse(null);
        Map<String, Object> formValues = new LinkedHashMap<>();
        formValueRepository.findByServiceRequestId(id)
                .forEach(v -> formValues.put(v.getFieldKey(), v.getFieldValue()));

        ApprovalRequest latestApproval = approvalRequestRepository
                .findTopByTicketTypeAndTicketIdOrderByIdDesc(TT, id).orElse(null);
        RequestDetailResponse.ApprovalInfo approvalInfo = new RequestDetailResponse.ApprovalInfo(
                latestApproval != null ? latestApproval.getId() : null,
                latestApproval != null ? latestApproval.getStatus().name() : null);

        RequestDetailResponse.SlaInfo slaInfo = new RequestDetailResponse.SlaInfo(
                SlaCalculator.status(request.getCreatedAt(), request.getSlaResponseDue(),
                        request.getStatus() != RequestStatus.SUBMITTED).name(),
                SlaCalculator.status(request.getCreatedAt(), request.getSlaResolveDue(),
                        isResolved(request.getStatus())).name());

        List<CommentResponse> comments = commentRepository.findByTicketTypeAndTicketIdOrderByCreatedAtAsc(TT, id).stream()
                .map(c -> new CommentResponse(c.getId(), userName(c.getAuthorId()), c.getBody(), c.getCreatedAt()))
                .toList();
        Map<String, String> actorCache = new java.util.HashMap<>();
        List<RequestDetailResponse.TimelineEntry> timeline =
                timelineRepository.findByTicketTypeAndTicketIdOrderByOccurredAtAsc(TT, id).stream()
                        .map(t -> new RequestDetailResponse.TimelineEntry(
                                t.getEventType(), t.getMessage(), t.getOccurredAt(),
                                actorCache.computeIfAbsent(t.getCreatedBy(), appUserRepository::resolveDisplayName)))
                        .toList();
        List<RequestDetailResponse.LinkedAsset> linkedAssets = ticketLinkRepository
                .findBySourceTypeAndSourceId(TT, id).stream()
                .filter(l -> l.getTargetType() == TicketType.ASSET)
                .map(l -> new RequestDetailResponse.LinkedAsset(l.getTargetId(), assetService.assetKeyOf(l.getTargetId())))
                .toList();

        return new RequestDetailResponse(
                request.getId(), request.getTicketKey(), item != null ? item.getName() : null,
                request.getStatus().name(), formValues,
                userName(request.getRequesterId()), userName(request.getAssigneeId()), queueName(request.getQueueId()),
                approvalInfo, slaInfo, List.of(), linkedAssets, comments, timeline,
                allowedTransitions(principal, request));
    }

    /** 현재 상태·요청자 역할 기준으로 수행 가능한 상태 전이 target 목록(FE 버튼 노출용).
     * 승인 게이트 통과 여부는 실제 전이 시도 시 판정하므로(409로 안내) 여기서는 선반영하지 않는다. */
    private List<String> allowedTransitions(AuthPrincipal principal, ServiceRequest sr) {
        if (sr.getStatus().isTerminal()) {
            return List.of();
        }
        boolean isRequester = sr.getRequesterId().equals(principal.userId());
        List<String> result = new java.util.ArrayList<>();
        for (RequestStatus target : RequestStateMachine.allowedTargets(sr.getStatus())) {
            boolean roleOk = (target == RequestStatus.CLOSED)
                    ? (isRequester || SecurityUtils.hasAnyRole(AGENT))
                    : SecurityUtils.hasAnyRole(AGENT);
            if (!roleOk) {
                continue;
            }
            result.add(target.name());
        }
        return result;
    }

    // ---------- assignee candidates (API-SRM-017) ----------

    @Transactional(readOnly = true)
    public List<AssigneeCandidateResponse> assigneeCandidates(Long id) {
        ServiceRequest sr = findRequest(id);
        ServiceCatalogItem item = catalogItemRepository.findById(sr.getCatalogItemId()).orElse(null);
        if (item == null || item.getAssigneeRoleId() == null) {
            return List.of();
        }
        String roleCode = roleRepository.findById(item.getAssigneeRoleId()).map(Role::getRoleCode).orElse(null);
        if (roleCode == null) {
            return List.of();
        }
        return appUserRepository.search(null, null, UserStatus.ACTIVE, roleCode, Pageable.unpaged())
                .stream()
                .map(u -> new AssigneeCandidateResponse(u.getId(), u.getName()))
                .toList();
    }

    // ---------- assign ----------

    @Transactional
    public RequestDetailResponse assign(Long id, AssignRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        ServiceRequest sr = findRequest(id);
        Long assigneeId = request != null && request.assigneeId() != null ? request.assigneeId() : principal.userId();
        if (appUserRepository.findById(assigneeId).isEmpty()) {
            throw new BusinessException(ErrorCode.ASSIGNEE_NOT_FOUND);
        }
        sr.assignTo(assigneeId);
        requestRepository.save(sr);
        timelineRepository.save(TimelineEvent.of(TT, id, "ASSIGN", "담당자가 배정되었습니다: " + userName(assigneeId)));
        return detail(id);
    }

    // ---------- status transition ----------

    @Transactional
    public StatusResponse transition(Long id, StatusTransitionRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        ServiceRequest sr = findRequest(id);
        RequestStatus target = request.targetStatus();

        if (sr.getStatus().isTerminal()) {
            throw new BusinessException(ErrorCode.REQUEST_ALREADY_CLOSED);
        }
        assertTransitionRole(principal, sr, target);
        if (!RequestStateMachine.isAllowed(sr.getStatus(), target)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        if (target == RequestStatus.ROUTED && sr.getAssigneeId() == null) {
            throw new BusinessException(ErrorCode.ASSIGNEE_REQUIRED_FOR_ROUTING);
        }

        if (target == RequestStatus.IN_FULFILLMENT) {
            approvalGateService.checkGate(DOMAIN, String.valueOf(sr.getCatalogItemId()), sr.getRequesterId(), TT, id);
        }

        sr.changeStatus(target);
        requestRepository.save(sr);
        timelineRepository.save(TimelineEvent.of(TT, id, "STATUS_" + target.name(),
                StringUtils.hasText(request.note()) ? request.note()
                        : "상태가 " + TimelineMessages.quotedWithParticle(target.label()) + " 변경되었습니다."));
        return new StatusResponse(id, target.name());
    }

    // ---------- comment ----------

    @Transactional
    public CommentResponse addComment(Long id, CommentCreateRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        ServiceRequest sr = findRequest(id);
        assertCanView(principal, sr);
        Comment comment = commentRepository.save(new Comment(TT, id, principal.userId(), request.body()));
        return new CommentResponse(comment.getId(), userName(principal.userId()), comment.getBody(), comment.getCreatedAt());
    }

    // ---------- CSAT ----------

    @Transactional
    public CsatResponse submitCsat(Long id, CsatRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        ServiceRequest sr = findRequest(id);
        if (!sr.getRequesterId().equals(principal.userId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (sr.getStatus() != RequestStatus.CLOSED) {
            throw new BusinessException(ErrorCode.CSAT_NOT_ALLOWED);
        }
        if (csatRepository.existsByServiceRequestId(id)) {
            throw new BusinessException(ErrorCode.CSAT_ALREADY_SUBMITTED);
        }
        Csat csat = csatRepository.save(new Csat(id, (short) (int) request.score(), request.comment()));
        return new CsatResponse(csat.getId(), csat.getScore());
    }

    // ---------- helpers ----------

    private void validateRequiredFields(Long catalogItemId, Map<String, Object> formValues) {
        formFieldRepository.findByCatalogItemIdOrderBySortOrderAsc(catalogItemId).stream()
                .filter(com.itsm.srm.domain.CatalogFormField::isRequired)
                .forEach(f -> {
                    Object v = formValues == null ? null : formValues.get(f.getFieldKey());
                    if (v == null || !StringUtils.hasText(String.valueOf(v))) {
                        throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING,
                                "필수 항목 누락: " + f.getLabel());
                    }
                });
    }

    private String nextTicketKey() {
        String prefix = "SRM-" + Year.now().getValue() + "-";
        long seq = requestRepository.countByTicketKeyStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private void assertTransitionRole(AuthPrincipal principal, ServiceRequest sr, RequestStatus target) {
        boolean isRequester = sr.getRequesterId().equals(principal.userId());
        if (target == RequestStatus.CLOSED) {
            if (!(isRequester || SecurityUtils.hasAnyRole(AGENT))) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        } else if (!SecurityUtils.hasAnyRole(AGENT)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void assertCanView(AuthPrincipal principal, ServiceRequest sr) {
        if (sr.getRequesterId().equals(principal.userId())) {
            return;
        }
        if (SecurityUtils.hasAnyRole(AGENT, PROCESS_OWNER)) {
            return;
        }
        if (!approvalGateService.canApproverView(DOMAIN, String.valueOf(sr.getCatalogItemId()), sr.getRequesterId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private boolean isResolved(RequestStatus status) {
        return status == RequestStatus.FULFILLED || status == RequestStatus.CLOSED;
    }

    private RequestSummaryResponse toSummary(ServiceRequest r) {
        SlaStatus sla = SlaCalculator.status(r.getCreatedAt(), r.getSlaResolveDue(), isResolved(r.getStatus()));
        return new RequestSummaryResponse(r.getId(), r.getTicketKey(), catalogName(r.getCatalogItemId()),
                r.getStatus().name(), sla.name(), userName(r.getAssigneeId()), r.getAssigneeId(), r.getUpdatedAt());
    }

    private ServiceRequest findRequest(Long id) {
        return requestRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND));
    }

    private String userName(Long id) {
        return id == null ? null : appUserRepository.findById(id).map(AppUser::getName).orElse(null);
    }

    private String queueName(Long id) {
        return id == null ? null : queueRepository.findById(id).map(Queue::getName).orElse(null);
    }

    private String catalogName(Long id) {
        return id == null ? null : catalogItemRepository.findById(id).map(ServiceCatalogItem::getName).orElse(null);
    }
}
