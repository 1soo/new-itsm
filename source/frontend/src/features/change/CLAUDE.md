# CLAUDE.md

변경(CHG) 관리 기능. 목록·RFC 생성·상세(6단계 전이·구현결과·연계)·일정 캘린더·지표 화면·API·타입, 상태/유형/위험도 표시 매핑 제공. CHANGE_MANAGER/APPROVER 역할 기반. API 계약: change.md. 승인 대기·결정은 승인 프로세스 커스텀 기능(유지보수 요청)으로 `features/common/ApprovalInboxPage.tsx`(SCR-COM-014, 전 도메인 공용)로 완전 이관(기존 SCR-CHG-004 제거, 위험도 기반 CAB 자동 라우팅·`approvalRoute` 개념도 제거 — 실제 게이트 차단(409)은 Stage 2에서 연동, 이번 스테이지는 화면·데이터 형태만 최종 스펙으로 전환).

## 파일
- `api.ts` — CHG API 호출(`changeApi`: 목록/RFC 생성/상세, 6단계 상태 전이, 유형·위험 변경, 구현 결과 기록, 인시던트/문제 연계, 일정 조회, 표준 변경 템플릿 목록, 지표). 승인 관련 API(구 API-CHG-006/007)는 공용 승인 API(`features/common/api.ts`)로 대체되어 제거.
- `types.ts` — CHG 도메인 타입(6단계 `ChangeStatus`, `ChangeType`/`Risk`, `ChangeSummary`/`ChangeDetail`/`ChangeMetrics` 등). `ChangeDetail.approval`(`ChangeApproval`)은 `approvalRequestId`/`status`만 보유(진행 상태 조회는 공용 API-COM-004). `ApprovalRoute`/`ChangeApprovalRecord`/`ApprovalQueueItem`은 제거됨.
- `status.ts` — 상태·유형·위험도 라벨/tone 매핑, 연계 항목 유형 라벨(`linkTargetLabel`, ChangeDetailPage 드롭다운·나열 패널 공용), 6단계 순서 전이 fallback(`fallbackTransitions`), 상수 목록.
- `format.ts` — 날짜·일시 표시 포맷터.
- `ChangeListPage.tsx` — 변경 목록(SCR-CHG-001).
- `ChangeCreatePage.tsx` — 변경 요청(RFC) 생성(SCR-CHG-002). 표준 변경 선택 시 템플릿 조회·승인 생략 안내.
- `ChangeDetailPage.tsx` — 변경 상세·상태 전이(승인 전 구현 전이 UI 차단)·구현결과·연계(SCR-CHG-003). 승인 패널은 공용 `ApprovalPanel`(`components/common`)에 API-COM-004 조회 결과 주입해 진행 상태만 표시.
- `ChangeSchedulePage.tsx` — 변경 일정 캘린더(SCR-CHG-005). 공용 캘린더 컴포넌트 미도입 — 기능 전용 월 그리드 최소 구현.
- `ChangeMetricsPage.tsx` — 변경 지표 대시보드(SCR-CHG-006).
