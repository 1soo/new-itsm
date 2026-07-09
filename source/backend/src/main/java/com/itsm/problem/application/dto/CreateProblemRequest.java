package com.itsm.problem.application.dto;

import com.itsm.problem.domain.Level;
import com.itsm.problem.domain.ProblemOrigin;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "문제 등록 요청")
public record CreateProblemRequest(
        @Schema(description = "요약(필수)")
        @NotBlank String summary,
        String description,
        @Schema(description = "출처 REACTIVE|PROACTIVE") ProblemOrigin origin,
        @Schema(description = "조사 사유") String investigationReason,
        @Schema(description = "영향도 HIGH|MEDIUM|LOW") Level impact,
        @Schema(description = "긴급도 HIGH|MEDIUM|LOW") Level urgency,
        @Schema(description = "구성요소") String component
) {
}
