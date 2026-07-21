package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기사 목록 요약 응답")
public record ArticleSummaryResponse(
        Long id,
        String title,
        String summary,
        String status,
        String category,
        double helpfulRate,
        @Schema(description = "진행 중인 승인 인스턴스의 targetState(원본 코드값, 없으면 null)") String pendingApprovalTargetState
) {
}
