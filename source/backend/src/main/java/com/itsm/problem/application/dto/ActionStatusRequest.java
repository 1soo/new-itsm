package com.itsm.problem.application.dto;

import com.itsm.problem.domain.ActionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "후속 조치 상태 변경 요청")
public record ActionStatusRequest(
        @Schema(description = "IN_PROGRESS|DONE")
        @NotNull ActionStatus status
) {
}
