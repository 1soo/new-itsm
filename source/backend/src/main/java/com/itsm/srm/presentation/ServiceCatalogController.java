package com.itsm.srm.presentation;

import com.itsm.srm.application.ServiceCatalogService;
import com.itsm.srm.application.dto.CatalogItemDetailResponse;
import com.itsm.srm.application.dto.CatalogItemSummaryResponse;
import com.itsm.srm.application.dto.CreateCatalogItemRequest;
import com.itsm.srm.application.dto.UpdateCatalogItemRequest;
import com.itsm.common.exception.ErrorResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "SRM - Service Catalog", description = "서비스 카탈로그 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/service-catalog/items")
public class ServiceCatalogController {

    private final ServiceCatalogService catalogService;

    public ServiceCatalogController(ServiceCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "서비스 카탈로그 목록")
    @GetMapping
    public ResponseEntity<List<CatalogItemSummaryResponse>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(catalogService.list(category, keyword));
    }

    @Operation(summary = "카탈로그 항목 상세(양식 스키마)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "항목 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CatalogItemDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.detail(id));
    }

    @Operation(summary = "카탈로그 항목 생성(Process Owner)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "이름·양식 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('PROCESS_OWNER')")
    @PostMapping
    public ResponseEntity<CatalogItemDetailResponse> create(@Valid @RequestBody CreateCatalogItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(request));
    }

    @Operation(summary = "카탈로그 항목 수정(Process Owner)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "항목 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('PROCESS_OWNER')")
    @PatchMapping("/{id}")
    public ResponseEntity<CatalogItemDetailResponse> update(@PathVariable Long id,
                                                            @RequestBody UpdateCatalogItemRequest request) {
        return ResponseEntity.ok(catalogService.update(id, request));
    }
}
