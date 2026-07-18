# CLAUDE.md

전역 공통 예외 처리 모듈. 모든 도메인 공유 에러 코드·예외 응답 표준.

## 파일
- `ErrorCode.java` — 도메인별 에러 코드 enum(HTTP 상태·메시지 매핑). 400/401/403/404/409/500 및 SRM/INC/PRB/CHG/KM/ITAM/ESM/VULN/COMP/IOM 도메인 코드 포함
- `BusinessException.java` — ErrorCode 담는 비즈니스 예외(RuntimeException)
- `ErrorResponse.java` — 표준 에러 응답 DTO(record)
- `GlobalExceptionHandler.java` — `@RestControllerAdvice` 전역 예외 → ErrorResponse 변환. `NoResourceFoundException`/`NoHandlerFoundException`(존재하지 않는 경로 호출) 전용 핸들러가 catch-all(`Exception.class`, 500)보다 우선 매칭돼 404(`ENDPOINT_NOT_FOUND`)로 응답(2026-07-18 버그 수정 — 이전엔 catch-all이 가로채 500 오응답)
