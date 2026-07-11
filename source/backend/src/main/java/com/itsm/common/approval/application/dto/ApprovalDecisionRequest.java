package com.itsm.common.approval.application.dto;

import com.itsm.common.approval.domain.DecisionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "승인/반려 결정 요청")
public record ApprovalDecisionRequest(
        @NotNull DecisionType decision,
        @Schema(description = "REJECT 시 필수") String reason
) {
}
