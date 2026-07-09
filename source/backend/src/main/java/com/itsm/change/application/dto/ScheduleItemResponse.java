package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "변경 일정 항목")
public record ScheduleItemResponse(Long id, String ticketKey, String summary, String type, OffsetDateTime scheduledAt) {
}
