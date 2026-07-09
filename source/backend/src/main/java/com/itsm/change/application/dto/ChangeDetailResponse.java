package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "변경 상세")
public record ChangeDetailResponse(
        Long id,
        String ticketKey,
        String summary,
        String description,
        String type,
        String risk,
        String status,
        String approvalRoute,
        String implementationPlan,
        String rollbackPlan,
        Result result,
        List<ApprovalDto> approvals,
        List<LinkRef> links,
        @Schema(description = "현재 상태에서 허용되는 다음 상태 목록") List<String> allowedTransitions
) {

    @Schema(description = "구현 결과")
    public record Result(String outcome, Boolean rolledBack, String note) {
    }

    @Schema(description = "승인 이력")
    public record ApprovalDto(String approver, String decision, String opinion, OffsetDateTime at) {
    }

    @Schema(description = "연계 티켓 참조")
    public record LinkRef(String type, String targetKey) {
    }
}
