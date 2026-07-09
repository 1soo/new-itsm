package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "연계 결과")
public record LinkAssetResponse(Long assetId, Long ticketId) {
}
