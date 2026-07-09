package com.itsm.auth.application;

import com.itsm.auth.application.dto.LoginRequest;
import com.itsm.auth.application.dto.LoginResponse;
import com.itsm.auth.application.dto.MeResponse;
import com.itsm.auth.application.dto.PasswordChangeRequest;
import com.itsm.auth.application.dto.TokenResponse;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.AuditResult;
import com.itsm.auth.domain.EventType;
import com.itsm.auth.domain.RefreshToken;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.auth.domain.repository.RefreshTokenRepository;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock AppUserRepository appUserRepository;
    @Mock RoleRepository roleRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider tokenProvider;
    @Mock AuditLogService auditLogService;

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(appUserRepository, roleRepository, refreshTokenRepository,
                passwordEncoder, tokenProvider, auditLogService);
        when(tokenProvider.getAccessTokenValiditySeconds()).thenReturn(300L);
        when(tokenProvider.getRefreshTokenValiditySeconds()).thenReturn(604800L);
        when(tokenProvider.createAccessToken(any(), any(), anyList(), any())).thenReturn("access-token");
        when(tokenProvider.createRefreshToken(any(), any())).thenReturn("refresh-token");
        when(roleRepository.findRolesByUserId(any())).thenReturn(List.of(new Role("SYSTEM_ADMIN", "관리자", null)));
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private AppUser user(UserStatus status) {
        return new AppUser("admin@itsm.local", "hash", "관리자", status);
    }

    // ---------- login ----------

    @Test
    void loginSuccess() {
        when(appUserRepository.findByEmail("admin@itsm.local")).thenReturn(Optional.of(user(UserStatus.ACTIVE)));
        when(passwordEncoder.matches("pw", "hash")).thenReturn(true);

        LoginResponse response = authService.login(new LoginRequest("admin@itsm.local", "pw"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(300L);
        assertThat(response.user().roles()).containsExactly("SYSTEM_ADMIN");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void loginUserNotFoundThrowsInvalidCredentials() {
        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("x@itsm.local", "pw")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void loginWrongPasswordThrowsInvalidCredentialsAndRecordsFailure() {
        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.of(user(UserStatus.ACTIVE)));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@itsm.local", "bad")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
        verify(auditLogService).recordSeparately(eq(EventType.LOGIN), any(), eq("admin@itsm.local"),
                eq("admin@itsm.local"), eq(AuditResult.FAILURE));
    }

    @Test
    void loginInactiveThrowsAccountInactive() {
        when(appUserRepository.findByEmail(anyString())).thenReturn(Optional.of(user(UserStatus.INACTIVE)));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@itsm.local", "pw")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_INACTIVE));
    }

    // ---------- refresh ----------

    @Test
    void refreshBlankThrows() {
        assertThatThrownBy(() -> authService.refresh("  "))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    @Test
    void refreshParseFailureThrows() {
        when(tokenProvider.parse("bad")).thenThrow(new JwtException("bad"));

        assertThatThrownBy(() -> authService.refresh("bad"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    @Test
    void refreshNotStoredThrows() {
        UUID jti = UUID.randomUUID();
        Claims claims = claimsWithId(jti);
        when(tokenProvider.parse("t")).thenReturn(claims);
        when(refreshTokenRepository.findByJti(jti)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("t"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    @Test
    void refreshRevokedThrows() {
        UUID jti = UUID.randomUUID();
        Claims claims = claimsWithId(jti);
        when(tokenProvider.parse("t")).thenReturn(claims);
        RefreshToken rt = new RefreshToken(jti, 1L, OffsetDateTime.now().plusDays(1));
        rt.revoke();
        when(refreshTokenRepository.findByJti(jti)).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> authService.refresh("t"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    @Test
    void refreshExpiredThrows() {
        UUID jti = UUID.randomUUID();
        Claims claims = claimsWithId(jti);
        when(tokenProvider.parse("t")).thenReturn(claims);
        RefreshToken rt = new RefreshToken(jti, 1L, OffsetDateTime.now().minusSeconds(1));
        when(refreshTokenRepository.findByJti(jti)).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> authService.refresh("t"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    @Test
    void refreshInactiveUserThrows() {
        UUID jti = UUID.randomUUID();
        Claims claims = claimsWithId(jti);
        when(tokenProvider.parse("t")).thenReturn(claims);
        when(refreshTokenRepository.findByJti(jti))
                .thenReturn(Optional.of(new RefreshToken(jti, 1L, OffsetDateTime.now().plusDays(1))));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user(UserStatus.INACTIVE)));

        assertThatThrownBy(() -> authService.refresh("t"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    @Test
    void refreshSuccess() {
        UUID jti = UUID.randomUUID();
        Claims claims = claimsWithId(jti);
        when(tokenProvider.parse("t")).thenReturn(claims);
        when(refreshTokenRepository.findByJti(jti))
                .thenReturn(Optional.of(new RefreshToken(jti, 1L, OffsetDateTime.now().plusDays(1))));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user(UserStatus.ACTIVE)));

        TokenResponse response = authService.refresh("t");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.expiresIn()).isEqualTo(300L);
    }

    // ---------- logout / me / password ----------

    @Test
    void logoutClearsSession() {
        AppUser u = user(UserStatus.ACTIVE);
        u.updateAccessTokenJti(UUID.randomUUID());
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(u));

        authService.logout(new AuthPrincipal(1L, "admin@itsm.local", List.of("SYSTEM_ADMIN"), UUID.randomUUID()), null);

        assertThat(u.getAccessTokenJti()).isNull();
        verify(appUserRepository).save(u);
    }

    @Test
    void meReturnsCurrentUser() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user(UserStatus.ACTIVE)));

        MeResponse me = authService.me(new AuthPrincipal(1L, "admin@itsm.local", List.of("SYSTEM_ADMIN"), UUID.randomUUID()));

        assertThat(me.email()).isEqualTo("admin@itsm.local");
        assertThat(me.status()).isEqualTo("ACTIVE");
        assertThat(me.roles()).containsExactly("SYSTEM_ADMIN");
    }

    @Test
    void meUserMissingThrowsUnauthenticated() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.me(new AuthPrincipal(1L, "e", List.of(), UUID.randomUUID())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHENTICATED));
    }

    @Test
    void changePasswordWrongCurrentThrows() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user(UserStatus.ACTIVE)));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(
                new AuthPrincipal(1L, "e", List.of(), UUID.randomUUID()),
                new PasswordChangeRequest("wrong", "Welcome123!")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PASSWORD_MISMATCH));
    }

    @Test
    void changePasswordPolicyViolationThrows() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user(UserStatus.ACTIVE)));
        when(passwordEncoder.matches("current", "hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(
                new AuthPrincipal(1L, "e", List.of(), UUID.randomUUID()),
                new PasswordChangeRequest("current", "short")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PASSWORD_POLICY_VIOLATION));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePasswordSuccess() {
        AppUser u = user(UserStatus.ACTIVE);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("current", "hash")).thenReturn(true);
        when(passwordEncoder.encode("Welcome123!")).thenReturn("new-hash");

        authService.changePassword(new AuthPrincipal(1L, "e", List.of(), UUID.randomUUID()),
                new PasswordChangeRequest("current", "Welcome123!"));

        assertThat(u.getPasswordHash()).isEqualTo("new-hash");
    }

    private Claims claimsWithId(UUID jti) {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(claims.getId()).thenReturn(jti.toString());
        when(claims.getSubject()).thenReturn("1");
        return claims;
    }
}
