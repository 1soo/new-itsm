package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "카탈로그 항목 수정 요청(부분 갱신)")
public record UpdateCatalogItemRequest(
        String name,
        String description,
        @Schema(description = "카테고리(선택)") Long categoryId,
        Integer slaResponseMinutes,
        Integer slaResolveMinutes,
        @Schema(description = "담당자 역할(선택)") Long assigneeRoleId,
        @Schema(description = "제공 시 양식 전체 교체(자체 8×n 그리드 스키마, {components})") Map<String, Object> formSchema
) {
}
