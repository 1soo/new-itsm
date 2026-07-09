package com.itsm.knowledge.application.dto;

import com.itsm.knowledge.domain.ReviewDecision;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "검토·게시 승인/반려 요청")
public record ReviewRequest(
        @Schema(description = "APPROVE|REJECT")
        @NotNull ReviewDecision decision,
        @Schema(description = "반려 시 필수") String reason
) {
}
