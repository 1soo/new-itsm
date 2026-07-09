package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구현 결과 응답")
public record ResultResponse(Long id, String outcome, Boolean rolledBack, String note) {
}
