package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "메뉴 역할 매핑 목록 응답")
public record ScreenRolesResponse(Long screenId, List<String> roles) {
}
