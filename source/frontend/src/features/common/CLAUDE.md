# CLAUDE.md

공통(알림 확인처리 · 전 도메인 공용 승인 대기함) 기능. 특정 도메인에 속하지 않는 교차 기능 디렉토리. 헤더 알림 팝오버(SCR-COM-002)의 "모두 지우기"·개별 X 버튼 확인처리(API-COM-001/002)와, 승인 프로세스 커스텀 기능(유지보수 요청)으로 도입된 전 도메인 공용 승인 대기함(SCR-COM-014, API-COM-003~005) 제공. API 계약: `api_spec/common.md`.

## 파일
- `api.ts` — common API 호출(`commonApi`: `dismissNotifications`(개별/일괄 확인처리, 멱등)·`listDismissals`(확인처리 이력 조회)·`listMyApprovals`(전 도메인 공용 승인 대기, scope=mine 고정)·`getApproval`(인스턴스 상세)·`decide`(승인/반려)). 공통 apiClient 경유.
- `types.ts` — common 도메인 타입. 알림 확인처리(`NotificationType`(APPROVAL|ASSET_EXPIRY로 전 도메인 통합)/`DismissalItem`/`DismissResult`/`NotificationDismissalListResponse`) + 공용 승인(`TicketType`(승인 대상 9개 도메인)/`ApprovalListItem`/`ApprovalDetail`(차수별 진행·역할별 결정)/`ApprovalDecisionRequest`/`ApprovalDecisionResult`). 차수·역할 결정 구조(`ApprovalStep`/`ApprovalStepStatus`)는 `components/common`(`approval-schema.ts`)의 공용 계약을 그대로 재사용(re-export) — 중복 정의 금지.
- `status.ts` — `TicketType`별 표시 라벨(`ticketTypeApprovalLabel`(헤더 알림 도메인 라벨)/`ticketTypeLabel`(목록 배지))과 상세 이동 경로(`ticketDetailPath`) 매핑. 헤더 알림(`routes/AppLayout.tsx`)과 승인 대기함(`ApprovalInboxPage.tsx`)이 공유.
- `format.ts` — 일시 표시 포맷터(`formatDateTime`). 다른 도메인 feature의 format 유틸을 cross-import하지 않으려 자체 보유.
- `ApprovalInboxPage.tsx` — 승인 대기함(전 도메인 공용, SCR-COM-014). 기존 SCR-SRM-006/SCR-CHG-004/SCR-KM-004 대체. 도메인 필터 → 목록(티켓유형 배지·티켓요약·차수·요청자·요청일) → 행 "상세" 클릭 시 모달로 차수 진행 현황(공용 `ApprovalStepProgress`, `components/common`)과 승인/반려 액션(반려 사유 필수, 이 화면이 직접 조립) 표시. 행별 "상세보기" 버튼(2026-07-16 유지보수 요청, "상세"와 별개)은 `status.ts`의 `ticketDetailPath`로 해당 티켓의 실제 상세 화면으로 이동(신규 API 없음).
