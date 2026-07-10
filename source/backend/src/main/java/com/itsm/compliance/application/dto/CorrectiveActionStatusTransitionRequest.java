package com.itsm.compliance.application.dto;

import com.itsm.compliance.domain.CorrectiveActionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "시정조치 상태 전이 요청")
public record CorrectiveActionStatusTransitionRequest(
        @Schema(description = "IN_PROGRESS|RESOLVED")
        @NotNull CorrectiveActionStatus targetStatus
) {
}
