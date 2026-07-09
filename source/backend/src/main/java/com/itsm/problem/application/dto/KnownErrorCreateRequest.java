package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "알려진 오류(KE) 생성 요청")
public record KnownErrorCreateRequest(
        @Schema(description = "제목(검색 대상)")
        @NotBlank String title,
        String rootCause,
        String workaround
) {
}
