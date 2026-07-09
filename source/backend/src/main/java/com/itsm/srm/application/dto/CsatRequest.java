package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "CSAT 제출 요청")
public record CsatRequest(
        @Schema(description = "만족도 1~5")
        @NotNull @Min(1) @Max(5) Integer score,
        String comment
) {
}
