package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문제 종료 응답")
public record CloseResponse(
        Long id,
        String status,
        @Schema(description = "미해결 후속조치가 있을 때 경고(없으면 null)") String warning
) {
}
