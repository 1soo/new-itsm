package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "변경 승인/반려 요청")
public record ChangeApprovalRequest(
        @Schema(description = "APPROVE|REJECT")
        @NotNull ChangeApprovalDecision decision,
        @Schema(description = "반려 시 필수") String opinion
) {
}
