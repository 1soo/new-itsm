package com.itsm.esm.application.dto;

import com.itsm.esm.domain.ChecklistTaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "하위 작업 상태 변경 요청")
public record ChecklistTaskStatusRequest(
        @Schema(description = "DONE")
        @NotNull ChecklistTaskStatus status
) {
}
