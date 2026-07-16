package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카탈로그 카테고리 수정 요청(부분 갱신)")
public record CategoryUpdateRequest(String name, Integer sortOrder) {
}
