package com.itsm.knowledge.presentation;

import com.itsm.knowledge.application.KnowledgeService;
import com.itsm.knowledge.application.dto.KnowledgeMetricsResponse;
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

@Tag(name = "Knowledge Metrics", description = "지식 지표 API (API-KM-012). Gatekeeper 전용.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/knowledge/metrics")
public class KnowledgeMetricsController {

    private final KnowledgeService knowledgeService;

    public KnowledgeMetricsController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Operation(summary = "지식 지표 조회", description = "API-KM-012 · usageCount/noResultSearchCount/helpfulRate/deflectionRate/topNoResultKeywords")
    @GetMapping
    public ResponseEntity<KnowledgeMetricsResponse> metrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(knowledgeService.metrics(from, to));
    }
}
