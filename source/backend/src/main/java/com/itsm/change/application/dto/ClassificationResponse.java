package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "변경 유형·위험 변경 응답")
public record ClassificationResponse(Long id, String type, String risk, String approvalRoute) {
}
