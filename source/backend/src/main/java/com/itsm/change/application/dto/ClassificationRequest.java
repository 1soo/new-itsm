package com.itsm.change.application.dto;

import com.itsm.change.domain.ChangeRisk;
import com.itsm.change.domain.ChangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "변경 유형·위험 변경 요청")
public record ClassificationRequest(
        @Schema(description = "STANDARD|NORMAL|EMERGENCY")
        @NotNull ChangeType type,
        @Schema(description = "HIGH|MEDIUM|LOW") ChangeRisk risk
) {
}
