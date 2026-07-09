package com.itsm.srm.presentation;

import com.itsm.change.application.ChangeService;
import com.itsm.srm.application.ServiceRequestService;
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

@Tag(name = "Approvals", description = "승인 대기 API (Approver). type=service-request|change 공유 대기함")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

    private final ServiceRequestService requestService;
    private final ChangeService changeService;

    public ApprovalController(ServiceRequestService requestService, ChangeService changeService) {
        this.requestService = requestService;
        this.changeService = changeService;
    }

    @Operation(summary = "승인 대기 목록(scope=mine)", description = "API-SRM-012 · API-CHG-007 · type=service-request|change")
    @PreAuthorize("hasRole('APPROVER')")
    @GetMapping
    public ResponseEntity<List<?>> pending(
            @RequestParam(required = false, defaultValue = "mine") String scope,
            @RequestParam(required = false, defaultValue = "service-request") String type) {
        if ("change".equals(type)) {
            return ResponseEntity.ok(changeService.pendingApprovals());
        }
        return ResponseEntity.ok(requestService.pendingApprovals());
    }
}
