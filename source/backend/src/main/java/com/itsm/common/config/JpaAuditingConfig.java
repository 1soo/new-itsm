package com.itsm.common.config;

import com.itsm.common.security.AuthPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * 공통 컬럼 created_by/updated_by 자동 세팅. 인증 주체가 있으면 email, 없으면 "system".
 */
@Configuration
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof AuthPrincipal principal) {
                return Optional.of(principal.email());
            }
            return Optional.of("system");
        };
    }

    /** 공통 컬럼 created_at/updated_at을 TIMESTAMPTZ(OffsetDateTime)로 채운다. */
    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
