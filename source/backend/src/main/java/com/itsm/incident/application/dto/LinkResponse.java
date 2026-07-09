package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문제 연계 응답")
public record LinkResponse(Long incidentId, Long problemId) {
}
