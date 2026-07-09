package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "승인/반려 응답")
public record ApprovalDecisionResponse(Long id, String approvalStatus) {
}
