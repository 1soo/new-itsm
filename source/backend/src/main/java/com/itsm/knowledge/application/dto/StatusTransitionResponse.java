package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상태 전이(검토 요청) 응답")
public record StatusTransitionResponse(
        Long id,
        @Schema(description = "IN_REVIEW|PUBLISHED") String status,
        @Schema(description = "매칭되는 승인 프로세스가 있을 때만 값 존재(IN_REVIEW), 없으면 null(즉시 PUBLISHED)") Long approvalRequestId
) {
}
