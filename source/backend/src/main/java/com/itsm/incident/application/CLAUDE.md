# CLAUDE.md

인시던트(incident) 도메인의 애플리케이션 서비스 계층.

## 파일
- `IncidentService.java` — 인시던트 생성·상태전이·심각도변경·대응자배정·에스컬레이션·해결·타임라인·문제연계·포스트모템 유스케이스
- `IncidentStateMachine.java` — 상태 전이 규칙(NEW→IN_PROGRESS→RESOLVED→CLOSED, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
