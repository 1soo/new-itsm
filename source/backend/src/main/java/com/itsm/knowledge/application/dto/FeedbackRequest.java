package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유용성 평가 요청")
public record FeedbackRequest(
        @Schema(description = "도움됨 여부") boolean helpful,
        String comment
) {
}
