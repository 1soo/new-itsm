package com.itsm.infra.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MetricResponse(Long id, String metricType, BigDecimal value, OffsetDateTime measuredAt) {
}
