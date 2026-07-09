package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "요청 생성 응답")
public record RequestCreatedResponse(
        Long id,
        String ticketKey,
        String status,
        OffsetDateTime createdAt
) {
}
