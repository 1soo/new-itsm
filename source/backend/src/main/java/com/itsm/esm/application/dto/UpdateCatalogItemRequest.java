package com.itsm.esm.application.dto;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.ChecklistTemplateType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "카탈로그 항목 수정 요청(부분 갱신)")
public record UpdateCatalogItemRequest(
        String name,
        String description,
        Department department,
        ChecklistTemplateType checklistTemplateType,
        @Schema(description = "제공 시 템플릿 전체 교체") List<ChecklistTemplateTaskDto> checklistTemplate,
        @Schema(description = "제공 시 양식 전체 교체(SRM과 완전히 동일한 자체 8×n 그리드 스키마, {components,labels})")
        Map<String, Object> formSchema
) {
}
