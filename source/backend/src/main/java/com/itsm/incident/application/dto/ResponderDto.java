package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대응 역할 배정")
public record ResponderDto(
        Long userId,
        String name,
        @Schema(description = "TECH_LEAD|COMMS|SCRIBE") String role
) {
}
