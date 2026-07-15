package com.itsm.auth.application;

import com.itsm.auth.application.dto.CreateUserRequest;
import com.itsm.auth.application.dto.StatusChangeResponse;
import com.itsm.auth.application.dto.UserDetailResponse;
import com.itsm.auth.application.dto.UserRolesResponse;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.AuditResult;
import com.itsm.auth.domain.EventType;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.UserRole;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.auth.domain.repository.UserRoleRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserAdminServiceTest {

    @Mock AppUserRepository appUserRepository;
    @Mock RoleRepository roleRepository;
    @Mock UserRoleRepository userRoleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditLogService auditLogService;

    UserAdminService service;

    @BeforeEach
    void setUp() {
        service = new UserAdminService(appUserRepository, roleRepository, userRoleRepository,
                passwordEncoder, auditLogService);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(roleRepository.findRolesByUserId(any())).thenReturn(List.of(new Role("END_USER", "사용자", null)));
        AuthPrincipal principal = new AuthPrincipal(1L, "admin@itsm.local", List.of("SYSTEM_ADMIN"), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private AppUser user() {
        return new AppUser("u@itsm.local", "hash", "사용자", UserStatus.ACTIVE);
    }

    @Test
    void createDuplicateEmailThrows() {
        when(appUserRepository.existsByEmail("u@itsm.local")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateUserRequest("u@itsm.local", "이름", "Welcome123!", List.of(1L))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.EMAIL_DUPLICATE));
    }

    @Test
    void createWeakPasswordThrows() {
        when(appUserRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.create(new CreateUserRequest("u@itsm.local", "이름", "short", List.of(1L))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PASSWORD_POLICY_VIOLATION));
    }

    @Test
    void createUnknownRoleThrows() {
        when(appUserRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(new Role("END_USER", "사용자", null)));

        assertThatThrownBy(() -> service.create(new CreateUserRequest("u@itsm.local", "이름", "Welcome123!", List.of(1L, 2L))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_FOUND));
    }

    @Test
    void createSuccess() {
        when(appUserRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findAllById(List.of(1L))).thenReturn(List.of(new Role("END_USER", "사용자", null)));

        UserDetailResponse response = service.create(new CreateUserRequest("u@itsm.local", "이름", "Welcome123!", List.of(1L)));

        assertThat(response.email()).isEqualTo("u@itsm.local");
        assertThat(response.status()).isEqualTo("ACTIVE");
        verify(passwordEncoder).encode("Welcome123!");
        verify(userRoleRepository).save(any(UserRole.class));
        // actor는 대상 계정(u@itsm.local)이 아니라 현재 로그인한 관리자(admin@itsm.local)여야 한다.
        verify(auditLogService).record(eq(EventType.USER_CHANGE), eq(1L), eq("admin@itsm.local"),
                eq("u@itsm.local"), eq(AuditResult.SUCCESS));
    }

    @Test
    void getNotFoundThrows() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void changeStatusInactiveClearsSession() {
        AppUser u = user();
        u.updateAccessTokenJti(java.util.UUID.randomUUID());
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(u));

        StatusChangeResponse response = service.changeStatus(1L, UserStatus.INACTIVE);

        assertThat(response.status()).isEqualTo("INACTIVE");
        assertThat(u.getAccessTokenJti()).isNull();
    }

    @Test
    void assignRoleUnknownRoleThrows() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(roleRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRole(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_FOUND));
    }

    @Test
    void assignRoleUserNotFoundThrows() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRole(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void assignRoleSuccess() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(roleRepository.findById(5L)).thenReturn(Optional.of(new Role("APPROVER", "승인자", null)));
        when(userRoleRepository.existsByUserIdAndRoleId(1L, 5L)).thenReturn(false);

        UserRolesResponse response = service.assignRole(1L, 5L);

        assertThat(response.userId()).isEqualTo(1L);
        verify(userRoleRepository).save(any(UserRole.class));
        // actor는 대상 계정(u@itsm.local)이 아니라 현재 로그인한 관리자(admin@itsm.local)여야 한다.
        verify(auditLogService).record(eq(EventType.ROLE_CHANGE), eq(1L), eq("admin@itsm.local"),
                eq("u@itsm.local"), eq(AuditResult.SUCCESS));
    }

    @Test
    void revokeRoleSuccess() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user()));
        UserRole ur = new UserRole(1L, 5L);
        when(userRoleRepository.findByUserIdAndRoleId(1L, 5L)).thenReturn(Optional.of(ur));

        UserRolesResponse response = service.revokeRole(1L, 5L);

        assertThat(response.userId()).isEqualTo(1L);
        verify(userRoleRepository).delete(ur);
    }
}
