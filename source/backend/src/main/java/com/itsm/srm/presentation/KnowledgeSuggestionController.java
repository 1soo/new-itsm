package com.itsm.srm.presentation;

import com.itsm.srm.application.ServiceCatalogService;
import com.itsm.srm.application.dto.KnowledgeSuggestionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "SRM - Knowledge Suggestion", description = "지식 기사 추천 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/knowledge/suggestions")
public class KnowledgeSuggestionController {

    private final ServiceCatalogService catalogService;

    public KnowledgeSuggestionController(ServiceCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "지식 기사 추천", description = "KM 도메인 미구축 상태에서는 빈 배열을 반환한다.")
    @GetMapping
    public ResponseEntity<List<KnowledgeSuggestionResponse>> suggestions(
            @RequestParam(required = false) Long catalogItemId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(catalogService.suggestions(catalogItemId, keyword));
    }
}
