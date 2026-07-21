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
        OffsetDateTime updatedAt,
        @Schema(description = "진행 중인 승인 인스턴스의 targetState(원본 코드값, 없으면 null)") String pendingApprovalTargetState
) {
}
