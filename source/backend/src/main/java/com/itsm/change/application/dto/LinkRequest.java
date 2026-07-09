package com.itsm.change.application.dto;

import com.itsm.change.domain.LinkTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "인시던트/문제 연계 요청")
public record LinkRequest(
        @Schema(description = "연계 대상 유형 INCIDENT|PROBLEM")
        @NotNull LinkTargetType targetType,
        @Schema(description = "연계 대상 id")
        @NotNull Long targetId
) {
}
