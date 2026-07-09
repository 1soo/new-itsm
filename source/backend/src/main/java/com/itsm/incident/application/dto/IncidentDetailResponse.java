package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "인시던트 상세")
public record IncidentDetailResponse(
        Long id,
        String ticketKey,
        String summary,
        String description,
        String severity,
        String priority,
        String status,
        String affectedService,
        String affectedProduct,
        List<ResponderDto> responders,
        IncidentMetrics metrics,
        List<LinkDto> links,
        List<TimelineEntry> timeline,
        @Schema(description = "현재 상태·역할 기준 수행 가능한 상태 전이 target 목록") List<String> allowedTransitions
) {
    @Schema(description = "연계 링크")
    public record LinkDto(String type, String targetKey) {
    }

    @Schema(description = "타임라인 항목")
    public record TimelineEntry(String type, String visibility, String message, OffsetDateTime at) {
    }
}
