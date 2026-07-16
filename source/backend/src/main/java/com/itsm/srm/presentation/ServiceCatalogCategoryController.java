package com.itsm.srm.presentation;

import com.itsm.common.exception.ErrorResponse;
import com.itsm.srm.application.ServiceCatalogCategoryService;
import com.itsm.srm.application.dto.CategoryCreateRequest;
import com.itsm.srm.application.dto.CategoryListResponse;
import com.itsm.srm.application.dto.CategoryResponse;
import com.itsm.srm.application.dto.CategoryUpdateRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "SRM - Catalog Category", description = "서비스 카탈로그 카테고리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/service-catalog/categories")
public class ServiceCatalogCategoryController {

    private final ServiceCatalogCategoryService categoryService;

    public ServiceCatalogCategoryController(ServiceCatalogCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "카탈로그 카테고리 목록")
    @GetMapping
    public ResponseEntity<List<CategoryListResponse>> list() {
        return ResponseEntity.ok(categoryService.list());
    }

    @Operation(summary = "카탈로그 카테고리 생성(Process Owner)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "이름 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이름 중복", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('PROCESS_OWNER')")
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @Operation(summary = "카탈로그 카테고리 수정(Process Owner)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카테고리 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이름 중복", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('PROCESS_OWNER')")
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @Operation(summary = "카탈로그 카테고리 삭제(Process Owner)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 완료"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카테고리 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "참조 중인 카탈로그 항목이 있음(CATEGORY_IN_USE)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('PROCESS_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
