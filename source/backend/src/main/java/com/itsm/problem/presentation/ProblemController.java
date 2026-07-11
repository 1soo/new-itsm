package com.itsm.problem.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.exception.ErrorResponse;
import com.itsm.problem.application.ProblemService;
import com.itsm.problem.application.dto.ActionCreateRequest;
import com.itsm.problem.application.dto.ActionResponse;
import com.itsm.problem.application.dto.ActionStatusRequest;
import com.itsm.problem.application.dto.CloseRequest;
import com.itsm.problem.application.dto.CloseResponse;
import com.itsm.problem.application.dto.CreateProblemRequest;
import com.itsm.problem.application.dto.KnownErrorCreateRequest;
import com.itsm.problem.application.dto.KnownErrorCreatedResponse;
import com.itsm.problem.application.dto.LinkRequest;
import com.itsm.problem.application.dto.LinkResponse;
import com.itsm.problem.application.dto.ProblemCreatedResponse;
import com.itsm.problem.application.dto.ProblemDetailResponse;
import com.itsm.problem.application.dto.ProblemSummaryResponse;
import com.itsm.problem.application.dto.RcaRequest;
import com.itsm.problem.application.dto.RcaResponse;
import com.itsm.problem.application.dto.StatusResponse;
import com.itsm.problem.application.dto.StatusTransitionRequest;
import com.itsm.problem.application.dto.WorkaroundRequest;
import com.itsm.problem.application.dto.WorkaroundResponse;
import com.itsm.problem.domain.ProblemOrigin;
import com.itsm.problem.domain.ProblemPriority;
import com.itsm.problem.domain.ProblemStatus;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@Tag(name = "Problem", description = "문제 관리 API (API-PRB-001~012). 모든 API는 PROBLEM_MANAGER 전용.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @Operation(summary = "문제 목록 조회", description = "API-PRB-001 · status/priority/origin/assignee/from/to 필터")
    @GetMapping
    public ResponseEntity<PageResponse<ProblemSummaryResponse>> list(
            @RequestParam(required = false) ProblemStatus status,
            @RequestParam(required = false) ProblemPriority priority,
            @RequestParam(required = false) ProblemOrigin origin,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(problemService.list(status, priority, origin, assignee, from, to, pageable));
    }

    @Operation(summary = "문제 등록", description = "API-PRB-002 · 영향도·긴급도 중 하나라도 없으면 priority=null")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "요약 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ProblemCreatedResponse> create(@Valid @RequestBody CreateProblemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(problemService.create(request));
    }

    @Operation(summary = "문제 상세 조회", description = "API-PRB-003")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "문제 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProblemDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.detail(id));
    }

    @Operation(summary = "상태(6단계) 전이", description = "API-PRB-004 · DETECTION→CLASSIFICATION→INVESTIGATION→KNOWN_ERROR→WORKAROUND→RESOLVED_CLOSED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전이 성공"),
            @ApiResponse(responseCode = "400", description = "순서 어긋난 전이", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문제 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "승인 완료 전 RESOLVED_CLOSED 전이 시도", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<StatusResponse> transition(@PathVariable Long id,
                                                     @Valid @RequestBody StatusTransitionRequest request) {
        return ResponseEntity.ok(problemService.transition(id, request));
    }

    @Operation(summary = "RCA 작성/수정", description = "API-PRB-005 · 개인(사람)을 근본 원인으로 강제하지 않음")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문제 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}/rca")
    public ResponseEntity<RcaResponse> saveRca(@PathVariable Long id, @RequestBody RcaRequest request) {
        return ResponseEntity.ok(problemService.saveRca(id, request));
    }

    @Operation(summary = "워크어라운드 등록", description = "API-PRB-006 · 내용 필수, 지식 문서 연결 선택")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "내용 빈 값", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문제 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/workaround")
    public ResponseEntity<WorkaroundResponse> addWorkaround(@PathVariable Long id,
                                                            @RequestBody WorkaroundRequest request) {
        return ResponseEntity.ok(problemService.addWorkaround(id, request));
    }

    @Operation(summary = "알려진 오류(KE) 생성", description = "API-PRB-007 · KEDB 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "제목 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문제 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/known-errors")
    public ResponseEntity<KnownErrorCreatedResponse> createKnownError(@PathVariable Long id,
                                                                      @Valid @RequestBody KnownErrorCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(problemService.createKnownError(id, request));
    }

    @Operation(summary = "인시던트/변경 연계", description = "API-PRB-009 · INCIDENT/CHANGE 연계(양방향). CHANGE는 기존 연계 또는 createNewChange=true로 신규 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연계"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 대상", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문제 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> link(@PathVariable Long id, @Valid @RequestBody LinkRequest request) {
        return ResponseEntity.ok(problemService.link(id, request));
    }

    @Operation(summary = "후속 조치 등록", description = "API-PRB-010")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "내용 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문제 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/actions")
    public ResponseEntity<ActionResponse> addAction(@PathVariable Long id,
                                                    @Valid @RequestBody ActionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(problemService.addAction(id, request));
    }

    @Operation(summary = "후속 조치 상태 변경", description = "API-PRB-011 · IN_PROGRESS|DONE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "정의되지 않은 값", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문제/조치 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/actions/{actionId}")
    public ResponseEntity<ActionResponse> changeActionStatus(@PathVariable Long id, @PathVariable Long actionId,
                                                             @Valid @RequestBody ActionStatusRequest request) {
        return ResponseEntity.ok(problemService.changeActionStatus(id, actionId, request));
    }

    @Operation(summary = "문제 종료", description = "API-PRB-012 · 미해결 후속조치 있으면 warning. force=true면 강제 종료")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상(경고 포함 가능)"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "문제 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/close")
    public ResponseEntity<CloseResponse> close(@PathVariable Long id, @RequestBody CloseRequest request) {
        return ResponseEntity.ok(problemService.close(id, request));
    }
}
