package com.itsm.infra.application.dto;

import java.math.BigDecimal;

public record UptimeStatusResponse(String assetKey, BigDecimal targetPercentage, BigDecimal actualPercentage,
                                    Boolean met) {
}
