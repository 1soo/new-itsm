package com.itsm.esm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ESM 지표 응답")
public record EsmMetricsResponse(
        long requestCount,
        double avgProcessingMinutes,
        double onboardingCompletionRate,
        double offboardingCompletionRate
) {
}
