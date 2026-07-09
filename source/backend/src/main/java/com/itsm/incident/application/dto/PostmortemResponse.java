package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "포스트모템")
public record PostmortemResponse(
        Long incidentId,
        String summary,
        String timeline,
        String rootCause,
        List<String> fiveWhys,
        List<ActionItemDto> actionItems
) {
}
