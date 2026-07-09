package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "CI 등록 요청")
public record CreateCiRequest(
        @Schema(description = "이름(필수)")
        @NotBlank String name,
        String type,
        @Schema(description = "연결 자산(선택)") Long assetId
) {
}
