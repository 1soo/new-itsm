package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "카탈로그 항목 수정 요청(부분 갱신)")
public record UpdateCatalogItemRequest(
        String name,
        String description,
        Long queueId,
        Integer slaResponseMinutes,
        Integer slaResolveMinutes,
        @Schema(description = "담당자 역할(선택)") Long assigneeRoleId,
        @Schema(description = "제공 시 양식 전체 교체") List<FormFieldDto> formSchema
) {
}
