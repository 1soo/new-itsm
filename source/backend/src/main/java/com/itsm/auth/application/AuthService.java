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
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.auth.domain.repository.RefreshTokenRepository;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 인증 유스케이스: 로그인·토큰 재발급·로그아웃·내 정보·비밀번호 변경.
 * (security/authentication.md 준수: JTI 세션 관리, BCrypt 검증)
 */
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    public AuthService(AppUserRepository appUserRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditLogService auditLogService) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            auditLogService.recordSeparately(EventType.LOGIN, user == null ? null : user.getId(),
                    request.email(), request.email(), AuditResult.FAILURE);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!user.isActive()) {
            auditLogService.recordSeparately(EventType.LOGIN, user.getId(), user.getEmail(), user.getEmail(), AuditResult.FAILURE);
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
        }

        List<String> roles = roleCodes(user.getId());
        UUID accessJti = UUID.randomUUID();
        UUID refreshJti = UUID.randomUUID();

        user.updateAccessTokenJti(accessJti);
        appUserRepository.save(user);
        refreshTokenRepository.save(new RefreshToken(refreshJti, user.getId(),
                OffsetDateTime.now().plusSeconds(tokenProvider.getRefreshTokenValiditySeconds())));

        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getEmail(), roles, accessJti);
        String refreshToken = tokenProvider.createRefreshToken(user.getId(), refreshJti);

        auditLogService.record(EventType.LOGIN, user.getId(), user.getEmail(), user.getEmail(), AuditResult.SUCCESS);

        return new LoginResponse(accessToken, refreshToken, "Bearer",
                tokenProvider.getAccessTokenValiditySeconds(),
                new LoginResponse.UserInfo(user.getId(), user.getEmail(), user.getName(), roles));
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenValue) {
        if (!StringUtils.hasText(refreshTokenValue)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        UUID jti;
        try {
            Claims claims = tokenProvider.parse(refreshTokenValue);
            jti = UUID.fromString(claims.getId());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (stored.isRevoked() || stored.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        AppUser user = appUserRepository.findById(stored.getUserId())
                .filter(AppUser::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        List<String> roles = roleCodes(user.getId());
        UUID accessJti = UUID.randomUUID();
        user.updateAccessTokenJti(accessJti);
        appUserRepository.save(user);

        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getEmail(), roles, accessJti);
        auditLogService.record(EventType.REFRESH, user.getId(), user.getEmail(), user.getEmail(), AuditResult.SUCCESS);

        return new TokenResponse(accessToken, "Bearer", tokenProvider.getAccessTokenValiditySeconds());
    }

    @Transactional
    public void logout(AuthPrincipal principal, String refreshTokenValue) {
        AppUser user = appUserRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        user.clearAccessTokenJti();
        appUserRepository.save(user);

        if (StringUtils.hasText(refreshTokenValue)) {
            try {
                UUID jti = UUID.fromString(tokenProvider.parse(refreshTokenValue).getId());
                refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                    rt.revoke();
                    refreshTokenRepository.save(rt);
                });
            } catch (JwtException | IllegalArgumentException ignored) {
                // 무효한 refresh token은 무시(로그아웃은 성공 처리)
            }
        }
        auditLogService.record(EventType.LOGOUT, user.getId(), user.getEmail(), user.getEmail(), AuditResult.SUCCESS);
    }

    @Transactional(readOnly = true)
    public MeResponse me(AuthPrincipal principal) {
        AppUser user = appUserRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        return new MeResponse(user.getId(), user.getEmail(), user.getName(),
                user.getStatus().name(), roleCodes(user.getId()));
    }

    @Transactional
    public void changePassword(AuthPrincipal principal, PasswordChangeRequest request) {
        AppUser user = appUserRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        PasswordPolicy.validate(request.newPassword());
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        appUserRepository.save(user);
    }

    private List<String> roleCodes(Long userId) {
        return roleRepository.findRolesByUserId(userId).stream().map(Role::getRoleCode).toList();
    }
}
