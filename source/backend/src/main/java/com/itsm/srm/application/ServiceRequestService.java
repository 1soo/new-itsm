package com.itsm.srm.application;

import com.itsm.asset.application.AssetService;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.Approval;
import com.itsm.common.ticket.ApprovalStatus;
import com.itsm.common.ticket.Comment;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
import com.itsm.common.ticket.repository.ApprovalRepository;
import com.itsm.common.ticket.repository.CommentRepository;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.srm.application.dto.ApprovalDecision;
import com.itsm.srm.application.dto.ApprovalDecisionRequest;
import com.itsm.srm.application.dto.ApprovalDecisionResponse;
import com.itsm.srm.application.dto.AssignRequest;
import com.itsm.srm.application.dto.CommentCreateRequest;
import com.itsm.srm.application.dto.CommentResponse;
import com.itsm.srm.application.dto.CreateRequestRequest;
import com.itsm.srm.application.dto.CsatRequest;
import com.itsm.srm.application.dto.CsatResponse;
import com.itsm.srm.application.dto.PendingApprovalResponse;
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
 * 서비스 요청 유스케이스: 제출·조회·배정·상태전이·승인·코멘트·CSAT.
 * RBAC: scope mine/all, Agent 배정·이행, 지정 Approver 승인, 요청자 CSAT.
 */
@Service
public class ServiceRequestService {

    private static final String AGENT = "SERVICE_DESK_AGENT";
    private static final String PROCESS_OWNER = "PROCESS_OWNER";
    private static final String APPROVER = "APPROVER";
    private static final TicketType TT = TicketType.SERVICE_REQUEST;

    private final ServiceRequestRepository requestRepository;
    private final ServiceRequestFormValueRepository formValueRepository;
    private final ServiceCatalogItemRepository catalogItemRepository;
    private final CatalogFormFieldRepository formFieldRepository;
    private final QueueRepository queueRepository;
    private final CsatRepository csatRepository;
    private final ApprovalRepository approvalRepository;
    private final CommentRepository commentRepository;
    private final TimelineEventRepository timelineRepository;
    private final AppUserRepository appUserRepository;
    private final TicketLinkRepository ticketLinkRepository;
    private final AssetService assetService;

    public ServiceRequestService(ServiceRequestRepository requestRepository,
                                 ServiceRequestFormValueRepository formValueRepository,
                                 ServiceCatalogItemRepository catalogItemRepository,
                                 CatalogFormFieldRepository formFieldRepository,
                                 QueueRepository queueRepository,
                                 CsatRepository csatRepository,
                                 ApprovalRepository approvalRepository,
                                 CommentRepository commentRepository,
                                 TimelineEventRepository timelineRepository,
                                 AppUserRepository appUserRepository,
                                 TicketLinkRepository ticketLinkRepository,
                                 AssetService assetService) {
        this.requestRepository = requestRepository;
        this.formValueRepository = formValueRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.formFieldRepository = formFieldRepository;
        this.queueRepository = queueRepository;
        this.csatRepository = csatRepository;
        this.approvalRepository = approvalRepository;
        this.commentRepository = commentRepository;
        this.timelineRepository = timelineRepository;
        this.appUserRepository = appUserRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.assetService = assetService;
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
            if (!hasAny(principal, AGENT, PROCESS_OWNER)) {
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

        Approval approval = approvalRepository.findByTicketTypeAndTicketId(TT, id).orElse(null);
        boolean approvalRequired = item != null && item.isApprovalRequired();
        boolean approvalApproved = approval != null && approval.getStatus() == ApprovalStatus.APPROVED;
        RequestDetailResponse.ApprovalInfo approvalInfo = new RequestDetailResponse.ApprovalInfo(
                approvalRequired,
                approval != null ? approval.getStatus().name() : null,
                approval != null ? approval.getDecisionReason() : null);

        RequestDetailResponse.SlaInfo slaInfo = new RequestDetailResponse.SlaInfo(
                SlaCalculator.status(request.getCreatedAt(), request.getSlaResponseDue(),
                        request.getStatus() != RequestStatus.SUBMITTED).name(),
                SlaCalculator.status(request.getCreatedAt(), request.getSlaResolveDue(),
                        isResolved(request.getStatus())).name());

        List<CommentResponse> comments = commentRepository.findByTicketTypeAndTicketIdOrderByCreatedAtAsc(TT, id).stream()
                .map(c -> new CommentResponse(c.getId(), userName(c.getAuthorId()), c.getBody(), c.getCreatedAt()))
                .toList();
        List<RequestDetailResponse.TimelineEntry> timeline =
                timelineRepository.findByTicketTypeAndTicketIdOrderByOccurredAtAsc(TT, id).stream()
                        .map(t -> new RequestDetailResponse.TimelineEntry(t.getEventType(), t.getMessage(), t.getOccurredAt()))
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
                allowedTransitions(principal, request, approvalRequired, approvalApproved));
    }

    /** 현재 상태·요청자 역할·승인 상태 기준으로 수행 가능한 상태 전이 target 목록(FE 버튼 노출용). */
    private List<String> allowedTransitions(AuthPrincipal principal, ServiceRequest sr,
                                            boolean approvalRequired, boolean approvalApproved) {
        if (sr.getStatus().isTerminal()) {
            return List.of();
        }
        boolean isRequester = sr.getRequesterId().equals(principal.userId());
        List<String> result = new java.util.ArrayList<>();
        for (RequestStatus target : RequestStateMachine.allowedTargets(sr.getStatus())) {
            boolean roleOk = (target == RequestStatus.CLOSED)
                    ? (isRequester || hasAny(principal, AGENT))
                    : hasAny(principal, AGENT);
            if (!roleOk) {
                continue;
            }
            if (target == RequestStatus.IN_FULFILLMENT && approvalRequired && !approvalApproved) {
                continue;
            }
            result.add(target.name());
        }
        return result;
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

        ServiceCatalogItem item = catalogItemRepository.findById(sr.getCatalogItemId()).orElse(null);
        boolean approvalRequired = item != null && item.isApprovalRequired();

        if (target == RequestStatus.IN_FULFILLMENT && approvalRequired) {
            boolean approved = approvalRepository.findByTicketTypeAndTicketId(TT, id)
                    .map(a -> a.getStatus() == ApprovalStatus.APPROVED).orElse(false);
            if (!approved) {
                throw new BusinessException(ErrorCode.APPROVAL_PENDING);
            }
        }

        RequestStatus effective = target;
        if (target == RequestStatus.ROUTED && approvalRequired) {
            effective = RequestStatus.APPROVAL_PENDING;
            if (approvalRepository.findByTicketTypeAndTicketId(TT, id).isEmpty()) {
                String approverRole = (item.getApproverRole() != null && !item.getApproverRole().isBlank())
                        ? item.getApproverRole() : APPROVER; // 카탈로그 approver_role 복사, 미지정 시 기본 APPROVER
                approvalRepository.save(new Approval(TT, id, approverRole));
            }
        }
        sr.changeStatus(effective);
        requestRepository.save(sr);
        timelineRepository.save(TimelineEvent.of(TT, id, "STATUS_" + effective.name(),
                StringUtils.hasText(request.note()) ? request.note() : "상태가 " + effective.name() + "로 변경되었습니다."));
        return new StatusResponse(id, effective.name());
    }

    // ---------- approval ----------

    @Transactional
    public ApprovalDecisionResponse decideApproval(Long id, ApprovalDecisionRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        ServiceRequest sr = findRequest(id);
        Approval approval = approvalRepository.findByTicketTypeAndTicketId(TT, id)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NOT_FOUND));
        if (!principal.roles().contains(approval.getApproverRole())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED); // 승인 담당 역할 미보유
        }
        if (!approval.isPending()) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY_DECIDED); // 이미 결정된 건 재처리 차단(상태 오염 방지)
        }
        if (request.decision() == ApprovalDecision.REJECT && !StringUtils.hasText(request.reason())) {
            throw new BusinessException(ErrorCode.REJECT_REASON_REQUIRED);
        }
        if (request.decision() == ApprovalDecision.APPROVE) {
            approval.approve(principal.userId(), request.reason());
            sr.changeStatus(RequestStatus.ROUTED);
            timelineRepository.save(TimelineEvent.of(TT, id, "APPROVAL_APPROVED", "승인되었습니다."));
        } else {
            approval.reject(principal.userId(), request.reason());
            sr.changeStatus(RequestStatus.REJECTED);
            timelineRepository.save(TimelineEvent.of(TT, id, "APPROVAL_REJECTED", "반려되었습니다: " + request.reason()));
        }
        approvalRepository.save(approval);
        requestRepository.save(sr);
        return new ApprovalDecisionResponse(id, approval.getStatus().name());
    }

    @Transactional(readOnly = true)
    public List<PendingApprovalResponse> pendingApprovals() {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        if (principal.roles().isEmpty()) {
            return List.of();
        }
        return approvalRepository.findByTicketTypeAndStatusAndApproverRoleIn(TT, ApprovalStatus.PENDING, principal.roles())
                .stream()
                .map(a -> {
                    ServiceRequest sr = requestRepository.findById(a.getTicketId()).orElse(null);
                    return new PendingApprovalResponse(
                            a.getTicketId(),
                            sr != null ? sr.getTicketKey() : null,
                            sr != null ? userName(sr.getRequesterId()) : null,
                            a.getCreatedAt());
                })
                .toList();
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
            if (!(isRequester || hasAny(principal, AGENT))) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        } else if (!hasAny(principal, AGENT)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void assertCanView(AuthPrincipal principal, ServiceRequest sr) {
        if (sr.getRequesterId().equals(principal.userId())) {
            return;
        }
        if (!hasAny(principal, AGENT, PROCESS_OWNER, APPROVER)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private boolean hasAny(AuthPrincipal principal, String... roles) {
        for (String r : roles) {
            if (principal.roles().contains(r)) {
                return true;
            }
        }
        return false;
    }

    private boolean isResolved(RequestStatus status) {
        return status == RequestStatus.FULFILLED || status == RequestStatus.CLOSED;
    }

    private RequestSummaryResponse toSummary(ServiceRequest r) {
        SlaStatus sla = SlaCalculator.status(r.getCreatedAt(), r.getSlaResolveDue(), isResolved(r.getStatus()));
        return new RequestSummaryResponse(r.getId(), r.getTicketKey(), catalogName(r.getCatalogItemId()),
                r.getStatus().name(), sla.name(), userName(r.getAssigneeId()), r.getUpdatedAt());
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
