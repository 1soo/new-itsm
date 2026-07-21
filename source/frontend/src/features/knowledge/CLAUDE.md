# CLAUDE.md

지식(KM) 관리 기능. 검색/목록·열람(셀프서비스)·작성/편집·지표 대시보드 화면·API·타입, 상태 표시 매핑 제공. 검색/열람은 END_USER/SERVICE_DESK_AGENT/KNOWLEDGE_CONTRIBUTOR/KNOWLEDGE_GATEKEEPER 공통, 작성/편집은 KNOWLEDGE_CONTRIBUTOR 전용, 지표는 KNOWLEDGE_GATEKEEPER 전용. API 계약: knowledge.md. 검토·게시 승인은 승인 프로세스 커스텀 기능(유지보수 요청)으로 `features/common/ApprovalInboxPage.tsx`(SCR-COM-014, 전 도메인 공용)로 이관(기존 SCR-KM-004 제거).

## 파일
- `api.ts` — KM API 호출(`knowledgeApi`: 검색/목록/상세, 작성/수정/삭제, 상태 전이(검토 요청), 유용성 평가, 카테고리 목록, KCS 티켓 연계, 지표). 검토 승인/반려·대기 목록 API(구 API-KM-007/008)는 공용 승인 API(`features/common/api.ts`)로 대체되어 제거.
- `types.ts` — KM 도메인 타입(`ArticleStatus`, `ArticleSummary`(`pendingApprovalTargetState` 포함, 2026-07-22 신규)/`ArticleDetail`(`approval: ArticleApproval` 포함, API-KM-002 v0.3)/`ArticleApproval`(`targetState` 포함, 2026-07-22 신규)/`ArticleTransitionResult`(API-KM-006 응답)/`Category`/`KnowledgeMetrics` 등).
- `status.ts` — 상태(초안/검토/게시) 라벨(`statusLabel(t, s)`, `knowledge:status.*`)/tone 매핑, 상수 목록(`ARTICLE_STATUSES`).
- `format.ts` — 일시 표시 포맷터.
- `KnowledgeListPage.tsx` — 지식베이스 검색/목록(SCR-KM-001). 상태 배지는 `pendingApprovalTargetState` 존재 시 공용 `deriveApprovalStatusDisplay`로 파생 표시(2026-07-22 신규).
- `ArticleViewPage.tsx` — 기사 열람·유용성 평가(SCR-KM-002). 상태 배지는 `deriveApprovalStatusDisplay`로 파생 표시(2026-07-22 신규, 재승인요청 버튼은 없음 — 편집 화면 전용).
- `ArticleEditPage.tsx` — 기사 작성·편집·검토 요청·삭제(SCR-KM-003). id 없으면 신규 작성, 있으면 편집(같은 컴포넌트 재사용). 상세 조회 시 `approval.approvalRequestId`가 있으면 API-COM-004로 차수 진행 상태 조회해 공용 `ApprovalPanel`(`components/common`)에 표시(SRM/CHANGE와 동일 패턴 — 페이지 재진입·새로고침에도 항상 복원). "검토 요청" 클릭 시 API-KM-006 응답이 `status="PUBLISHED"`면 승인 없이 게시 안내 토스트, `status="IN_REVIEW"`면 승인 대기 안내 토스트 후 상세 재조회로 패널 반영(결정은 SCR-COM-014에서). 상단 상태 배지는 `deriveApprovalStatusDisplay`로 파생 표시, `ApprovalPanel`에 `targetStateLabel`/`status`/`onResubmit`(반려 시 API-COM-006 재승인요청) 배선(2026-07-22 신규).
- `KnowledgeMetricsPage.tsx` — 지식 지표 대시보드(SCR-KM-005).
