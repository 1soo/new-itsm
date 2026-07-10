package com.itsm.esm.application.dto;

import com.itsm.auth.domain.Department;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "부서 요청 상세")
public record RequestDetailResponse(
        Long id,
        String ticketKey,
        String catalogItemName,
        Department department,
        String status,
        Map<String, Object> formValues,
        String requester,
        String assignee,
        Long checklistId,
        List<CommentResponse> comments,
        List<TimelineEntry> timeline
) {
    @Schema(description = "타임라인 항목")
    public record TimelineEntry(String type, String message, OffsetDateTime at) {
    }
}
