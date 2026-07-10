package com.itsm.esm.application;

import com.itsm.asset.domain.repository.AssetRepository;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.Department;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.esm.application.dto.ChecklistTaskStatusRequest;
import com.itsm.esm.domain.ChecklistStatus;
import com.itsm.esm.domain.ChecklistTaskStatus;
import com.itsm.esm.domain.ChecklistTemplateType;
import com.itsm.esm.domain.EsmChecklist;
import com.itsm.esm.domain.EsmChecklistTask;
import com.itsm.esm.domain.EsmRequest;
import com.itsm.esm.domain.EsmRequestStatus;
import com.itsm.esm.domain.repository.EsmChecklistRepository;
import com.itsm.esm.domain.repository.EsmChecklistTaskRepository;
import com.itsm.esm.domain.repository.EsmRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EsmChecklistServiceTest {

    @Mock EsmChecklistRepository checklistRepository;
    @Mock EsmChecklistTaskRepository checklistTaskRepository;
    @Mock EsmRequestRepository requestRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock AssetRepository assetRepository;

    EsmChecklistService service;

    @BeforeEach
    void setUp() {
        service = new EsmChecklistService(checklistRepository, checklistTaskRepository, requestRepository,
                appUserRepository, assetRepository);
        when(checklistTaskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(checklistRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login(Long userId, String... roles) {
        AuthPrincipal principal = new AuthPrincipal(userId, "u@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private ErrorCode codeOf(Throwable e) {
        return ((BusinessException) e).getErrorCode();
    }

    private EsmChecklist checklist(Long id) {
        EsmChecklist c = new EsmChecklist(ChecklistTemplateType.ONBOARDING, "김철수");
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    private EsmChecklistTask task(Long id, Long checklistId, Department department, ChecklistTaskStatus status) {
        EsmChecklistTask t = new EsmChecklistTask(checklistId, department, "설명", null);
        ReflectionTestUtils.setField(t, "id", id);
        if (status == ChecklistTaskStatus.DONE) {
            t.markDone();
        }
        return t;
    }

    private AppUser userWithDepartment(Department department) {
        AppUser user = new AppUser("u@itsm.local", "hash", "사용자", UserStatus.ACTIVE);
        ReflectionTestUtils.setField(user, "department", department);
        return user;
    }

    // ---------- detail ----------

    @Test
    void detailNotFoundThrows() {
        login(1L, "END_USER");
        when(checklistRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ESM_CHECKLIST_NOT_FOUND));
    }

    @Test
    void detailAllowedForRequesterOwner() {
        login(1L, "END_USER");
        when(checklistRepository.findById(1L)).thenReturn(Optional.of(checklist(1L)));
        when(checklistTaskRepository.findByChecklistId(1L)).thenReturn(List.of(task(1L, 1L, Department.IT, ChecklistTaskStatus.PENDING)));
        EsmRequest linkedRequest = new EsmRequest("ESM-2026-0001", 10L, 1L, Department.HR, "김철수", 1L);
        when(requestRepository.findByChecklistId(1L)).thenReturn(Optional.of(linkedRequest));

        var response = service.detail(1L);

        assertThat(response.targetUserName()).isEqualTo("김철수");
    }

    @Test
    void detailForbiddenWhenNoAccess() {
        login(2L, "END_USER");
        when(checklistRepository.findById(1L)).thenReturn(Optional.of(checklist(1L)));
        when(checklistTaskRepository.findByChecklistId(1L)).thenReturn(List.of(task(1L, 1L, Department.IT, ChecklistTaskStatus.PENDING)));
        EsmRequest linkedRequest = new EsmRequest("ESM-2026-0001", 10L, 1L, Department.HR, "김철수", 1L);
        when(requestRepository.findByChecklistId(1L)).thenReturn(Optional.of(linkedRequest));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(userWithDepartment(null)));

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void detailAllowedForTaskDepartmentMember() {
        login(3L, "DEPT_COORDINATOR");
        when(checklistRepository.findById(1L)).thenReturn(Optional.of(checklist(1L)));
        when(checklistTaskRepository.findByChecklistId(1L)).thenReturn(List.of(task(1L, 1L, Department.IT, ChecklistTaskStatus.PENDING)));
        when(requestRepository.findByChecklistId(1L)).thenReturn(Optional.empty());
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(userWithDepartment(Department.IT)));

        var response = service.detail(1L);

        assertThat(response.tasks()).hasSize(1);
    }

    // ---------- myTasks ----------

    @Test
    void myTasksForbiddenForNonCoordinator() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.myTasks(null, org.springframework.data.domain.PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void myTasksEmptyWhenNoDepartment() {
        login(1L, "DEPT_COORDINATOR");
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(userWithDepartment(null)));

        var response = service.myTasks(null, org.springframework.data.domain.PageRequest.of(0, 20));

        assertThat(response.content()).isEmpty();
    }

    // ---------- updateTaskStatus ----------

    @Test
    void updateTaskStatusForbiddenForNonCoordinator() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.updateTaskStatus(1L, new ChecklistTaskStatusRequest(ChecklistTaskStatus.DONE)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void updateTaskStatusNotFoundThrows() {
        login(1L, "DEPT_COORDINATOR");
        when(checklistTaskRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTaskStatus(9L, new ChecklistTaskStatusRequest(ChecklistTaskStatus.DONE)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ESM_CHECKLIST_TASK_NOT_FOUND));
    }

    @Test
    void updateTaskStatusDepartmentMismatchForbidden() {
        login(1L, "DEPT_COORDINATOR");
        when(checklistTaskRepository.findById(1L)).thenReturn(Optional.of(task(1L, 1L, Department.IT, ChecklistTaskStatus.PENDING)));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(userWithDepartment(Department.HR)));

        assertThatThrownBy(() -> service.updateTaskStatus(1L, new ChecklistTaskStatusRequest(ChecklistTaskStatus.DONE)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void updateTaskStatusCompletesChecklistWhenLastTask() {
        login(1L, "DEPT_COORDINATOR");
        when(checklistTaskRepository.findById(1L)).thenReturn(Optional.of(task(1L, 1L, Department.IT, ChecklistTaskStatus.PENDING)));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(userWithDepartment(Department.IT)));
        when(checklistRepository.findById(1L)).thenReturn(Optional.of(checklist(1L)));
        when(checklistTaskRepository.countByChecklistIdAndStatusNot(1L, ChecklistTaskStatus.DONE)).thenReturn(0L);

        var response = service.updateTaskStatus(1L, new ChecklistTaskStatusRequest(ChecklistTaskStatus.DONE));

        assertThat(response.status()).isEqualTo("DONE");
        assertThat(response.checklistStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void updateTaskStatusKeepsInProgressWhenTasksRemain() {
        login(1L, "DEPT_COORDINATOR");
        when(checklistTaskRepository.findById(1L)).thenReturn(Optional.of(task(1L, 1L, Department.IT, ChecklistTaskStatus.PENDING)));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(userWithDepartment(Department.IT)));
        when(checklistRepository.findById(1L)).thenReturn(Optional.of(checklist(1L)));
        when(checklistTaskRepository.countByChecklistIdAndStatusNot(1L, ChecklistTaskStatus.DONE)).thenReturn(1L);

        var response = service.updateTaskStatus(1L, new ChecklistTaskStatusRequest(ChecklistTaskStatus.DONE));

        assertThat(response.checklistStatus()).isEqualTo("IN_PROGRESS");
    }
}
