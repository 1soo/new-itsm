package com.itsm.esm.presentation;

import com.itsm.auth.domain.Department;
import com.itsm.common.exception.ErrorResponse;
import com.itsm.esm.application.EsmCatalogService;
import com.itsm.esm.application.dto.CatalogItemDetailResponse;
import com.itsm.esm.application.dto.CatalogItemSummaryResponse;
import com.itsm.esm.application.dto.CreateCatalogItemRequest;
import com.itsm.esm.application.dto.UpdateCatalogItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "ESM - Catalog", description = "부서 카탈로그 API (API-ESM-001~004). 생성/수정은 PROCESS_OWNER 전용.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/esm/catalog-items")
public class EsmCatalogController {

    private final EsmCatalogService catalogService;

    public EsmCatalogController(EsmCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "부서 카탈로그 목록", description = "API-ESM-001 · department/keyword 필터")
    @GetMapping
    public ResponseEntity<List<CatalogItemSummaryResponse>> list(
            @RequestParam(required = false) Department department,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(catalogService.list(department, keyword));
    }

    @Operation(summary = "카탈로그 항목 상세(체크리스트 템플릿·양식 스키마)", description = "API-ESM-002")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "항목 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CatalogItemDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.detail(id));
    }

    @Operation(summary = "카탈로그 항목 생성(Process Owner)", description = "API-ESM-003")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "이름·담당 부서 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CatalogItemDetailResponse> create(@Valid @RequestBody CreateCatalogItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(request));
    }

    @Operation(summary = "카탈로그 항목 수정(Process Owner)", description = "API-ESM-004")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "입력 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "항목 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<CatalogItemDetailResponse> update(@PathVariable Long id,
                                                            @RequestBody UpdateCatalogItemRequest request) {
        return ResponseEntity.ok(catalogService.update(id, request));
    }
}
