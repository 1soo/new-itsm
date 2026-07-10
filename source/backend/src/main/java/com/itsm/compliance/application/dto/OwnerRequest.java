package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "책임자 지정 요청")
public record OwnerRequest(
        @NotNull Long ownerId
) {
}
