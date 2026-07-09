package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유용성 평가 응답(집계)")
public record FeedbackResponse(int helpful, int notHelpful) {
}
