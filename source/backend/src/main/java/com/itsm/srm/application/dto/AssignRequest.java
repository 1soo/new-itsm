package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "담당자 배정 요청")
public record AssignRequest(
        @Schema(description = "배정 대상 사용자 id(미지정 시 본인)") Long assigneeId
) {
}
