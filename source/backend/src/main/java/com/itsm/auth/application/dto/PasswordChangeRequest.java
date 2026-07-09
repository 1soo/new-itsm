package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 변경 요청")
public record PasswordChangeRequest(
        @Schema(description = "현재 비밀번호")
        @NotBlank String currentPassword,
        @Schema(description = "새 비밀번호(정책 검증 대상)")
        @NotBlank String newPassword
) {
}
