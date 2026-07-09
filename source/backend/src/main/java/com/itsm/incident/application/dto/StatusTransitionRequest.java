package com.itsm.incident.application.dto;

import com.itsm.incident.domain.IncidentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상태 전이 요청")
public record StatusTransitionRequest(
        @Schema(description = "IN_PROGRESS|RESOLVED|CLOSED")
        @NotNull IncidentStatus targetStatus,
        String note
) {
}
