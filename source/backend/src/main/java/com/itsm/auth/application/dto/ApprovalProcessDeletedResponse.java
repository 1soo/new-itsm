package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "승인 프로세스 삭제 응답")
public record ApprovalProcessDeletedResponse(Long id, boolean deleted) {
}
