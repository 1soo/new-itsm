package com.itsm.infra.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CapacityPlanCreateRequest(
        @NotBlank String teamOrService,
        @NotNull @Positive BigDecimal capacity,
        @NotNull BigDecimal demand) {
}
