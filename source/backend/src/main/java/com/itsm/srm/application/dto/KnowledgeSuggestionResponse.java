package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지식 기사 추천 항목")
public record KnowledgeSuggestionResponse(
        Long articleId,
        String title,
        Double score
) {
}
