# CLAUDE.md

변경(CHG) 관리 기능. 목록·RFC 생성·상세(6단계 전이·승인경로·구현결과·연계)·CAB 승인 대기함·일정 캘린더·지표 화면과 API·타입, 상태/유형/위험도/승인경로 표시 매핑을 제공한다. CHANGE_MANAGER/APPROVER 역할 기반. API 계약은 change.md 기준.

## 파일
- `api.ts` — CHG API 호출(`changeApi`: 목록/RFC 생성/상세, 6단계 상태 전이, 유형·위험 변경, 승인/반려·대기 목록, 구현 결과 기록, 인시던트/문제 연계, 일정 조회, 표준 변경 템플릿 목록, 지표).
- `types.ts` — CHG 도메인 타입(6단계 `ChangeStatus`, `ChangeType`/`Risk`/`ApprovalRoute`, `ChangeSummary`/`ChangeDetail`/`ChangeMetrics` 등).
- `status.ts` — 상태·유형·위험도·승인경로 라벨/tone 매핑, 6단계 순서 전이 fallback(`fallbackTransitions`), 상수 목록.
- `format.ts` — 날짜·일시 표시 포맷터.
- `ChangeListPage.tsx` — 변경 목록(SCR-CHG-001).
- `ChangeCreatePage.tsx` — 변경 요청(RFC) 생성(SCR-CHG-002). 표준 변경 선택 시 템플릿 조회·승인 생략 안내.
- `ChangeDetailPage.tsx` — 변경 상세·상태 전이(승인 전 구현 전이 UI 차단)·구현결과·연계(SCR-CHG-003).
- `ChangeApprovalInboxPage.tsx` — CAB 승인 대기함(SCR-CHG-004).
- `ChangeSchedulePage.tsx` — 변경 일정 캘린더(SCR-CHG-005). 공용 캘린더 컴포넌트 미도입 — 기능 전용 월 그리드 최소 구현.
- `ChangeMetricsPage.tsx` — 변경 지표 대시보드(SCR-CHG-006).
