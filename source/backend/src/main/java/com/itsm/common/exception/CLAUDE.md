# CLAUDE.md

전역 공통 예외 처리 모듈. 모든 도메인이 공유하는 에러 코드와 예외 응답 표준.

## 파일
- `ErrorCode.java` — 도메인별 에러 코드 enum(HTTP 상태·메시지 매핑). 400/401/403/404/409/500 및 SRM/INC/PRB 도메인 코드 포함
- `BusinessException.java` — ErrorCode를 담는 비즈니스 예외(RuntimeException)
- `ErrorResponse.java` — 표준 에러 응답 DTO(record)
- `GlobalExceptionHandler.java` — `@RestControllerAdvice` 전역 예외 → ErrorResponse 변환
