package com.itsm.problem.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.problem.application.ProblemService;
import com.itsm.problem.application.dto.KnownErrorSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Known Error (KEDB)", description = "알려진 오류 데이터베이스 검색 (API-PRB-008). PROBLEM_MANAGER 전용.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/known-errors")
public class KnownErrorController {

    private final ProblemService problemService;

    public KnownErrorController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @Operation(summary = "KEDB 검색", description = "API-PRB-008 · title/root_cause 키워드 검색. 매칭 없으면 빈 목록.")
    @ApiResponse(responseCode = "200", description = "정상")
    @GetMapping
    public ResponseEntity<PageResponse<KnownErrorSearchResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(problemService.searchKnownErrors(keyword, pageable));
    }
}
