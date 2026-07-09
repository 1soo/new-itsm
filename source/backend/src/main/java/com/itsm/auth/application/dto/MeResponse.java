package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 정보 응답")
public record MeResponse(
        Long id,
        String email,
        String name,
        String status,
        List<String> roles
) {
}
