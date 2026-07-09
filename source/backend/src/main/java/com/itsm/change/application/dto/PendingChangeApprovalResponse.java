package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "변경 승인 대기 항목")
public record PendingChangeApprovalResponse(
        Long changeId,
        String ticketKey,
        String type,
        String risk,
        String requester
) {
}
