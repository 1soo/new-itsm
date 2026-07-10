package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "요구사항 수정 요청")
public record UpdateRequirementRequest(
        @Schema(description = "이름(필수)")
        @NotBlank String name,
        @Schema(description = "근거(필수)")
        @NotBlank String basis,
        String scope
) {
}
