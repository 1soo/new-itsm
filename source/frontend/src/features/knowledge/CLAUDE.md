# CLAUDE.md

지식(KM) 관리 기능. 검색/목록·열람(셀프서비스)·작성/편집·검토·게시 승인함·지표 대시보드 화면과 API·타입, 상태 표시 매핑을 제공한다. 검색/열람은 END_USER/SERVICE_DESK_AGENT/KNOWLEDGE_CONTRIBUTOR/KNOWLEDGE_GATEKEEPER 공통, 작성/편집은 KNOWLEDGE_CONTRIBUTOR 전용, 검토·지표는 KNOWLEDGE_GATEKEEPER 전용. API 계약은 knowledge.md 기준.

## 파일
- `api.ts` — KM API 호출(`knowledgeApi`: 검색/목록/상세, 작성/수정/삭제, 상태 전이(검토 요청), 검토 승인/반려·대기 목록, 유용성 평가, 카테고리 목록, KCS 티켓 연계, 지표).
- `types.ts` — KM 도메인 타입(`ArticleStatus`, `ArticleSummary`/`ArticleDetail`/`Category`/`ReviewQueueItem`/`KnowledgeMetrics` 등).
- `status.ts` — 상태(초안/검토/게시) 라벨/tone 매핑, 상수 목록(`ARTICLE_STATUSES`).
- `format.ts` — 일시 표시 포맷터.
- `KnowledgeListPage.tsx` — 지식베이스 검색/목록(SCR-KM-001).
- `ArticleViewPage.tsx` — 기사 열람·유용성 평가(SCR-KM-002).
- `ArticleEditPage.tsx` — 기사 작성·편집·검토 요청·삭제(SCR-KM-003). id 없으면 신규 작성, 있으면 편집(같은 컴포넌트 재사용).
- `ReviewInboxPage.tsx` — 검토·게시 승인함(SCR-KM-004).
- `KnowledgeMetricsPage.tsx` — 지식 지표 대시보드(SCR-KM-005).
