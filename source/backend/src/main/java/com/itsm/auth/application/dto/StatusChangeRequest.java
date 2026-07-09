package com.itsm.auth.application.dto;

import com.itsm.auth.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "계정 상태 변경 요청")
public record StatusChangeRequest(
        @Schema(description = "상태", example = "INACTIVE")
        @NotNull UserStatus status
) {
}
