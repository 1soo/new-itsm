package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메뉴 수정 요청(모두 선택). screenCode·domain은 식별자로 취급해 제외")
public record UpdateScreenRequest(
        String screenName,
        String screenNameEn,
        String path,
        String iconName,
        String groupCode,
        String groupLabel,
        String groupLabelEn,
        Integer sortOrder,
        Boolean navVisible
) {
}
