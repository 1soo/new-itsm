package com.itsm.problem.application.dto;

import com.itsm.problem.domain.LinkTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "인시던트/변경 연계 요청")
public record LinkRequest(
        @Schema(description = "연계 대상 유형 INCIDENT|CHANGE")
        @NotNull LinkTargetType targetType,
        @Schema(description = "기존 대상 id") Long targetId,
        @Schema(description = "true면 신규 변경(CHANGE) 생성") boolean createNewChange
) {
}
