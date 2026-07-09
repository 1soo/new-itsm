# CLAUDE.md

문제(PRB) 관리 기능. 목록·등록·상세·KEDB(알려진 오류) 검색 화면과 API·타입, 상태/우선순위/출처 표시 매핑을 제공한다. PROBLEM_MANAGER 역할 기반. API 계약은 problem.md 기준.

## 파일
- `api.ts` — PRB API 호출(`problemApi`: 목록/등록/상세, 6단계 상태 전이, RCA 저장, 워크어라운드, 알려진 오류 생성·KEDB 검색, 인시던트/변경 연계, 후속 조치 등록·상태 변경, 종료).
- `types.ts` — PRB 도메인 타입(6단계 `ProblemStatus`, `Origin`/`Level`/`ProblemPriority`, `ProblemSummary`/`ProblemDetail`/`Rca`/`KnownError`, 입력·쿼리 타입 등).
- `status.ts` — 상태·우선순위·출처·조치 라벨/tone 매핑, 우선순위 매트릭스(`computePriority`), 6단계 순서 전이 fallback(`fallbackTransitions`), 상수 목록.
- `format.ts` — 날짜 표시 포맷터.
- `ProblemListPage.tsx` — 문제 목록(SCR-PRB-001).
- `ProblemCreatePage.tsx` — 문제 등록(SCR-PRB-002).
- `ProblemDetailPage.tsx` — 문제 상세·상태 전이·RCA·조치(SCR-PRB-003).
- `KnownErrorSearchPage.tsx` — KEDB(알려진 오류) 검색(SCR-PRB-004).
