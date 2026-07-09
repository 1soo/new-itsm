package com.itsm.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 (application.yml `jwt.*`).
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenValiditySeconds,
        long refreshTokenValiditySeconds
) {
}
