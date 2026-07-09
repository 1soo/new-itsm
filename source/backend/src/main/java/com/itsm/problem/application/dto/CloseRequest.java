package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문제 종료 요청")
public record CloseRequest(
        @Schema(description = "true면 미해결 후속조치 경고를 무시하고 강제 종료") boolean force
) {
}
