package com.itsm.infra.application.dto;

import com.itsm.infra.domain.MetricType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MetricCreateRequest(
        @NotNull Long assetId,
        @NotNull MetricType metricType,
        @NotNull BigDecimal value,
        OffsetDateTime measuredAt) {
}
