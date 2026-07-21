package com.itsm.asset.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "자산 상세")
public record AssetDetailResponse(
        Long id,
        String assetKey,
        String name,
        String type,
        String status,
        String owner,
        String location,
        Map<String, String> attributes,
        Expiry expiry,
        List<LifecycleEntry> lifecycleHistory,
        @Schema(description = "승인 정보(null=매칭되는 승인 프로세스 없음, 게이트 없이 진행)") ApprovalInfo approval,
        List<LinkedTicket> linkedTickets,
        List<LinkedCi> linkedCis
) {
    @Schema(description = "만료일 3종(임박/경과 상태 포함)")
    public record Expiry(ExpiryDate license, ExpiryDate warranty, ExpiryDate contract) {
    }

    @Schema(description = "승인 정보")
    public record ApprovalInfo(Long approvalRequestId, String status,
                                @Schema(description = "원본 코드값(도착 상태, 생성 시점 스냅샷)") String targetState) {
    }

    @Schema(description = "만료일과 상태(OK/EXPIRING/EXPIRED, 날짜 없으면 상태도 null)")
    public record ExpiryDate(LocalDate date, String status) {
    }

    @Schema(description = "생애주기 이력 항목")
    public record LifecycleEntry(String stage, OffsetDateTime at) {
    }

    @Schema(description = "연계 티켓")
    public record LinkedTicket(String type, String ticketKey) {
    }

    @Schema(description = "연결된 CI")
    public record LinkedCi(Long ciId, String name) {
    }
}
