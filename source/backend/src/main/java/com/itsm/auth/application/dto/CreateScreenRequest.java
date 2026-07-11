package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "메뉴 생성 요청")
public record CreateScreenRequest(
        @Schema(description = "화면 식별 코드(필수)")
        @NotBlank String screenCode,
        @Schema(description = "화면명(필수)")
        @NotBlank String screenName,
        @Schema(description = "라우팅 경로(필수)")
        @NotBlank String path,
        @Schema(description = "소속 도메인(필수)")
        @NotBlank String domain,
        @Schema(description = "사이드바 아이콘(lucide-react 컴포넌트명, 선택)")
        String iconName,
        @Schema(description = "사이드바 그룹 키(선택)")
        String groupCode,
        @Schema(description = "사이드바 그룹 표시명(선택, groupCode와 함께 지정)")
        String groupLabel,
        @Schema(description = "정렬 순서(선택, 기본 0)")
        Integer sortOrder,
        @Schema(description = "사이드바 노출 여부(선택, 기본 true)")
        Boolean navVisible
) {
}
