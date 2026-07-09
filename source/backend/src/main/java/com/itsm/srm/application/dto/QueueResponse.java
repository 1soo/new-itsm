package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "큐 목록·건수 항목")
public record QueueResponse(
        Long id,
        String name,
        @Schema(description = "미분류 기본 큐") boolean isDefault,
        @Schema(description = "미종료 요청 건수") long openCount
) {
}
