# CLAUDE.md

서비스 요청(SRM) 관리 기능. 포털·요청 제출·목록·큐·상세·카탈로그 관리·지표 화면·API·타입, 상태/SLA 표시 매핑 제공. END_USER/SERVICE_DESK_AGENT/PROCESS_OWNER 역할 기반. API 계약: service-request.md. 승인 대기·결정은 승인 프로세스 커스텀 기능(유지보수 요청)으로 `features/common/ApprovalInboxPage.tsx`(SCR-COM-014, 전 도메인 공용)로 이관(기존 SCR-SRM-006 제거).

## 파일
- `api.ts` — SRM API 호출(`srmApi`: 카탈로그 목록/상세/생성/수정, 지식 추천, 요청 생성/목록/상세, 큐 목록, 담당자 배정, 상태 전이, 코멘트, CSAT, 지표). 승인 관련 API(구 API-SRM-011/012)는 공용 승인 API(`features/common/api.ts`)로 대체되어 제거.
- `types.ts` — SRM 도메인 타입(`SrStatus`/`SlaStatus`/`TargetStatus`, `CatalogItem*`/`RequestSummary`/`RequestDetail`/`RequestMetrics` 등). `RequestDetail.approval`(`RequestApproval`)은 `approvalRequestId`/`status`만 보유(진행 상태 조회는 공용 API-COM-004). 동적 폼 스키마(`FormFieldSchema`)는 공통 컴포넌트와 계약 공유.
- `status.ts` — 요청 상태·SLA 라벨/tone 매핑.
- `format.ts` — 날짜·일시 표시 포맷터.
- `PortalPage.tsx` — 서비스 포털/카탈로그 탐색(SCR-SRM-001).
- `RequestSubmitPage.tsx` — 요청 제출(동적 폼)(SCR-SRM-002).
- `RequestListPage.tsx` — 요청 목록(SCR-SRM-003).
- `RequestQueuePage.tsx` — 요청 큐(Agent)(SCR-SRM-004).
- `RequestDetailPage.tsx` — 요청 상세·상태 전이·코멘트·CSAT(SCR-SRM-005). 승인 패널은 공용 `ApprovalPanel`(`components/common`)에 API-COM-004 조회 결과(`steps`/`currentStepNo`) 주입해 진행 상태만 표시(결정 액션 없음, 처리는 SCR-COM-014에서).
- `CatalogManagePage.tsx` — 서비스 카탈로그 관리(양식 빌더)(SCR-SRM-007). 승인 필요 토글 제거(승인 프로세스 커스텀 기능으로 SCR-ADMIN-008에서 별도 설정).
- `MetricsPage.tsx` — 요청 지표(CSAT·SLA 등)(SCR-SRM-008).
