# CLAUDE.md

인시던트(INC) 관리 기능. 목록·등록·상세·포스트모템·지표 화면·API·타입, 상태/심각도 표시 매핑 제공. SERVICE_DESK_AGENT/INCIDENT_MANAGER 역할 기반. API 계약: incident.md. 승인 대기·결정은 승인 프로세스 커스텀 기능(유지보수 요청)으로 IN_PROGRESS→RESOLVED 전이에 공용 게이트 연동(매칭 승인 프로세스 없으면 기존처럼 즉시 전이). 처리는 `features/common/ApprovalInboxPage.tsx`(SCR-COM-014, 전 도메인 공용).

## 파일
- `api.ts` — INC API 호출(`incidentApi`: 목록/등록/상세, 심각도·상태 전이, 대응 역할 배정, 에스컬레이션, 타임라인 업데이트, 해결, 포스트모템, 문제 연계, 지표).
- `types.ts` — INC 도메인 타입(`Severity`/`Priority`/`IncidentStatus`, `IncidentSummary`/`IncidentDetail`/`Postmortem`/`IncidentMetrics`, 입력·쿼리 타입 등). `IncidentDetail.approval`(`IncidentApproval`)은 `approvalRequestId`/`status`만 보유(진행 상태 조회는 공용 API-COM-004).
- `status.ts` — 상태·심각도 라벨/tone 매핑과 상수 목록(`SEVERITIES`/`PRIORITIES`/`INCIDENT_STATUSES`).
- `format.ts` — 날짜·일시·분 단위 지표 표시 포맷터.
- `IncidentListPage.tsx` — 인시던트 목록(SCR-INC-001).
- `IncidentCreatePage.tsx` — 인시던트 등록(SCR-INC-002).
- `IncidentDetailPage.tsx` — 인시던트 상세·상태 전이(RESOLVED 전이 승인 게이트로 버튼 disable+tooltip)·해결 처리(`ResolveForm` 제출도 동일 게이트로 disable+tooltip, 409 시 조용히 재조회)·대응(SCR-INC-003). 승인 패널은 공용 `ApprovalPanel`(`components/common`)에 API-COM-004 조회 결과 주입해 진행 상태만 표시(매칭 없으면 패널 미노출).
- `PostmortemPage.tsx` — 포스트모템 작성/수정(SCR-INC-004).
- `IncidentMetricsPage.tsx` — 인시던트 지표(심각도 분포·MTTR 등)(SCR-INC-005).
