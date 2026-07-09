package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "감사 로그 항목")
public record AuditLogResponse(
        Long id,
        String eventType,
        String actor,
        String target,
        String result,
        OffsetDateTime occurredAt
) {
}
