package com.itsm.common.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 확인처리 결과 응답")
public record DismissResultResponse(
        @Schema(description = "이번 요청으로 신규 확인처리된 건수")
        int dismissedCount
) {
}
