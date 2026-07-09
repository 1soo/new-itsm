package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "포스트모템 작성/수정 요청")
public record PostmortemRequest(
        String summary,
        @Schema(description = "타임라인 요약") String timeline,
        @Schema(description = "Why 사슬") List<String> fiveWhys,
        @Schema(description = "근본원인(필수)") String rootCause,
        List<ActionItemDto> actionItems
) {
}
