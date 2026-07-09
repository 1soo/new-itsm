package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "기사 수정 요청(부분 갱신, 미지정 필드는 유지)")
public record UpdateArticleRequest(
        String title,
        String body,
        Long categoryId,
        @Schema(description = "지정 시 라벨 전체 교체") List<String> labels
) {
}
