package com.itsm.change.application.dto;

import com.itsm.change.domain.Outcome;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "구현 결과 기록 요청")
public record ResultRequest(
        @Schema(description = "SUCCESS|FAILURE")
        @NotNull Outcome outcome,
        boolean rolledBack,
        String note
) {
}
