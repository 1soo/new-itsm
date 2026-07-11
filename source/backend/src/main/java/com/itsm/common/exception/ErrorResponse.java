package com.itsm.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * 표준 오류 응답 본문 (모든 4xx/5xx 공통).
 */
@Schema(description = "표준 오류 응답")
public record ErrorResponse(
        @Schema(description = "오류 코드", example = "INVALID_CREDENTIALS") String code,
        @Schema(description = "사용자 메시지", example = "이메일 또는 비밀번호가 일치하지 않습니다.") String message,
        @Schema(description = "발생 시각(ISO-8601)") OffsetDateTime timestamp,
        @Schema(description = "승인 게이트로 생성된 인스턴스 id(APPROVAL_PENDING 409 전용, 그 외 null)") Long approvalRequestId
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, OffsetDateTime.now(), null);
    }

    public static ErrorResponse of(String code, String message, Long approvalRequestId) {
        return new ErrorResponse(code, message, OffsetDateTime.now(), approvalRequestId);
    }
}
