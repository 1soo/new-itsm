# CLAUDE.md

컴플라이언스(COMP) 관리 기능. 요구사항 목록·등록·상세(책임자 지정·시정조치 등록/순서전이·변경 요청 연계·감사 로그 조회)·준수 현황 대시보드 화면과 API·타입, 준수/시정조치 상태 표시 매핑을 제공한다. COMPLIANCE_OFFICER 역할 기반. API 계약은 compliance.md 기준.

## 파일
- `api.ts` — COMP API 호출(`complianceApi`: 목록/등록/상세/수정, 변경 연계, 책임자 지정, 시정조치 등록·상태 전이, 컴플라이언스 감사 로그 조회, 준수 현황 지표).
- `types.ts` — COMP 도메인 타입(`ComplianceStatus`/`CorrectiveActionStatus`, `RequirementSummary`/`RequirementDetail`/`CorrectiveAction`/`ComplianceAuditLog`/`ComplianceMetrics`, 입력·쿼리 타입 등).
- `status.ts` — 준수 상태·시정조치 상태 라벨/tone 매핑, 시정조치 순서 전이(탐지→조치중→해결) 다음 단계 계산(`nextActionTransition`).
- `format.ts` — 날짜·일시 표시 포맷터.
- `ComplianceListPage.tsx` — 요구사항 목록(SCR-COMP-001). 필터: 준수 상태·책임자 지정 여부.
- `ComplianceCreatePage.tsx` — 요구사항 등록(SCR-COMP-002). 이름·근거(필수)·적용범위(선택).
- `ComplianceDetailPage.tsx` — 요구사항 상세·책임자 지정·시정조치 등록/전이·변경 연계·감사 로그(SCR-COMP-003). 상단 "수정" 버튼으로 이름·근거·적용범위 인라인 편집 폼 전환(API-COMP-004, 구성요소 표에 명시되지 않았으나 dev-lead 판단에 따라 최소 UI로 추가 — 등록 폼(SCR-COMP-002) 필드 재사용).
- `ComplianceMetricsPage.tsx` — 준수 현황 대시보드(SCR-COMP-004). KPI(준수율·미해결 시정조치 건수)는 metrics API, 요구사항별 상태 표는 목록 API(전체 조회, size=100)로 채운다(metrics API가 집계값만 제공하기 때문).
