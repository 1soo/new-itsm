package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "에스컬레이션 응답")
public record EscalateResponse(Long incidentId, Long targetUserId, String type, OffsetDateTime at) {
}
