package com.itsm.infra.application.dto;

import java.math.BigDecimal;

public record ThresholdResponse(String metricType, BigDecimal upperLimit, BigDecimal lowerLimit) {
}
