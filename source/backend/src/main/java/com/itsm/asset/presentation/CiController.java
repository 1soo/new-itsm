package com.itsm.asset.presentation;

import com.itsm.asset.application.AssetService;
import com.itsm.asset.application.dto.CiCreatedResponse;
import com.itsm.asset.application.dto.CiImpactResponse;
import com.itsm.asset.application.dto.CiListResponse;
import com.itsm.asset.application.dto.CiRelationRequest;
import com.itsm.asset.application.dto.CiRelationResponse;
import com.itsm.asset.application.dto.CreateCiRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "CI/CMDB", description = "구성 항목(CI)·CMDB 관계 API (API-ITAM-008~011). 인증된 사용자 전반 허용.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/cis")
public class CiController {

    private final AssetService assetService;

    public CiController(AssetService assetService) {
        this.assetService = assetService;
    }

    @Operation(summary = "CI 목록 조회", description = "API-ITAM-008 · keyword/type 필터")
    @GetMapping
    public ResponseEntity<CiListResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(assetService.listCis(keyword, type, pageable));
    }

    @Operation(summary = "CI 등록", description = "API-ITAM-009 · 연결 자산은 선택, 미존재 자산 지정 시 400")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성"),
            @ApiResponse(responseCode = "400", description = "이름 누락 또는 유효하지 않은 연결 자산", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CiCreatedResponse> create(@Valid @RequestBody CreateCiRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.createCi(request));
    }

    @Operation(summary = "CI 관계 등록", description = "API-ITAM-010 · 자기참조·존재하지 않는 CI는 400")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장된 관계"),
            @ApiResponse(responseCode = "400", description = "자기참조 또는 존재하지 않는 CI", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "출발 CI 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/relations")
    public ResponseEntity<CiRelationResponse> createRelation(@PathVariable Long id,
                                                             @Valid @RequestBody CiRelationRequest request) {
        return ResponseEntity.ok(assetService.createRelation(id, request));
    }

    @Operation(summary = "CI 영향 범위 조회", description = "API-ITAM-011 · 관계 없으면 빈 목록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상(관계 없으면 빈 배열)"),
            @ApiResponse(responseCode = "404", description = "CI 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/impact")
    public ResponseEntity<List<CiImpactResponse>> impact(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.impact(id));
    }
}
