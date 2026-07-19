package com.itsm.esm.application.dto;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.ChecklistTemplateType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Schema(description = "카탈로그 항목 생성 요청")
public record CreateCatalogItemRequest(
        @Schema(description = "요청 유형명(필수)")
        @NotBlank String name,
        String description,
        @Schema(description = "담당 부서(필수) HR|LEGAL|FACILITIES|FINANCE")
        @NotNull Department department,
        ChecklistTemplateType checklistTemplateType,
        List<ChecklistTemplateTaskDto> checklistTemplate,
        @Schema(description = "동적 양식(SRM과 완전히 동일한 자체 8×n 그리드 스키마, {components,labels})")
        Map<String, Object> formSchema
) {
}
