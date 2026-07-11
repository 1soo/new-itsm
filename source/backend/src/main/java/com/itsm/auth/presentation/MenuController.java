package com.itsm.auth.presentation;

import com.itsm.auth.application.MyMenuService;
import com.itsm.auth.application.dto.MyMenuResponse;
import com.itsm.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Menus", description = "내 메뉴 조회 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MyMenuService myMenuService;

    public MenuController(MyMenuService myMenuService) {
        this.myMenuService = myMenuService;
    }

    @Operation(summary = "내 메뉴 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상(그룹 없으면 빈 배열)"),
            @ApiResponse(responseCode = "401", description = "미인증", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/mine")
    public ResponseEntity<MyMenuResponse> mine() {
        return ResponseEntity.ok(myMenuService.myMenu());
    }
}
