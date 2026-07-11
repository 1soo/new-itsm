package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "승인 프로세스 대상 도메인")
public record ApprovalDomainResponse(String domain, String label, boolean hasRequestSubtype) {
}
