---
date: 20260712-014911
domain: srm
change_type: [new, modified, removed]
keywords: [공용 승인 엔진 이관, approverRole 하드코딩 제거, 승인 게이트, 서비스 카탈로그]
---

# 유지보수 이력 — srm

> 유지보수 일시: 20260712-014911 · 도메인: srm

## 1. 요구사항

기존 서비스요청(SRM)의 approverRole 하드코딩 승인 로직을 공용 승인 엔진으로 이관해야 한다.
승인 대기 중에는 이행(전이) 시도가 게이트에 의해 차단되어야 한다.

## 2. 해결 방법

`ServiceRequestService`, `ServiceCatalogService`, `RequestStateMachine`을 공용 승인 엔진 연동으로 수정했다.
`SrmApprovalRequestSubtypeProvider`, `SrmApprovalTicketSummaryProvider`를 신규 구현했다.
기존 하드코딩 승인 DTO(ApprovalDecision, ApprovalDecisionRequest, ApprovalDecisionResponse, PendingApprovalResponse)와 전용 `ApprovalController`, 전용 승인함 화면(ApprovalInboxPage)을 제거했다.
서비스 카탈로그 항목에서 approval_required/approver_role 컬럼 기반 로직을 제거해 카탈로그 생성 시 승인 관련 필드를 더 이상 요구하지 않도록 했다.

## 3. 변경 파일

- `source/backend/.../srm/application/{ServiceRequestService,ServiceCatalogService,RequestStateMachine}.java`
- `source/backend/.../srm/application/{SrmApprovalRequestSubtypeProvider,SrmApprovalTicketSummaryProvider}.java`(신규)
- `source/backend/.../srm/application/dto/RequestDetailResponse.java`(및 ApprovalDecision류 DTO 제거)
- `source/backend/.../srm/domain/{RequestStatus,ServiceCatalogItem}.java`
- `source/backend/.../srm/presentation/ApprovalController.java`(제거), `ServiceRequestController.java`
- `source/frontend/src/features/service-request/{ApprovalInboxPage.tsx(제거),CatalogManagePage.tsx,PortalPage.tsx,RequestDetailPage.tsx,api.ts,types.ts}`
- `test/.../srm/application/{ServiceCatalogServiceTest,ServiceRequestServiceTest}.java`
- `test/.../srm/integration/SrmApprovalIntegrationTest.java`
- `docs/02_plan/{api_spec/service-request.md,database/service-request.md,screen/service-request.md}`

## 4. 테스트 결과

Stage 1 통합 테스트 TC-SRM-001~009 전부 PASS했다.
AND 차수(두 역할 모두 승인 시 APPROVED)·OR 차수(1건 결정만으로 즉시 APPROVED)·반려 시 재시도 409 유지·이미 결정한 역할 재결정 시 409(APPROVAL_ALREADY_DECIDED)·전체 승인 후 재시도 200·0차 승인 규칙 매칭 시 게이트 없이 통과를 확인했다.
PROCESS_OWNER가 승인 필드 없이 카탈로그 항목을 생성할 수 있고(201), 무관 역할의 결정 시도는 403임을 확인했다.
Stage 2(CHANGE 연동) 재테스트에서 승인 대기 중 SRM 상세의 전이 버튼이 노출되던 결함을 FE에서 필터링하도록 수정해 회귀 없음을 확인했다.
커밋 `0b1caa3`으로 반영했다.
