# 개발 계획 — problem (문제 관리, PRB)

> 도메인: problem (PRB) · 개발 순서 4/7 · 작성: dev-lead · 2026-07-09

## 1. 목표

문제 등록·목록·상세(6단계 프로세스 상태·우선순위(영향도×긴급도)·RCA(5 Whys)·워크어라운드·알려진 오류(KEDB)·인시던트/변경 연계·후속 조치·종료), KEDB 검색을 구현한다. auth/incident 기반과 common(ticket_link, timeline_event, comment) 재사용.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/problem.md`(SCR-PRB-001~004), 공통 SCR-COM-007/008
- API: `api_spec/problem.md`(API-PRB-001~012)
- DB: `database/problem.md`(problem/problem_five_why/known_error/problem_action) + `database/common.md`(ticket_link, timeline_event, comment)
- 역할: `security/authorization/problem_manager.md`(PROBLEM_MANAGER)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- PRB 테이블: problem(ticket_key PRB-YYYY-####, origin REACTIVE/PROACTIVE, impact/urgency/priority(둘 중 하나 없으면 NULL=미산정), status 6단계 DETECTION/CLASSIFICATION/INVESTIGATION/KNOWN_ERROR/WORKAROUND/RESOLVED_CLOSED, root_cause·root_cause_category·workaround·component), problem_five_why(UNIQUE(problem_id, step_no)), known_error(title 검색 인덱스), problem_action(status IN_PROGRESS/DONE). 공통컬럼·FK.
- ticket_link는 incident 단계에서 이미 도입됨 → 재사용(source_type='PROBLEM'). 신규 테이블 아님.
- screen/screen_role 증분: SCR-PRB-001~004 + PROBLEM_MANAGER 역할 매핑(problem_manager.md 기준). 기존 seed 유지.
- 테스트 유저: PROBLEM_MANAGER 권한 계정(예: pm@itsm.local / Admin@1234) 시드 추가(auth/incident 유저 규칙 동일).

### BE (dev-backend) — `source/backend/`
- API-PRB-001~012(api_spec). problem 패키지 추가(incident/srm 패키지 컨벤션 재사용).
- 상태 전이: 6단계 순서 전이, 순서 어긋난 전이 400. 권한 없으면 403.
- 우선순위 산정: impact×urgency 매트릭스(둘 중 하나 없으면 null=미산정). 입력값도 보관해 재산정 가능.
- RCA(PUT, 5 Whys append/replace), 워크어라운드(내용 빈 값 400), KE 생성(KEDB 등록), KEDB 검색(title 키워드, 매칭 없으면 빈 목록).
- 후속 조치 등록/상태 변경(IN_PROGRESS/DONE), 문제 종료(미해결 후속조치 있으면 warning; force=false 경고, true 종료).
- RBAC는 role 정의서(problem_manager.md, 단일 원천) 기준으로 각 API 인가.
- JUnit 통합테스트(incident/srm integration 테스트 패턴 재사용).

### 크로스 도메인 (중요)
- **API-PRB-009 연계**: `targetType=INCIDENT`는 **incident 도메인이 이미 구축됨 → 정상 구현**(기존 incidentId 검증 + ticket_link 양방향). `targetType=CHANGE` 및 `createNewChange`는 change 도메인(5/7) 미구축 → **스텁/400 안내 + TODO**(change 단계에서 완성), problem 통합테스트 범위 제외.
- **incident API-INC-012 완성(연기분 회수)**: incident 단계에서 problem 미구축으로 스텁(PROBLEM_LINK_UNAVAILABLE)했던 문제 연계를, problem 엔티티가 생기는 이번 단계에서 **완성**한다(기존 problemId 검증 + createNewProblem 생성 + ticket_link 양방향). dev-backend는 incident의 링크 처리와 problem의 링크 처리가 동일 ticket_link 인프라 위에서 일관되게 동작하도록 구현. 이 부분은 problem 통합테스트에 포함.
- **워크어라운드 linkedArticleId / KNOWLEDGE 연결**: knowledge 도메인(6/7) 미구축 → linkedArticleId는 선택 필드로 저장만(검증/역참조는 knowledge 단계). KEDB 자체는 problem 내 known_error로 완결.

### UI (dev-ui) — `source/frontend/` 공통 영역
- 대부분 기존 컴포넌트 재사용(목록/상세 패턴, 배지, FieldBuilder=5 Whys/후속조치 반복 입력, 우선순위 배지). 신규 최소화, dev-frontend와 합의.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- 화면: SCR-PRB-001 목록(필터: 상태·우선순위·출처·담당자·기간), 002 등록(영향도/긴급도→우선순위 실시간 미리보기, 요약 필수), 003 상세(6단계 전이·RCA·워크어라운드·KE 생성·인시던트/변경 연계·후속조치·종료(미해결 경고 다이얼로그)), 004 KEDB 검색.
- 사이드바 문제 메뉴 RBAC 추가(PROBLEM_MANAGER). screen.path 정합.
- 변경 연계 버튼은 change 단계까지 비활성/안내(문제연계 incident 패턴 재사용). 상태 전이 버튼은 BE allowedTransitions 있으면 그 기준.

## 4. 진행 순서 · 의존성
1. DB(테이블·seed·테스트유저) → BE 연동. BE 계약(api_spec) → FE 연동. UI 신규 최소.
2. 계약 단일 기준 api_spec/problem.md. 이견 dev-lead, 설계 이슈 designer.

## 5. 완료(테스트 통과) 기준
- BE: API-PRB-001~012 정상+오류(400/401/403/404), 6단계 전이·우선순위 산정·RCA·워크어라운드·KE/KEDB·후속조치·종료(warning). **API-PRB-009 INCIDENT 연계 + incident API-INC-012 완성 포함**. CHANGE 연계/createNewChange는 범위 제외/스텁.
- FE: 등록→목록→상세(전이·RCA·워크어라운드·KE·인시던트연계·후속조치·종료)→KEDB 검색 E2E.
- tester 통합테스트 실패 0 → `feat(problem): ...` 커밋/푸시.

## 6. 파일 소유
- `source/db/` DB / `source/backend/`(problem 패키지 + incident 링크 완성분) BE / `source/frontend/` 공통 UI·기능 FE. 기존 도메인 파일 수정 최소.

## 7. 특이사항
- ticket_link는 incident 단계 도입분 재사용. INCIDENT 연계는 완성, CHANGE 연계는 change 단계로 연기(교차 표기). incident API-INC-012 스텁은 이번 단계에서 실구현으로 대체.

## i18n 다국어 전환 (유지보수 요청, 2026-07-12)

> i18n 인프라·SweetAlert2·언어 선택은 common phase에서 완료됨(`docs/03_develop/plan/common.md` v3절). 레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환(`docs/02_plan/screen/common.md` 6절). BE/DB 변경 없음.

### 담당 범위 — dev-fe 단독(UI 미소집)

- 대상 화면(`docs/02_plan/screen/problem.md` 3절): `ProblemListPage.tsx`(SCR-PRB-001), `ProblemCreatePage.tsx`(002), `ProblemDetailPage.tsx`(003), `KnownErrorSearchPage.tsx`(004).
- `features/problem/status.ts` — `t` 인자를 받도록 전환(6.3절 패턴), 호출부(각 Page.tsx, `features/search/status.ts`의 PROBLEM 분기) 갱신.
- `features/problem/format.ts` 확인 필수 — incident phase에서 발견된 것처럼 날짜/숫자 "포맷" 함수 안에 텍스트 라벨(예: 값 없음 폴백 문구)이 섞여 있으면 그 라벨만 번역 키로 전환하고, 실제 `ko-KR` 날짜/숫자 포맷 자체는 그대로 유지.
- `useTranslation(["problem", "common"])` 사용. `locales/{ko,en}/problem.json`(현재 `{}` 스캐폴딩) 단독 소유, 직접 채운다.
- 타임라인 메시지 등 BE 하드코딩 데이터(DB 저장분)는 번역 대상 아님(incident phase와 동일, 회귀 아님).

### 완료 기준
- English 전환 시 목록/등록/상세/KEDB 검색 전체 텍스트(상태·우선순위·영향도/긴급도 라벨 포함) 영어 전환.
- RCA·워크어라운드·인시던트 연계·후속조치·종료 등 기존 기능 회귀 없음(텍스트만 치환).

## 승인 대상자 역할 기반 동적 상세조회 권한 — PROBLEM 부분 (유지보수 요청, 2026-07-15)

> 8개 도메인 공용 작업. 전체 설계·담당범위·완료기준은 `docs/03_develop/plan/common.md` 동일 제목 절 참조. 이 도메인 BE 작업: `problem/application/ProblemService.java` 상세조회 가드(PROBLEM_MANAGER 전용)에 `approvalGateService.canApproverView("PROBLEM", null, requesterIdOf(problem))` OR 추가(신규 권한, 매니저 전용 조건은 유지). FE 라우트 가드(`routes/index.tsx`)는 공용 작업에 포함되어 별도 진행 불필요.
