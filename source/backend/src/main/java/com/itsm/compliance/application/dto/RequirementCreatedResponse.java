package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "요구사항 등록 응답")
public record RequirementCreatedResponse(Long id, String requirementKey) {
}
