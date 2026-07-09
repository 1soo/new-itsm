package com.itsm.incident.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "심각도·우선순위 변경 응답")
public record SeverityChangeResponse(Long id, String severity, String priority) {
}
