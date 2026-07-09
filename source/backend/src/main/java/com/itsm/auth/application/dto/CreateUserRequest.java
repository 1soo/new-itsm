package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "계정 생성 요청")
public record CreateUserRequest(
        @Schema(description = "이메일(필수)")
        @NotBlank @Email String email,
        @Schema(description = "이름(필수)")
        @NotBlank String name,
        @Schema(description = "초기 비밀번호(필수, 정책 검증 대상)")
        @NotBlank String initialPassword,
        @Schema(description = "초기 역할 id(1개 이상)")
        @NotEmpty List<Long> roleIds
) {
}
