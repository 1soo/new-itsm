package com.itsm.esm.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.exception.ErrorResponse;
import com.itsm.esm.application.EsmRequestService;
import com.itsm.esm.application.dto.CommentCreateRequest;
import com.itsm.esm.application.dto.CommentResponse;
import com.itsm.esm.application.dto.CreateRequestRequest;
import com.itsm.esm.application.dto.RequestCreatedResponse;
import com.itsm.esm.application.dto.RequestDetailResponse;
import com.itsm.esm.application.dto.RequestSummaryResponse;
import com.itsm.esm.application.dto.StatusResponse;
import com.itsm.esm.application.dto.StatusTransitionRequest;
import com.itsm.esm.domain.EsmRequestStatus;
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

@Tag(name = "ESM - Requests", description = "부서 요청 API (API-ESM-005~009)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/esm/requests")
public class EsmRequestController {

    private final EsmRequestService requestService;

    public EsmRequestController(EsmRequestService requestService) {
        this.requestService = requestService;
    }

    @Operation(summary = "부서 요청 제출", description = "API-ESM-005")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "필수 필드 미입력/체크리스트 템플릿 미정의", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<RequestCreatedResponse> create(@Valid @RequestBody CreateRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.create(request));
    }

    @Operation(summary = "부서 요청 목록 조회", description = "API-ESM-006 · scope=mine(본인) | all(소속 부서 처리자, DEPT_COORDINATOR)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PageResponse<RequestSummaryResponse>> list(
            @RequestParam(required = false, defaultValue = "mine") String scope,
            @RequestParam(required = false) EsmRequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(requestService.list(scope, status, from, to, pageable));
    }

    @Operation(summary = "부서 요청 상세 조회", description = "API-ESM-007")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RequestDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.detail(id));
    }

    @Operation(summary = "부서 요청 상태 전이(담당 부서 처리자)", description = "API-ESM-008")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전이 성공"),
            @ApiResponse(responseCode = "400", description = "허용되지 않은 전이", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "담당 부서 아님", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<StatusResponse> transition(@PathVariable Long id,
                                                      @Valid @RequestBody StatusTransitionRequest request) {
        return ResponseEntity.ok(requestService.transition(id, request));
    }

    @Operation(summary = "부서 요청 코멘트 등록", description = "API-ESM-009")
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
}
