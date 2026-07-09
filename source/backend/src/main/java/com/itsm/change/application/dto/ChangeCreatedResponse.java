package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "변경 요청(RFC) 생성 응답")
public record ChangeCreatedResponse(Long id, String ticketKey, String status, String type) {
}
