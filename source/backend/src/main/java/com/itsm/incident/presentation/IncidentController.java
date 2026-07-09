package com.itsm.incident.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.exception.ErrorResponse;
import com.itsm.incident.application.IncidentService;
import com.itsm.incident.application.dto.AssignRoleRequest;
import com.itsm.incident.application.dto.CreateIncidentRequest;
import com.itsm.incident.application.dto.EscalateRequest;
import com.itsm.incident.application.dto.EscalateResponse;
import com.itsm.incident.application.dto.IncidentCreatedResponse;
import com.itsm.incident.application.dto.IncidentDetailResponse;
import com.itsm.incident.application.dto.IncidentMetricsResponse;
import com.itsm.incident.application.dto.IncidentSummaryResponse;
import com.itsm.incident.application.dto.LinkProblemRequest;
import com.itsm.incident.application.dto.LinkResponse;
import com.itsm.incident.application.dto.PostmortemRequest;
import com.itsm.incident.application.dto.PostmortemResponse;
import com.itsm.incident.application.dto.ResolveRequest;
import com.itsm.incident.application.dto.ResolveResponse;
import com.itsm.incident.application.dto.SeverityChangeRequest;
import com.itsm.incident.application.dto.SeverityChangeResponse;
import com.itsm.incident.application.dto.StatusResponse;
import com.itsm.incident.application.dto.StatusTransitionRequest;
import com.itsm.incident.application.dto.TimelineUpdateRequest;
import com.itsm.incident.application.dto.TimelineUpdateResponse;
import com.itsm.incident.domain.IncidentStatus;
import com.itsm.incident.domain.Severity;
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

@Tag(name = "Incident", description = "인시던트 관리 API (API-INC-001~013)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @Operation(summary = "인시던트 등록", description = "API-INC-002")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "요약·심각도 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<IncidentCreatedResponse> create(@Valid @RequestBody CreateIncidentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.create(request));
    }

    @Operation(summary = "인시던트 목록 조회", description = "API-INC-001 · status/severity/assignee/keyword/from/to 필터")
    @GetMapping
    public ResponseEntity<PageResponse<IncidentSummaryResponse>> list(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) Long assignee,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(incidentService.list(status, severity, assignee, keyword, from, to, pageable));
    }

    @Operation(summary = "인시던트 지표 조회", description = "API-INC-013 · 건수·심각도 분포·평균 MTTR")
    @GetMapping("/metrics")
    public ResponseEntity<IncidentMetricsResponse> metrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(incidentService.metrics(from, to));
    }

    @Operation(summary = "인시던트 상세 조회", description = "API-INC-003")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "인시던트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<IncidentDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.detail(id));
    }

    @Operation(summary = "심각도·우선순위 변경", description = "API-INC-004 · INCIDENT_MANAGER 전용, 변경 이력 기록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "정의되지 않은 값", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "인시던트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/severity")
    public ResponseEntity<SeverityChangeResponse> changeSeverity(@PathVariable Long id,
                                                                 @RequestBody SeverityChangeRequest request) {
        return ResponseEntity.ok(incidentService.changeSeverity(id, request));
    }

    @Operation(summary = "상태 전이", description = "API-INC-005 · NEW→IN_PROGRESS→RESOLVED→CLOSED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전이 성공"),
            @ApiResponse(responseCode = "400", description = "허용되지 않은 전이", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "인시던트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<StatusResponse> transition(@PathVariable Long id,
                                                     @Valid @RequestBody StatusTransitionRequest request) {
        return ResponseEntity.ok(incidentService.transition(id, request));
    }

    @Operation(summary = "대응 역할 배정", description = "API-INC-006 · INCIDENT_MANAGER 전용")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "대상 사용자 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "IM 권한 아님", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "인시던트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/roles")
    public ResponseEntity<IncidentDetailResponse> assignRole(@PathVariable Long id,
                                                             @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(incidentService.assignRole(id, request));
    }

    @Operation(summary = "에스컬레이션", description = "API-INC-007 · HIERARCHICAL/FUNCTIONAL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "대상 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "인시던트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/escalate")
    public ResponseEntity<EscalateResponse> escalate(@PathVariable Long id,
                                                     @Valid @RequestBody EscalateRequest request) {
        return ResponseEntity.ok(incidentService.escalate(id, request));
    }

    @Operation(summary = "상태 업데이트(타임라인)", description = "API-INC-008 · INTERNAL/EXTERNAL")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "인시던트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/updates")
    public ResponseEntity<TimelineUpdateResponse> addUpdate(@PathVariable Long id,
                                                            @Valid @RequestBody TimelineUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.addUpdate(id, request));
    }

    @Operation(summary = "해결 처리·시간 지표", description = "API-INC-009 · MTTD/MTTA/MTTR(시각 없으면 null)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "인시던트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ResolveResponse> resolve(@PathVariable Long id, @RequestBody ResolveRequest request) {
        return ResponseEntity.ok(incidentService.resolve(id, request));
    }

    @Operation(summary = "포스트모템 조회", description = "API-INC-010")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "미작성/인시던트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/postmortem")
    public ResponseEntity<PostmortemResponse> getPostmortem(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getPostmortem(id));
    }

    @Operation(summary = "포스트모템 작성/수정", description = "API-INC-011 · INCIDENT_MANAGER 전용, rootCause 필수")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "근본원인 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "인시던트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}/postmortem")
    public ResponseEntity<PostmortemResponse> savePostmortem(@PathVariable Long id,
                                                             @RequestBody PostmortemRequest request) {
        return ResponseEntity.ok(incidentService.savePostmortem(id, request));
    }

    @Operation(summary = "문제 연계(링크)", description = "API-INC-012 · 기존 문제 연계 또는 신규 문제 생성(양방향 ticket_link)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연계"),
            @ApiResponse(responseCode = "400", description = "연계 대상 미지정/문제 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "인시던트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> linkProblem(@PathVariable Long id, @RequestBody LinkProblemRequest request) {
        return ResponseEntity.ok(incidentService.linkProblem(id, request));
    }
}
