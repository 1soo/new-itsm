package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계정 수정 요청")
public record UpdateUserRequest(
        @Schema(description = "이름(선택)") String name
) {
}
