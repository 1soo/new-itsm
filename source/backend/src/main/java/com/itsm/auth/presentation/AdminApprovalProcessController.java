package com.itsm.auth.presentation;

import com.itsm.auth.application.ApprovalProcessAdminService;
import com.itsm.auth.application.dto.ApprovalDomainResponse;
import com.itsm.auth.application.dto.ApprovalProcessDeletedResponse;
import com.itsm.auth.application.dto.ApprovalProcessDetailResponse;
import com.itsm.auth.application.dto.ApprovalProcessSummaryResponse;
import com.itsm.auth.application.dto.CreateApprovalProcessRequest;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.application.dto.UpdateApprovalProcessRequest;
import com.itsm.common.approval.application.RequestSubtypeOption;
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

import java.util.List;

@Tag(name = "Admin - Approval Processes", description = "승인 프로세스 커스텀 관리 API (SYSTEM_ADMIN, API-AUTH-023~029)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/approval-processes")
public class AdminApprovalProcessController {

    private final ApprovalProcessAdminService approvalProcessAdminService;

    public AdminApprovalProcessController(ApprovalProcessAdminService approvalProcessAdminService) {
        this.approvalProcessAdminService = approvalProcessAdminService;
    }

    @Operation(summary = "승인 프로세스 대상 도메인 목록 조회", description = "API-AUTH-023")
    @GetMapping("/domains")
    public ResponseEntity<List<ApprovalDomainResponse>> domains() {
        return ResponseEntity.ok(approvalProcessAdminService.domains());
    }

    @Operation(summary = "도메인별 요청유형 후보 목록 조회", description = "API-AUTH-024")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상(하위유형 없는 도메인·매칭 데이터 없으면 빈 배열)"),
            @ApiResponse(responseCode = "400", description = "정의되지 않은 domain", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/domains/{domain}/request-subtypes")
    public ResponseEntity<List<RequestSubtypeOption>> requestSubtypes(@PathVariable String domain) {
        return ResponseEntity.ok(approvalProcessAdminService.requestSubtypes(domain));
    }

    @Operation(summary = "승인 프로세스 목록 조회", description = "API-AUTH-025")
    @GetMapping
    public ResponseEntity<PageResponse<ApprovalProcessSummaryResponse>> list(
            @RequestParam(required = false) String domain,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "priorityTier", "id"));
        return ResponseEntity.ok(approvalProcessAdminService.list(domain, pageable));
    }

    @Operation(summary = "승인 프로세스 상세 조회", description = "API-AUTH-026")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "프로세스 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApprovalProcessDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(approvalProcessAdminService.detail(id));
    }

    @Operation(summary = "승인 프로세스 생성", description = "API-AUTH-027 · steps가 빈 배열이면 승인자 없이 요청자만 설정된 프로세스로 저장")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "domain 누락 · step roleIds 비어있음 · steps 10개 초과", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "우선순위 충돌", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApprovalProcessDetailResponse> create(@Valid @RequestBody CreateApprovalProcessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(approvalProcessAdminService.create(request));
    }

    @Operation(summary = "승인 프로세스 수정", description = "API-AUTH-028 · requesterRoleIds·steps 전달 시 전체 교체")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "프로세스 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "우선순위 충돌", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApprovalProcessDetailResponse> update(@PathVariable Long id,
                                                                 @RequestBody UpdateApprovalProcessRequest request) {
        return ResponseEntity.ok(approvalProcessAdminService.update(id, request));
    }

    @Operation(summary = "승인 프로세스 삭제", description = "API-AUTH-029 · soft delete, 진행 중 인스턴스는 스냅샷으로 계속 진행")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "404", description = "프로세스 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApprovalProcessDeletedResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(approvalProcessAdminService.delete(id));
    }
}
