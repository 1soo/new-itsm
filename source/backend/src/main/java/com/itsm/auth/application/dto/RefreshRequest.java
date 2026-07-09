package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 요청(Body 방식). Cookie 방식이면 생략 가능.")
public record RefreshRequest(
        @Schema(description = "Refresh Token") String refreshToken
) {
}
