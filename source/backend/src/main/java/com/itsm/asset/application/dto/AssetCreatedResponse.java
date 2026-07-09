package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자산 등록 응답")
public record AssetCreatedResponse(Long id, String assetKey, String status) {
}
