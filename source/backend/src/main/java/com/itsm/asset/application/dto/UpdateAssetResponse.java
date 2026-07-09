package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자산 수정 응답")
public record UpdateAssetResponse(Long id, @Schema(description = "만료일 과거 입력 시 경고") String warning) {
}
