package com.itsm.esm.application.dto;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.ChecklistTemplateType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "부서 카탈로그 목록 항목")
public record CatalogItemSummaryResponse(
        Long id,
        String name,
        String description,
        Department department,
        ChecklistTemplateType checklistTemplateType
) {
}
