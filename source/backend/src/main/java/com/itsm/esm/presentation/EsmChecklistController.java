package com.itsm.esm.presentation;

import com.itsm.auth.application.dto.PageResponse;
import com.itsm.common.exception.ErrorResponse;
import com.itsm.esm.application.EsmChecklistService;
import com.itsm.esm.application.dto.ChecklistDetailResponse;
import com.itsm.esm.application.dto.ChecklistTaskStatusRequest;
import com.itsm.esm.application.dto.ChecklistTaskStatusResponse;
import com.itsm.esm.application.dto.MyChecklistTaskResponse;
import com.itsm.esm.domain.ChecklistTaskStatus;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ESM - Checklists", description = "온보딩/오프보딩 체크리스트 API (API-ESM-014~016)")
@SecurityRequirement(name = "bearerAuth")
@RestController
public class EsmChecklistController {

    private final EsmChecklistService checklistService;

    public EsmChecklistController(EsmChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @Operation(summary = "체크리스트 상세 조회", description = "API-ESM-014 · 연계 요청 접근 권한자 또는 하위 작업 담당 부서 소속만")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "체크리스트 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/esm/checklists/{id}")
    public ResponseEntity<ChecklistDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(checklistService.detail(id));
    }

    @Operation(summary = "내 하위 작업 목록 조회", description = "API-ESM-015 · scope=mine(소속 부서 배정 하위 작업), DEPT_COORDINATOR 전용")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/api/v1/esm/checklist-tasks")
    public ResponseEntity<PageResponse<MyChecklistTaskResponse>> myTasks(
            @RequestParam(required = false, defaultValue = "mine") String scope,
            @RequestParam(required = false) ChecklistTaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(checklistService.myTasks(status, PageRequest.of(page, size)));
    }

    @Operation(summary = "하위 작업 상태 변경", description = "API-ESM-016 · 배정 부서 소속(DEPT_COORDINATOR)만, 전체 완료 시 체크리스트 자동 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "403", description = "배정 부서 아님", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "하위 작업 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/api/v1/esm/checklist-tasks/{taskId}/status")
    public ResponseEntity<ChecklistTaskStatusResponse> updateTaskStatus(@PathVariable Long taskId,
                                                                        @Valid @RequestBody ChecklistTaskStatusRequest request) {
        return ResponseEntity.ok(checklistService.updateTaskStatus(taskId, request));
    }
}
