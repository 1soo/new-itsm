package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "연계 결과(양방향 링크 생성)")
public record LinkResponse(
        Long problemId,
        String targetType,
        Long targetId
) {
}
