# CLAUDE.md

문제(PRB) 관리 기능. 목록·등록·상세·KEDB(알려진 오류) 검색 화면과 API·타입, 상태/우선순위/출처 표시 매핑을 제공한다. PROBLEM_MANAGER 역할 기반. API 계약은 problem.md 기준. 승인 대기·결정은 승인 프로세스 커스텀 기능(유지보수 요청)으로 WORKAROUND→RESOLVED_CLOSED 전이(상태 전이 API 경유)에 공용 게이트가 연동됐다(매칭되는 승인 프로세스가 없으면 기존처럼 즉시 전이). 별도의 "종료"(force 경고) 액션은 게이트 대상이 아니다. 처리는 `features/common/ApprovalInboxPage.tsx`(SCR-COM-014, 전 도메인 공용)에서 수행.

## 파일
- `api.ts` — PRB API 호출(`problemApi`: 목록/등록/상세, 6단계 상태 전이, RCA 저장, 워크어라운드, 알려진 오류 생성·KEDB 검색, 인시던트/변경 연계, 후속 조치 등록·상태 변경, 종료).
- `types.ts` — PRB 도메인 타입(6단계 `ProblemStatus`, `Origin`/`Level`/`ProblemPriority`, `ProblemSummary`/`ProblemDetail`/`Rca`/`KnownError`, 입력·쿼리 타입 등). `ProblemDetail.approval`(`ProblemApproval`)은 `approvalRequestId`/`status`만 보유(진행 상태 조회는 공용 API-COM-004).
- `status.ts` — 상태·우선순위·출처·조치 라벨/tone 매핑, 우선순위 매트릭스(`computePriority`), 6단계 순서 전이 fallback(`fallbackTransitions`), 상수 목록.
- `format.ts` — 날짜 표시 포맷터.
- `ProblemListPage.tsx` — 문제 목록(SCR-PRB-001).
- `ProblemCreatePage.tsx` — 문제 등록(SCR-PRB-002).
- `ProblemDetailPage.tsx` — 문제 상세·상태 전이(RESOLVED_CLOSED 전이 승인 게이트로 버튼 disable+tooltip)·RCA·조치(SCR-PRB-003). 승인 패널은 공용 `ApprovalPanel`(`components/common`)에 API-COM-004 조회 결과를 주입해 진행 상태만 표시(매칭 없으면 패널 미노출).
- `KnownErrorSearchPage.tsx` — KEDB(알려진 오류) 검색(SCR-PRB-004).
