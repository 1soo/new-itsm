package com.itsm.compliance.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.exception.ErrorResponse;
import com.itsm.compliance.application.ComplianceService;
import com.itsm.compliance.application.dto.ComplianceAuditLogResponse;
import com.itsm.compliance.application.dto.ComplianceMetricsResponse;
import com.itsm.compliance.application.dto.CorrectiveActionCreateRequest;
import com.itsm.compliance.application.dto.CorrectiveActionCreatedResponse;
import com.itsm.compliance.application.dto.CorrectiveActionStatusResponse;
import com.itsm.compliance.application.dto.CorrectiveActionStatusTransitionRequest;
import com.itsm.compliance.application.dto.CreateRequirementRequest;
import com.itsm.compliance.application.dto.LinkRequest;
import com.itsm.compliance.application.dto.OwnerRequest;
import com.itsm.compliance.application.dto.OwnerResponse;
import com.itsm.compliance.application.dto.RequirementCreatedResponse;
import com.itsm.compliance.application.dto.RequirementDetailResponse;
import com.itsm.compliance.application.dto.RequirementSummaryResponse;
import com.itsm.compliance.application.dto.UpdateRequirementRequest;
import com.itsm.compliance.domain.ComplianceStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@Tag(name = "Compliance", description = "컴플라이언스 관리 API (API-COMP-001~010). COMPLIANCE_OFFICER 전용.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/compliance")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @Operation(summary = "요구사항 목록 조회", description = "API-COMP-001 · complianceStatus/ownerAssigned/keyword 필터")
    @GetMapping("/requirements")
    public ResponseEntity<PageResponse<RequirementSummaryResponse>> list(
            @RequestParam(required = false) ComplianceStatus complianceStatus,
            @RequestParam(required = false) Boolean ownerAssigned,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(complianceService.list(complianceStatus, ownerAssigned, keyword, pageable));
    }

    @Operation(summary = "요구사항 등록", description = "API-COMP-002")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "이름·근거 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/requirements")
    public ResponseEntity<RequirementCreatedResponse> create(@Valid @RequestBody CreateRequirementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complianceService.create(request));
    }

    @Operation(summary = "요구사항 상세 조회", description = "API-COMP-003")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "요구사항 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/requirements/{id}")
    public ResponseEntity<RequirementDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(complianceService.detail(id));
    }

    @Operation(summary = "요구사항 수정", description = "API-COMP-004")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "이름·근거 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요구사항 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/requirements/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateRequirementRequest request) {
        complianceService.update(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "변경 요청 연계", description = "API-COMP-005")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연계"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 변경 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요구사항 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/requirements/{id}/links")
    public ResponseEntity<Void> link(@PathVariable Long id, @Valid @RequestBody LinkRequest request) {
        complianceService.link(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "책임자 지정", description = "API-COMP-006")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요구사항 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/requirements/{id}/owner")
    public ResponseEntity<OwnerResponse> assignOwner(@PathVariable Long id, @Valid @RequestBody OwnerRequest request) {
        return ResponseEntity.ok(complianceService.assignOwner(id, request));
    }

    @Operation(summary = "시정조치 등록", description = "API-COMP-007")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "내용 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요구사항 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/requirements/{id}/corrective-actions")
    public ResponseEntity<CorrectiveActionCreatedResponse> addCorrectiveAction(
            @PathVariable Long id, @Valid @RequestBody CorrectiveActionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complianceService.addCorrectiveAction(id, request));
    }

    @Operation(summary = "시정조치 상태 전이", description = "API-COMP-008 · DETECTED→IN_PROGRESS→RESOLVED 순차만 허용")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전이 성공"),
            @ApiResponse(responseCode = "400", description = "정의되지 않은 순서의 전이", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "시정조치 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "승인 완료 전 RESOLVED 전이 시도", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/corrective-actions/{actionId}/status")
    public ResponseEntity<CorrectiveActionStatusResponse> transitionCorrectiveAction(
            @PathVariable Long actionId, @Valid @RequestBody CorrectiveActionStatusTransitionRequest request) {
        return ResponseEntity.ok(complianceService.transitionCorrectiveAction(actionId, request));
    }

    @Operation(summary = "컴플라이언스 감사 로그 조회", description = "API-COMP-009 · 컴플라이언스 이벤트만(최소 권한 원칙)")
    @GetMapping("/audit-logs")
    public ResponseEntity<List<ComplianceAuditLogResponse>> auditLogs(
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(complianceService.auditLogs(requirementId, from, to));
    }

    @Operation(summary = "준수 현황 조회", description = "API-COMP-010 · 데이터 없으면 0")
    @GetMapping("/metrics")
    public ResponseEntity<ComplianceMetricsResponse> metrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(complianceService.metrics(from, to));
    }
}
