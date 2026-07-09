package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알려진 오류 생성 응답")
public record KnownErrorCreatedResponse(Long id, String title) {
}
