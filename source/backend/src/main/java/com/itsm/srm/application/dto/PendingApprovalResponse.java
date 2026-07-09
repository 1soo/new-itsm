package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "승인 대기 항목")
public record PendingApprovalResponse(
        Long requestId,
        String ticketKey,
        String requester,
        OffsetDateTime requestedAt
) {
}
