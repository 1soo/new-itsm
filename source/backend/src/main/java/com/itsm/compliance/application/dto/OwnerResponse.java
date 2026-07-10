package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "책임자 지정 응답")
public record OwnerResponse(Long id, String owner) {
}
