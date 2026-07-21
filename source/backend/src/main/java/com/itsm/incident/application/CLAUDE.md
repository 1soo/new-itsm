# CLAUDE.md

인시던트(incident) 도메인 애플리케이션 서비스 계층.

## 파일
- `IncidentService.java` — 인시던트 생성·상태전이·심각도변경·대응자배정·에스컬레이션·해결·타임라인·문제연계·포스트모템 유스케이스. Stage 4(2026-07-11)에서 승인 게이트 연동. 2026-07-22 상태별 승인자 지정 확장: `transition()`은 target 가드 없이 무조건 `checkGate(domain=INCIDENT, requestSubtypeKey=null, requesterId=SecurityUtils.currentPrincipal().userId(), TT, id, targetState=target)` 호출(NEW→IN_PROGRESS→RESOLVED→CLOSED 어떤 전이든 게이트 대상). API-INC-005(상태전이)뿐 아니라 동일 전이를 수행하는 API-INC-009(resolve)도 우회 경로 방지 위해 같은 게이트 통과(2026-07-12 추가, 이번에도 requesterId를 호출자 기준으로 통일). `create()`(API-INC-002)는 `TicketCreationGateSupport.createThenGate`로 REQUIRES_NEW 분리(생성시점 targetState="NEW"). 상세 조회(API-INC-003)의 `approval`(`ApprovalInfo`)에 `targetState` 포함. 목록(API-INC-001)은 `pendingApprovalTargetStatesOf` 배치 조회로 `IncidentSummaryResponse.pendingApprovalTargetState` 채움(N+1 방지). `detail()` 가드(2026-07-15 신규 — 기존엔 역할체크 자체가 없던 결함, 이번 유지보수로 변경 없음 — canApproverView는 인시던트 등록자 기준 유지): `SecurityUtils.hasAnyRole(SERVICE_DESK_AGENT, INCIDENT_MANAGER)` 또는 `ApprovalGateService.canApproverView(INCIDENT, null, requesterIdOf(inc))` 매칭 시에만 조회 허용, 둘 다 아니면 403
- `IncidentApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 INCIDENT 티켓 요약(ticketKey·요약·요청자명) 어댑터(`ApprovalTicketSummaryProvider` 구현, 자연키 ticketKey 그대로 사용)
- `IncidentStateMachine.java` — 상태 전이 규칙(NEW→IN_PROGRESS→RESOLVED→CLOSED, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
