package com.itsm.compliance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "요구사항 상세 응답")
public record RequirementDetailResponse(
        Long id,
        String requirementKey,
        String name,
        String basis,
        String scope,
        String owner,
        @Schema(description = "COMPLIANT|NON_COMPLIANT(계산값)") String complianceStatus,
        List<CorrectiveActionDto> correctiveActions,
        List<LinkedChange> linkedChanges
) {
    @Schema(description = "시정조치")
    public record CorrectiveActionDto(
            Long id, String description, String status, OffsetDateTime updatedAt,
            @Schema(description = "승인 정보(null=매칭되는 승인 프로세스 없음, 게이트 없이 진행)") ApprovalInfo approval
    ) {
        @Schema(description = "승인 정보")
        public record ApprovalInfo(Long approvalRequestId, String status,
                                    @Schema(description = "원본 코드값(도착 상태, 생성 시점 스냅샷)") String targetState) {
        }
    }

    @Schema(description = "연계 변경 요청")
    public record LinkedChange(Long id, String ticketKey) {
    }
}
