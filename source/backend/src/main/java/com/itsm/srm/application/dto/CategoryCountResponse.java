package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리별 미종료 요청 건수(API-SRM-016)")
public record CategoryCountResponse(
        @Schema(description = "카테고리 ID(null=미분류)") Long categoryId,
        @Schema(description = "카테고리명(null이면 미분류)") String categoryName,
        @Schema(description = "미종료(CLOSED 제외) 요청 건수") long openCount
) {
}
