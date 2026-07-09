package com.itsm.srm.application.dto;

import com.itsm.srm.domain.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상태 전이 요청")
public record StatusTransitionRequest(
        @Schema(description = "VALIDATED|ROUTED|IN_FULFILLMENT|FULFILLED|CLOSED")
        @NotNull RequestStatus targetStatus,
        String note
) {
}
