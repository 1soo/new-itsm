package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "변경 지표")
public record ChangeMetricsResponse(double successRate, double failureRate, double emergencyRate, long total) {
}
