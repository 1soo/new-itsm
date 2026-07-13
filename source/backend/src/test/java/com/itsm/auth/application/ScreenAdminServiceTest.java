package com.itsm.auth.application;

import com.itsm.auth.application.dto.CreateScreenRequest;
import com.itsm.auth.application.dto.ScreenDeletedResponse;
import com.itsm.auth.application.dto.ScreenResponse;
import com.itsm.auth.application.dto.ScreenRolesResponse;
import com.itsm.auth.application.dto.UpdateScreenRequest;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.Screen;
import com.itsm.auth.domain.ScreenRole;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.auth.domain.repository.ScreenRepository;
import com.itsm.auth.domain.repository.ScreenRoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScreenAdminServiceTest {

    @Mock ScreenRepository screenRepository;
    @Mock ScreenRoleRepository screenRoleRepository;
    @Mock RoleRepository roleRepository;
    @Mock AuditLogService auditLogService;

    ScreenAdminService service;

    @BeforeEach
    void setUp() {
        service = new ScreenAdminService(screenRepository, screenRoleRepository, roleRepository, auditLogService);
        when(screenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(screenRoleRepository.findByScreenId(any())).thenReturn(List.of());
        login("SYSTEM_ADMIN");
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login(String... roles) {
        AuthPrincipal principal = new AuthPrincipal(1L, "admin@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private Screen screen() {
        return new Screen("SCR-ADMIN-006", "메뉴 관리", "Menu Management", "/admin/menus", "auth", "ListTree",
                "admin", "관리자", "Admin", 420, true);
    }

    private ErrorCode codeOf(Throwable e) {
        return ((BusinessException) e).getErrorCode();
    }

    @Test
    void createDuplicateScreenCodeThrows() {
        when(screenRepository.existsByScreenCode("SCR-ADMIN-006")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateScreenRequest(
                "SCR-ADMIN-006", "메뉴 관리", "Menu Management", "/admin/menus", "auth", null, null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.SCREEN_CODE_DUPLICATE));
    }

    @Test
    void createDuplicatePathThrows() {
        when(screenRepository.existsByScreenCode(anyString())).thenReturn(false);
        when(screenRepository.existsByPath("/admin/menus")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateScreenRequest(
                "SCR-NEW-001", "새 메뉴", "New Menu", "/admin/menus", "auth", null, null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.PATH_DUPLICATE));
    }

    @Test
    void createSuccessAppliesDefaults() {
        when(screenRepository.existsByScreenCode(anyString())).thenReturn(false);
        when(screenRepository.existsByPath(anyString())).thenReturn(false);

        ScreenResponse response = service.create(new CreateScreenRequest(
                "SCR-NEW-001", "새 메뉴", "New Menu", "/new-menu", "auth", null, null, null, null, null, null));

        assertThat(response.screenCode()).isEqualTo("SCR-NEW-001");
        assertThat(response.sortOrder()).isEqualTo(0);
        assertThat(response.navVisible()).isTrue();
        assertThat(response.roles()).isEmpty();
        verify(auditLogService).record(com.itsm.auth.domain.EventType.ROLE_CHANGE, 1L, "admin@itsm.local", "SCR-NEW-001",
                com.itsm.auth.domain.AuditResult.SUCCESS);
    }

    @Test
    void createWithGroupCodeMissingGroupLabelEnThrows() {
        when(screenRepository.existsByScreenCode(anyString())).thenReturn(false);
        when(screenRepository.existsByPath(anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.create(new CreateScreenRequest(
                "SCR-NEW-001", "새 메뉴", "New Menu", "/new-menu", "auth", null, "admin", "관리자", null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void createWithGroupCodeBlankGroupLabelEnThrows() {
        when(screenRepository.existsByScreenCode(anyString())).thenReturn(false);
        when(screenRepository.existsByPath(anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.create(new CreateScreenRequest(
                "SCR-NEW-001", "새 메뉴", "New Menu", "/new-menu", "auth", null, "admin", "관리자", "  ", null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void updateNotFoundThrows() {
        when(screenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UpdateScreenRequest(null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.SCREEN_NOT_FOUND));
    }

    @Test
    void updateDuplicatePathThrows() {
        when(screenRepository.findById(1L)).thenReturn(Optional.of(screen()));
        when(screenRepository.existsByPathAndIdNot("/dup", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UpdateScreenRequest(null, null, "/dup", null, null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.PATH_DUPLICATE));
    }

    @Test
    void updateSuccessAppliesPartialFields() {
        when(screenRepository.findById(1L)).thenReturn(Optional.of(screen()));

        ScreenResponse response = service.update(1L,
                new UpdateScreenRequest("메뉴 관리(변경)", null, null, null, null, null, null, 999, false));

        assertThat(response.screenName()).isEqualTo("메뉴 관리(변경)");
        assertThat(response.sortOrder()).isEqualTo(999);
        assertThat(response.navVisible()).isFalse();
        assertThat(response.path()).isEqualTo("/admin/menus");
    }

    @Test
    void deleteNotFoundThrows() {
        when(screenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.SCREEN_NOT_FOUND));
    }

    @Test
    void deleteSuccessMarksDeleted() {
        Screen screen = screen();
        when(screenRepository.findById(1L)).thenReturn(Optional.of(screen));

        ScreenDeletedResponse response = service.delete(1L);

        assertThat(response.deleted()).isTrue();
        assertThat(screen.isDeleted()).isTrue();
    }

    @Test
    void assignRoleScreenNotFoundThrows() {
        when(screenRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRole(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.SCREEN_NOT_FOUND));
    }

    @Test
    void assignRoleUnknownRoleThrows() {
        when(screenRepository.findById(1L)).thenReturn(Optional.of(screen()));
        when(roleRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRole(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ROLE_NOT_FOUND));
    }

    @Test
    void assignRoleAlreadyMappedThrows() {
        when(screenRepository.findById(1L)).thenReturn(Optional.of(screen()));
        when(roleRepository.findById(5L)).thenReturn(Optional.of(new Role("APPROVER", "승인자", null)));
        when(screenRoleRepository.existsByScreenIdAndRoleId(1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.assignRole(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.SCREEN_ROLE_MAPPING_DUPLICATE));
    }

    @Test
    void assignRoleSuccess() {
        when(screenRepository.findById(1L)).thenReturn(Optional.of(screen()));
        when(roleRepository.findById(5L)).thenReturn(Optional.of(new Role("APPROVER", "승인자", null)));
        when(screenRoleRepository.existsByScreenIdAndRoleId(1L, 5L)).thenReturn(false);
        when(screenRoleRepository.findByScreenId(1L)).thenReturn(List.of(new ScreenRole(1L, 5L)));
        when(roleRepository.findAllById(List.of(5L))).thenReturn(List.of(new Role("APPROVER", "승인자", null)));

        ScreenRolesResponse response = service.assignRole(1L, 5L);

        assertThat(response.screenId()).isEqualTo(1L);
        assertThat(response.roles()).containsExactly("APPROVER");
        verify(screenRoleRepository).save(any(ScreenRole.class));
    }

    @Test
    void revokeRoleScreenNotFoundThrows() {
        when(screenRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revokeRole(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.SCREEN_NOT_FOUND));
    }

    @Test
    void revokeRoleSuccess() {
        when(screenRepository.findById(1L)).thenReturn(Optional.of(screen()));
        ScreenRole mapping = new ScreenRole(1L, 5L);
        when(screenRoleRepository.findByScreenIdAndRoleId(1L, 5L)).thenReturn(Optional.of(mapping));

        ScreenRolesResponse response = service.revokeRole(1L, 5L);

        assertThat(response.screenId()).isEqualTo(1L);
        verify(screenRoleRepository).delete(mapping);
    }

    @Test
    void listReturnsScreensWithMappedRoles() {
        Screen screen = screen();
        when(screenRepository.search(null, "auth", null)).thenReturn(
                new org.springframework.data.domain.PageImpl<>(List.of(screen)));
        when(screenRoleRepository.findByScreenId(any())).thenReturn(List.of(new ScreenRole(null, 5L)));
        when(roleRepository.findAllById(List.of(5L))).thenReturn(List.of(new Role("SYSTEM_ADMIN", "관리자", null)));

        var result = service.list(null, "auth", null);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).roles()).containsExactly("SYSTEM_ADMIN");
    }
}
