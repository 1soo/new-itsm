package com.itsm.infra.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AlertResponse(Long id, String assetKey, String metricType, BigDecimal value, String thresholdType,
                             boolean acknowledged, OffsetDateTime occurredAt) {
}
