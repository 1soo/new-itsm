# CLAUDE.md

서비스 요청(srm) 도메인 애플리케이션 서비스 계층.

## 파일
- `ServiceRequestService.java` — 요청 생성·상태전이·배정·댓글·CSAT·지식연계·자산연계 조회(REQ-ITAM-006, TicketLink 재사용) 유스케이스. IN_FULFILLMENT 전이 시 공용 승인 게이트(`common.approval.application.ApprovalGateService`) 호출(2026-07-11 승인 프로세스 커스텀 기능, 승인 결정·대기 목록은 이 서비스가 아닌 공용 엔진이 담당). `assigneeCandidates()`(API-SRM-017, 2026-07-15): 카탈로그 항목의 assigneeRoleId 보유+ACTIVE 사용자 목록(`AppUserRepository.search(roleCode 필터)` 재사용, 역할 미지정 시 빈 배열). `transition()`: target=ROUTED이고 담당자 미배정이면 `ASSIGNEE_REQUIRED_FOR_ROUTING`(409, vulnerability의 ASSIGNEE_REQUIRED_FOR_REMEDIATION과 동일 패턴), 승인 게이트 체크보다 먼저 판정. `assertCanView`: 기존 정적 APPROVER 전체조회 폐지, AGENT/PROCESS_OWNER 또는 `ApprovalGateService.canApproverView(SERVICE_REQUEST, catalogItemId, requesterId)` 매칭 시에만 조회 허용(2026-07-15 승인 대상자 동적 상세조회 권한)
- `ServiceCatalogService.java` — 서비스 카탈로그 항목 CRUD·동적 양식 관리(승인 필요 여부 필드는 제거됨). 담당자 역할(assigneeRoleId, 2026-07-15) CRUD 지원, 상세 응답에 `RoleRepository`로 역할명(assigneeRoleName) resolve해 포함
- `SrmApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 SERVICE_REQUEST 티켓 요약(ticketKey·카탈로그명·요청자명) 어댑터(`ApprovalTicketSummaryProvider` 구현)
- `SrmApprovalRequestSubtypeProvider.java` — 승인 프로세스 관리자 CRUD(API-AUTH-024)의 SERVICE_REQUEST 요청유형 후보(서비스 카탈로그 항목) 어댑터(`ApprovalRequestSubtypeProvider` 구현)
- `QueueService.java` — 큐 조회·요청 라우팅
- `MetricsService.java` — SRM 지표(CSAT·SLA 등) 집계
- `RequestStateMachine.java` — 수동 상태 전이 규칙(package-private, SUBMITTED→VALIDATED→ROUTED→IN_FULFILLMENT→FULFILLED→CLOSED)
- `SlaCalculator.java` — 기한 대비 잔여 시간으로 SLA 상태(OK/WARNING/BREACHED) 산정(package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
