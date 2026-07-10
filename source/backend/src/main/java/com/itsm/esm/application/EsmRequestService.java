package com.itsm.esm.application;

import com.itsm.asset.domain.Asset;
import com.itsm.asset.domain.AssetStatus;
import com.itsm.asset.domain.repository.AssetRepository;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.Department;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.common.ticket.Comment;
import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;
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
import com.itsm.esm.domain.EsmRequestFormValue;
import com.itsm.esm.domain.EsmRequestStatus;
import com.itsm.esm.domain.repository.EsmCatalogFormFieldRepository;
import com.itsm.esm.domain.repository.EsmCatalogItemRepository;
import com.itsm.esm.domain.repository.EsmChecklistRepository;
import com.itsm.esm.domain.repository.EsmChecklistTaskRepository;
import com.itsm.esm.domain.repository.EsmChecklistTemplateTaskRepository;
import com.itsm.esm.domain.repository.EsmRequestFormValueRepository;
import com.itsm.esm.domain.repository.EsmRequestRepository;
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
 * 부서 요청 유스케이스(API-ESM-005~009). 제출은 인증 사용자 전반, 처리(상태전이)는 DEPT_COORDINATOR
 * 소속 부서 일치 시에만 허용. 온보딩/오프보딩 유형은 제출 시 체크리스트를 자동 생성한다.
 */
@Service
public class EsmRequestService {

    private static final String DEPT_COORDINATOR = "DEPT_COORDINATOR";
    private static final TicketType TT = TicketType.ESM_REQUEST;
    private static final OffsetDateTime EPOCH = OffsetDateTime.parse("1970-01-01T00:00:00Z");

    private final EsmRequestRepository requestRepository;
    private final EsmRequestFormValueRepository formValueRepository;
    private final EsmCatalogItemRepository catalogItemRepository;
    private final EsmCatalogFormFieldRepository formFieldRepository;
    private final EsmChecklistTemplateTaskRepository templateTaskRepository;
    private final EsmChecklistRepository checklistRepository;
    private final EsmChecklistTaskRepository checklistTaskRepository;
    private final AssetRepository assetRepository;
    private final AppUserRepository appUserRepository;
    private final CommentRepository commentRepository;
    private final TimelineEventRepository timelineRepository;

    public EsmRequestService(EsmRequestRepository requestRepository,
                             EsmRequestFormValueRepository formValueRepository,
                             EsmCatalogItemRepository catalogItemRepository,
                             EsmCatalogFormFieldRepository formFieldRepository,
                             EsmChecklistTemplateTaskRepository templateTaskRepository,
                             EsmChecklistRepository checklistRepository,
                             EsmChecklistTaskRepository checklistTaskRepository,
                             AssetRepository assetRepository,
                             AppUserRepository appUserRepository,
                             CommentRepository commentRepository,
                             TimelineEventRepository timelineRepository) {
        this.requestRepository = requestRepository;
        this.formValueRepository = formValueRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.formFieldRepository = formFieldRepository;
        this.templateTaskRepository = templateTaskRepository;
        this.checklistRepository = checklistRepository;
        this.checklistTaskRepository = checklistTaskRepository;
        this.assetRepository = assetRepository;
        this.appUserRepository = appUserRepository;
        this.commentRepository = commentRepository;
        this.timelineRepository = timelineRepository;
    }

    // ---------- create (API-ESM-005) ----------

    @Transactional
    public RequestCreatedResponse create(CreateRequestRequest request) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        EsmCatalogItem item = catalogItemRepository.findById(request.catalogItemId())
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "유효하지 않은 카탈로그 항목입니다."));

        validateRequiredFields(item.getId(), request.formValues());

        Long checklistId = null;
        if (item.getChecklistTemplateType() != ChecklistTemplateType.NONE) {
            if (!StringUtils.hasText(request.targetUserName())) {
                throw new BusinessException(ErrorCode.TARGET_USER_NAME_REQUIRED);
            }
            checklistId = createChecklist(item, request.targetUserName());
        }

        EsmRequest saved = requestRepository.save(new EsmRequest(
                nextTicketKey(), item.getId(), principal.userId(), item.getDepartment(),
                request.targetUserName(), checklistId));

        if (request.formValues() != null) {
            request.formValues().forEach((key, value) -> formValueRepository.save(
                    new EsmRequestFormValue(saved.getId(), key, value == null ? null : String.valueOf(value))));
        }
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

    private void validateRequiredFields(Long catalogItemId, Map<String, Object> formValues) {
        formFieldRepository.findByCatalogItemIdOrderBySortOrderAsc(catalogItemId).stream()
                .filter(com.itsm.esm.domain.EsmCatalogFormField::isRequired)
                .forEach(f -> {
                    Object v = formValues == null ? null : formValues.get(f.getFieldKey());
                    if (v == null || !StringUtils.hasText(String.valueOf(v))) {
                        throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING,
                                "필수 항목 누락: " + f.getLabel());
                    }
                });
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
            if (!principal.roles().contains(DEPT_COORDINATOR)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
            requesterFilter = null;
            departmentFilter = myDepartment(principal);
            if (departmentFilter == null) {
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
        assertCanView(principal, request);

        EsmCatalogItem item = catalogItemRepository.findById(request.getCatalogItemId()).orElse(null);
        Map<String, Object> formValues = new LinkedHashMap<>();
        formValueRepository.findByEsmRequestId(id).forEach(v -> formValues.put(v.getFieldKey(), v.getFieldValue()));

        List<CommentResponse> comments = commentRepository.findByTicketTypeAndTicketIdOrderByCreatedAtAsc(TT, id).stream()
                .map(c -> new CommentResponse(c.getId(), userName(c.getAuthorId()), c.getBody(), c.getCreatedAt()))
                .toList();
        List<RequestDetailResponse.TimelineEntry> timeline =
                timelineRepository.findByTicketTypeAndTicketIdOrderByOccurredAtAsc(TT, id).stream()
                        .map(t -> new RequestDetailResponse.TimelineEntry(t.getEventType(), t.getMessage(), t.getOccurredAt()))
                        .toList();

        return new RequestDetailResponse(
                request.getId(), request.getTicketKey(), item != null ? item.getName() : null,
                request.getDepartment(), request.getStatus().name(), formValues,
                userName(request.getRequesterId()), userName(request.getAssigneeId()),
                request.getChecklistId(), comments, timeline);
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
        if (esmRequest.getAssigneeId() == null) {
            esmRequest.assignTo(principal.userId());
        }
        esmRequest.changeStatus(target);
        requestRepository.save(esmRequest);
        timelineRepository.save(TimelineEvent.of(TT, id, "STATUS_" + target.name(),
                StringUtils.hasText(request.note()) ? request.note() : "상태가 " + target.name() + "로 변경되었습니다."));
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

    private void assertCanView(AuthPrincipal principal, EsmRequest request) {
        if (request.getRequesterId().equals(principal.userId())) {
            return;
        }
        if (principal.roles().contains(DEPT_COORDINATOR)) {
            Department myDept = myDepartment(principal);
            if (myDept != null && myDept == request.getDepartment()) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    private void assertCanProcess(AuthPrincipal principal, EsmRequest request) {
        if (!principal.roles().contains(DEPT_COORDINATOR)) {
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
}
