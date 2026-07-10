package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "시정조치 등록 요청")
public record CorrectiveActionCreateRequest(
        @Schema(description = "내용(필수)")
        @NotBlank String description
) {
}
