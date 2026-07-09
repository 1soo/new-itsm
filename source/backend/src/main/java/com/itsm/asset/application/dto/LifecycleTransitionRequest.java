package com.itsm.asset.application.dto;

import com.itsm.asset.domain.AssetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "생애주기 단계 전이 요청")
public record LifecycleTransitionRequest(
        @Schema(description = "PLANNING|PROCUREMENT|OPERATION|MAINTENANCE|RETIREMENT")
        @NotNull AssetStatus targetStage
) {
}
