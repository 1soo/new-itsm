package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "승인 프로세스 수정 요청(전달된 필드만 갱신, requesterRoleIds·steps는 전달 시 전체 교체)")
public record UpdateApprovalProcessRequest(
        String name,
        String description,
        List<Long> requesterRoleIds,
        List<ApprovalProcessStepInput> steps
) {
}
