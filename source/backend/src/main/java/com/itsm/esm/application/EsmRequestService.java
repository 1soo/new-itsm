package com.itsm.esm.application;

import tools.jackson.databind.ObjectMapper;
import com.itsm.asset.domain.Asset;
import com.itsm.asset.domain.AssetStatus;
import com.itsm.asset.domain.repository.AssetRepository;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.Department;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.domain.ApprovalRequest;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.form.FormJsonMapper;
import com.itsm.common.form.FormSubmissionValidator;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.Comment;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
import com.itsm.common.ticket.TimelineMessages;
import com.itsm.common.ticket.repository.CommentRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.esm.application.dto.CommentCreateRequest;
import com.itsm.esm.application.dto.CommentResponse;
import com.itsm.esm.application.dto.CreateRequestRequest;
import com.itsm.esm.application.dto.RequestCreatedResponse;
import com.itsm.esm.application.dto.RequestDetailResponse;
import com.itsm.esm.application.dto.RequestSummaryResponse;
import com.itsm.esm.application.dto.StatusResponse;
import com.itsm.esm.application.dto.StatusTransitionRequest;
import com.itsm.esm.domain.ChecklistTemplateType;
import com.itsm.esm.domain.EsmCatalogItem;
import com.itsm.esm.domain.EsmChecklist;
import com.itsm.esm.domain.EsmChecklistTask;
import com.itsm.esm.domain.EsmChecklistTemplateTask;
import com.itsm.esm.domain.EsmRequest;
import com.itsm.esm.domain.EsmRequestStatus;
import com.itsm.esm.domain.repository.EsmCatalogItemRepository;
import com.itsm.esm.domain.repository.EsmChecklistRepository;
import com.itsm.esm.domain.repository.EsmChecklistTaskRepository;
import com.itsm.esm.domain.repository.EsmChecklistTemplateTaskRepository;
import com.itsm.esm.domain.repository.EsmRequestRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 부서 요청 유스케이스(API-ESM-005~009). 제출은 인증 사용자 전반, 처리(상태전이)는 DEPT_COORDINATOR
 * 소속 부서 일치 시에만 허용. 온보딩/오프보딩 유형은 제출 시 체크리스트를 자동 생성한다.
 * 양식 제출 데이터(formValues)는 컴포넌트 key 기준 key-value 맵을 통째로 JSONB에 저장하고,
 * 제출 시 공용 FormSubmissionValidator(common.form)로 재검증한다(2026-07-19 유지보수 요청, 레거시 EAV 폐기).
 */
@Service
public class EsmRequestService {

    private static final String DEPT_COORDINATOR = "DEPT_COORDINATOR";
    private static final TicketType TT = TicketType.ESM_REQUEST;
    private static final String DOMAIN = "ESM";
    private static final OffsetDateTime EPOCH = OffsetDateTime.parse("1970-01-01T00:00:00Z");
    private static final String EMPTY_FORM_VALUES = "{}";

    private final EsmRequestRepository requestRepository;
    private final EsmCatalogItemRepository catalogItemRepository;
    private final EsmChecklistTemplateTaskRepository templateTaskRepository;
    private final EsmChecklistRepository checklistRepository;
    private final EsmChecklistTaskRepository checklistTaskRepository;
    private final AssetRepository assetRepository;
    private final AppUserRepository appUserRepository;
    private final CommentRepository commentRepository;
    private final TimelineEventRepository timelineRepository;
    private final ApprovalGateService approvalGateService;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ObjectMapper objectMapper;

    public EsmRequestService(EsmRequestRepository requestRepository,
                             EsmCatalogItemRepository catalogItemRepository,
                             EsmChecklistTemplateTaskRepository templateTaskRepository,
                             EsmChecklistRepository checklistRepository,
                             EsmChecklistTaskRepository checklistTaskRepository,
                             AssetRepository assetRepository,
                             AppUserRepository appUserRepository,
                             CommentRepository commentRepository,
                             TimelineEventRepository timelineRepository,
                             ApprovalGateService approvalGateService,
                             ApprovalRequestRepository approvalRequestRepository,
                             ObjectMapper objectMapper) {
        this.requestRepository = requestRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.templateTaskRepository = templateTaskRepository;
        this.checklistRepository = checklistRepository;
        this.checklistTaskRepository = checklistTaskRepository;
        this.assetRepository = assetRepository;
        this.appUserRepository = appUserRepository;
        this.commentRepository = commentRepository;
        this.timelineRepository = timelineRepository;
        this.approvalGateService = approvalGateService;
        this.approvalRequestRepository = approvalRequestRepository;
        this.objectMapper = objectMapper;
    }

    // ---------- create (API-ESM-005) ----------

    @Transactional
    public RequestCreatedResponse create(CreateRequestRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        EsmCatalogItem item = catalogItemRepository.findById(request.catalogItemId())
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "유효하지 않은 카탈로그 항목입니다."));

        FormSubmissionValidator.validate(readSchema(item.getFormSchema()), request.formValues());

        Long checklistId = null;
        if (item.getChecklistTemplateType() != ChecklistTemplateType.NONE) {
            if (!StringUtils.hasText(request.targetUserName())) {
                throw new BusinessException(ErrorCode.TARGET_USER_NAME_REQUIRED);
            }
            checklistId = createChecklist(item, request.targetUserName());
        }

        EsmRequest saved = requestRepository.save(new EsmRequest(
                nextTicketKey(), item.getId(), principal.userId(), item.getDepartment(),
                request.targetUserName(), checklistId, writeValues(request.formValues())));

        timelineRepository.save(TimelineEvent.of(TT, saved.getId(), "SUBMIT", "부서 요청이 제출되었습니다."));
        return new RequestCreatedResponse(saved.getId(), saved.getTicketKey(), saved.getStatus().name(), checklistId);
    }

    private Long createChecklist(EsmCatalogItem item, String targetUserName) {
        List<EsmChecklistTemplateTask> templateTasks =
                templateTaskRepository.findByCatalogItemIdOrderBySortOrderAsc(item.getId());
        if (templateTasks.isEmpty()) {
            throw new BusinessException(ErrorCode.ESM_CHECKLIST_TEMPLATE_REQUIRED);
        }
        EsmChecklist checklist = checklistRepository.save(
                new EsmChecklist(item.getChecklistTemplateType(), targetUserName));
        for (EsmChecklistTemplateTask t : templateTasks) {
            checklistTaskRepository.save(new EsmChecklistTask(checklist.getId(), t.getDepartment(), t.getTaskDescription(), null));
        }
        if (item.getChecklistTemplateType() == ChecklistTemplateType.OFFBOARDING) {
            addAssetRecoveryTasks(checklist.getId(), targetUserName);
        }
        return checklist.getId();
    }

    /** 대상자 보유 자산(폐기 제외)마다 IT 부서 자산 회수 하위 작업을 자동 추가한다. 보유 자산 없으면 스킵(에러 아님). */
    private void addAssetRecoveryTasks(Long checklistId, String targetUserName) {
        List<Asset> assets = assetRepository.search(null, null, targetUserName, null, false, null, Pageable.unpaged())
                .stream()
                .filter(a -> a.getStatus() != AssetStatus.RETIREMENT)
                .toList();
        for (Asset asset : assets) {
            String description = "자산 회수: " + asset.getName() + "(" + asset.getAssetKey() + ")";
            checklistTaskRepository.save(new EsmChecklistTask(checklistId, Department.IT, description, asset.getId()));
        }
    }

    private String nextTicketKey() {
        String prefix = "ESM-" + Year.now().getValue() + "-";
        long seq = requestRepository.countByTicketKeyStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    // ---------- list (API-ESM-006) ----------

    @Transactional(readOnly = true)
    public PageResponse<RequestSummaryResponse> list(String scope, EsmRequestStatus status,
                                                      OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        Long requesterFilter;
        Department departmentFilter;
        if (scope != null && scope.equalsIgnoreCase("all")) {
            if (!SecurityUtils.hasRole(DEPT_COORDINATOR)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
            requesterFilter = null;
            departmentFilter = myDepartment(principal);
            if (departmentFilter == null && !SecurityUtils.isSystemAdmin()) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        } else {
            requesterFilter = principal.userId();
            departmentFilter = null;
        }
        OffsetDateTime fromV = from != null ? from : EPOCH;
        OffsetDateTime toV = to != null ? to : OffsetDateTime.now().plusYears(100);
        return PageResponse.from(
                requestRepository.search(requesterFilter, departmentFilter, status, fromV, toV, pageable),
                this::toSummary);
    }

    // ---------- detail (API-ESM-007) ----------

    @Transactional(readOnly = true)
    public RequestDetailResponse detail(Long id) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        EsmRequest request = findRequest(id);
        assertCanViewDetail(principal, request);

        EsmCatalogItem item = catalogItemRepository.findById(request.getCatalogItemId()).orElse(null);
        Map<String, Object> formValues = readValues(request.getFormValues());

        List<CommentResponse> comments = commentRepository.findByTicketTypeAndTicketIdOrderByCreatedAtAsc(TT, id).stream()
                .map(c -> new CommentResponse(c.getId(), userName(c.getAuthorId()), c.getBody(), c.getCreatedAt()))
                .toList();
        Map<String, String> actorCache = new HashMap<>();
        List<RequestDetailResponse.TimelineEntry> timeline =
                timelineRepository.findByTicketTypeAndTicketIdOrderByOccurredAtAsc(TT, id).stream()
                        .map(t -> new RequestDetailResponse.TimelineEntry(
                                t.getEventType(), t.getMessage(), t.getOccurredAt(),
                                actorCache.computeIfAbsent(t.getCreatedBy(), appUserRepository::resolveDisplayName)))
                        .toList();

        ApprovalRequest latestApproval = approvalRequestRepository
                .findTopByTicketTypeAndTicketIdOrderByIdDesc(TT, id).orElse(null);
        RequestDetailResponse.ApprovalInfo approvalInfo = new RequestDetailResponse.ApprovalInfo(
                latestApproval != null ? latestApproval.getId() : null,
                latestApproval != null ? latestApproval.getStatus().name() : null);

        return new RequestDetailResponse(
                request.getId(), request.getTicketKey(), item != null ? item.getName() : null,
                request.getDepartment(), request.getStatus().name(), formValues,
                userName(request.getRequesterId()), userName(request.getAssigneeId()),
                request.getChecklistId(), approvalInfo, comments, timeline);
    }

    // ---------- transition (API-ESM-008) ----------

    @Transactional
    public StatusResponse transition(Long id, StatusTransitionRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        EsmRequest esmRequest = findRequest(id);
        assertCanProcess(principal, esmRequest);

        EsmRequestStatus target = request.targetStatus();
        if (!allowedTargets(esmRequest.getStatus()).contains(target)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        if (target == EsmRequestStatus.COMPLETED) {
            approvalGateService.checkGate(DOMAIN, null, esmRequest.getRequesterId(), TT, id);
        }
        if (esmRequest.getAssigneeId() == null) {
            esmRequest.assignTo(principal.userId());
        }
        esmRequest.changeStatus(target);
        requestRepository.save(esmRequest);
        timelineRepository.save(TimelineEvent.of(TT, id, "STATUS_" + target.name(),
                StringUtils.hasText(request.note()) ? request.note()
                        : "상태가 " + TimelineMessages.quotedWithParticle(target.label()) + " 변경되었습니다."));
        return new StatusResponse(id, target.name());
    }

    private List<EsmRequestStatus> allowedTargets(EsmRequestStatus current) {
        return switch (current) {
            case SUBMITTED -> List.of(EsmRequestStatus.IN_PROGRESS, EsmRequestStatus.COMPLETED, EsmRequestStatus.REJECTED);
            case IN_PROGRESS -> List.of(EsmRequestStatus.COMPLETED, EsmRequestStatus.REJECTED);
            case COMPLETED, REJECTED -> List.of();
        };
    }

    // ---------- comment (API-ESM-009) ----------

    @Transactional
    public CommentResponse addComment(Long id, CommentCreateRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        EsmRequest esmRequest = findRequest(id);
        assertCanView(principal, esmRequest);
        Comment comment = commentRepository.save(new Comment(TT, id, principal.userId(), request.body()));
        return new CommentResponse(comment.getId(), userName(principal.userId()), comment.getBody(), comment.getCreatedAt());
    }

    // ---------- helpers ----------

    /** 요청자 본인 또는 소속 부서 DEPT_COORDINATOR인지 여부(댓글 작성(API-ESM-009) 등 조회 외 권한의 공통 기준). */
    private boolean canView(AuthPrincipal principal, EsmRequest request) {
        if (SecurityUtils.isSystemAdmin() || request.getRequesterId().equals(principal.userId())) {
            return true;
        }
        if (SecurityUtils.hasRole(DEPT_COORDINATOR)) {
            Department myDept = myDepartment(principal);
            if (myDept != null && myDept == request.getDepartment()) {
                return true;
            }
        }
        return false;
    }

    private void assertCanView(AuthPrincipal principal, EsmRequest request) {
        if (!canView(principal, request)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 상세 조회(API-ESM-007) 전용 가드. {@link #canView} 조건에 더해 승인 대상자 역할 기반 동적 상세조회
     * 권한(2026-07-15)을 OR로 추가한다 — 이 동적 판정은 상세조회에만 적용되며 댓글 작성 등 다른 권한에는
     * 영향을 주지 않는다(코드리뷰 지적, common.md 0-1절/approver.md 3절 범위 준수).
     */
    private void assertCanViewDetail(AuthPrincipal principal, EsmRequest request) {
        if (!canView(principal, request) && !approvalGateService.canApproverView(DOMAIN, null, request.getRequesterId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void assertCanProcess(AuthPrincipal principal, EsmRequest request) {
        if (SecurityUtils.isSystemAdmin()) {
            return;
        }
        if (!SecurityUtils.hasRole(DEPT_COORDINATOR)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        Department myDept = myDepartment(principal);
        if (myDept == null || myDept != request.getDepartment()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private Department myDepartment(AuthPrincipal principal) {
        return appUserRepository.findById(principal.userId()).map(AppUser::getDepartment).orElse(null);
    }

    private EsmRequest findRequest(Long id) {
        return requestRepository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.ESM_REQUEST_NOT_FOUND));
    }

    private RequestSummaryResponse toSummary(EsmRequest r) {
        return new RequestSummaryResponse(r.getId(), r.getTicketKey(), catalogName(r.getCatalogItemId()),
                r.getDepartment(), r.getStatus().name(), r.getUpdatedAt());
    }

    private String catalogName(Long id) {
        return id == null ? null : catalogItemRepository.findById(id).map(EsmCatalogItem::getName).orElse(null);
    }

    private String userName(Long id) {
        return id == null ? null : appUserRepository.findById(id).map(AppUser::getName).orElse(null);
    }

    private Map<String, Object> readSchema(String json) {
        return FormJsonMapper.readJsonMap(objectMapper, json);
    }

    private String writeValues(Map<String, Object> formValues) {
        return FormJsonMapper.writeJson(objectMapper, formValues, EMPTY_FORM_VALUES, "formValues 직렬화 실패");
    }

    private Map<String, Object> readValues(String json) {
        return FormJsonMapper.readJsonMap(objectMapper, json);
    }
}
