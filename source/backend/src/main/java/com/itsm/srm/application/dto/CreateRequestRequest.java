package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(description = "서비스 요청 생성(제출)")
public record CreateRequestRequest(
        @Schema(description = "카탈로그 항목 id(필수)")
        @NotNull Long catalogItemId,
        @Schema(description = "양식 필드 값 { key: value }")
        Map<String, Object> formValues
) {
}
