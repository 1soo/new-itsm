package com.itsm.esm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 하위 작업 목록 항목")
public record MyChecklistTaskResponse(
        Long id,
        Long checklistId,
        String checklistType,
        String targetUserName,
        String description,
        String status
) {
}
