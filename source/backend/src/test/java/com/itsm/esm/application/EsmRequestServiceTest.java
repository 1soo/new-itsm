package com.itsm.esm.application;

import com.itsm.asset.domain.Asset;
import com.itsm.asset.domain.AssetType;
import com.itsm.asset.domain.repository.AssetRepository;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.Department;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.ticket.repository.CommentRepository;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import com.itsm.esm.application.dto.CommentCreateRequest;
import com.itsm.esm.application.dto.CreateRequestRequest;
import com.itsm.esm.application.dto.StatusTransitionRequest;
import com.itsm.esm.domain.ChecklistTemplateType;
import com.itsm.esm.domain.EsmCatalogFormField;
import com.itsm.esm.domain.EsmCatalogItem;
import com.itsm.esm.domain.EsmChecklist;
import com.itsm.esm.domain.EsmChecklistTemplateTask;
import com.itsm.esm.domain.EsmRequest;
import com.itsm.esm.domain.EsmRequestStatus;
import com.itsm.esm.domain.repository.EsmCatalogFormFieldRepository;
import com.itsm.esm.domain.repository.EsmCatalogItemRepository;
import com.itsm.esm.domain.repository.EsmChecklistRepository;
import com.itsm.esm.domain.repository.EsmChecklistTaskRepository;
import com.itsm.esm.domain.repository.EsmChecklistTemplateTaskRepository;
import com.itsm.esm.domain.repository.EsmRequestFormValueRepository;
import com.itsm.esm.domain.repository.EsmRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EsmRequestServiceTest {

    @Mock EsmRequestRepository requestRepository;
    @Mock EsmRequestFormValueRepository formValueRepository;
    @Mock EsmCatalogItemRepository catalogItemRepository;
    @Mock EsmCatalogFormFieldRepository formFieldRepository;
    @Mock EsmChecklistTemplateTaskRepository templateTaskRepository;
    @Mock EsmChecklistRepository checklistRepository;
    @Mock EsmChecklistTaskRepository checklistTaskRepository;
    @Mock AssetRepository assetRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock CommentRepository commentRepository;
    @Mock TimelineEventRepository timelineRepository;
    @Mock ApprovalGateService approvalGateService;
    @Mock ApprovalRequestRepository approvalRequestRepository;

    EsmRequestService service;

    @BeforeEach
    void setUp() {
        service = new EsmRequestService(requestRepository, formValueRepository, catalogItemRepository,
                formFieldRepository, templateTaskRepository, checklistRepository, checklistTaskRepository,
                assetRepository, appUserRepository, commentRepository, timelineRepository,
                approvalGateService, approvalRequestRepository);
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(requestRepository.countByTicketKeyStartingWith(any())).thenReturn(0L);
        when(checklistRepository.save(any())).thenAnswer(inv -> {
            EsmChecklist c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 100L);
            return c;
        });
        when(checklistTaskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formFieldRepository.findByCatalogItemIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        when(approvalRequestRepository.findTopByTicketTypeAndTicketIdOrderByIdDesc(any(), any()))
                .thenReturn(Optional.empty());
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login(Long userId, String... roles) {
        AuthPrincipal principal = new AuthPrincipal(userId, "u" + userId + "@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private ErrorCode codeOf(Throwable e) {
        return ((BusinessException) e).getErrorCode();
    }

    private EsmCatalogItem catalogItem(Department department, ChecklistTemplateType type) {
        EsmCatalogItem item = new EsmCatalogItem("항목", null, department, type);
        ReflectionTestUtils.setField(item, "id", 10L);
        return item;
    }

    private EsmRequest esmRequest(Long requesterId, Department department, EsmRequestStatus status) {
        EsmRequest r = new EsmRequest("ESM-2026-0001", 10L, requesterId, department, null, null);
        if (status != EsmRequestStatus.SUBMITTED) {
            r.changeStatus(status);
        }
        return r;
    }

    private AppUser userWithDepartment(Department department) {
        AppUser user = new AppUser("u@itsm.local", "hash", "사용자", UserStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "department", department);
        return user;
    }

    // ---------- create ----------

    @Test
    void createInvalidCatalogItemThrows() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new CreateRequestRequest(10L, Map.of(), null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void createRequiredFieldMissingThrows() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalogItem(Department.LEGAL, ChecklistTemplateType.NONE)));
        when(formFieldRepository.findByCatalogItemIdOrderBySortOrderAsc(10L))
                .thenReturn(List.of(new EsmCatalogFormField(10L, "title", "제목", "text", true, null, 0)));

        assertThatThrownBy(() -> service.create(new CreateRequestRequest(10L, Map.of(), null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.REQUIRED_FIELD_MISSING));
    }

    @Test
    void createOnboardingWithoutTargetUserNameThrows() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalogItem(Department.HR, ChecklistTemplateType.ONBOARDING)));

        assertThatThrownBy(() -> service.create(new CreateRequestRequest(10L, Map.of(), " ")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.TARGET_USER_NAME_REQUIRED));
    }

    @Test
    void createOnboardingWithoutTemplateThrows() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalogItem(Department.HR, ChecklistTemplateType.ONBOARDING)));
        when(templateTaskRepository.findByCatalogItemIdOrderBySortOrderAsc(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.create(new CreateRequestRequest(10L, Map.of(), "김철수")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ESM_CHECKLIST_TEMPLATE_REQUIRED));
    }

    @Test
    void createOnboardingSuccessGeneratesChecklist() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalogItem(Department.HR, ChecklistTemplateType.ONBOARDING)));
        when(templateTaskRepository.findByCatalogItemIdOrderBySortOrderAsc(10L)).thenReturn(List.of(
                new EsmChecklistTemplateTask(10L, Department.HR, "인사 서류 확인", 1),
                new EsmChecklistTemplateTask(10L, Department.IT, "장비 지급", 2)));

        var response = service.create(new CreateRequestRequest(10L, Map.of(), "김철수"));

        assertThat(response.checklistId()).isEqualTo(100L);
        verify(checklistTaskRepository, times(2)).save(any());
        verify(assetRepository, never()).search(any(), any(), any(), any(), any(Boolean.class), any(), any());
    }

    @Test
    void createOffboardingAddsAssetRecoveryTasks() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalogItem(Department.HR, ChecklistTemplateType.OFFBOARDING)));
        when(templateTaskRepository.findByCatalogItemIdOrderBySortOrderAsc(10L)).thenReturn(List.of(
                new EsmChecklistTemplateTask(10L, Department.IT, "계정 비활성화", 1)));
        Asset asset = new Asset("AST-0001", "노트북", AssetType.HARDWARE, "김철수", null, null, null, null, null, null);
        ReflectionTestUtils.setField(asset, "id", 55L);
        when(assetRepository.search(any(), any(), any(), any(), any(Boolean.class), any(), any()))
                .thenReturn(new PageImpl<>(List.of(asset)));

        var response = service.create(new CreateRequestRequest(10L, Map.of(), "김철수"));

        assertThat(response.checklistId()).isEqualTo(100L);
        verify(checklistTaskRepository, times(2)).save(any());
    }

    @Test
    void createSuccessNoChecklistWhenTypeNone() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(10L)).thenReturn(Optional.of(catalogItem(Department.LEGAL, ChecklistTemplateType.NONE)));

        var response = service.create(new CreateRequestRequest(10L, Map.of(), null));

        assertThat(response.checklistId()).isNull();
        assertThat(response.status()).isEqualTo("SUBMITTED");
    }

    // ---------- list ----------

    @Test
    void listScopeAllWithoutCoordinatorRoleForbidden() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.list("all", null, null, null, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void listScopeAllWithoutDepartmentForbidden() {
        login(2L, "DEPT_COORDINATOR");
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(userWithDepartment(null)));

        assertThatThrownBy(() -> service.list("all", null, null, null, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    // ---------- detail ----------

    @Test
    void detailNotFoundThrows() {
        login(1L, "END_USER");
        when(requestRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ESM_REQUEST_NOT_FOUND));
    }

    @Test
    void detailForbiddenForOtherUsersRequest() {
        login(1L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(esmRequest(2L, Department.LEGAL, EsmRequestStatus.SUBMITTED)));

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void detailAllowedForMatchingDeptCoordinator() {
        login(3L, "DEPT_COORDINATOR");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(esmRequest(2L, Department.LEGAL, EsmRequestStatus.SUBMITTED)));
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(userWithDepartment(Department.LEGAL)));
        when(commentRepository.findByTicketTypeAndTicketIdOrderByCreatedAtAsc(any(), any())).thenReturn(List.of());
        when(timelineRepository.findByTicketTypeAndTicketIdOrderByOccurredAtAsc(any(), any())).thenReturn(List.of());

        var response = service.detail(1L);

        assertThat(response.department()).isEqualTo(Department.LEGAL);
    }

    @Test
    void detailForbiddenForMismatchedDeptCoordinator() {
        login(3L, "DEPT_COORDINATOR");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(esmRequest(2L, Department.LEGAL, EsmRequestStatus.SUBMITTED)));
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(userWithDepartment(Department.FACILITIES)));

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    // ---------- transition ----------

    @Test
    void transitionForbiddenForNonCoordinator() {
        login(1L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(esmRequest(1L, Department.LEGAL, EsmRequestStatus.SUBMITTED)));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(EsmRequestStatus.IN_PROGRESS, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void transitionDepartmentMismatchForbidden() {
        login(3L, "DEPT_COORDINATOR");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(esmRequest(2L, Department.LEGAL, EsmRequestStatus.SUBMITTED)));
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(userWithDepartment(Department.FACILITIES)));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(EsmRequestStatus.IN_PROGRESS, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void transitionFromTerminalStateThrows() {
        login(3L, "DEPT_COORDINATOR");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(esmRequest(2L, Department.LEGAL, EsmRequestStatus.COMPLETED)));
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(userWithDepartment(Department.LEGAL)));

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(EsmRequestStatus.IN_PROGRESS, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    void transitionSuccessAssignsAssignee() {
        login(3L, "DEPT_COORDINATOR");
        EsmRequest request = esmRequest(2L, Department.LEGAL, EsmRequestStatus.SUBMITTED);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(userWithDepartment(Department.LEGAL)));

        var response = service.transition(1L, new StatusTransitionRequest(EsmRequestStatus.IN_PROGRESS, "처리중"));

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
        assertThat(request.getAssigneeId()).isEqualTo(3L);
    }

    @Test
    void transitionToCompletedGatePassSucceeds() {
        login(3L, "DEPT_COORDINATOR");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(esmRequest(2L, Department.LEGAL, EsmRequestStatus.IN_PROGRESS)));
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(userWithDepartment(Department.LEGAL)));
        doNothing().when(approvalGateService).checkGate(any(), any(), any(), any(), any());

        var response = service.transition(1L, new StatusTransitionRequest(EsmRequestStatus.COMPLETED, null));

        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    void transitionToCompletedGateBlockedPropagates409() {
        login(3L, "DEPT_COORDINATOR");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(esmRequest(2L, Department.LEGAL, EsmRequestStatus.IN_PROGRESS)));
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(userWithDepartment(Department.LEGAL)));
        doThrow(new BusinessException(ErrorCode.APPROVAL_PENDING, ErrorCode.APPROVAL_PENDING.getDefaultMessage(), 77L))
                .when(approvalGateService).checkGate(any(), any(), any(), any(), any());

        assertThatThrownBy(() -> service.transition(1L, new StatusTransitionRequest(EsmRequestStatus.COMPLETED, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    assertThat(codeOf(e)).isEqualTo(ErrorCode.APPROVAL_PENDING);
                    assertThat(((BusinessException) e).getApprovalRequestId()).isEqualTo(77L);
                });
    }

    // ---------- comment ----------

    @Test
    void addCommentForbiddenThrows() {
        login(1L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(esmRequest(2L, Department.LEGAL, EsmRequestStatus.SUBMITTED)));

        assertThatThrownBy(() -> service.addComment(1L, new CommentCreateRequest("코멘트")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void addCommentSuccess() {
        login(1L, "END_USER");
        when(requestRepository.findById(1L)).thenReturn(Optional.of(esmRequest(1L, Department.LEGAL, EsmRequestStatus.SUBMITTED)));
        when(commentRepository.save(any())).thenAnswer(inv -> {
            com.itsm.common.ticket.Comment c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 5L);
            return c;
        });

        var response = service.addComment(1L, new CommentCreateRequest("코멘트"));

        assertThat(response.body()).isEqualTo("코멘트");
    }
}
