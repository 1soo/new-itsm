package com.itsm.esm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "하위 작업 상태 변경 응답")
public record ChecklistTaskStatusResponse(
        Long id,
        String status,
        @Schema(description = "전체 하위 작업 완료 여부 반영") String checklistStatus
) {
}
