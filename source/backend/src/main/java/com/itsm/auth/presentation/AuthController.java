package com.itsm.auth.presentation;

import com.itsm.auth.application.AuthService;
import com.itsm.auth.application.dto.LoginRequest;
import com.itsm.auth.application.dto.LoginResponse;
import com.itsm.auth.application.dto.LogoutRequest;
import com.itsm.auth.application.dto.MeResponse;
import com.itsm.auth.application.dto.MessageResponse;
import com.itsm.auth.application.dto.PasswordChangeRequest;
import com.itsm.auth.application.dto.RefreshRequest;
import com.itsm.auth.application.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.common.exception.ErrorResponse;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@Tag(name = "Auth", description = "인증/세션 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "refreshToken";
    private static final String COOKIE_PATH = "/";

    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final long refreshMaxAgeSeconds;

    public AuthController(AuthService authService, JwtTokenProvider tokenProvider, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
        this.refreshMaxAgeSeconds = tokenProvider.getRefreshTokenValiditySeconds();
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 토큰을 발급한다. Refresh Token은 httpOnly Cookie로도 내려간다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "입력 형식 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "이메일/비밀번호 불일치", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "비활성화 계정", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(response.refreshToken(), refreshMaxAgeSeconds).toString())
                .body(response);
    }

    @Operation(summary = "토큰 재발급",
            description = "httpOnly Cookie의 Refresh Token(우선) 또는 Body의 Refresh Token으로 Access Token을 재발급한다. "
                    + "Content-Type 무관·빈 body 허용(쿠키 자동 전송 전제).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token 만료·무효·무효화", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(value = REFRESH_COOKIE, required = false) String cookieToken,
            HttpServletRequest httpRequest) {
        // 쿠키 우선(FE 방식). 없으면 body에서 tolerant하게 추출(Content-Type 무관, 빈/비-JSON 허용).
        String token = StringUtils.hasText(cookieToken) ? cookieToken : extractRefreshFromBody(httpRequest);
        return ResponseEntity.ok(authService.refresh(token));
    }

    /** body를 Content-Type 무관하게 관대히 파싱해 refreshToken을 추출한다(실패 시 null → 401 위임). */
    private String extractRefreshFromBody(HttpServletRequest request) {
        String param = request.getParameter("refreshToken"); // form-urlencoded/query
        if (StringUtils.hasText(param)) {
            return param;
        }
        try {
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                String body = request.getReader().lines().collect(Collectors.joining());
                if (StringUtils.hasText(body)) {
                    RefreshRequest parsed = objectMapper.readValue(body, RefreshRequest.class);
                    return parsed != null ? parsed.refreshToken() : null;
                }
            }
        } catch (Exception ignored) {
            // 파싱 실패는 무효 요청으로 간주 → token=null → refresh()가 401 처리
        }
        return null;
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 무효화한다.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "세션 무효화 완료"),
            @ApiResponse(responseCode = "401", description = "미인증", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody(required = false) LogoutRequest request,
            @CookieValue(value = REFRESH_COOKIE, required = false) String cookieToken) {
        String token = (request != null && StringUtils.hasText(request.refreshToken()))
                ? request.refreshToken() : cookieToken;
        authService.logout(principal, token);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie("", 0).toString())
                .body(new MessageResponse("로그아웃 완료"));
    }

    @Operation(summary = "내 정보 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "401", description = "미인증", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(authService.me(principal));
    }

    @Operation(summary = "비밀번호 변경", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "새 비밀번호 정책 위반", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "현재 비밀번호 불일치/미인증", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(principal, request);
        return ResponseEntity.ok(new MessageResponse("비밀번호가 변경되었습니다"));
    }

    private ResponseCookie refreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(false) // local(HTTP). 운영은 인프라/보안 설계에 따라 true.
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }
}
