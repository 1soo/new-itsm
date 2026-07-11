# docs/06_maintenance — 유지보수 이력 인덱스

이 디렉토리는 개발 완료 후 발생한 추가 요구사항(유지보수)의 이력을 도메인별로 기록한다.

## 구조

- `{yyyyMMdd-HHmmss}/{domain}/report.md`: 각 유지보수 건의 상세 이력 (요구사항 · 해결 방법 · 변경 파일 · 테스트 결과)

## 인덱스

`maintainer` 에이전트는 유지보수 이력을 생성할 때마다 아래 표에 한 줄씩 추가한다.
신규 유지보수 요청을 분석할 때는 **이 표만 먼저 확인**하여 유사 이력이 있는지 판단하고, 관련 있어 보이는 항목의 `report.md`만 선택적으로 열람한다. (전체 report를 전수 조사하지 않는다 — 토큰 최소화)

| 일시 | 도메인 | 요약 | 경로 |
|------|--------|------|------|
| 20260711-140000 | auth | 역할-메뉴 동적 매핑(screen/screen_role 확장, 메뉴 관리 화면 신규) | docs/06_maintenance/20260711-140000/auth/report.md |
| 20260711-140000 | common | 헤더 알림 확인처리(모두 지우기/개별 X, notification_dismissal 신규) | docs/06_maintenance/20260711-140000/common/report.md |
| 20260711-160000 | common | 헤더 알림 5초 polling 전환(merge 정책·8건 cap 폐지·백그라운드 정지) | docs/06_maintenance/20260711-160000/common/report.md |
| 20260712-020000 | common | 공용 승인 엔진 신규 구축(도메인/요청유형/역할 기반 커스텀 다단계 승인, 6개 하드코딩 승인 로직 대체) | docs/06_maintenance/20260712-020000/common/report.md |
| 20260712-020000 | auth | 승인 프로세스 관리 화면(SCR-ADMIN-007/008) 및 CRUD API(API-AUTH-023~029) 신규 | docs/06_maintenance/20260712-020000/auth/report.md |
| 20260712-020000 | srm | 서비스요청 approverRole 하드코딩 승인을 공용 승인 엔진으로 이관 | docs/06_maintenance/20260712-020000/srm/report.md |
| 20260712-020000 | change | CAB 승인을 공용 승인 엔진으로 이관 | docs/06_maintenance/20260712-020000/change/report.md |
| 20260712-020000 | knowledge | 게시 승인(게이트키퍼)을 공용 승인 엔진으로 이관(승인/반려 시 자동 게시·DRAFT 전환) | docs/06_maintenance/20260712-020000/knowledge/report.md |
| 20260712-020000 | incident | IN_PROGRESS→RESOLVED 전이 및 해결 처리(resolve) 신규 승인 게이트 추가(우회 결함 수정) | docs/06_maintenance/20260712-020000/incident/report.md |
| 20260712-020000 | problem | WORKAROUND→RESOLVED_CLOSED 전이 신규 승인 게이트 추가(강제종료 제외) | docs/06_maintenance/20260712-020000/problem/report.md |
| 20260712-020000 | asset | 생애주기 전이·자산 폐기(RETIREMENT) 신규 승인 게이트 추가 | docs/06_maintenance/20260712-020000/asset/report.md |
| 20260712-020000 | vulnerability | PRIORITIZATION→REMEDIATION 전이 신규 승인 게이트 추가 | docs/06_maintenance/20260712-020000/vulnerability/report.md |
| 20260712-020000 | compliance | 시정조치 상태전이 신규 승인 게이트 추가(TicketType.CORRECTIVE_ACTION으로 항목별 독립 승인) | docs/06_maintenance/20260712-020000/compliance/report.md |
| 20260712-020000 | esm | 부서요청 상태전이 신규 승인 게이트 추가(HR 케이스·체크리스트 제외) | docs/06_maintenance/20260712-020000/esm/report.md |
