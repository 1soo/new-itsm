# CLAUDE.md

compliance 도메인 애플리케이션 서비스 계층.

## 파일
- `ComplianceService.java` — 요구사항 등록·조회·수정·변경연계·책임자지정·시정조치(등록/전이)·컴플라이언스 전용 감사로그조회·준수현황 유스케이스(COMPLIANCE_OFFICER 전용). 등록/수정/시정조치 전이 시 auth.AuditLogService.record()로 같은 트랜잭션에 감사 로그 기록. Stage 6(2026-07-12)에서 시정조치 상태전이 시 공용 승인 게이트 연동. 개별 시정조치가 요구사항과 별개로 승인 인스턴스를 가지므로 `TicketType.CORRECTIVE_ACTION`(ticketId=corrective_action.id) 사용(요구사항 자체는 여전히 `COMPLIANCE_REQUIREMENT`, 게이트 대상 아님). 2026-07-22 상태별 승인자 지정 확장: `transitionCorrectiveAction()`은 target 가드 없이 무조건 `checkGate(domain=COMPLIANCE, requestSubtypeKey=null, requesterId=SecurityUtils.currentPrincipal().userId(), TT, actionId, targetState=target)` 호출(DETECTED/IN_PROGRESS/RESOLVED 어떤 전이든 게이트 대상, checkGate의 requesterId는 호출자로 통일). `addCorrectiveAction()`(API-COMP-007)은 `TicketCreationGateSupport.createThenGate`로 REQUIRES_NEW 분리(생성시점 targetState="DETECTED", requesterId=호출자) — 시정조치의 "생성"이 곧 이 도메인의 생성 시점 게이트 지점(요구사항 자체는 게이트 없음). 상세 조회(API-COMP-003)는 시정조치 항목별로 `approval` 필드 노출(요구사항 전체가 아니라 각 CorrectiveActionDto에 개별 부여, `ApprovalInfo`에 targetState 포함(2026-07-22 신규)). 목록(API-COMP-001)의 `pendingApprovalTargetState`는 게이트가 시정조치 단위라 요구사항 목록에 자연스러운 단일 필드로 대응되지 않아 추가하지 않음(dev-lead 확인 필요 시 에스컬레이션). `detail()` 가드(2026-07-15 신규, 이번 유지보수로 변경 없음 — canApproverView는 시정조치 등록자 기준 유지, checkGate만 호출자 기준으로 전환): COMPLIANCE_OFFICER 또는 `canApproverViewRequirement()`(요구사항에 속한 시정조치를 순회하며 각각 `ApprovalGateService.canApproverView(COMPLIANCE, null, requesterIdOf(action))` 호출, 하나라도 매칭되면 허용 — 요구사항 자체엔 요청자 개념이 없어 시정조치 단위로 판정, 시정조치 0건이면 false)
- `CorrectiveActionApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 CORRECTIVE_ACTION 티켓 요약 어댑터(`ApprovalTicketSummaryProvider` 구현). 시정조치는 자연키가 없어 ticketKey를 "CA-{actionId}" 형태로 합성(KNOWLEDGE의 "KM-{id}" 패턴과 동일)
- `CorrectiveActionStateMachine.java` — 시정조치 상태 전이 규칙(DETECTED→IN_PROGRESS→RESOLVED 순차, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
