# CLAUDE.md

승인 엔진 애플리케이션 서비스 계층.

## 파일
- `ApprovalGateService.java` — 게이트 체크(common.md 0절, 2026-07-22 상태별 승인자 지정 확장으로 4번째 매칭 축 `targetState` 반영). `matchProcess(domain, requestSubtypeKey, requesterId, targetState)`는 티켓 도메인 규칙 + 전체 도메인(domain null) 규칙을 함께 후보로 조회해 `targetState`(candidate.targetState가 null이거나 대상과 일치) + subtype + role 조건을 만족하는 후보 중 priorityTier가 가장 큰 규칙을 선택. `checkGate(..., targetState)`(SRM/CHANGE류, 던지는 방식): 우선순위 매칭 → 0차/무매칭 통과 → 이 targetState의 최신 인스턴스(`findTopByTicketTypeAndTicketIdAndTargetStateOrderByIdDesc`) 없음 시 스냅샷 생성+409(APPROVAL_PENDING) → IN_PROGRESS 409(APPROVAL_PENDING) → REJECTED 409(APPROVAL_REJECTED, 신규 — 자동 재시도 없음, `resubmit`로만 재개) → APPROVED 통과. `evaluateAndCreateIfNeeded(..., targetState)`(KNOWLEDGE류, 논-스로잉): 항상 200 성공해야 하는 도메인용, `GateDecision`(passed, approvalRequestId) 반환, 매칭 시 이전 인스턴스 상태 무관하게 항상 새 인스턴스 생성. `canApproverView(domain, requestSubtypeKey, requesterId)` — 승인 대상자 역할 기반 동적 상세조회 권한(common.md 0-1절, 2026-07-22 집계 로직 변경). 신규 `collectApproverRoleCandidates` 헬퍼로 `matchProcess`의 "tier 최고 1건" 경로와 별개로 targetState 무관 전체 후보(전체 상태 공통 + 상태별 N건) 각각의 전체 차수 승인자 역할을 합집합해 로그인 사용자(조회자) role과 교집합 판정(기존 하드코딩 게이트 지점의 승인자 상세조회 권한 회귀 방지). `resubmit(ticketType, ticketId, requesterId)` — 반려 후 재승인요청(API-COM-006, common.md 2절). targetState 무관 "티켓 전체의 최신 인스턴스" 기준으로 REJECTED 판정(아니면 400/없으면 404) 후, 그 인스턴스의 도메인/요청유형/targetState + 호출자 requesterId로 현재 시점 규칙 재매칭(`matchProcess`)해 REQUIRES_NEW로 새 인스턴스 생성. 매칭 규칙이 사라졌으면 인스턴스 생성 없이 `ApprovalResubmitResponse.status="NO_RULE_MATCHED"` 반환(던지지 않음 — 명시적 사용자 액션이라 정상 응답). `pendingApprovalTargetStatesOf(ticketType, ticketIds)` — 목록 API의 pendingApprovalTargetState 배치 조회 공용 메서드(N+1 방지, ticketId → 최신 IN_PROGRESS 인스턴스의 targetState). 8개 도메인(Asset/Change/Esm/Incident/Knowledge/Problem/ServiceRequest/Vulnerability)이 각자 두던 동일 중복 로직을 여기로 추출(2026-07-22 code review 반영). COMPLIANCE는 게이트가 요구사항이 아니라 시정조치 단위라 대상 아님
- `TicketCreationGateSupport.java` — 생성 시점(최초 상태) 게이트 지원(신규). 엔티티 생성+저장을 REQUIRES_NEW로 먼저 커밋한 뒤 커밋 후 별도 트랜잭션에서 `ApprovalGateService.checkGate` 호출(`createThenGate`), 게이트가 막혀도 방금 커밋한 마스터 레코드는 롤백되지 않는다(요구사항 1: 승인 전까지 "임시/승인대기"로만 표시). 9개 도메인 `create()`가 사용(task 13)
- `TargetStateLabelResolver.java` — targetState 원본 코드값 → 표시 라벨 공용 리졸버(신규, 도메인별 상태 enum 라벨 정적 맵, FE `features/{domain}/status.ts` STATUS_LABEL과 동일 값). `ApprovalInstanceService`(API-COM-003/004 targetStateLabel)와 `auth.ApprovalProcessAdminService`(API-AUTH-031)가 공용으로 재사용(중복 구현 금지)
- `ApprovalInstanceService.java` — 대기함 목록(API-COM-003)·상세(API-COM-004)·결정(API-COM-005) 유스케이스. OR(최초 1건 확정)/AND(역할별 전원 승인 또는 즉시 반려) 집계, 차수 진행·SKIPPED 처리. 인스턴스 최종 확정(APPROVED/REJECTED) 시 `ApprovalDecisionCallback` 구현 빈(ticketType별) 호출
- `ApprovalDecisionCallback.java` — 승인 인스턴스 최종 확정 시 도메인 반응용 확장 포인트(onApproved/onRejected). 전이 재시도하는 SRM/CHANGE는 불필요, 결정 즉시 자동 전환 필요한 KNOWLEDGE가 구현
- `ApprovalRoleResolver.java` — role_id ↔ role_code(role claim) 상호 변환 공용 헬퍼(auth.domain.repository.RoleRepository 위임)
- `ApprovalTicketSummaryProvider.java` — 대기함·상세가 노출할 티켓 요약(ticketKey·제목·요청자) 조회 확장 포인트. 도메인별 구현 빈 등록(현재 SRM/CHANGE/KNOWLEDGE)
- `TicketSummary.java` — 위 확장 포인트 반환 타입(ticketKey, title, requesterName)
- `ApprovalRequestSubtypeProvider.java` — 관리자 CRUD(API-AUTH-024)가 도메인별 요청유형 후보 조회용 확장 포인트(하위유형 있는 도메인만 구현 빈 등록, 현재 SRM). CHANGE는 고정 코드라 auth 서비스가 직접 하드코딩, 이 확장 포인트 미사용
- `RequestSubtypeOption.java` — 위 확장 포인트 반환 타입(key, label). auth의 API-AUTH-024 응답으로 그대로 재사용

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
