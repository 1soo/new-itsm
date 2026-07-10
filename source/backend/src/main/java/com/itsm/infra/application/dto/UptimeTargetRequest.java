package com.itsm.infra.application.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UptimeTargetRequest(@NotNull BigDecimal targetPercentage) {
}
