package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "카탈로그 카테고리 생성 요청")
public record CategoryCreateRequest(
        @Schema(description = "카테고리명(필수)") @NotBlank String name,
        @Schema(description = "정렬 순서(선택, 미지정 시 0)") Integer sortOrder
) {
}
