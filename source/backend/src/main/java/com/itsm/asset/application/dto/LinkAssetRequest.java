package com.itsm.asset.application.dto;

import com.itsm.common.ticket.TicketType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "자산 티켓 연계 요청")
public record LinkAssetRequest(
        @Schema(description = "SERVICE_REQUEST|INCIDENT|PROBLEM|CHANGE")
        @NotNull TicketType ticketType,
        @NotNull Long ticketId
) {
}
