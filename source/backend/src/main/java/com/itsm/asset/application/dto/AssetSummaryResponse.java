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
        String expiryStatus,
        @Schema(description = "진행 중인 승인 인스턴스의 targetState(원본 코드값, 없으면 null)") String pendingApprovalTargetState
) {
}
