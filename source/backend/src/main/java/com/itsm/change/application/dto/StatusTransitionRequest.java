package com.itsm.change.application.dto;

import com.itsm.change.domain.ChangeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상태 전이 요청")
public record StatusTransitionRequest(
        @Schema(description = "REVIEW|PLANNING|APPROVAL|IMPLEMENTATION|CLOSED")
        @NotNull ChangeStatus targetStatus,
        String note
) {
}
