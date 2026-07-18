package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(description = "카탈로그 항목 생성 요청")
public record CreateCatalogItemRequest(
        @Schema(description = "요청 유형명(필수)")
        @NotBlank String name,
        String description,
        @Schema(description = "카테고리(선택, 미지정 시 미분류)") Long categoryId,
        Integer slaResponseMinutes,
        Integer slaResolveMinutes,
        @Schema(description = "담당자 역할(선택, 배정 후보 역할)") Long assigneeRoleId,
        @Schema(description = "동적 양식(자체 8×n 그리드 스키마, {components})")
        @NotNull Map<String, Object> formSchema
) {
}
