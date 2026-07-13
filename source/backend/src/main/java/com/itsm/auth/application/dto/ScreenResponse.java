package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "화면(메뉴) 응답")
public record ScreenResponse(
        Long id,
        String screenCode,
        String screenName,
        String screenNameEn,
        String path,
        String domain,
        String iconName,
        String groupCode,
        String groupLabel,
        String groupLabelEn,
        int sortOrder,
        boolean navVisible,
        List<String> roles
) {
}
