package com.itsm.change.application.dto;

import com.itsm.change.domain.ChangeRisk;
import com.itsm.change.domain.ChangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "변경 요청(RFC) 생성 요청")
public record CreateChangeRequest(
        @Schema(description = "요약(필수)")
        @NotBlank String summary,
        String description,
        @Schema(description = "유형(필수) STANDARD|NORMAL|EMERGENCY")
        @NotNull ChangeType type,
        @Schema(description = "위험도 HIGH|MEDIUM|LOW(미평가 시 생략)") ChangeRisk risk,
        String implementationPlan,
        List<String> affectedSystems,
        String rollbackPlan,
        OffsetDateTime scheduledAt,
        @Schema(description = "표준 변경 시 템플릿 id") Long templateId
) {
}
