package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "변경 목록 요약 응답")
public record ChangeSummaryResponse(
        Long id,
        String ticketKey,
        String summary,
        String type,
        String status,
        String risk,
        OffsetDateTime scheduledAt,
        OffsetDateTime updatedAt
) {
}
