package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "CI 관계 등록 응답")
public record CiRelationResponse(Long id, Long sourceCiId, Long targetCiId, String relationType) {
}
