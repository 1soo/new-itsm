package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "CI 목록 응답")
public record CiListResponse(List<CiSummaryResponse> content, long totalElements) {
}
