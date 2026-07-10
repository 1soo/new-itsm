package com.itsm.infra.presentation;

import com.itsm.common.exception.ErrorResponse;
import com.itsm.infra.application.InfraMonitoringService;
import com.itsm.infra.application.dto.AlertResponse;
import com.itsm.infra.application.dto.CapacityPlanCreateRequest;
import com.itsm.infra.application.dto.CapacityPlanCreatedResponse;
import com.itsm.infra.application.dto.CapacityPlanResponse;
import com.itsm.infra.application.dto.InfraReportResponse;
import com.itsm.infra.application.dto.MetricCreateRequest;
import com.itsm.infra.application.dto.MetricCreatedResponse;
import com.itsm.infra.application.dto.MetricResponse;
import com.itsm.infra.application.dto.ThresholdResponse;
import com.itsm.infra.application.dto.ThresholdUpdateRequest;
import com.itsm.infra.application.dto.UptimeStatusResponse;
import com.itsm.infra.application.dto.UptimeTargetRequest;
import com.itsm.infra.domain.MetricType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.List;

@Tag(name = "Infra Monitoring", description = "IT 인프라 모니터링 & 용량관리 API (API-IOM-001~011). INFRA_OPERATOR 전용.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/infra")
public class InfraMonitoringController {

    private final InfraMonitoringService infraMonitoringService;

    public InfraMonitoringController(InfraMonitoringService infraMonitoringService) {
        this.infraMonitoringService = infraMonitoringService;
    }

    @Operation(summary = "지표 등록", description = "API-IOM-001 · 전역 임계치 초과 시 알림 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "자산·지표값 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 자산", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/metrics")
    public ResponseEntity<MetricCreatedResponse> registerMetric(@Valid @RequestBody MetricCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(infraMonitoringService.registerMetric(request));
    }

    @Operation(summary = "지표 시계열 조회", description = "API-IOM-002 · assetId/metricType/from/to 필터")
    @GetMapping("/metrics")
    public ResponseEntity<List<MetricResponse>> listMetrics(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) MetricType metricType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(infraMonitoringService.listMetrics(assetId, metricType, from, to));
    }

    @Operation(summary = "인프라 지표 리포팅 조회", description = "API-IOM-011 · 데이터 없으면 0")
    @GetMapping("/metrics/report")
    public ResponseEntity<InfraReportResponse> report(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) Long assetId) {
        return ResponseEntity.ok(infraMonitoringService.report(from, to, assetId));
    }

    @Operation(summary = "임계치 목록 조회", description = "API-IOM-003")
    @GetMapping("/metric-thresholds")
    public ResponseEntity<List<ThresholdResponse>> listThresholds() {
        return ResponseEntity.ok(infraMonitoringService.listThresholds());
    }

    @Operation(summary = "임계치 설정", description = "API-IOM-004 · upsert")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "정의되지 않은 지표 항목", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/metric-thresholds/{metricType}")
    public ResponseEntity<Void> setThreshold(@PathVariable MetricType metricType,
                                              @RequestBody ThresholdUpdateRequest request) {
        infraMonitoringService.setThreshold(metricType, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "임계치 초과 알림 목록 조회", description = "API-IOM-005 · assetId/acknowledged 필터")
    @GetMapping("/metric-alerts")
    public ResponseEntity<List<AlertResponse>> listAlerts(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) Boolean acknowledged) {
        return ResponseEntity.ok(infraMonitoringService.listAlerts(assetId, acknowledged));
    }

    @Operation(summary = "임계치 초과 알림 확인 처리", description = "API-IOM-006")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "알림 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/metric-alerts/{id}/acknowledge")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable Long id) {
        infraMonitoringService.acknowledgeAlert(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자산 가동률 목표 설정", description = "API-IOM-007 · upsert")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "목표값 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 자산", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/assets/{assetId}/uptime-target")
    public ResponseEntity<Void> setUptimeTarget(@PathVariable Long assetId,
                                                 @Valid @RequestBody UptimeTargetRequest request) {
        infraMonitoringService.setUptimeTarget(assetId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자산 가동률 현황 조회", description = "API-IOM-008 · actualPercentage는 조회 시점 계산값, 목표 없으면 met=null")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 자산", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/assets/{assetId}/uptime")
    public ResponseEntity<UptimeStatusResponse> getUptimeStatus(
            @PathVariable Long assetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(infraMonitoringService.getUptimeStatus(assetId, from, to));
    }

    @Operation(summary = "용량 계획 등록", description = "API-IOM-009")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "역량·수요 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/capacity-plans")
    public ResponseEntity<CapacityPlanCreatedResponse> createCapacityPlan(
            @Valid @RequestBody CapacityPlanCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(infraMonitoringService.createCapacityPlan(request));
    }

    @Operation(summary = "용량 계획 목록 조회", description = "API-IOM-010 · utilizationRate는 조회 시점 계산값")
    @GetMapping("/capacity-plans")
    public ResponseEntity<List<CapacityPlanResponse>> listCapacityPlans() {
        return ResponseEntity.ok(infraMonitoringService.listCapacityPlans());
    }
}
