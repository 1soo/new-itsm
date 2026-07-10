package com.itsm.esm.application.dto;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.ChecklistTemplateType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "카탈로그 항목 상세(체크리스트 템플릿·양식 스키마 포함)")
public record CatalogItemDetailResponse(
        Long id,
        String name,
        String description,
        Department department,
        ChecklistTemplateType checklistTemplateType,
        List<ChecklistTemplateTaskDto> checklistTemplate,
        List<FormFieldDto> formSchema
) {
}
