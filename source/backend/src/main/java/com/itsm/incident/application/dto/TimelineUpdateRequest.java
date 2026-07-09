package com.itsm.incident.application.dto;

import com.itsm.common.ticket.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "상태 업데이트(타임라인) 요청")
public record TimelineUpdateRequest(
        @NotBlank String message,
        @Schema(description = "INTERNAL|EXTERNAL(기본 INTERNAL)") Visibility visibility
) {
}
