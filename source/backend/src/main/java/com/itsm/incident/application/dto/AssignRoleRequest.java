package com.itsm.incident.application.dto;

import com.itsm.incident.domain.ResponseRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "대응 역할 배정 요청")
public record AssignRoleRequest(
        @NotNull Long userId,
        @Schema(description = "TECH_LEAD|COMMS|SCRIBE")
        @NotNull ResponseRole role
) {
}
