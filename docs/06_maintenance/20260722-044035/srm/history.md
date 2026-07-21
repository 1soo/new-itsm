---
date: 20260722-044035
domain: srm
change_type: [new, modified, removed]
keywords: [targetState, 상태별승인게이트, 재승인요청]
---

# 유지보수 이력 — srm

> 유지보수 일시: 20260722-044035 · 도메인: srm

## 1. 요구사항

서비스 요청의 "등록-검증-라우팅-이행" 각 상태 전이마다 독립적으로 승인자를 지정할 수 있어야 한다.
최초 상태(등록)에도 승인 게이트를 걸 수 있어야 하며, 마스터 레코드는 제출 즉시 생성하고 승인 전까지는 임시/승인대기로 표시해야 한다.
승인 반려 시 재승인요청이 가능해야 한다.

## 2. 해결 방법

`ServiceRequestService.transition()`의 `if (target == IN_FULFILLMENT)` 하드코딩 가드를 제거하고, target이 무엇이든 무조건 게이트를 호출하도록 일반화했다.
`create()`를 `TicketCreationGateSupport`로 리팩터링해 `SUBMITTED`(최초 상태) 게이트를 REQUIRES_NEW 트랜잭션으로 분리 적용했다.
게이트 호출 시 requesterId를 티켓의 원 요청자 필드가 아니라 그 전이를 지금 호출하는 현재 사용자로 통일했다.
상세 응답 DTO에 `targetState`를 추가하고, 목록 응답에 `pendingApprovalTargetState`를 추가했다.
프론트엔드에서 `SrStatus`의 죽은 pseudo-status(`APPROVAL_PENDING`/`REJECTED`, 실제 백엔드 enum에 없어 도달 불가능하던 분기)를 제거하고 공용 `deriveApprovalStatusDisplay` 기반 파생 표시로 대체했다.
통합 테스트에서 발견된 CRITICAL 회귀(기존 활성 규칙 id=1의 요청자 스코프가 END_USER로 남아있어 게이트가 무력화되던 문제)를 42번 마이그레이션으로 SERVICE_DESK_AGENT로 보정했다.
통합 테스트에서 발견된 FE 결함(전이 버튼 disable 조건이 `IN_FULFILLMENT` 하드코딩이라 신규 targetState 지점에서 오동작하던 문제)을 targetState 일반화 로직으로 수정했다.

## 3. 변경 파일

- `source/backend/src/main/java/com/itsm/srm/application/{ServiceRequestService,dto/ServiceRequestDetailResponse}.java`
- `source/db/sql/42_approval_process_id1_requester_role_fix.sql`
- `source/frontend/src/features/service-request/{types.ts,status.ts,RequestDetailPage.tsx,RequestListPage.tsx}`
- `docs/02_plan/api_spec/service-request.md`

## 4. 테스트 결과

1차 통합 테스트에서 SRM 관련 CRITICAL 회귀(규칙 id=1 요청자 스코프 불일치) 1건과 FE 전이 버튼 disable 결함 1건이 발견됐다.
각각 42번 마이그레이션과 `RequestDetailPage.tsx` 수정으로 해결 후 재테스트에서 전체 PASS했다(`docs/04_test/20260722-040618`, `docs/04_test/20260722-042424`).
코드 리뷰 발견 사항 없었다.
커밋 `c8f9386`으로 반영했다.
