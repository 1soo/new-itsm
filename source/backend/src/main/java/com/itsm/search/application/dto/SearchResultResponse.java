package com.itsm.search.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "통합 검색 결과 항목")
public record SearchResultResponse(
        String domain,
        String key,
        String title,
        String status,
        String snippet,
        OffsetDateTime updatedAt,
        String url
) {
}
