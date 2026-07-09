package com.itsm.auth.presentation;

import com.itsm.auth.application.UserAdminService;
import com.itsm.auth.application.dto.CreateUserRequest;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.application.dto.StatusChangeRequest;
import com.itsm.auth.application.dto.StatusChangeResponse;
import com.itsm.auth.application.dto.UpdateUserRequest;
import com.itsm.auth.application.dto.UserDetailResponse;
import com.itsm.auth.application.dto.UserRolesResponse;
import com.itsm.auth.application.dto.UserSummaryResponse;
import com.itsm.auth.application.dto.AssignRoleRequest;
import com.itsm.auth.domain.UserStatus;
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

@Tag(name = "Admin - Users", description = "계정 관리 API (SYSTEM_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserAdminService userAdminService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @Operation(summary = "계정 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "401", description = "미인증", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PageResponse<UserSummaryResponse>> list(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(userAdminService.list(email, name, status, role, pageable));
    }

    @Operation(summary = "계정 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "필수 누락·형식 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이메일 중복", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<UserDetailResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.create(request));
    }

    @Operation(summary = "계정 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "계정 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> get(@PathVariable Long userId) {
        return ResponseEntity.ok(userAdminService.get(userId));
    }

    @Operation(summary = "계정 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "계정 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> update(@PathVariable Long userId,
                                                     @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userAdminService.update(userId, request));
    }

    @Operation(summary = "계정 상태 변경(활성/비활성)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "400", description = "형식 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "계정 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{userId}/status")
    public ResponseEntity<StatusChangeResponse> changeStatus(@PathVariable Long userId,
                                                             @Valid @RequestBody StatusChangeRequest request) {
        return ResponseEntity.ok(userAdminService.changeStatus(userId, request.status()));
    }

    @Operation(summary = "사용자 역할 부여")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "부여 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 역할", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "계정 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{userId}/roles")
    public ResponseEntity<UserRolesResponse> assignRole(@PathVariable Long userId,
                                                        @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(userAdminService.assignRole(userId, request.roleId()));
    }

    @Operation(summary = "사용자 역할 회수")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회수 성공"),
            @ApiResponse(responseCode = "404", description = "계정 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserRolesResponse> revokeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        return ResponseEntity.ok(userAdminService.revokeRole(userId, roleId));
    }
}
