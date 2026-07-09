package com.itsm.change.presentation;

import com.itsm.change.application.ChangeService;
import com.itsm.change.application.dto.ChangeTemplateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Change Templates", description = "표준 변경 템플릿 API (API-CHG-011). CHANGE_MANAGER 전용.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/change-templates")
public class ChangeTemplateController {

    private final ChangeService changeService;

    public ChangeTemplateController(ChangeService changeService) {
        this.changeService = changeService;
    }

    @Operation(summary = "표준 변경 템플릿 목록", description = "API-CHG-011")
    @GetMapping
    public ResponseEntity<List<ChangeTemplateResponse>> list() {
        return ResponseEntity.ok(changeService.listTemplates());
    }
}
