package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "포스트모템 조치항목")
public record ActionItemDto(
        String description,
        String owner,
        LocalDate dueDate,
        @Schema(description = "OPEN|DONE") String status
) {
}
