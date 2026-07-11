package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "승인 프로세스 목록 항목")
public record ApprovalProcessSummaryResponse(
        Long id, String domain, String requestSubtypeKey, String requestSubtypeLabel,
        short priorityTier, String name, List<String> requesterRoles, int stepCount
) {
}
