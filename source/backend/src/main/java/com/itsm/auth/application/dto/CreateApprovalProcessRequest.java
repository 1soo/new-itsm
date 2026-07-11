package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "승인 프로세스 생성 요청")
public record CreateApprovalProcessRequest(
        @NotNull String domain,
        @Schema(description = "하위유형 있는 도메인만, 전체 선택 시 null") String requestSubtypeKey,
        @NotBlank String name,
        String description,
        @Schema(description = "0개 이상, 승인 요청자 박스에서 선택한 역할") List<Long> requesterRoleIds,
        @Schema(description = "순서=차수(1차부터, 최대 10개)") List<ApprovalProcessStepInput> steps
) {
}
