package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "카탈로그 항목 생성 요청")
public record CreateCatalogItemRequest(
        @Schema(description = "요청 유형명(필수)")
        @NotBlank String name,
        String description,
        Long queueId,
        Integer slaResponseMinutes,
        Integer slaResolveMinutes,
        @Schema(description = "담당자 역할(선택, 요청 큐 배정 후보 역할)") Long assigneeRoleId,
        @Schema(description = "동적 양식(1개 이상)")
        @NotEmpty List<FormFieldDto> formSchema
) {
}
