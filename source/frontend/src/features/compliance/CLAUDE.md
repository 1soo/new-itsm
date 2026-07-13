# CLAUDE.md

컴플라이언스(COMP) 관리 기능. 요구사항 목록·등록·상세(책임자 지정·시정조치 등록/순서전이·변경 요청 연계·감사 로그 조회)·준수 현황 대시보드 화면·API·타입, 준수/시정조치 상태 표시 매핑 제공. COMPLIANCE_OFFICER 역할 기반. API 계약: compliance.md. 승인 대기·결정은 승인 프로세스 커스텀 기능(유지보수 요청)으로 시정조치 IN_PROGRESS→RESOLVED 전이에 공용 게이트 연동(매칭 승인 프로세스 없으면 기존처럼 즉시 전이). **게이트가 시정조치 개별 단위**로 걸려(요구사항 하나에 시정조치 여러 건 가능) `approval`도 시정조치 항목마다 별도로 붙음(요구사항 전체 단일 필드 아님). 처리는 `features/common/ApprovalInboxPage.tsx`(SCR-COM-014, 전 도메인 공용).

## 파일
- `api.ts` — COMP API 호출(`complianceApi`: 목록/등록/상세/수정, 변경 연계, 책임자 지정, 시정조치 등록·상태 전이, 컴플라이언스 감사 로그 조회, 준수 현황 지표).
- `types.ts` — COMP 도메인 타입(`ComplianceStatus`/`CorrectiveActionStatus`, `RequirementSummary`/`RequirementDetail`/`CorrectiveAction`/`ComplianceAuditLog`/`ComplianceMetrics`, 입력·쿼리 타입 등). `CorrectiveAction.approval`(`ComplianceApproval`)은 시정조치 항목별로 `approvalRequestId`/`status`만 보유(진행 상태 조회는 공용 API-COM-004, 항목마다 개별 조회).
- `status.ts` — 준수 상태·시정조치 상태 라벨(`complianceStatusLabel`/`actionStatusLabel`, `(t, value)` 시그니처)/tone 매핑, 감사 로그 이벤트 유형 라벨(`auditEventTypeLabel`, api_spec 3종 코드 + 미지정 코드는 원문 표시), 시정조치 순서 전이(탐지→조치중→해결) 다음 단계 계산(`nextActionTransition`).
- `format.ts` — 날짜·일시 표시 포맷터.
- `ComplianceListPage.tsx` — 요구사항 목록(SCR-COMP-001). 필터: 준수 상태·책임자 지정 여부.
- `ComplianceCreatePage.tsx` — 요구사항 등록(SCR-COMP-002). 이름·근거(필수)·적용범위(선택).
- `ComplianceDetailPage.tsx` — 요구사항 상세·책임자 지정·시정조치 등록/전이(RESOLVED 전이는 시정조치별 승인 게이트로 버튼 disable+tooltip)·변경 연계·감사 로그(SCR-COMP-003). 상단 "수정" 버튼으로 이름·근거·적용범위 인라인 편집 폼 전환(API-COMP-004, 구성요소 표에 명시 안 됐으나 dev-lead 판단으로 최소 UI 추가 — 등록 폼(SCR-COMP-002) 필드 재사용). 시정조치 목록의 각 항목마다 매칭 승인 프로세스 있으면 공용 `ApprovalPanel`(`components/common`)을 항목별로 렌더링(전 도메인 중 유일하게 승인 패널이 단일 아니라 항목 개수만큼 반복).
- `ComplianceMetricsPage.tsx` — 준수 현황 대시보드(SCR-COMP-004). KPI(준수율·미해결 시정조치 건수)는 metrics API, 요구사항별 상태 표는 목록 API(전체 조회, size=100)로 채움(metrics API가 집계값만 제공).
