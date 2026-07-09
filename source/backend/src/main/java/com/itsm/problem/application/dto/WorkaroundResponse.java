package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "워크어라운드 응답")
public record WorkaroundResponse(Long id, String workaround) {
}
