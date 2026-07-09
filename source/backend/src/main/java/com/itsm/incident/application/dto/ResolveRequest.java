package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "해결 처리 요청")
public record ResolveRequest(
        OffsetDateTime impactStartAt,
        OffsetDateTime detectedAt,
        OffsetDateTime impactEndAt,
        String resolutionNote
) {
}
