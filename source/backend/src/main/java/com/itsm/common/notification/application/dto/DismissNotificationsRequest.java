package com.itsm.common.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "알림 확인처리 요청(개별 1건 또는 일괄)")
public record DismissNotificationsRequest(
        @Schema(description = "확인처리 대상(1개 이상)")
        @NotEmpty @Valid List<Item> items
) {
    @Schema(description = "확인처리 대상 항목")
    public record Item(
            @Schema(description = "SERVICE_REQUEST_APPROVAL|CHANGE_APPROVAL|ASSET_EXPIRY")
            @NotBlank String notificationType,
            @Schema(description = "승인 대기(requestId/changeId) 또는 자산 id")
            @NotNull Long sourceId
    ) {
    }
}
