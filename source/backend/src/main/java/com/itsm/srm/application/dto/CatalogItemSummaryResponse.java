package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카탈로그 목록 항목")
public record CatalogItemSummaryResponse(
        Long id,
        String name,
        String description,
        Long categoryId,
        String categoryName
) {
}
