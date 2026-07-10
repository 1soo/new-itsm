package com.itsm.infra.application.dto;

import java.math.BigDecimal;

public record CapacityPlanResponse(Long id, String teamOrService, BigDecimal capacity, BigDecimal demand,
                                    BigDecimal utilizationRate) {
}
