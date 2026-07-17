package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "카탈로그 항목 상세(양식 스키마)")
public record CatalogItemDetailResponse(
        Long id,
        String name,
        String description,
        Long categoryId,
        String categoryName,
        Long queueId,
        Integer slaResponseMinutes,
        Integer slaResolveMinutes,
        Long assigneeRoleId,
        String assigneeRoleName,
        @Schema(description = "동적 양식(Form.io Form JSON, {display, components})") Map<String, Object> formSchema
) {
}
