package com.itsm.srm.presentation;

import com.itsm.srm.application.ServiceRequestService;
import com.itsm.srm.application.dto.PendingApprovalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "SRM - Approvals", description = "승인 대기 API (Approver)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

    private final ServiceRequestService requestService;

    public ApprovalController(ServiceRequestService requestService) {
        this.requestService = requestService;
    }

    @Operation(summary = "승인 대기 목록(scope=mine)")
    @PreAuthorize("hasRole('APPROVER')")
    @GetMapping
    public ResponseEntity<List<PendingApprovalResponse>> pending(
            @RequestParam(required = false, defaultValue = "mine") String scope,
            @RequestParam(required = false, defaultValue = "service-request") String type) {
        return ResponseEntity.ok(requestService.pendingApprovals());
    }
}
