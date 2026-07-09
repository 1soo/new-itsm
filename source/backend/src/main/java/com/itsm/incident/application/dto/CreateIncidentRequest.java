package com.itsm.incident.application.dto;

import com.itsm.incident.domain.Severity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "인시던트 등록 요청")
public record CreateIncidentRequest(
        @Schema(description = "요약(필수)")
        @NotBlank String summary,
        String description,
        @Schema(description = "심각도(필수) SEV1|SEV2|SEV3")
        @NotNull Severity severity,
        String affectedService,
        String affectedProduct
) {
}
