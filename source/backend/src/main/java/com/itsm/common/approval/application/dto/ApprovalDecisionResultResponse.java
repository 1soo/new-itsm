package com.itsm.common.approval.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "승인/반려 결정 처리 결과")
public record ApprovalDecisionResultResponse(
        Long approvalRequestId,
        short stepNo,
        @Schema(description = "PENDING|APPROVED|REJECTED") String stepStatus,
        @Schema(description = "IN_PROGRESS|APPROVED|REJECTED") String requestStatus
) {
}
