package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "CI 영향 범위 항목")
public record CiImpactResponse(Long ciId, String name, String relationType, int depth) {
}
