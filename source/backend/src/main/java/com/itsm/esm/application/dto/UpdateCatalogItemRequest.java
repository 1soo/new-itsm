package com.itsm.esm.application.dto;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.ChecklistTemplateType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "카탈로그 항목 수정 요청(부분 갱신)")
public record UpdateCatalogItemRequest(
        String name,
        String description,
        Department department,
        ChecklistTemplateType checklistTemplateType,
        @Schema(description = "제공 시 템플릿 전체 교체") List<ChecklistTemplateTaskDto> checklistTemplate,
        @Schema(description = "제공 시 양식 전체 교체") List<FormFieldDto> formSchema
) {
}
