package com.itsm.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 모든 보호 API 요청마다 Access Token을 검증한다.
 * 서명·만료 검증 → userId/jti 추출 → app_user.access_token_jti 비교 → 일치 시 인증 설정.
 * 토큰 없음/무효/jti 불일치는 인증을 설정하지 않아 EntryPoint에서 401 처리된다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final AccessTokenSessionChecker sessionChecker;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, AccessTokenSessionChecker sessionChecker) {
        this.tokenProvider = tokenProvider;
        this.sessionChecker = sessionChecker;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            authenticate(token);
        }
        chain.doFilter(request, response);
    }

    private void authenticate(String token) {
        try {
            Claims claims = tokenProvider.parse(token);
            Long userId = Long.valueOf(claims.getSubject());
            UUID jti = UUID.fromString(claims.getId());
            if (!sessionChecker.isCurrentAccessToken(userId, jti)) {
                return; // jti 불일치(강제 로그아웃/다른 기기 로그인) → 미인증
            }
            List<String> roles = tokenProvider.extractRoles(claims);
            String email = claims.get("email", String.class);
            AuthPrincipal principal = new AuthPrincipal(userId, email, roles, jti);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException ex) {
            // 서명 오류·만료·형식 오류 → 미인증(인증 미설정)
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
