package com.itsm.infra.application.dto;

import java.math.BigDecimal;

public record ThresholdUpdateRequest(BigDecimal upperLimit, BigDecimal lowerLimit) {
}
