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
        OffsetDateTime updatedAt,
        @Schema(description = "진행 중인 승인 인스턴스의 targetState(원본 코드값, 없으면 null)") String pendingApprovalTargetState
) {
}
