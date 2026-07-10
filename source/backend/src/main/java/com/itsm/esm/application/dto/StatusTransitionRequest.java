package com.itsm.esm.application.dto;

import com.itsm.esm.domain.EsmRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "부서 요청 상태 전이 요청")
public record StatusTransitionRequest(
        @Schema(description = "IN_PROGRESS|COMPLETED|REJECTED")
        @NotNull EsmRequestStatus targetStatus,
        String note
) {
}
