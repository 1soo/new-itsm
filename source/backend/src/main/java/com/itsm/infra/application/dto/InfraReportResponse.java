package com.itsm.infra.application.dto;

import java.math.BigDecimal;

public record InfraReportResponse(BigDecimal avgUptime, BigDecimal avgCpu, BigDecimal avgMemory,
                                   BigDecimal avgResponseTime, BigDecimal avgCapacityUtilization) {
}
