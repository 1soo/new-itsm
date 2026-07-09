package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "로그인 응답")
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserInfo user
) {
    @Schema(description = "로그인 사용자 요약")
    public record UserInfo(Long id, String email, String name, List<String> roles) {
    }
}
