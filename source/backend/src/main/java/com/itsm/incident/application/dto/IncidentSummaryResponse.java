package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "인시던트 목록 항목")
public record IncidentSummaryResponse(
        Long id,
        String ticketKey,
        String summary,
        String severity,
        String status,
        String assignee,
        boolean postmortemRequired,
        OffsetDateTime updatedAt
) {
}
