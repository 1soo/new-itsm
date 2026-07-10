package com.itsm.esm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "부서 요청 생성 응답")
public record RequestCreatedResponse(
        Long id,
        String ticketKey,
        String status,
        Long checklistId
) {
}
