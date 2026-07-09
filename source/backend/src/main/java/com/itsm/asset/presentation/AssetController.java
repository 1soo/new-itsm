package com.itsm.asset.presentation;

import com.itsm.asset.application.AssetService;
import com.itsm.asset.application.dto.AssetCreatedResponse;
import com.itsm.asset.application.dto.AssetDetailResponse;
import com.itsm.asset.application.dto.AssetMetricsResponse;
import com.itsm.asset.application.dto.AssetSummaryResponse;
import com.itsm.asset.application.dto.CreateAssetRequest;
import com.itsm.asset.application.dto.LifecycleTransitionRequest;
import com.itsm.asset.application.dto.LinkAssetRequest;
import com.itsm.asset.application.dto.LinkAssetResponse;
import com.itsm.asset.application.dto.StatusResponse;
import com.itsm.asset.application.dto.UpdateAssetRequest;
import com.itsm.asset.application.dto.UpdateAssetResponse;
import com.itsm.asset.domain.AssetStatus;
import com.itsm.asset.domain.AssetType;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.exception.ErrorResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.OffsetDateTime;

@Tag(name = "Asset", description = "IT 자산 관리 API (API-ITAM-001~007/012). 등록·수정·생애주기전이·폐기는 ASSET_MANAGER 전용, 조회·연계·지표는 인증된 사용자 전반.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @Operation(summary = "자산 목록 조회", description = "API-ITAM-001 · type/status/owner/expiringWithinDays/keyword 필터")
    @GetMapping
    public ResponseEntity<PageResponse<AssetSummaryResponse>> list(
            @RequestParam(required = false) AssetType type,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer expiringWithinDays,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(assetService.list(type, status, owner, keyword, expiringWithinDays, pageable));
    }

    @Operation(summary = "자산 등록", description = "API-ITAM-002 · ASSET_MANAGER 전용")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "이름·유형 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AssetCreatedResponse> create(@Valid @RequestBody CreateAssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(request));
    }

    @Operation(summary = "자산 지표 조회", description = "API-ITAM-012 · from/to 필터, 데이터 없으면 빈 결과")
    @GetMapping("/metrics")
    public ResponseEntity<AssetMetricsResponse> metrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        return ResponseEntity.ok(assetService.metrics(from, to));
    }

    @Operation(summary = "자산 상세 조회", description = "API-ITAM-003")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "자산 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AssetDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.detail(id));
    }

    @Operation(summary = "자산 수정", description = "API-ITAM-004 · ASSET_MANAGER 전용, 만료일 과거 입력 시 200+경고")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상(만료일 과거 시 warning 포함)"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "자산 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UpdateAssetResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateAssetRequest request) {
        return ResponseEntity.ok(assetService.update(id, request));
    }

    @Operation(summary = "생애주기 단계 전이", description = "API-ITAM-005 · ASSET_MANAGER 전용, 5단계 임의 전이 허용")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전이 성공(이력 기록)"),
            @ApiResponse(responseCode = "400", description = "정의되지 않은 단계", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "자산 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/lifecycle")
    public ResponseEntity<StatusResponse> transition(@PathVariable Long id,
                                                     @Valid @RequestBody LifecycleTransitionRequest request) {
        return ResponseEntity.ok(assetService.transition(id, request));
    }

    @Operation(summary = "자산 폐기", description = "API-ITAM-006 · ASSET_MANAGER 전용")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "자산 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/retire")
    public ResponseEntity<StatusResponse> retire(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.retire(id));
    }

    @Operation(summary = "자산 티켓 연계", description = "API-ITAM-007 · 존재하지 않는 티켓은 400")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연계"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 티켓", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "자산 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/links")
    public ResponseEntity<LinkAssetResponse> link(@PathVariable Long id, @Valid @RequestBody LinkAssetRequest request) {
        return ResponseEntity.ok(assetService.linkAsset(id, request));
    }
}
