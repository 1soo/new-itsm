package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "승인 프로세스 상세")
public record ApprovalProcessDetailResponse(
        Long id, String domain, String targetState, String requestSubtypeKey, String name, String description,
        List<Long> requesterRoleIds, List<StepDto> steps
) {
    @Schema(description = "차수")
    public record StepDto(short stepNo, String decisionMode, List<Long> roleIds) {
    }
}
