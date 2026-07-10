package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시정조치 등록 응답")
public record CorrectiveActionCreatedResponse(Long id, String status) {
}
