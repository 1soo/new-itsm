package com.itsm.esm.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.exception.ErrorResponse;
import com.itsm.esm.application.EsmHrCaseService;
import com.itsm.esm.application.dto.CreateHrCaseRequest;
import com.itsm.esm.application.dto.HrCaseCreatedResponse;
import com.itsm.esm.application.dto.HrCaseDetailResponse;
import com.itsm.esm.application.dto.HrCaseStatusTransitionRequest;
import com.itsm.esm.application.dto.HrCaseSummaryResponse;
import com.itsm.esm.application.dto.StatusResponse;
import com.itsm.esm.domain.HrCaseStatus;
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

@Tag(name = "ESM - HR Cases", description = "HR 케이스 API (API-ESM-010~013). HR_CASE_MANAGER 전용(SYSTEM_ADMIN 포함 그 외 전부 403).")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/esm/hr-cases")
public class EsmHrCaseController {

    private final EsmHrCaseService hrCaseService;

    public EsmHrCaseController(EsmHrCaseService hrCaseService) {
        this.hrCaseService = hrCaseService;
    }

    @Operation(summary = "HR 케이스 접수", description = "API-ESM-010")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "제목 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "HR 역할 아님", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<HrCaseCreatedResponse> create(@Valid @RequestBody CreateHrCaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hrCaseService.create(request));
    }

    @Operation(summary = "HR 케이스 목록 조회", description = "API-ESM-011")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "HR 역할 아님", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PageResponse<HrCaseSummaryResponse>> list(
            @RequestParam(required = false) HrCaseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(hrCaseService.list(status, pageable));
    }

    @Operation(summary = "HR 케이스 상세 조회", description = "API-ESM-012")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "HR 역할 아님", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "케이스 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<HrCaseDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(hrCaseService.detail(id));
    }

    @Operation(summary = "HR 케이스 상태 전이", description = "API-ESM-013 · 접수→기록→조사→해결 순서만 허용")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전이 성공"),
            @ApiResponse(responseCode = "400", description = "정의된 순서 외 전이", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "HR 역할 아님", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "케이스 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<StatusResponse> transition(@PathVariable Long id,
                                                      @Valid @RequestBody HrCaseStatusTransitionRequest request) {
        return ResponseEntity.ok(hrCaseService.transition(id, request));
    }
}
