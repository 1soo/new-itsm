package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "문제 목록 항목")
public record ProblemSummaryResponse(
        Long id,
        String ticketKey,
        String summary,
        String status,
        String priority,
        String origin,
        String assignee,
        OffsetDateTime updatedAt
) {
}
