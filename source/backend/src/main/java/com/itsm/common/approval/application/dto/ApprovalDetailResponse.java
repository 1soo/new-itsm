package com.itsm.common.approval.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "승인 인스턴스 상세(차수별 진행 상태)")
public record ApprovalDetailResponse(
        Long id,
        String ticketType,
        Long ticketId,
        String ticketKey,
        String status,
        Short currentStepNo,
        List<StepDto> steps
) {
    @Schema(description = "차수별 진행 상태")
    public record StepDto(short stepNo, String decisionMode, String status, List<RoleDto> roles) {
    }

    @Schema(description = "차수 내 역할별 결정 현황")
    public record RoleDto(String roleCode, String roleName, String decision, String decidedBy,
                           String reason, OffsetDateTime decidedAt) {
    }
}
