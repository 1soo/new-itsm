package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인시던트 지표")
public record IncidentMetricsResponse(
        long count,
        SeverityDistribution severityDistribution,
        double avgMttrMinutes
) {
    @Schema(description = "심각도 분포")
    public record SeverityDistribution(long SEV1, long SEV2, long SEV3) {
    }
}
