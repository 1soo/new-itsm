package com.itsm.srm.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.exception.ErrorResponse;
import com.itsm.srm.application.MetricsService;
import com.itsm.srm.application.ServiceRequestService;
import com.itsm.srm.application.dto.AssignRequest;
import com.itsm.srm.application.dto.AssigneeCandidateResponse;
import com.itsm.srm.application.dto.CommentCreateRequest;
import com.itsm.srm.application.dto.CommentResponse;
import com.itsm.srm.application.dto.CreateRequestRequest;
import com.itsm.srm.application.dto.CsatRequest;
import com.itsm.srm.application.dto.CsatResponse;
import com.itsm.srm.application.dto.MetricsResponse;
import com.itsm.srm.application.dto.RequestCreatedResponse;
import com.itsm.srm.application.dto.RequestDetailResponse;
import com.itsm.srm.application.dto.RequestSummaryResponse;
import com.itsm.srm.application.dto.StatusResponse;
import com.itsm.srm.application.dto.StatusTransitionRequest;
import com.itsm.srm.domain.RequestStatus;
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
import org.springframework.security.access.prepost.PreAuthorize;
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

@Tag(name = "SRM - Service Requests", description = "서비스 요청 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService requestService;
    private final MetricsService metricsService;

    public ServiceRequestController(ServiceRequestService requestService, MetricsService metricsService) {
        this.requestService = requestService;
        this.metricsService = metricsService;
    }

    @Operation(summary = "요청 생성(제출)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "필수 필드 미입력", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<RequestCreatedResponse> create(@Valid @RequestBody CreateRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.create(request));
    }

    @Operation(summary = "요청 목록 조회", description = "scope=mine(본인) | all|queue(상담원 이상)")
    @GetMapping
    public ResponseEntity<PageResponse<RequestSummaryResponse>> list(
            @RequestParam(required = false, defaultValue = "mine") String scope,
            @RequestParam(required = false) Long queue,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(requestService.list(scope, queue, status, from, to, pageable));
    }

    @Operation(summary = "요청 지표 조회(PROCESS_OWNER)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상(데이터 없으면 0값)"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('PROCESS_OWNER')")
    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> metrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(metricsService.compute(from, to));
    }

    @Operation(summary = "요청 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RequestDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.detail(id));
    }

    @Operation(summary = "담당자 배정 후보 조회(Agent)", description = "카탈로그 항목에 담당자 역할이 지정되지 않았으면 빈 배열")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상(후보 없으면 빈 배열)"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('SERVICE_DESK_AGENT')")
    @GetMapping("/{id}/assignee-candidates")
    public ResponseEntity<List<AssigneeCandidateResponse>> assigneeCandidates(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.assigneeCandidates(id));
    }

    @Operation(summary = "요청 담당자 배정(Agent)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "권한 없는 배정", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('SERVICE_DESK_AGENT')")
    @PostMapping("/{id}/assign")
    public ResponseEntity<RequestDetailResponse> assign(@PathVariable Long id,
                                                        @RequestBody(required = false) AssignRequest request) {
        return ResponseEntity.ok(requestService.assign(id, request));
    }

    @Operation(summary = "요청 상태 전이(전이별 권한 상이)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전이 성공"),
            @ApiResponse(responseCode = "400", description = "허용되지 않은 전이/재종료", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "승인 대기 중 이행 시도", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<StatusResponse> transition(@PathVariable Long id,
                                                     @Valid @RequestBody StatusTransitionRequest request) {
        return ResponseEntity.ok(requestService.transition(id, request));
    }

    @Operation(summary = "요청 코멘트 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> comment(@PathVariable Long id,
                                                   @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.addComment(id, request));
    }

    @Operation(summary = "CSAT 제출(요청자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "종료되지 않은 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "요청자 아님", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/csat")
    public ResponseEntity<CsatResponse> csat(@PathVariable Long id, @Valid @RequestBody CsatRequest request) {
        return ResponseEntity.ok(requestService.submitCsat(id, request));
    }
}
