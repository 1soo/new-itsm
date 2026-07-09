# CLAUDE.md

인시던트(INC) 관리 기능. 목록·등록·상세·포스트모템·지표 화면과 API·타입, 상태/심각도 표시 매핑을 제공한다. SERVICE_DESK_AGENT/INCIDENT_MANAGER 역할 기반. API 계약은 incident.md 기준.

## 파일
- `api.ts` — INC API 호출(`incidentApi`: 목록/등록/상세, 심각도·상태 전이, 대응 역할 배정, 에스컬레이션, 타임라인 업데이트, 해결, 포스트모템, 문제 연계, 지표).
- `types.ts` — INC 도메인 타입(`Severity`/`Priority`/`IncidentStatus`, `IncidentSummary`/`IncidentDetail`/`Postmortem`/`IncidentMetrics`, 입력·쿼리 타입 등).
- `status.ts` — 상태·심각도 라벨/tone 매핑과 상수 목록(`SEVERITIES`/`PRIORITIES`/`INCIDENT_STATUSES`).
- `format.ts` — 날짜·일시·분 단위 지표 표시 포맷터.
- `IncidentListPage.tsx` — 인시던트 목록(SCR-INC-001).
- `IncidentCreatePage.tsx` — 인시던트 등록(SCR-INC-002).
- `IncidentDetailPage.tsx` — 인시던트 상세·상태 전이·대응(SCR-INC-003).
- `PostmortemPage.tsx` — 포스트모템 작성/수정(SCR-INC-004).
- `IncidentMetricsPage.tsx` — 인시던트 지표(심각도 분포·MTTR 등)(SCR-INC-005).
