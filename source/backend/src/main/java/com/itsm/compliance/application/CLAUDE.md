# CLAUDE.md

compliance 도메인의 애플리케이션 서비스 계층.

## 파일
- `ComplianceService.java` — 요구사항 등록·조회·수정·변경연계·책임자지정·시정조치(등록/전이)·컴플라이언스 전용 감사로그조회·준수현황 유스케이스(COMPLIANCE_OFFICER 전용). 등록/수정/시정조치 전이 시 auth.AuditLogService.record()로 같은 트랜잭션에 감사 로그 기록
- `CorrectiveActionStateMachine.java` — 시정조치 상태 전이 규칙(DETECTED→IN_PROGRESS→RESOLVED 순차, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
