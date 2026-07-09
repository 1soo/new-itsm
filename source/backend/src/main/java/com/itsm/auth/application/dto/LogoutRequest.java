package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그아웃 요청")
public record LogoutRequest(
        @Schema(description = "무효화 대상 Refresh Token(선택)") String refreshToken
) {
}
