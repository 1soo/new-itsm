package com.itsm.esm.application.dto;

import com.itsm.auth.domain.Department;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "부서 요청 목록 항목")
public record RequestSummaryResponse(
        Long id,
        String ticketKey,
        String catalogItemName,
        Department department,
        String status,
        OffsetDateTime updatedAt,
        @Schema(description = "진행 중인 승인 인스턴스의 targetState(원본 코드값, 없으면 null)") String pendingApprovalTargetState
) {
}
