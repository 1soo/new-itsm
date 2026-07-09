package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "CI 등록 응답")
public record CiCreatedResponse(Long id, String name) {
}
