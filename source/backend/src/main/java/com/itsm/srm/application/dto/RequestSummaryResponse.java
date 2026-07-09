package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "요청 목록 항목")
public record RequestSummaryResponse(
        Long id,
        String ticketKey,
        String catalogItemName,
        String status,
        String slaStatus,
        String assignee,
        OffsetDateTime updatedAt
) {
}
