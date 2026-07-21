package com.itsm.common.approval.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.approval.application.ApprovalGateService;
import com.itsm.common.approval.application.ApprovalInstanceService;
import com.itsm.common.approval.application.dto.ApprovalDecisionRequest;
import com.itsm.common.approval.application.dto.ApprovalDecisionResultResponse;
import com.itsm.common.approval.application.dto.ApprovalDetailResponse;
import com.itsm.common.approval.application.dto.ApprovalInboxItemResponse;
import com.itsm.common.approval.application.dto.ApprovalResubmitRequest;
import com.itsm.common.approval.application.dto.ApprovalResubmitResponse;
import com.itsm.common.exception.ErrorResponse;
import com.itsm.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Approvals", description = "전 도메인 공용 승인 대기함·상세·결정 API (API-COM-003~005)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

    private final ApprovalInstanceService approvalInstanceService;
    private final ApprovalGateService approvalGateService;

    public ApprovalController(ApprovalInstanceService approvalInstanceService, ApprovalGateService approvalGateService) {
        this.approvalInstanceService = approvalInstanceService;
        this.approvalGateService = approvalGateService;
    }

    @Operation(summary = "승인 대기함 목록 조회", description = "API-COM-003 · scope=mine 고정(역할 기반 공유 대기함), domain 선택")
    @GetMapping
    public ResponseEntity<PageResponse<ApprovalInboxItemResponse>> inbox(
            @RequestParam(required = false, defaultValue = "mine") String scope,
            @RequestParam(required = false) String domain,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(approvalInstanceService.inbox(domain, page, size));
    }

    @Operation(summary = "승인 인스턴스 상세 조회", description = "API-COM-004 · 차수별 진행 상태(역할별 결정 현황 포함)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "인스턴스 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{approvalRequestId}")
    public ResponseEntity<ApprovalDetailResponse> detail(@PathVariable Long approvalRequestId) {
        return ResponseEntity.ok(approvalInstanceService.detail(approvalRequestId));
    }

    @Operation(summary = "승인/반려 결정", description = "API-COM-005 · 현재 대기 차수에 필요한 역할 보유자만 처리 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결정 반영"),
            @ApiResponse(responseCode = "400", description = "REJECT인데 사유 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "현재 대기 차수에 필요한 역할 미보유", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "인스턴스 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 결정된 역할 슬롯 재처리 또는 이미 종료된 인스턴스", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{approvalRequestId}/decisions")
    public ResponseEntity<ApprovalDecisionResultResponse> decide(@PathVariable Long approvalRequestId,
                                                                  @Valid @RequestBody ApprovalDecisionRequest request) {
        return ResponseEntity.ok(approvalInstanceService.decide(approvalRequestId, request.decision(), request.reason()));
    }

    @Operation(summary = "반려 후 재승인요청", description = "API-COM-006 · 최신 인스턴스가 REJECTED인 티켓에 대해 현재 호출자 기준으로 재매칭 후 새 인스턴스 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재승인요청 접수(새 인스턴스 생성 또는 매칭 규칙 소멸 안내)"),
            @ApiResponse(responseCode = "400", description = "최신 인스턴스 상태가 REJECTED가 아님", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "대상 티켓의 승인 인스턴스 자체가 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/resubmit")
    public ResponseEntity<ApprovalResubmitResponse> resubmit(@Valid @RequestBody ApprovalResubmitRequest request) {
        Long requesterId = SecurityUtils.currentPrincipal().userId();
        return ResponseEntity.ok(approvalGateService.resubmit(request.ticketType(), request.ticketId(), requesterId));
    }
}
