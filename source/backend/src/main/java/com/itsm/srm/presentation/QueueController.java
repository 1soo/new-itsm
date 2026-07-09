package com.itsm.srm.presentation;

import com.itsm.srm.application.QueueService;
import com.itsm.srm.application.dto.QueueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "SRM - Queues", description = "큐 목록·건수 API (Agent 이상)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/queues")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @Operation(summary = "큐 목록·건수 조회")
    @PreAuthorize("hasAnyRole('SERVICE_DESK_AGENT','PROCESS_OWNER')")
    @GetMapping
    public ResponseEntity<List<QueueResponse>> list() {
        return ResponseEntity.ok(queueService.list());
    }
}
