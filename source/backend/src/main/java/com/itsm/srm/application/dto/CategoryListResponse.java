package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카탈로그 카테고리 목록 항목")
public record CategoryListResponse(
        Long id,
        String name,
        Integer sortOrder,
        @Schema(description = "이 카테고리를 참조하는 카탈로그 항목 수(SCR-SRM-009 목록 표시용)") long itemCount
) {
}
