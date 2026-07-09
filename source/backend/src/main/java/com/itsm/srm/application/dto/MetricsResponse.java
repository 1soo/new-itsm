package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "요청 지표")
public record MetricsResponse(
        double csatAvg,
        double avgResponseMinutes,
        double avgResolveMinutes,
        double slaComplianceRate
) {
}
