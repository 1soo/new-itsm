package com.itsm.incident.application.dto;

import com.itsm.incident.domain.EscalationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "에스컬레이션 요청")
public record EscalateRequest(
        @NotNull Long targetUserId,
        @Schema(description = "HIERARCHICAL|FUNCTIONAL")
        @NotNull EscalationType type,
        String reason
) {
}
