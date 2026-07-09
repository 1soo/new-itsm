package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계정 상태 변경 응답")
public record StatusChangeResponse(Long id, String status) {
}
