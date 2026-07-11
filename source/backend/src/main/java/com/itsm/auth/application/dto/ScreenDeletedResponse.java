package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 삭제 응답")
public record ScreenDeletedResponse(Long id, boolean deleted) {
}
