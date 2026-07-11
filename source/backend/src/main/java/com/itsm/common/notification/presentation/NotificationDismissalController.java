package com.itsm.common.notification.presentation;

import com.itsm.common.exception.ErrorResponse;
import com.itsm.common.notification.application.NotificationDismissalService;
import com.itsm.common.notification.application.dto.DismissNotificationsRequest;
import com.itsm.common.notification.application.dto.DismissResultResponse;
import com.itsm.common.notification.application.dto.NotificationDismissalListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notifications", description = "헤더 알림 확인처리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/notifications/dismissals")
public class NotificationDismissalController {

    private final NotificationDismissalService notificationDismissalService;

    public NotificationDismissalController(NotificationDismissalService notificationDismissalService) {
        this.notificationDismissalService = notificationDismissalService;
    }

    @Operation(summary = "알림 확인처리(개별/일괄)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인처리 완료(멱등)"),
            @ApiResponse(responseCode = "400", description = "items 누락·형식 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "미인증", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DismissResultResponse> dismiss(@Valid @RequestBody DismissNotificationsRequest request) {
        return ResponseEntity.ok(notificationDismissalService.dismiss(request.items()));
    }

    @Operation(summary = "확인처리된 알림 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상(이력 없으면 빈 배열)"),
            @ApiResponse(responseCode = "401", description = "미인증", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<NotificationDismissalListResponse> list() {
        return ResponseEntity.ok(notificationDismissalService.list());
    }
}
