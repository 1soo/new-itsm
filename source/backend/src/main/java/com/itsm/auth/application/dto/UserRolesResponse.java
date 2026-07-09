package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사용자 역할 목록 응답")
public record UserRolesResponse(Long userId, List<String> roles) {
}
