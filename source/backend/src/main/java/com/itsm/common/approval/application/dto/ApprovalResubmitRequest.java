package com.itsm.common.approval.application.dto;

import com.itsm.common.ticket.TicketType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "반려 후 재승인요청(API-COM-006)")
public record ApprovalResubmitRequest(
        @NotNull TicketType ticketType,
        @NotNull Long ticketId
) {
}
