package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리")
public record CategoryResponse(Long id, String name) {
}
