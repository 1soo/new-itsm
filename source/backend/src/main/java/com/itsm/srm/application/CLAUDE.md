# CLAUDE.md

서비스 요청(srm) 도메인의 애플리케이션 서비스 계층.

## 파일
- `ServiceRequestService.java` — 요청 생성·상태전이·배정·댓글·CSAT·지식연계 유스케이스
- `ServiceCatalogService.java` — 서비스 카탈로그 항목 CRUD·동적 양식 관리
- `QueueService.java` — 큐 조회·요청 라우팅
- `MetricsService.java` — SRM 지표(CSAT·SLA 등) 집계
- `RequestStateMachine.java` — 수동 상태 전이 규칙(package-private)
- `SlaCalculator.java` — 기한 대비 잔여 시간으로 SLA 상태(OK/WARNING/BREACHED) 산정(package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
