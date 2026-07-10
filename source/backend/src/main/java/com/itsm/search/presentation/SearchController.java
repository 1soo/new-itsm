package com.itsm.search.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.search.application.SearchService;
import com.itsm.search.application.dto.SearchResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search", description = "통합 검색 API (API-SEARCH-001)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @Operation(summary = "통합 검색(지식+티켓 교차 도메인)",
            description = "API-SEARCH-001 · 역할별 접근 가능 도메인만 결과 포함, updatedAt 내림차순 단일 정렬")
    @GetMapping
    public ResponseEntity<PageResponse<SearchResultResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(searchService.search(keyword, pageable));
    }
}
