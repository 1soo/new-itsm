package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "기사 작성 요청")
public record CreateArticleRequest(
        @Schema(description = "제목(필수)")
        @NotBlank String title,
        @Schema(description = "본문(필수)")
        @NotBlank String body,
        @Schema(description = "카테고리 id") Long categoryId,
        @Schema(description = "라벨명 목록") List<String> labels
) {
}
