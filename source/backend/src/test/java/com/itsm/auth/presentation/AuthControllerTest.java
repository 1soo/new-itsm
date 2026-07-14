package com.itsm.auth.presentation;

import tools.jackson.databind.ObjectMapper;
import com.itsm.auth.application.AuthService;
import com.itsm.auth.application.dto.LoginRequest;
import com.itsm.auth.application.dto.LoginResponse;
import com.itsm.auth.application.dto.MessageResponse;
import com.itsm.auth.application.dto.TokenResponse;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CSRF 더블서밋 쿠키 검증(security/authentication.md 4·7절, API-AUTH-002) 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService authService;
    @Mock JwtTokenProvider tokenProvider;
    @Mock HttpServletRequest httpRequest;

    AuthController controller;

    @BeforeEach
    void setUp() {
        when(tokenProvider.getRefreshTokenValiditySeconds()).thenReturn(604800L);
        controller = new AuthController(authService, tokenProvider, new ObjectMapper());
    }

    @Test
    void loginSetsRefreshAndCsrfCookiesWithExpectedAttributes() {
        when(authService.login(any())).thenReturn(new LoginResponse(
                "access-token", "refresh-token", "Bearer", 300L,
                new LoginResponse.UserInfo(1L, "admin@itsm.local", "관리자", List.of("SYSTEM_ADMIN"))));

        ResponseEntity<LoginResponse> response = controller.login(new LoginRequest("admin@itsm.local", "pw"));

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(2);
        String refreshCookie = cookies.stream().filter(c -> c.startsWith("refreshToken=")).findFirst().orElseThrow();
        String csrfCookie = cookies.stream().filter(c -> c.startsWith("XSRF-TOKEN=")).findFirst().orElseThrow();

        assertThat(refreshCookie).contains("HttpOnly").contains("SameSite=Strict");
        assertThat(csrfCookie).doesNotContain("HttpOnly").contains("SameSite=Strict");
    }

    @Test
    void refreshSucceedsWhenCsrfHeaderMatchesCookie() {
        when(authService.refresh("refresh-token")).thenReturn(new TokenResponse("new-access-token", "Bearer", 300L));

        ResponseEntity<TokenResponse> response =
                controller.refresh("refresh-token", "csrf-value", "csrf-value", httpRequest);

        assertThat(response.getBody().accessToken()).isEqualTo("new-access-token");
    }

    @Test
    void refreshRejectsWhenCsrfHeaderMissing() {
        assertThatThrownBy(() -> controller.refresh("refresh-token", "csrf-value", null, httpRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CSRF_TOKEN_MISMATCH));
        verify(authService, never()).refresh(any());
    }

    @Test
    void refreshRejectsWhenCsrfCookieMissing() {
        assertThatThrownBy(() -> controller.refresh("refresh-token", null, "csrf-value", httpRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CSRF_TOKEN_MISMATCH));
        verify(authService, never()).refresh(any());
    }

    @Test
    void refreshRejectsWhenCsrfHeaderAndCookieMismatch() {
        assertThatThrownBy(() -> controller.refresh("refresh-token", "cookie-value", "header-value", httpRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CSRF_TOKEN_MISMATCH));
        verify(authService, never()).refresh(any());
    }

    @Test
    void logoutExpiresRefreshAndCsrfCookies() {
        ResponseEntity<MessageResponse> response = controller.logout(null, null, "refresh-token");

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(2);
        assertThat(cookies).allSatisfy(c -> assertThat(c).contains("Max-Age=0"));
    }
}
