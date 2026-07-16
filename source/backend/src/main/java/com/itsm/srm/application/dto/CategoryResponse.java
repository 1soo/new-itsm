package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카탈로그 카테고리 생성/수정 응답")
public record CategoryResponse(Long id, String name, Integer sortOrder) {
}
