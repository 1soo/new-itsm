package com.itsm.esm.application.dto;

import com.itsm.auth.domain.Department;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체크리스트 하위 작업 템플릿")
public record ChecklistTemplateTaskDto(
        Department department,
        String taskDescription
) {
}
