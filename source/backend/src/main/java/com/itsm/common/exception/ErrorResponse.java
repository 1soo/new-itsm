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
        @Schema(description = "발생 시각(ISO-8601)") OffsetDateTime timestamp
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, OffsetDateTime.now());
    }
}
