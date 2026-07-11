package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "메뉴 역할 매핑 부여 요청")
public record AssignScreenRoleRequest(
        @Schema(description = "부여할 역할 id")
        @NotNull Long roleId
) {
}
