package com.itsm.problem.application.dto;

import com.itsm.problem.domain.ProblemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상태 전이 요청")
public record StatusTransitionRequest(
        @Schema(description = "CLASSIFICATION|INVESTIGATION|KNOWN_ERROR|WORKAROUND|RESOLVED_CLOSED")
        @NotNull ProblemStatus targetStatus,
        String note
) {
}
