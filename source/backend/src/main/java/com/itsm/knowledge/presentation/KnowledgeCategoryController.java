package com.itsm.knowledge.presentation;

import com.itsm.knowledge.application.KnowledgeService;
import com.itsm.knowledge.application.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Knowledge Categories", description = "카테고리 목록 API (API-KM-010).")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/knowledge/categories")
public class KnowledgeCategoryController {

    private final KnowledgeService knowledgeService;

    public KnowledgeCategoryController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Operation(summary = "카테고리 목록", description = "API-KM-010")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(knowledgeService.categories());
    }
}
