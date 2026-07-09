package com.itsm.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "Y2hhbmdlLW1lLXRoaXMtaXMtYS1kZXYtb25seS1zZWNyZXQtMzJieXRlcysrKw==";

    private JwtTokenProvider provider(long accessSeconds, long refreshSeconds) {
        return new JwtTokenProvider(new JwtProperties(SECRET, accessSeconds, refreshSeconds));
    }

    @Test
    void createAndParseAccessToken() {
        JwtTokenProvider provider = provider(300, 604800);
        UUID jti = UUID.randomUUID();

        String token = provider.createAccessToken(1L, "user@itsm.local", List.of("SYSTEM_ADMIN"), jti);
        Claims claims = provider.parse(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.getId()).isEqualTo(jti.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("user@itsm.local");
        assertThat(provider.extractRoles(claims)).containsExactly("SYSTEM_ADMIN");
    }

    @Test
    void createAndParseRefreshToken() {
        JwtTokenProvider provider = provider(300, 604800);
        UUID jti = UUID.randomUUID();

        String token = provider.createRefreshToken(7L, jti);
        Claims claims = provider.parse(token);

        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.getId()).isEqualTo(jti.toString());
    }

    @Test
    void parseExpiredTokenThrows() {
        JwtTokenProvider provider = provider(-10, 604800); // 만료 시각을 과거로
        String token = provider.createAccessToken(1L, "e", List.of("END_USER"), UUID.randomUUID());

        assertThatThrownBy(() -> provider.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void parseTamperedTokenThrows() {
        JwtTokenProvider provider = provider(300, 604800);
        String token = provider.createAccessToken(1L, "e", List.of("END_USER"), UUID.randomUUID());
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> provider.parse(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void extractRolesReturnsEmptyWhenAbsent() {
        JwtTokenProvider provider = provider(300, 604800);
        String token = provider.createRefreshToken(1L, UUID.randomUUID());
        Claims claims = provider.parse(token);

        assertThat(provider.extractRoles(claims)).isEmpty();
    }
}
