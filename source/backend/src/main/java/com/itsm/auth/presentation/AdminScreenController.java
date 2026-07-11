package com.itsm.auth.presentation;

import com.itsm.auth.application.ScreenAdminService;
import com.itsm.auth.application.dto.AssignScreenRoleRequest;
import com.itsm.auth.application.dto.CreateScreenRequest;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.application.dto.ScreenDeletedResponse;
import com.itsm.auth.application.dto.ScreenResponse;
import com.itsm.auth.application.dto.ScreenRolesResponse;
import com.itsm.auth.application.dto.UpdateScreenRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Screens", description = "메뉴(화면) 관리 API (SYSTEM_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/screens")
public class AdminScreenController {

    private final ScreenAdminService screenAdminService;

    public AdminScreenController(ScreenAdminService screenAdminService) {
        this.screenAdminService = screenAdminService;
    }

    @Operation(summary = "메뉴(화면) 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "401", description = "미인증", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PageResponse<ScreenResponse>> list(
            @RequestParam(required = false) String groupCode,
            @RequestParam(required = false) String domain,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sortOrder", "id"));
        return ResponseEntity.ok(screenAdminService.list(groupCode, domain, pageable));
    }

    @Operation(summary = "메뉴 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "필수 누락·형식 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "screenCode 또는 path 중복", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ScreenResponse> create(@Valid @RequestBody CreateScreenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(screenAdminService.create(request));
    }

    @Operation(summary = "메뉴 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "형식 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "메뉴 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "path 중복", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{screenId}")
    public ResponseEntity<ScreenResponse> update(@PathVariable Long screenId,
                                                 @RequestBody UpdateScreenRequest request) {
        return ResponseEntity.ok(screenAdminService.update(screenId, request));
    }

    @Operation(summary = "메뉴 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "메뉴 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{screenId}")
    public ResponseEntity<ScreenDeletedResponse> delete(@PathVariable Long screenId) {
        return ResponseEntity.ok(screenAdminService.delete(screenId));
    }

    @Operation(summary = "메뉴에 역할 매핑 부여")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "부여 성공(즉시 반영)"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 역할", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "메뉴 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 매핑됨", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{screenId}/roles")
    public ResponseEntity<ScreenRolesResponse> assignRole(@PathVariable Long screenId,
                                                          @Valid @RequestBody AssignScreenRoleRequest request) {
        return ResponseEntity.ok(screenAdminService.assignRole(screenId, request.roleId()));
    }

    @Operation(summary = "메뉴 역할 매핑 회수")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회수 성공"),
            @ApiResponse(responseCode = "404", description = "메뉴 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{screenId}/roles/{roleId}")
    public ResponseEntity<ScreenRolesResponse> revokeRole(@PathVariable Long screenId, @PathVariable Long roleId) {
        return ResponseEntity.ok(screenAdminService.revokeRole(screenId, roleId));
    }
}
