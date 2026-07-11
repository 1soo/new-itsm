# CLAUDE.md

알림 확인처리 애플리케이션 서비스 계층. 유스케이스 조율·트랜잭션 처리.

## 파일
- `NotificationDismissalService.java` — 알림 확인처리(개별/일괄, 멱등)·이력 조회 유스케이스(API-COM-001/002). 로그인 사용자는 SecurityUtils.currentPrincipal()로 조회, 원본 승인 대기·자산 만료 데이터는 변경하지 않음

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
