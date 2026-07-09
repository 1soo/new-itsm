package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "후속 조치 응답")
public record ActionResponse(Long id, String status) {
}
