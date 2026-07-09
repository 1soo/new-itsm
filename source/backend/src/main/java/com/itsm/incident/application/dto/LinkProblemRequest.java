package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문제 연계 요청")
public record LinkProblemRequest(
        @Schema(description = "기존 문제 id") Long problemId,
        @Schema(description = "true면 신규 문제 생성") boolean createNewProblem
) {
}
