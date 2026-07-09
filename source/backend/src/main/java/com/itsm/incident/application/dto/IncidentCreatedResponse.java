package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인시던트 등록 응답")
public record IncidentCreatedResponse(Long id, String ticketKey, String status) {
}
