package com.itsm.knowledge.application.dto;

import com.itsm.common.ticket.TicketType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "KCS 티켓 연계(작성/연결) 요청")
public record LinkArticleRequest(
        @Schema(description = "SERVICE_REQUEST|INCIDENT|PROBLEM")
        @NotNull TicketType ticketType,
        @NotNull Long ticketId,
        @Schema(description = "기존 기사 연결 시") Long articleId,
        @Schema(description = "신규 기사 작성 시") NewArticleDto newArticle
) {
    @Schema(description = "신규 기사 초안")
    public record NewArticleDto(String title, String body) {
    }
}
