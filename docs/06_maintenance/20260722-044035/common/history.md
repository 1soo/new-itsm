---
date: 20260722-044035
domain: common
change_type: [new, modified]
keywords: [targetState, 승인게이트일반화, 재승인요청, canApproverView합집합]
---

# 유지보수 이력 — common

> 유지보수 일시: 20260722-044035 · 도메인: common

## 1. 요구사항

기존 공용 승인 엔진(2026-07-11 도입)은 도메인당 게이트 지점이 정확히 1곳으로 하드코딩되어 있었다.
프랙티스(도메인)의 상태 머신에서 모든 상태 전이 지점(최초 상태 포함)을 승인 게이트 대상으로 확장해야 한다.
승인 전/후는 각 도메인의 상태 enum·DB 컬럼을 바꾸지 않고, 열린 승인요청 존재 여부로 조회 시점에 파생 표시해야 한다.
승인이 반려(REJECTED)되면 해당 전이에 대해 다시 재승인요청을 할 수 있어야 한다(영구 차단 금지).
승인요청자는 티켓에 고정된 원 요청자가 아니라, 그 전이를 지금 시도하는 현재 호출자(현재 담당자)여야 한다.
특정 상태(targetState)를 지정하는 규칙은 승인요청자 역할을 반드시 함께 지정해야 한다.

## 2. 해결 방법

`approval_process`/`approval_request`에 4번째 매칭 축 `target_state`를 추가했다(DB 마이그레이션 41번).
tier 산정식에 targetState weight=8을 추가해 신규 tier 43/55만 발생하도록 하고, 기존 6개 tier(0/11/14/23/25/37)는 그대로 보존했다.
`ApprovalGateService.checkGate`에 targetState 매칭 축을 추가하고, 최신 인스턴스 상태가 IN_PROGRESS면 기존 `APPROVAL_PENDING`을, REJECTED면 신규 `APPROVAL_REJECTED`(409)를 던지도록 분기했다.
반려된 건에 대한 재승인요청을 위해 `ApprovalGateService.resubmit()`을 신규 구현하고, 공용 API `POST /api/v1/approvals/resubmit`(API-COM-006)을 추가했다.
생성 시점(최초 상태) 게이트를 위해 `TicketCreationGateSupport`를 신규 구현해, 엔티티 생성·저장을 REQUIRES_NEW로 먼저 커밋한 뒤 별도 트랜잭션에서 게이트를 평가하도록 했다(게이트 실패가 방금 저장한 마스터 레코드를 롤백시키지 않음).
9개 도메인 모든 게이트 호출부에서 requesterId 산출 방식을 "티켓 고정 필드"에서 "그 전이를 지금 호출하는 현재 사용자"(`SecurityUtils.currentPrincipal().userId()`)로 통일했다.
`canApproverView`의 후보 매칭 로직을 `checkGate`가 쓰는 "tier 최고 1건 선택" 방식에서, targetState 무관하게 매칭되는 모든 후보(전체공통 규칙 + 상태별 규칙 전부)를 모아 step role을 합집합하는 방식(`collectApproverRoleCandidates`)으로 교체했다.
이는 targetState 백필로 인해 기존 승인자가 상세조회 권한을 잃는 회귀를 방지하기 위한 결정이다.
승인 대기함/상세 응답에 targetState/targetStateLabel을 반영하기 위해 도메인별 상태 라벨을 공용 조회하는 `TargetStateLabelResolver`를 신규 구현했다(admin/common 양쪽 재사용).
코드 리뷰 과정에서 8개 도메인에 중복 작성되던 "티켓의 대기 중 targetState 목록 조회" 로직을 `pendingApprovalTargetStatesOf`로 공용화했다.
프론트엔드 공용 컴포넌트에 `deriveApprovalStatusDisplay`(승인대기/반려 파생 배지 산출)를 신규 구현하고, `ApprovalPanel`에 대상 상태 표시와 재승인요청 버튼을 추가했으며, `ApprovalInboxPage`(SCR-COM-014)에 대상 상태 컬럼을 추가했다.

## 3. 변경 파일

- `source/db/sql/41_approval_process_target_state.sql`
- `source/db/sql/42_approval_process_id1_requester_role_fix.sql`
- `source/backend/src/main/java/com/itsm/common/approval/domain/ApprovalProcess.java`
- `source/backend/src/main/java/com/itsm/common/approval/domain/ApprovalRequest.java`
- `source/backend/src/main/java/com/itsm/common/approval/application/ApprovalGateService.java`
- `source/backend/src/main/java/com/itsm/common/approval/application/TicketCreationGateSupport.java`(신규)
- `source/backend/src/main/java/com/itsm/common/approval/application/TargetStateLabelResolver.java`(신규)
- `source/backend/src/main/java/com/itsm/common/approval/application/ApprovalInstanceService.java`
- `source/backend/src/main/java/com/itsm/common/approval/presentation/ApprovalController.java`(API-COM-006 신규)
- `source/backend/src/main/java/com/itsm/common/exception/ErrorCode.java`(`APPROVAL_REJECTED` 신규)
- `source/backend/src/main/java/com/itsm/common/approval/domain/repository/{ApprovalProcessRepository,ApprovalRequestRepository}.java`
- `source/frontend/src/components/common/{approval-schema.ts,approval-panel.tsx}`
- `source/frontend/src/features/common/{types.ts,api.ts,ApprovalInboxPage.tsx}`
- `docs/02_plan/{database/common.md,api_spec/common.md}`
- `docs/00_context/glossary.md`

## 4. 테스트 결과

통합 테스트 1차 18건 중 16건 PASS, CRITICAL 회귀 1건과 FE 결함 1건이 발견됐다.
CRITICAL 회귀는 SRM 기존 활성 규칙(id=1)의 요청자 스코프가 END_USER로 남아있어 "요청자=현재 호출자" 전환 후 실제 호출 권한자와 스코프가 불일치해 게이트가 무력화되던 문제였고, 42번 마이그레이션(SERVICE_DESK_AGENT로 보정)으로 해결했다.
재테스트에서 전체 PASS했다(`docs/04_test/20260722-040618`, `docs/04_test/20260722-042424`).
코드 리뷰 Standards축에서 8개 도메인 중복 헬퍼 1건을 발견해 `pendingApprovalTargetStatesOf`로 공용화했다.
Spec축(계획 파일 및 docs/02_plan 대조)은 불일치 없었다.
커밋 `c8f9386`으로 반영했다.
