package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 메뉴 조회 응답")
public record MyMenuResponse(List<MenuGroupResponse> groups) {
}
