package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "변경 승인/반려 응답")
public record ChangeApprovalResponse(Long id, String status) {
}
