package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "기사 검색/목록 응답")
public record ArticleListResponse(
        List<ArticleSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        @Schema(description = "무결과 검색 기록 여부") boolean noResult
) {
}
