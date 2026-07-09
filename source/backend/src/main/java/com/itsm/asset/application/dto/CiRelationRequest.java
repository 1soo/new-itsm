package com.itsm.asset.application.dto;

import com.itsm.asset.domain.RelationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "CI 관계 등록 요청")
public record CiRelationRequest(
        @NotNull Long targetCiId,
        @Schema(description = "DEPENDS_ON|RUNS_ON|CONNECTS_TO")
        @NotNull RelationType relationType
) {
}
