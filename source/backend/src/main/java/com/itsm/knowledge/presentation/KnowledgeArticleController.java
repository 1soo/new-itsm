package com.itsm.knowledge.presentation;

import com.itsm.common.exception.ErrorResponse;
import com.itsm.knowledge.application.KnowledgeService;
import com.itsm.knowledge.application.dto.ArticleCreatedResponse;
import com.itsm.knowledge.application.dto.ArticleDetailResponse;
import com.itsm.knowledge.application.dto.ArticleListResponse;
import com.itsm.knowledge.application.dto.CreateArticleRequest;
import com.itsm.knowledge.application.dto.FeedbackRequest;
import com.itsm.knowledge.application.dto.FeedbackResponse;
import com.itsm.knowledge.application.dto.LinkArticleRequest;
import com.itsm.knowledge.application.dto.LinkArticleResponse;
import com.itsm.knowledge.application.dto.StatusResponse;
import com.itsm.knowledge.application.dto.StatusTransitionRequest;
import com.itsm.knowledge.application.dto.StatusTransitionResponse;
import com.itsm.knowledge.application.dto.UpdateArticleRequest;
import com.itsm.knowledge.domain.ArticleStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Knowledge", description = "지식 관리 API (API-KM-001~006,009,011). 작성/수정/삭제/검토요청/연계는 CONTRIBUTOR 전용. "
        + "검토승인/반려는 공용 승인 대기함(common.approval, API-COM-003~005)이 담당")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/knowledge/articles")
public class KnowledgeArticleController {

    private final KnowledgeService knowledgeService;

    public KnowledgeArticleController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Operation(summary = "기사 검색/목록", description = "API-KM-001 · keyword/category/label/status 필터. 최종 사용자는 게시 기사만 반환")
    @GetMapping
    public ResponseEntity<ArticleListResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) ArticleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewCount"));
        return ResponseEntity.ok(knowledgeService.search(keyword, category, label, status, pageable));
    }

    @Operation(summary = "기사 작성", description = "API-KM-003 · Contributor 전용")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "제목·본문 누락/존재하지 않는 카테고리", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ArticleCreatedResponse> create(@Valid @RequestBody CreateArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(knowledgeService.create(request));
    }

    @Operation(summary = "기사 상세/열람", description = "API-KM-002 · 미게시 기사는 작성자 본인/Gatekeeper만 열람 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "미게시 기사 접근", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "기사 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(knowledgeService.detail(id));
    }

    @Operation(summary = "기사 수정", description = "API-KM-004 · Contributor 전용, 부분 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "제목·본문 빈 값/존재하지 않는 카테고리", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "기사 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<StatusResponse> update(@PathVariable Long id, @RequestBody UpdateArticleRequest request) {
        return ResponseEntity.ok(knowledgeService.update(id, request));
    }

    @Operation(summary = "기사 삭제", description = "API-KM-005 · Contributor 전용")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "기사 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "기사 상태 전이", description = "API-KM-006 · DRAFT→IN_REVIEW(검토 요청)만 허용. "
            + "공용 승인 게이트(domain=KNOWLEDGE) 매칭 결과에 따라 즉시 PUBLISHED 또는 IN_REVIEW(승인 대기)로 응답")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상(항상 성공, status로 결과 확인)"),
            @ApiResponse(responseCode = "400", description = "허용되지 않은 전이", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "기사 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<StatusTransitionResponse> transition(@PathVariable Long id,
                                                                @Valid @RequestBody StatusTransitionRequest request) {
        return ResponseEntity.ok(knowledgeService.transition(id, request));
    }

    @Operation(summary = "유용성 평가", description = "API-KM-009 · 미게시 기사 평가 거부")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "미게시 기사 평가", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "기사 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/feedback")
    public ResponseEntity<FeedbackResponse> feedback(@PathVariable Long id, @RequestBody FeedbackRequest request) {
        return ResponseEntity.ok(knowledgeService.feedback(id, request));
    }

    @Operation(summary = "KCS 티켓 연계(작성/연결)", description = "API-KM-011 · Contributor 전용, 존재하지 않는 티켓은 400")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연계"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 티켓", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/link")
    public ResponseEntity<LinkArticleResponse> linkArticle(@Valid @RequestBody LinkArticleRequest request) {
        return ResponseEntity.ok(knowledgeService.linkArticle(request));
    }
}
