package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "검토 대기 항목")
public record PendingReviewResponse(Long articleId, String title, String author, OffsetDateTime requestedAt) {
}
