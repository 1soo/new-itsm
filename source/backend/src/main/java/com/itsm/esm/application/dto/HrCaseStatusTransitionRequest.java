package com.itsm.esm.application.dto;

import com.itsm.esm.domain.HrCaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "HR 케이스 상태 전이 요청")
public record HrCaseStatusTransitionRequest(
        @Schema(description = "DOCUMENTATION|INVESTIGATION|RESOLUTION")
        @NotNull HrCaseStatus targetStatus,
        String note
) {
}
