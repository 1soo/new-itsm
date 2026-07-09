package com.itsm.change.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.change.application.ChangeService;
import com.itsm.change.application.dto.ChangeApprovalRequest;
import com.itsm.change.application.dto.ChangeApprovalResponse;
import com.itsm.change.application.dto.ChangeCreatedResponse;
import com.itsm.change.application.dto.ChangeDetailResponse;
import com.itsm.change.application.dto.ChangeMetricsResponse;
import com.itsm.change.application.dto.ChangeSummaryResponse;
import com.itsm.change.application.dto.ClassificationRequest;
import com.itsm.change.application.dto.ClassificationResponse;
import com.itsm.change.application.dto.CreateChangeRequest;
import com.itsm.change.application.dto.LinkRequest;
import com.itsm.change.application.dto.LinkResponse;
import com.itsm.change.application.dto.ResultRequest;
import com.itsm.change.application.dto.ResultResponse;
import com.itsm.change.application.dto.ScheduleItemResponse;
import com.itsm.change.application.dto.StatusResponse;
import com.itsm.change.application.dto.StatusTransitionRequest;
import com.itsm.change.domain.ChangeRisk;
import com.itsm.change.domain.ChangeStatus;
import com.itsm.change.domain.ChangeType;
import com.itsm.common.exception.ErrorResponse;
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
import org.springframework.data.domain.Sort;
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

@Tag(name = "Change", description = "변경 관리 API (API-CHG-001~010). 대부분 CHANGE_MANAGER 전용, 상세 조회는 APPROVER도 가능.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/changes")
public class ChangeController {

    private final ChangeService changeService;

    public ChangeController(ChangeService changeService) {
        this.changeService = changeService;
    }

    @Operation(summary = "변경 목록 조회", description = "API-CHG-001 · type/status/risk/from/to 필터")
    @GetMapping
    public ResponseEntity<PageResponse<ChangeSummaryResponse>> list(
            @RequestParam(required = false) ChangeType type,
            @RequestParam(required = false) ChangeStatus status,
            @RequestParam(required = false) ChangeRisk risk,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(changeService.list(type, status, risk, from, to, pageable));
    }

    @Operation(summary = "변경 요청(RFC) 생성", description = "API-CHG-002")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "요약·유형 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ChangeCreatedResponse> create(@Valid @RequestBody CreateChangeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(changeService.create(request));
    }

    @Operation(summary = "변경 일정(캘린더) 조회", description = "API-CHG-010 · from/to/type 필터, 예정 없으면 빈 배열")
    @GetMapping("/schedule")
    public ResponseEntity<List<ScheduleItemResponse>> schedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) ChangeType type) {
        return ResponseEntity.ok(changeService.schedule(from, to, type));
    }

    @Operation(summary = "변경 지표 조회", description = "API-CHG-012 · successRate/failureRate/emergencyRate, 데이터 없으면 0")
    @GetMapping("/metrics")
    public ResponseEntity<ChangeMetricsResponse> metrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(changeService.metrics(from, to));
    }

    @Operation(summary = "변경 상세 조회", description = "API-CHG-003 · CHANGE_MANAGER/APPROVER")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "변경 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ChangeDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(changeService.detail(id));
    }

    @Operation(summary = "상태(6단계) 전이", description = "API-CHG-004 · REQUESTED→REVIEW→PLANNING→APPROVAL→IMPLEMENTATION→CLOSED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전이 성공(표준 변경은 승인 자동 통과)"),
            @ApiResponse(responseCode = "400", description = "허용되지 않은 전이", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "변경 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "승인 완료 전 구현 전이 시도", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<StatusResponse> transition(@PathVariable Long id,
                                                      @Valid @RequestBody StatusTransitionRequest request) {
        return ResponseEntity.ok(changeService.transition(id, request));
    }

    @Operation(summary = "변경 유형·위험 변경", description = "API-CHG-005 · 위험도 미평가·고위험 시 기본 CAB 경로")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "정의되지 않은 유형", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "변경 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/classification")
    public ResponseEntity<ClassificationResponse> classify(@PathVariable Long id,
                                                            @Valid @RequestBody ClassificationRequest request) {
        return ResponseEntity.ok(changeService.classify(id, request));
    }

    @Operation(summary = "승인/반려", description = "API-CHG-006 · 승인 경로 approver_role(CAB/동료검토→APPROVER) 보유자")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "approver_role 미보유", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "변경/승인 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 결정됨", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/approval")
    public ResponseEntity<ChangeApprovalResponse> approve(@PathVariable Long id,
                                                          @Valid @RequestBody ChangeApprovalRequest request) {
        return ResponseEntity.ok(changeService.decideApproval(id, request));
    }

    @Operation(summary = "구현 결과 기록", description = "API-CHG-008 · 승인되지 않은 변경은 400")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "승인되지 않은 변경", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "변경 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/result")
    public ResponseEntity<ResultResponse> recordResult(@PathVariable Long id, @Valid @RequestBody ResultRequest request) {
        return ResponseEntity.ok(changeService.recordResult(id, request));
    }

    @Operation(summary = "인시던트/문제 연계", description = "API-CHG-009 · 존재하지 않는 대상은 400")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연계"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 대상", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "변경 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> link(@PathVariable Long id, @Valid @RequestBody LinkRequest request) {
        return ResponseEntity.ok(changeService.link(id, request));
    }
}
