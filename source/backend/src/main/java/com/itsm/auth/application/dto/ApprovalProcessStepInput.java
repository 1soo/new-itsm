package com.itsm.auth.application.dto;

import com.itsm.common.approval.domain.DecisionMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "승인 프로세스 차수 입력(순서=차수)")
public record ApprovalProcessStepInput(
        @Schema(description = "AND|OR · 역할 2개 이상일 때만 의미") @NotNull DecisionMode decisionMode,
        @Schema(description = "승인 역할(1개 이상)") @NotEmpty List<Long> roleIds
) {
}
