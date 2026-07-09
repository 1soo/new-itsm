package com.itsm.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT 생성·검증. Access claim: userId(subject)·email·roles·jti / Refresh claim: userId(subject)·jti.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessValiditySeconds;
    private final long refreshValiditySeconds;

    public JwtTokenProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.secret()));
        this.accessValiditySeconds = properties.accessTokenValiditySeconds();
        this.refreshValiditySeconds = properties.refreshTokenValiditySeconds();
    }

    public String createAccessToken(Long userId, String email, List<String> roles, UUID jti) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessValiditySeconds * 1000);
        return Jwts.builder()
                .id(jti.toString())
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId, UUID jti) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshValiditySeconds * 1000);
        return Jwts.builder()
                .id(jti.toString())
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 서명·만료 검증 후 Claims 반환. 유효하지 않으면 {@link JwtException}.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTokenValiditySeconds() {
        return accessValiditySeconds;
    }

    public long getRefreshTokenValiditySeconds() {
        return refreshValiditySeconds;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        return roles instanceof List ? (List<String>) roles : List.of();
    }
}
