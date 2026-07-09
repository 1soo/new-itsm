package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상태 응답")
public record StatusResponse(Long id, String status) {
}
