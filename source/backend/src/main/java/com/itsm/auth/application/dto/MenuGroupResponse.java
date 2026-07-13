package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "메뉴 그룹(그룹 라벨 없으면 groupCode/groupLabel 모두 null)")
public record MenuGroupResponse(String groupCode, String groupLabel, String groupLabelEn, List<MenuItemResponse> items) {
}
