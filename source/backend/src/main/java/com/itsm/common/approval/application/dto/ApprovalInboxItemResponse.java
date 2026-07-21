package com.itsm.common.approval.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "승인 대기함 항목(전 도메인 공용)")
public record ApprovalInboxItemResponse(
        Long approvalRequestId,
        String ticketType,
        Long ticketId,
        String ticketKey,
        @Schema(description = "알림·목록 표시용 제목(도메인별 summary/title/name 등을 그대로 노출)") String ticketSummary,
        @Schema(description = "원본 코드값(도착 상태, 생성 시점 스냅샷)") String targetState,
        @Schema(description = "표시명(백엔드 resolve)") String targetStateLabel,
        String requester,
        Short currentStepNo,
        OffsetDateTime requestedAt
) {
}
