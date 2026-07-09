package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "CI 요약")
public record CiSummaryResponse(Long id, String name, String type) {
}
