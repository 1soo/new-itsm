package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "역할 생성 응답")
public record RoleCreatedResponse(Long id, String roleCode, String name, String description) {
}
