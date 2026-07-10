package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "변경 요청 연계 요청")
public record LinkRequest(
        @Schema(description = "연계할 변경 요청 id(필수)")
        @NotNull Long changeId
) {
}
