package com.itsm.esm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Schema(description = "부서 요청 제출")
public record CreateRequestRequest(
        @Schema(description = "카탈로그 항목 id(필수)")
        @NotNull Long catalogItemId,
        @Schema(description = "양식 필드 값 { key: value }")
        Map<String, Object> formValues,
        @Schema(description = "온보딩/오프보딩 대상자명(해당 유형일 때 필수)")
        String targetUserName
) {
}
