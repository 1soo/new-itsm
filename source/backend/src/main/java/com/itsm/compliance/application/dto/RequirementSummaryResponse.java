package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "요구사항 목록 요약 응답")
public record RequirementSummaryResponse(
        Long id,
        String requirementKey,
        String name,
        String basis,
        String owner,
        @Schema(description = "COMPLIANT|NON_COMPLIANT(계산값)") String complianceStatus,
        OffsetDateTime updatedAt
) {
}
