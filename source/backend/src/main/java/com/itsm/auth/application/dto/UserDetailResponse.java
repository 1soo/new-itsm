package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "계정 상세")
public record UserDetailResponse(
        Long id,
        String email,
        String name,
        String status,
        List<String> roles,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
