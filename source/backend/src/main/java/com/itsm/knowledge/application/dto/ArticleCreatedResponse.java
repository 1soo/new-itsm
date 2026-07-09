package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기사 작성 응답")
public record ArticleCreatedResponse(Long id, String status) {
}
