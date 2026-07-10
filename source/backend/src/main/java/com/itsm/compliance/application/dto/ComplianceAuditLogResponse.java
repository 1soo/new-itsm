package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "컴플라이언스 감사 로그 항목")
public record ComplianceAuditLogResponse(
        String eventType,
        String actor,
        String target,
        String result,
        OffsetDateTime occurredAt
) {
}
