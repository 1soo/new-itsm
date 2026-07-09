package com.itsm.knowledge.presentation;

import com.itsm.knowledge.application.KnowledgeService;
import com.itsm.knowledge.application.dto.PendingReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Knowledge Reviews", description = "검토 대기 목록 API (API-KM-008). Gatekeeper 전용.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/knowledge/reviews")
public class KnowledgeReviewController {

    private final KnowledgeService knowledgeService;

    public KnowledgeReviewController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Operation(summary = "검토 대기 목록(scope=mine)", description = "API-KM-008")
    @GetMapping
    public ResponseEntity<List<PendingReviewResponse>> pending(
            @RequestParam(required = false, defaultValue = "mine") String scope) {
        return ResponseEntity.ok(knowledgeService.pendingReviews());
    }
}
