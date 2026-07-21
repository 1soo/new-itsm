# CLAUDE.md

문제(problem) 도메인 애플리케이션 서비스 계층.

## 파일
- `ProblemService.java` — 문제 생성·상태전이·RCA·워크어라운드·후속조치·연계·종료 및 기지 오류(Known Error) 유스케이스. 2026-07-22 상태별 승인자 지정 확장: `transition()`은 target 가드 없이 무조건 `checkGate(domain=PROBLEM, requestSubtypeKey=null, requesterId=SecurityUtils.currentPrincipal().userId(), TT, id, targetState=target)` 호출(6단계 어떤 전이든 게이트 대상). `create()`(API-PRB-002)·`createReactiveProblem()`(인시던트 연계 재사용)는 둘 다 `TicketCreationGateSupport.createThenGate`로 REQUIRES_NEW 분리(생성시점 targetState="DETECTION", requesterId=호출자 — Problem 생성 경로가 2곳이라 둘 다 동일하게 게이트 적용, 우회 방지). 목록(API-PRB-001)은 `pendingApprovalTargetStatesOf` 배치 조회로 `ProblemSummaryResponse.pendingApprovalTargetState` 채움(N+1 방지). 상세 조회(API-PRB-003)의 `approval`(`ApprovalInfo`)에 `targetState` 포함. `detail()` 가드(2026-07-15 신규, 이번 유지보수로 변경 없음 — canApproverView는 문제 등록자 기준 유지, checkGate만 호출자 기준으로 전환): PROBLEM_MANAGER 또는 `ApprovalGateService.canApproverView(PROBLEM, null, requesterIdOf(problem))` 매칭 시에만 조회 허용
- `ProblemStateMachine.java` — 상태 전이 규칙(DETECTION→…→RESOLVED_CLOSED 6단계 순차, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
