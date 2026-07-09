package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "자산 목록 요약 응답")
public record AssetSummaryResponse(
        Long id,
        String assetKey,
        String name,
        String type,
        String status,
        String owner,
        LocalDate expiryDate,
        String expiryStatus
) {
}
