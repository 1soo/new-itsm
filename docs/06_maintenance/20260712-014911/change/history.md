---
date: 20260712-014911
domain: change
change_type: [modified]
keywords: [공용 승인 엔진 이관, CAB 승인, 구현 버튼 가드]
---

# 유지보수 이력 — change

> 유지보수 일시: 20260712-014911 · 도메인: change

## 1. 요구사항

CAB 승인을 공용 승인 엔진으로 이관해야 한다.
승인 대기 중에는 "구현"(이행) 버튼이 비활성화되고, 클릭 시도 시 안내가 노출되어야 한다.

## 2. 해결 방법

먼저(0b1caa3) 기존 자동라우팅 스텁(ApprovalRoute 도메인, ChangeApprovalDecision/ChangeApprovalRequest/ChangeApprovalResponse/PendingChangeApprovalResponse DTO, 전용 승인함 화면 ChangeApprovalInboxPage)을 제거했다.
이후(e133dfb) `ChangeService`를 공용 엔진 연동으로 수정하고 `ChangeApprovalTicketSummaryProvider`를 신규 구현해 실연동을 완료했다.
승인 대기 중 "구현" 버튼은 `disabled` 처리 + 툴팁("승인 완료 전에는 구현 단계로 전이할 수 없습니다")으로 안내하고, 클릭(409 유발) 시 즉시 토스트를 노출하며 승인 패널이 새로고침 없이 갱신되도록 구현했다.

## 3. 변경 파일

- `source/backend/.../change/application/ChangeService.java`
- `source/backend/.../change/application/ChangeApprovalTicketSummaryProvider.java`(신규, e133dfb)
- `source/backend/.../change/application/CLAUDE.md`(e133dfb)
- `source/backend/.../change/application/dto/{ChangeDetailResponse,ClassificationResponse}.java`(및 ChangeApprovalDecision류 DTO 제거)
- `source/backend/.../change/domain/{ApprovalRoute(제거),ChangeRequest}.java`
- `source/backend/.../change/presentation/ChangeController.java`
- `test/.../change/application/ChangeServiceTest.java`
- `test/.../change/integration/ChangeIntegrationTest.java`
- `source/frontend/src/features/change/{ChangeApprovalInboxPage.tsx(제거),ChangeDetailPage.tsx,api.ts,status.ts,types.ts}`
- `source/frontend/src/features/admin/ApprovalProcessFormPage.tsx`(e133dfb, CHANGE 요청유형 연동 보정)
- `source/frontend/src/features/service-request/RequestDetailPage.tsx`(e133dfb, SRM 전이버튼 숨김 보정)
- `docs/02_plan/{api_spec/change.md,database/change.md,screen/change.md}`

## 4. 테스트 결과

Stage 2 통합 테스트 총 13건 전부 PASS했다.
NORMAL RFC는 REVIEW→PLANNING→APPROVAL 통과 후 IMPLEMENTATION 전이 시도 시 게이트 409로 차단되고, 승인 후 재시도 시 200으로 통과함을 확인했다.
EMERGENCY RFC는 매칭 규칙이 없어 전 구간 게이트 없이 통과함을 확인했다.
승인 대기 중 SRM 상세의 "이행 중" 버튼이 숨김 처리 없이 노출되던 결함을 FE에서 전이 버튼 필터링으로 수정해 회귀 없음을 재확인했다.
커밋 `0b1caa3`(스텁 제거), `e133dfb`(실연동)로 반영했다.
