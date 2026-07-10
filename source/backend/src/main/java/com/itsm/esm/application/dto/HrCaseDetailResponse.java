package com.itsm.esm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "HR 케이스 상세")
public record HrCaseDetailResponse(
        Long id,
        String title,
        String description,
        String subjectUserName,
        String status,
        List<HistoryEntry> history
) {
    @Schema(description = "상태 변경 이력 항목")
    public record HistoryEntry(String status, String changedBy, OffsetDateTime at) {
    }
}
