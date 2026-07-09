package com.itsm.srm.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "요청 상세")
public record RequestDetailResponse(
        Long id,
        String ticketKey,
        String catalogItemName,
        String status,
        Map<String, Object> formValues,
        String requester,
        String assignee,
        String queue,
        ApprovalInfo approval,
        SlaInfo sla,
        List<LinkedArticle> linkedArticles,
        List<LinkedAsset> linkedAssets,
        List<CommentResponse> comments,
        List<TimelineEntry> timeline,
        @Schema(description = "현재 상태·역할·승인 기준 수행 가능한 상태 전이 target 목록") List<String> allowedTransitions
) {
    @Schema(description = "승인 정보")
    public record ApprovalInfo(boolean required, String status, String reason) {
    }

    @Schema(description = "SLA 상태")
    public record SlaInfo(String responseStatus, String resolveStatus) {
    }

    @Schema(description = "연결 지식 기사")
    public record LinkedArticle(Long articleId, String title) {
    }

    @Schema(description = "연결 자산(REQ-ITAM-006)")
    public record LinkedAsset(Long id, String assetKey) {
    }

    @Schema(description = "타임라인 항목")
    public record TimelineEntry(String type, String message, OffsetDateTime at) {
    }
}
