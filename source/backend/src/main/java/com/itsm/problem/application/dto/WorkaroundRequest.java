package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "워크어라운드 등록 요청")
public record WorkaroundRequest(
        @Schema(description = "워크어라운드 내용(필수)")
        @NotBlank String content,
        @Schema(description = "연결 지식 문서 id(선택)") Long linkedArticleId
) {
}
