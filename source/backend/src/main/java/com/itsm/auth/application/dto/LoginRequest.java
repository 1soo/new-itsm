package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "이메일", example = "admin@itsm.local")
        @NotBlank @Email String email,
        @Schema(description = "비밀번호", example = "Admin123!")
        @NotBlank String password
) {
}
