package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "승인/반려 요청")
public record ApprovalDecisionRequest(
        @Schema(description = "APPROVE|REJECT")
        @NotNull ApprovalDecision decision,
        @Schema(description = "반려 시 필수") String reason
) {
}
