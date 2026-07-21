package com.itsm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "승인 프로세스 생성 요청")
public record CreateApprovalProcessRequest(
        @Schema(description = "선택, null=전체 도메인 적용") String domain,
        @Schema(description = "domain이 null이면 반드시 null. domain 지정 시 API-AUTH-031 후보 중 값 지정 가능, 전체 선택 시 null") String targetState,
        @Schema(description = "domain이 null이면 반드시 null. 하위유형 있는 도메인만, 전체 선택 시 null") String requestSubtypeKey,
        @NotBlank String name,
        String description,
        @Schema(description = "0개 이상, 승인 요청자 박스에서 선택한 역할") List<Long> requesterRoleIds,
        @Schema(description = "순서=차수(1차부터, 최대 10개)") List<ApprovalProcessStepInput> steps
) {
}
