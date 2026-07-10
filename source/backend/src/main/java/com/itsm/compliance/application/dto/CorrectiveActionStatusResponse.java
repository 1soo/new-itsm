package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시정조치 상태 응답")
public record CorrectiveActionStatusResponse(Long id, String status) {
}
