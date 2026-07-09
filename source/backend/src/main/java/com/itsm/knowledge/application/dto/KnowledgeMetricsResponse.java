package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "지식 지표")
public record KnowledgeMetricsResponse(
        long usageCount,
        long noResultSearchCount,
        double helpfulRate,
        @Schema(description = "게시 기사 열람 대비 관련 티켓 미연계 비율(%)") double deflectionRate,
        List<String> topNoResultKeywords
) {
}
