package com.itsm.esm.application.dto;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.ChecklistTemplateType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "카탈로그 항목 상세(체크리스트 템플릿·양식 스키마 포함)")
public record CatalogItemDetailResponse(
        Long id,
        String name,
        String description,
        Department department,
        ChecklistTemplateType checklistTemplateType,
        List<ChecklistTemplateTaskDto> checklistTemplate,
        @Schema(description = "동적 양식(SRM과 완전히 동일한 자체 8×n 그리드 스키마, {components,labels})")
        Map<String, Object> formSchema
) {
}
