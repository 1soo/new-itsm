package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "준수 현황 응답")
public record ComplianceMetricsResponse(
        long totalRequirements,
        long compliantCount,
        long nonCompliantCount,
        long openCorrectiveActionCount,
        double complianceRate
) {
}
