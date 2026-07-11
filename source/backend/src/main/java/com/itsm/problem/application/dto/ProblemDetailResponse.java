package com.itsm.problem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "문제 상세")
public record ProblemDetailResponse(
        Long id,
        String ticketKey,
        String summary,
        String description,
        String status,
        String priority,
        String impact,
        String urgency,
        Rca rca,
        String workaround,
        @Schema(description = "승인 정보(null=매칭되는 승인 프로세스 없음, 게이트 없이 진행)") ApprovalInfo approval,
        List<LinkRef> linkedIncidents,
        List<LinkRef> linkedChanges,
        List<LinkRef> linkedAssets,
        List<ActionDto> actions,
        @Schema(description = "현재 상태에서 허용되는 다음 상태 목록") List<String> allowedTransitions
) {

    @Schema(description = "근본 원인 분석(RCA)")
    public record Rca(String rootCause, List<String> fiveWhys, String category) {
    }

    @Schema(description = "승인 정보")
    public record ApprovalInfo(Long approvalRequestId, String status) {
    }

    @Schema(description = "연계 티켓/자산 참조")
    public record LinkRef(Long id, String ticketKey) {
    }

    @Schema(description = "후속 조치")
    public record ActionDto(Long id, String description, String status) {
    }
}
