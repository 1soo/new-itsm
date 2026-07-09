package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메시지 응답")
public record MessageResponse(String message) {
}
