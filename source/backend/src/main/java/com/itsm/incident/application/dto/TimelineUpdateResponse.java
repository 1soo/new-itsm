package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "상태 업데이트 응답")
public record TimelineUpdateResponse(Long id, OffsetDateTime at) {
}
