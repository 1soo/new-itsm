package com.itsm.knowledge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "기사 상세/열람")
public record ArticleDetailResponse(
        Long id,
        String title,
        String body,
        String status,
        String category,
        List<String> labels,
        int helpful,
        int notHelpful,
        @Schema(description = "승인 정보(null=매칭되는 승인 프로세스 없음)") ApprovalInfo approval
) {
    @Schema(description = "승인 정보")
    public record ApprovalInfo(Long approvalRequestId, String status) {
    }
}
