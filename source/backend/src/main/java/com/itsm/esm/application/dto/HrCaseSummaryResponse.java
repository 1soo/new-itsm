package com.itsm.esm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "HR 케이스 목록 항목")
public record HrCaseSummaryResponse(
        Long id,
        String title,
        String status,
        OffsetDateTime updatedAt
) {
}
