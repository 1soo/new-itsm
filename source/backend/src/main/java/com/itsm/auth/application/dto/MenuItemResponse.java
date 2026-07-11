package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 항목")
public record MenuItemResponse(String screenCode, String screenName, String path, String iconName) {
}
