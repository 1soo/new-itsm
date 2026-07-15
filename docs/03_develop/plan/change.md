# 개발 계획 — change (변경 관리, CHG)

> 도메인: change (CHG) · 개발 순서 5/7 · 작성: dev-lead · 2026-07-09

## 1. 목표

변경 요청(RFC) 등록·목록·상세(유형(표준/일반/긴급)·위험도·6단계 프로세스·승인 경로(자동/동료검토/CAB)·구현 결과 기록·인시던트/문제 연계), CAB 승인 대기함, 변경 일정(캘린더), 변경 지표 대시보드를 구현한다. auth/incident/problem/srm 기반과 common(approval, ticket_link, timeline_event, comment) 재사용.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/change.md`(SCR-CHG-001~006), 공통 SCR-COM-007/008
- API: `api_spec/change.md`(API-CHG-001~012)
- DB: `database/change.md`(change_template/change_request/change_affected_system) + `database/common.md`(approval, ticket_link, timeline_event, comment)
- 역할: `security/authorization/change_manager.md`(CHANGE_MANAGER), `security/authorization/approver.md`(APPROVER, CAB 승인)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- CHG 테이블: change_template(name UNIQUE), change_request(ticket_key CHG-YYYY-####, type STANDARD/NORMAL/EMERGENCY, risk HIGH/MEDIUM/LOW(미평가 NULL), status 6단계 REQUESTED/REVIEW/PLANNING/APPROVAL/IMPLEMENTATION/CLOSED, approval_route AUTO/PEER_REVIEW/CAB, implementation_plan, rollback_plan, scheduled_at, template_id FK, outcome/rolled_back/result_note), change_affected_system(change_id FK, system_name). 공통컬럼·FK.
- approval·ticket_link는 srm/incident/problem 단계에서 이미 도입됨 → 재사용(approval.ticket_type='CHANGE', ticket_link `*_type='CHANGE'`). 신규 테이블 아님.
- screen/screen_role 증분: SCR-CHG-001~006 + CHANGE_MANAGER, APPROVER 역할 매핑(change_manager.md, approver.md 기준). APPROVER는 이미 SRM 단계에서 존재하는 역할이면 화면/API 매핑만 증분.
- 표준 변경 템플릿 시드 데이터 1건 이상(예: "표준 패치 배포").
- 테스트 유저: CHANGE_MANAGER 권한 계정, APPROVER(CAB) 권한 계정 시드 추가(기존 유저 규칙 동일). APPROVER가 기존에 없다면 신규 시드.

### BE (dev-backend) — `source/backend/`
- API-CHG-001~012(api_spec). change 패키지 추가(incident/problem/srm 패키지 컨벤션 재사용).
- 상태 전이: 6단계 순서 전이, 순서 어긋난 전이 400. 승인 완료 전 IMPLEMENTATION 전이 시도 409. 권한 없으면 403.
- 유형·위험 분류(classification): 위험도 미평가·고위험 시 기본 CAB 경로, 그 외 PEER_REVIEW 또는 AUTO(표준 변경). 표준 변경(type=STANDARD, templateId 지정)은 승인 단계 자동 통과(approval_route=AUTO, 승인 생성 없이 즉시 통과 처리 또는 approval 자동 승인 기록 — 설계 문서 기준 "승인 단계 자동 통과").
- 승인/반려(API-CHG-006): common.ticket.Approval 재사용, SRM ApprovalController/역할 기반 승인 패턴과 동일하게 approver_role(CAB→APPROVER) 보유자가 공유 대기함에서 처리, 먼저 처리한 사용자가 결정자. 반려 사유 필수(누락 400), 이미 결정 409, approver_role 미보유 403.
- 승인 대기 목록(API-CHG-007): `GET /api/v1/approvals?scope=mine&type=change` — SRM의 `type=service-request`와 동일 엔드포인트를 공유하므로 SRM ApprovalController에 `type=change` 분기 추가(또는 공용 서비스로 위임). 신규 컨트롤러를 만들지 말고 기존 승인 인프라 확장.
- 구현 결과 기록(API-CHG-008): 승인되지 않은 변경(APPROVAL 이전 단계) → 400.
- 인시던트/문제 연계(API-CHG-009): ticket_link 재사용, INCIDENT/PROBLEM 모두 이미 구축됨 → **양방향 연계 정상 구현**(problem/incident 쪽 링크 조회에도 CHANGE 노출). 존재하지 않는 대상 → 400.
  - **연기분 회수**: problem 단계에서 CHANGE 연계를 `CHANGE_LINK_UNAVAILABLE`(400)로 스텁 처리한 부분(ProblemService)과, incident 쪽에 CHANGE 연계 스텁이 있다면 이번 단계에서 **완성**한다(change 엔티티 존재 검증 + ticket_link 양방향). problem/incident 통합회귀에 포함.
- 변경 일정(API-CHG-010): scheduled_at 기간 필터, 없으면 빈 배열.
- 표준 변경 템플릿 목록(API-CHG-011), 변경 지표(API-CHG-012: successRate/failureRate/emergencyRate, 데이터 없으면 빈 결과 — incident/problem MetricsService 패턴 재사용 가능하면 재사용).
- RBAC는 role 정의서(change_manager.md, approver.md, 단일 원천) 기준으로 각 API 인가.
- JUnit 통합테스트(incident/problem/srm integration 테스트 패턴 재사용).

### 크로스 도메인 (중요)
- **problem→CHANGE 연계 완성**: `source/backend/src/main/java/com/itsm/problem/application/ProblemService.java`의 `CHANGE_LINK_UNAVAILABLE` 스텁을 change_request 존재 검증 + ticket_link 양방향 생성으로 교체. problem 쪽 상세 응답(linkedChanges)에도 노출되는지 확인.
- **incident→CHANGE 연계**: incident에 CHANGE 연계 스텁/미구현이 있다면 동일하게 완성. 없다면(설계 문서에 인시던트→변경 직접 연계 API가 없으면) 스킵.
- 파일을 수정하게 되는 problem/incident 쪽은 각 담당(dev-backend)이 동일 인물이므로 충돌 없음. 단, 기존 로직 최소 수정 원칙 유지.

### UI (dev-ui) — `source/frontend/` 공통 영역
- 대부분 기존 컴포넌트 재사용(목록/상세 패턴, 배지, 캘린더 뷰는 신규 — 월/주 그리드 컴포넌트 필요 시 공용화). KPI 카드(지표 대시보드)는 problem/incident 대시보드 있으면 재사용, 없으면 최소 신규.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- 화면: SCR-CHG-001 목록(필터: 유형·상태·위험도·기간), 002 RFC 생성(유형/위험도 셀렉트, 표준 변경 선택 시 템플릿 목록·승인 생략 안내), 003 상세(6단계 전이(승인 전 구현 전이 차단 UI)·승인경로 배지·구현결과 기록·인시던트/문제 연계), 004 CAB 승인 대기함(승인/반려+의견), 005 변경 일정 캘린더, 006 변경 지표 대시보드.
- 사이드바 변경 메뉴 RBAC 추가(CHANGE_MANAGER: 001/002/003/005/006, APPROVER: 004(+003 읽기)). screen.path 정합. roles.ts에 CHANGE_MANAGER, (신규라면) APPROVER 상수 추가.
- 라우팅(routes/index.tsx, navConfig.tsx)에 SCR-CHG-001~006 경로 추가 — auth/incident/problem 도메인 라우팅 패턴 재사용.

## 4. 진행 순서 · 의존성
1. DB(테이블·template seed·CHANGE_MANAGER/APPROVER 유저 시드) → BE 연동(change 패키지 + problem/incident CHANGE 연계 완성) → FE 연동. UI 신규 최소(캘린더 그리드만 신규 가능성).
2. 계약 단일 기준 api_spec/change.md. 이견 dev-lead, 설계 이슈 designer.

## 5. 완료(테스트 통과) 기준
- BE: API-CHG-001~012 정상+오류(400/401/403/404/409), 6단계 전이(승인 전 구현 차단 409)·분류(위험도→승인경로)·승인(역할기반, 반려사유필수, 재결정409)·구현결과 기록(미승인 400)·인시던트/문제 양방향 연계·일정·템플릿·지표. **problem→CHANGE 연계 완성 회귀 포함**.
- FE: RFC 생성→목록→상세(전이·승인경로·구현결과·연계)→CAB 승인 대기함(승인/반려)→일정 캘린더→지표 대시보드 E2E.
- tester 통합테스트 실패 0 → `feat(change): ...` 커밋/푸시.

## 6. 파일 소유
- `source/db/` DB / `source/backend/`(change 패키지 + problem/incident CHANGE 링크 완성분) BE / `source/frontend/` 공통 UI·기능 FE. 기존 도메인 파일 수정 최소.

## 7. 특이사항
- approval·ticket_link는 srm 단계 도입분 재사용. APPROVER 역할이 이미 SRM 승인 대기함(SCR-SRM-006)에 매핑되어 있으므로, change 단계에서는 화면/API 매핑만 증분(CAB=APPROVER, 별도 역할 신설 아님 — approver.md 기준 단일 APPROVER 역할).
- problem 단계에서 연기했던 CHANGE 연계 스텁을 이번 단계에서 실구현으로 대체(교차 표기 완료 처리).

## i18n 다국어 전환 (유지보수 요청, 2026-07-12)

> i18n 인프라·SweetAlert2·언어 선택은 common phase에서 완료됨(`docs/03_develop/plan/common.md` v3절). 레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환(`docs/02_plan/screen/common.md` 6절). BE/DB 변경 없음. SCR-CHG-004(CAB 승인 대기함)는 SCR-COM-014로 대체·제거되어 이번 phase 대상 아님.

### 담당 범위 — dev-fe 단독(UI 미소집)

- 대상 화면(`docs/02_plan/screen/change.md` 3절): `ChangeListPage.tsx`(SCR-CHG-001), `ChangeCreatePage.tsx`(002), `ChangeDetailPage.tsx`(003), `ChangeSchedulePage.tsx`(005), `ChangeMetricsPage.tsx`(006).
- `features/change/status.ts` — `t` 인자를 받도록 전환, 호출부(각 Page.tsx, `features/search/status.ts`의 CHANGE 분기) 갱신.
- `format.ts` 확인 필수 — 라벨이 섞여 있으면(incident phase처럼) 라벨만 번역 키로 전환, ko-KR 날짜/숫자 포맷 자체는 유지.
- `useTranslation(["change", "common"])` 사용. `locales/{ko,en}/change.json`(현재 `{}` 스캐폴딩) 단독 소유, 직접 채운다.
- 값이 비어있는 레거시/옵셔널 필드 라벨 조회 시 problem phase에서 발견된 것과 같은 "원시 키 노출" 회귀가 없는지 미리 점검(해당 라벨 함수에 falsy 가드 적용).
- 타임라인 메시지 등 BE 하드코딩 데이터는 번역 대상 아님.

### 완료 기준
- English 전환 시 목록/RFC 생성/상세/일정 캘린더/지표 대시보드 전체 텍스트(상태·유형·위험도 라벨 포함) 영어 전환.
- CAB 승인(공용 패널)·구현결과·인시던트/문제 연계 등 기존 기능 회귀 없음(텍스트만 치환).

## 승인 대상자 역할 기반 동적 상세조회 권한 — CHANGE 부분 (유지보수 요청, 2026-07-15)

> 8개 도메인 공용 작업. 전체 설계·담당범위·완료기준은 `docs/03_develop/plan/common.md` 동일 제목 절 참조. 이 도메인 BE 작업: `change/application/ChangeService.java` 상세조회 가드의 기존 정적 APPROVER 전체조회 조건을 제거하고 `approvalGateService.canApproverView("CHANGE", 변경유형 코드, requesterIdOf(change))` OR로 대체(변경유형 코드·requesterIdOf는 기존 `checkGate` 호출부와 동일 값 재사용). FE 라우트 가드(`routes/index.tsx`)는 공용 작업에 포함되어 별도 진행 불필요.
