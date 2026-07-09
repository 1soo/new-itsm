package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "자산 지표")
public record AssetMetricsResponse(
        @Schema(description = "OPERATION 상태 자산 비율(%)") double utilizationRate,
        @Schema(description = "만료 임박·만료 자산 수") long expiringCount,
        Map<String, Long> typeDistribution
) {
}
