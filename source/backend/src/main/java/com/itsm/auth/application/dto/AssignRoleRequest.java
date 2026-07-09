package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자 역할 부여 요청")
public record AssignRoleRequest(
        @Schema(description = "부여할 역할 id")
        @NotNull Long roleId
) {
}
