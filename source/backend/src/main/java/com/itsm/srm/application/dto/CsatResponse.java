package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "CSAT 응답")
public record CsatResponse(Long id, int score) {
}
