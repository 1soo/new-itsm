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
        @Schema(description = "배정 담당자 id(미배정 시 null) — 요청 큐 배정 버튼 노출 조건(본인 배정 여부) 판정용, 2026-07-15")
        Long assigneeId,
        OffsetDateTime updatedAt,
        @Schema(description = "진행 중인 승인 인스턴스의 targetState(원본 코드값, 없으면 null)") String pendingApprovalTargetState
) {
}
