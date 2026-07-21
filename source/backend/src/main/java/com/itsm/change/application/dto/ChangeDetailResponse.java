package com.itsm.change.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
        String implementationPlan,
        String rollbackPlan,
        Result result,
        @Schema(description = "승인 정보(null=매칭되는 승인 프로세스 없음, 게이트 없이 진행)") ApprovalInfo approval,
        List<LinkRef> links,
        @Schema(description = "현재 상태에서 허용되는 다음 상태 목록") List<String> allowedTransitions
) {

    @Schema(description = "구현 결과")
    public record Result(String outcome, Boolean rolledBack, String note) {
    }

    @Schema(description = "승인 정보")
    public record ApprovalInfo(Long approvalRequestId, String status,
                                @Schema(description = "원본 코드값(도착 상태, 생성 시점 스냅샷)") String targetState) {
    }

    @Schema(description = "연계 티켓 참조")
    public record LinkRef(String type, String targetKey) {
    }
}
