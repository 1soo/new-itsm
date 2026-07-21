package com.itsm.common.approval.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "반려 후 재승인요청 결과(API-COM-006)")
public record ApprovalResubmitResponse(
        Long approvalRequestId,
        String ticketType,
        Long ticketId,
        String targetState,
        @Schema(description = "IN_PROGRESS(새 인스턴스 생성) 또는 NO_RULE_MATCHED(매칭 규칙 소멸, 승인 없이 통과 가능)")
        String status,
        Short currentStepNo
) {
}
