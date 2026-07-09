package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "표준 변경 템플릿")
public record ChangeTemplateResponse(Long id, String name, String description) {
}
