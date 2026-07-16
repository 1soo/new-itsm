# 개발 계획 — incident (인시던트 관리, INC)

> 도메인: incident (INC) · 개발 순서 3/7 · 작성: dev-lead · 2026-07-09

## 1. 목표

인시던트 등록·목록·상세(심각도/우선순위·상태 전이·대응 역할 배정·에스컬레이션·타임라인 업데이트·해결/시간지표(MTTD/MTTA/MTTR)·포스트모템·문제 연계), 지표 대시보드를 구현한다. auth/SRM 기반과 common(timeline_event) 재사용, **common.ticket_link 도입**.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/incident.md`(SCR-INC-001~005), 공통 SCR-COM-007/008
- API: `api_spec/incident.md`(API-INC-001~013)
- DB: `database/incident.md`(incident/incident_responder/severity_history/postmortem/five_why/action_item) + `database/common.md`(ticket_link, timeline_event)
- 역할: `security/authorization/`(SERVICE_DESK_AGENT 접수, INCIDENT_MANAGER 역할배정 등)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- INC 테이블: incident(ticket_key INC-YYYY-####, severity SEV1~3, priority P1~4 독립, status NEW/IN_PROGRESS/RESOLVED/CLOSED, 시간필드·MTTD/MTTA/MTTR), incident_responder(UNIQUE(incident,user,role)), incident_severity_history, postmortem(1:1), postmortem_five_why(UNIQUE(pm,step)), postmortem_action_item. 공통컬럼·FK·UNIQUE(6절).
- **common.ticket_link 도입**(database/common.md): source/target 다형(INCIDENT/PROBLEM/ASSET/...), UNIQUE(4튜플). DB FK 없이 앱레벨 검증.
- screen/screen_role 증분: SCR-INC-001~005 + 역할 매핑(role 정의서 기준). 기존 seed 유지.

### BE (dev-backend) — `source/backend/`
- API-INC-001~013(api_spec). incident 패키지 추가.
- 상태 전이: NEW→IN_PROGRESS→RESOLVED→CLOSED. 허용외 400, 권한 403.
- 심각도/우선순위 변경(이력 severity_history append), 대응 역할 배정(**API-INC-006 = INCIDENT_MANAGER 전용**, 그외 403), 에스컬레이션(HIERARCHICAL/FUNCTIONAL, 대상 없음 400), 타임라인 업데이트(common.timeline_event, INTERNAL/EXTERNAL).
- 해결 처리(API-INC-009): 시간필드로 MTTD/MTTA/MTTR 계산(시각 없으면 해당 지표 null=미산정). 포스트모템 조회/작성(rootCause 필수 400, 미작성 404), 지표(count·severityDistribution·avgMttr).
- **RBAC는 role 정의서(단일 원천) 기준**으로 각 API 인가 판정.

### 크로스 도메인 (중요)
- **API-INC-012 문제 연계**: problem 도메인(다음 순서) 미구축이라 problem 엔티티 없음. 이번 단계에서는 **ticket_link 인프라만 구축**하고, 문제 연계(기존 problemId 검증 + createNewProblem 생성)의 **완성은 problem 도메인으로 연기**. 인시던트 상세 `links`는 존재하는 ticket_link만 반환(현재 비어있음). BE는 API-INC-012를 명확히 "problem 미구축 시 400/안내" 또는 스텁 처리하고 TODO 표기(문제 단계에서 완성). incident 통합테스트에서 문제 연계는 범위 제외.

### UI (dev-ui) — `source/frontend/` 공통 영역
- 대부분 기존 컴포넌트 재사용. 필요 시 심각도 분포 차트(TrendChart/분포형), 5 Whys/조치항목 반복 입력(FieldBuilder 재사용 검토). 신규 최소화, dev-frontend와 합의.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- 화면: SCR-INC-001 목록, 002 등록, 003 상세(심각도/우선순위 편집·상태전이·역할배정[IM]·에스컬레이션·타임라인 업데이트·해결/지표·문제연계 버튼[problem 단계까지 비활성/안내]), 004 포스트모템 편집(5 Whys·근본원인·조치항목), 005 지표(KPI+심각도 분포 차트).
- 사이드바 인시던트 메뉴 RBAC 추가(SERVICE_DESK_AGENT 등 접수·조회, INCIDENT_MANAGER 역할배정). screen.path 정합.
- 상태 전이 버튼은 BE allowedTransitions 있으면 그 기준(SRM 패턴 재사용), 없으면 status/역할 기반. 포스트모템 필요 배너(SEV1·2 해결·PM 미작성).

## 4. 진행 순서 · 의존성
1. DB(테이블·ticket_link·seed) → BE 연동. BE 계약(api_spec) → FE 연동. UI 신규 최소.
2. 계약 단일 기준 api_spec/incident.md. 이견 dev-lead, 설계 이슈 designer.

## 5. 완료(테스트 통과) 기준
- BE: API-INC-001~013 정상+오류(400/401/403/404), 상태전이·역할배정(IM)·에스컬레이션·MTTx 계산·포스트모템(rootCause 필수)·지표. **API-INC-012는 problem 단계 완성 전제로 범위 제외/스텁**.
- FE: 등록→목록→상세(전이·역할·에스컬·타임라인·해결/지표)→포스트모템→지표 E2E.
- tester 통합테스트 실패 0 → `feat(incident): ...` 커밋/푸시.

## 6. 파일 소유
- `source/db/` DB / `source/backend/`(incident 패키지) BE / `source/frontend/` 공통 UI·기능 FE. 기존 도메인 파일 수정 최소.

## 7. 특이사항
- ticket_link 이 단계 도입. 문제 연계 완성은 problem 도메인(교차 표기). 지표 심각도 분포 차트는 유효(metrics가 분포 반환).

## i18n 다국어 전환 (유지보수 요청, 2026-07-12)

> i18n 인프라·SweetAlert2·언어 선택은 common phase에서 완료됨(`docs/03_develop/plan/common.md` v3절). 레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환(`docs/02_plan/screen/common.md` 6절). BE/DB 변경 없음.

### 담당 범위 — dev-fe 단독(UI 미소집)

- 대상 화면(`docs/02_plan/screen/incident.md` 3절): `IncidentListPage.tsx`(SCR-INC-001), `IncidentCreatePage.tsx`(002), `IncidentDetailPage.tsx`(003), `PostmortemPage.tsx`(004), `IncidentMetricsPage.tsx`(005).
- `features/incident/status.ts` — 6.3절 패턴대로 `t` 인자를 받도록 전환, 호출부(각 Page.tsx, `features/search/status.ts`의 INCIDENT 분기) 갱신.
- `useTranslation(["incident", "common"])` 사용.
- `locales/{ko,en}/incident.json`(현재 `{}` 스캐폴딩) — 단독 소유, 직접 채운다.
- `format.ts`는 건드리지 않는다(ko-KR 고정 유지).
- 승인 패널(SCR-COM-014 공용)·`ticketTypeApprovalLabel` 등 common 소유 텍스트는 이미 common phase에서 전환 완료 — 인시던트 상세에서 재사용되는 부분은 그대로 동작하는지만 확인.

### 완료 기준
- English 전환 시 목록/등록/상세/포스트모템/지표 대시보드 전체 텍스트(상태·심각도 라벨 포함) 영어 전환.
- 상태 전이·에스컬레이션·타임라인·문제 연계·5 Whys 등 기존 기능 회귀 없음(텍스트만 치환).

## 승인 대상자 역할 기반 동적 상세조회 권한 — INCIDENT 부분 (유지보수 요청, 2026-07-15)

> 8개 도메인 공용 작업. 전체 설계·담당범위·완료기준은 `docs/03_develop/plan/common.md` 동일 제목 절 참조. 이 도메인 BE 작업: `incident/application/IncidentService.java` `detail()`은 현재 역할 체크가 전혀 없는 결함 상태 — **신규로** `SecurityUtils.hasAnyRole("SERVICE_DESK_AGENT", "INCIDENT_MANAGER") || approvalGateService.canApproverView("INCIDENT", null, requesterIdOf(inc))` 가드를 추가하고 불만족 시 403(결함 정리 겸 신규 기능). FE 라우트 가드는 이 도메인 **제외**(백엔드 신규 역할체크로 커버, `routes/index.tsx`에 ROLE_APPROVER 추가 안 함).

## 전이 버튼 라벨·타임라인 actor (유지보수 요청, 2026-07-16)

### 설계 근거
- 화면: `docs/02_plan/screen/incident.md` v0.4 SCR-INC-003(전이 라벨표 90~97행, 타임라인 actor).
- API: `docs/02_plan/api_spec/incident.md`(상세 응답 `timeline[].actor`).
- 공통 아키텍처: `docs/03_develop/plan/common.md` "상태 전이 버튼 라벨·타임라인 actor 공통 아키텍처" 절.
- 참고 기존 코드: `source/backend/.../incident/domain/IncidentStatus.java`, `application/IncidentService.java`(212~236행 타임라인 저장부); `source/frontend/.../incident/status.ts`·`types.ts`·`IncidentDetailPage.tsx`(175행 부근 타임라인 매핑).

### 담당 범위

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/incident/`
- `domain/IncidentStatus.java`에 `label()` 메서드 추가(FE `features/incident/status.ts`의 `STATUS_LABEL`과 동일 값 4종: 신규/대응중/해결/종료).
- `application/dto/IncidentDetailResponse.TimelineEntry`(현재 `(String type, String visibility, String message, OffsetDateTime at)`)에 `actor` 필드 추가.
- `application/IncidentService.java`의 상세 조회 메서드: 타임라인 조회 시 각 `TimelineEvent.getCreatedBy()`(email)로 `appUserRepository.findByEmail()` 조회해 이름 resolve(실패 시 email 폴백, 다른 도메인과 동일 패턴)해 `actor`에 채움.
- `IncidentService.java`의 상태 전이 메서드(234~235행)에서 `TimelineEvent.of(TT, id, "STATUS_" + target.name(), ... "상태가 " + target.name() + "로 변경되었습니다.")` 메시지 부분의 `target.name()` → `target.label()`로 교체(이벤트 타입 문자열은 유지). SEVERITY_CHANGE/ROLE_ASSIGN/ESCALATE/UPDATE/RESOLVE/LINK 등 다른 메시지 유형은 변경 없음(기존 한글 하드코딩 유지).

#### FE (dev-fe) — `source/frontend/src/features/incident/`
- `status.ts`에 `transitionLabel(t, target: IncidentStatus): string` 신규(i18n 키 `incident:transition.*`, 매핑값 SCR-INC-003 90~97행 표). 기존 `statusLabel`은 변경하지 않음.
- `IncidentDetailPage.tsx`의 전이 버튼 텍스트만 `statusLabel(t, target)` → `transitionLabel(t, target)`으로 교체(토스트 문구는 `statusLabel` 유지).
- `types.ts`의 `IncidentDetail.timeline` 항목 타입에 `actor: string` 추가.
- `IncidentDetailPage.tsx`의 `timelineItems` 매핑(175행 부근)에 `actor: entry.actor` 추가.

### 완료 기준
- 인시던트 상세(SCR-INC-003)의 전이 버튼에 동작 동사형 라벨이 표시되고, 전이 완료 토스트는 기존처럼 도착 상태명을 사용한다.
- 타임라인의 상태 변경 항목에 행위 수행자 이름과 상태 한글 라벨(코드 아님)이 표시된다.
