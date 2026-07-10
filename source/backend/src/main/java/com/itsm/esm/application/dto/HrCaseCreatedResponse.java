package com.itsm.esm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "HR 케이스 접수 응답")
public record HrCaseCreatedResponse(
        Long id,
        String status
) {
}
