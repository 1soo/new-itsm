package com.itsm.esm.presentation;

import com.itsm.auth.domain.Department;
import com.itsm.esm.application.EsmMetricsService;
import com.itsm.esm.application.dto.EsmMetricsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@Tag(name = "ESM - Metrics", description = "ESM 지표 API (API-ESM-017)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/esm/metrics")
public class EsmMetricsController {

    private final EsmMetricsService metricsService;

    public EsmMetricsController(EsmMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Operation(summary = "ESM 지표 조회", description = "API-ESM-017 · 데이터 없으면 0")
    @GetMapping
    public ResponseEntity<EsmMetricsResponse> metrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) Department department) {
        return ResponseEntity.ok(metricsService.compute(from, to, department));
    }
}
