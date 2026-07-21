# CLAUDE.md

change 도메인 애플리케이션 서비스 계층.

## 파일
- `ChangeService.java` — 변경 요청(RFC) 생성·조회·6단계 전이·분류·구현결과·인시던트/문제/자산 연계·일정·템플릿·지표 유스케이스. 문제→변경 연계(API-PRB-009) 재사용 메서드(createLinkedChange/existsChange/ticketKeyOf) 포함(역할 검사 없음, 호출측 ProblemService에서 수행). 자산 연계(REQ-ITAM-006)는 AssetService.linkAsset()의 양방향 저장으로 생성되며 상세 조회 시 links에 노출. 컴플라이언스 요구사항 연계(API-COMP-005, ticket_link source_type='COMPLIANCE_REQUIREMENT'/target_type='CHANGE')는 change가 target 쪽이라 findByTargetTypeAndTargetId로 별도 조회해 상세 응답 links에 COMPLIANCE_REQUIREMENT 타입으로 노출(ComplianceRequirementRepository 참조, 역할 검사 없음). 승인 경로 자동 라우팅·승인/반려·승인대기 코드는 2026-07-11 제거됨 — Stage 2(2026-07-11)에서 승인 게이트 연동. 2026-07-22 상태별 승인자 지정 확장: `transition()`은 target 가드 없이 무조건 `checkGate(domain=CHANGE, requestSubtypeKey=change_request.type, requesterId=SecurityUtils.currentPrincipal().userId(), TT, id, targetState=target)` 호출(6단계 어떤 전이든 게이트 대상). `create()`(API-CHG-002)·`createLinkedChange()`(문제 연계 재사용, ChangeType.NORMAL 고정) 둘 다 `TicketCreationGateSupport.createThenGate`로 REQUIRES_NEW 분리(생성시점 targetState="REQUESTED", requesterId=호출자 — RFC 생성 경로가 2곳이라 둘 다 동일하게 게이트 적용). 목록(API-CHG-001)은 `pendingApprovalTargetStatesOf` 배치 조회로 `ChangeSummaryResponse.pendingApprovalTargetState` 채움(N+1 방지). 상세 조회(API-CHG-003)의 `approval`(`ApprovalInfo`)에 `targetState` 포함. `detail()` 가드(이번 유지보수로 변경 없음 — canApproverView는 RFC 등록자 기준 유지, checkGate만 호출자 기준으로 전환): 기존 정적 "APPROVER 전체조회" 폐지, CM 또는 `ApprovalGateService.canApproverView(CHANGE, change.type, requesterIdOf(change))` 매칭 시에만 조회 허용(2026-07-15 승인 대상자 동적 상세조회 권한)
- `ChangeApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 CHANGE 티켓 요약(ticketKey·요약·요청자명) 어댑터(`ApprovalTicketSummaryProvider` 구현)
- `ChangeStateMachine.java` — 상태 전이 규칙(REQUESTED→REVIEW→PLANNING→APPROVAL→IMPLEMENTATION→CLOSED, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
