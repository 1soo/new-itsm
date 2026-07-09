package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기사 목록 요약 응답")
public record ArticleSummaryResponse(
        Long id,
        String title,
        String summary,
        String status,
        String category,
        double helpfulRate
) {
}
