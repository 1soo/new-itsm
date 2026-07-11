package com.itsm.common.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "확인처리된 알림 항목")
public record NotificationDismissalResponse(
        String notificationType,
        Long sourceId,
        OffsetDateTime dismissedAt
) {
}
