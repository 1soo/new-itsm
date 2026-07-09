# CLAUDE.md

서비스 요청(SRM) 관리 기능. 포털·요청 제출·목록·큐·상세·승인 대기함·카탈로그 관리·지표 화면과 API·타입, 상태/SLA 표시 매핑을 제공한다. END_USER/SERVICE_DESK_AGENT/APPROVER/PROCESS_OWNER 역할 기반. API 계약은 service-request.md 기준.

## 파일
- `api.ts` — SRM API 호출(`srmApi`: 카탈로그 목록/상세/생성/수정, 지식 추천, 요청 생성/목록/상세, 큐 목록, 담당자 배정, 상태 전이, 승인/반려·대기 목록, 코멘트, CSAT, 지표).
- `types.ts` — SRM 도메인 타입(`SrStatus`/`SlaStatus`/`TargetStatus`, `CatalogItem*`/`RequestSummary`/`RequestDetail`/`ApprovalItem`/`RequestMetrics` 등). 동적 폼 스키마(`FormFieldSchema`)는 공통 컴포넌트와 계약 공유.
- `status.ts` — 요청 상태·SLA 라벨/tone 매핑.
- `format.ts` — 날짜·일시 표시 포맷터.
- `PortalPage.tsx` — 서비스 포털/카탈로그 탐색(SCR-SRM-001).
- `RequestSubmitPage.tsx` — 요청 제출(동적 폼)(SCR-SRM-002).
- `RequestListPage.tsx` — 요청 목록(SCR-SRM-003).
- `RequestQueuePage.tsx` — 요청 큐(Agent)(SCR-SRM-004).
- `RequestDetailPage.tsx` — 요청 상세·상태 전이·코멘트·CSAT(SCR-SRM-005).
- `ApprovalInboxPage.tsx` — 승인 대기함(Approver)(SCR-SRM-006).
- `CatalogManagePage.tsx` — 서비스 카탈로그 관리(양식 빌더)(SCR-SRM-007).
- `MetricsPage.tsx` — 요청 지표(CSAT·SLA 등)(SCR-SRM-008).
