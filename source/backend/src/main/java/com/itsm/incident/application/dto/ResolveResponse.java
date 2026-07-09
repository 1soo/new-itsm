package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "해결 처리 응답")
public record ResolveResponse(Long id, String status, IncidentMetrics metrics) {
}
