# 통합 테스트 시나리오 — problem (PRB)

> 실행 타임스탬프: 20260709-165140 · 도메인: problem
> 범위: API-PRB-001~012 정상+오류(400/401/403/404), 6단계 상태 전이, 우선순위 산정, RCA(5 Whys), 워크어라운드, KE/KEDB, 후속조치, 종료(warning/force), **API-PRB-009 INCIDENT 연계** + **incident API-INC-012(인시던트→문제 연계) 완성분 검증**
> **범위 제외**: API-PRB-009의 **CHANGE 연계/createNewChange**(change 도메인 미구축, 스텁) — TC 미작성·미수행.

## 사전 조건

- 빌드 테스트 통과(BE `gradlew test`, FE `npm run build`)
- BE(:8080)·FE dev(:5173) 기동, PostgreSQL(`itsm-postgres`) healthy
- DB 스키마/시드 적용: problem 스키마·시드(dev-database 예정), incident 스키마(`06/07`) 포함
- **CORS**: 브라우저 E2E는 반드시 `http://localhost:5173`로 접속(BE 허용 origin이 5173만)
- 테스트 계정:
  - `pm@itsm.local` / `Admin@1234` — **PROBLEM_MANAGER**, 문제 도메인 전 API 권한 보유
  - `im@itsm.local` — INCIDENT_MANAGER (인시던트 생성·API-INC-012 연계용)
  - `agent@itsm.local` — SERVICE_DESK_AGENT (문제 권한 없음 → **403 검증용**)
  - `admin@itsm.local` / `Admin@1234` — SYSTEM_ADMIN (계정/역할 셋업)
- 격리: playwright 매 항목 새 context/storage 초기화. API는 계정당 1회 로그인 세션 재사용. baseline은 상대 검증(생성 후 포함), 절대 개수 assert 금지.
- 근거 기준: @docs/01_analyze/prd/problem.md, @docs/01_analyze/feature/problem.md, @docs/02_plan/api_spec/problem.md, @docs/02_plan/api_spec/incident.md(API-INC-012), @docs/02_plan/security/authorization/problem_manager.md, @docs/02_plan/screen/problem.md

## 시나리오

### A. 빌드
- **TC-BUILD-001** · BE `gradlew test` 통과(모든 예외 JUnit 포함) — 근거: @docs/01_analyze/feature/problem.md 전 FEAT
- **TC-BUILD-002** · FE `npm run build`(tsc + vite build) 통과 — 근거: @docs/02_plan/screen/problem.md SCR-PRB-001~004

### B. 인증 (공통 · Bearer 필요)
- **TC-AUTH-001** · 미인증으로 `GET /api/v1/problems` 호출 401 — @docs/02_plan/api_spec/problem.md 공통 규약
- **TC-AUTH-002** · 미인증으로 `GET /api/v1/known-errors` 호출 401 — @docs/02_plan/api_spec/problem.md 공통 규약

### C. 인가 (403 · 문제 권한 없는 역할 = agent)
- **TC-PRB-RBAC-001** · 비-PM(agent)이 문제 등록(POST /problems) 시도 403 — @docs/02_plan/security/authorization/problem_manager.md 4항, FEAT-PRB-001
- **TC-PRB-RBAC-002** · 비-PM(agent)이 RCA(PUT .../rca) 시도 403 — @docs/01_analyze/feature/problem.md FEAT-PRB-004 (Unwanted 403)
- **TC-PRB-RBAC-003** · 비-PM(agent)이 상태 전이(PATCH .../status) 시도 403 — API-PRB-004 403
- **TC-PRB-RBAC-004** · 비-PM(agent)이 후속조치 등록(POST .../actions) 시도 403 — API-PRB-010 403
- **TC-PRB-RBAC-005** · 비-PM(agent)이 문제 종료(POST .../close) 시도 403 — API-PRB-012 403
> RBAC 항목은 대상 리소스 존재 여부와 무관하게 인가 단계에서 403이어야 함(권한 우선).

### D. 문제 등록 (FEAT-PRB-001 / API-PRB-002)
- **TC-PRB-001** · 요약·설명·출처·조사사유·영향도·긴급도·구성요소 포함 등록 201, `ticketKey=PRB-YYYY-####`, `status=DETECTION`, id 반환 — @docs/01_analyze/prd/problem.md REQ-PRB-001 (Event-driven)
- **TC-PRB-002** · 요약(summary) 누락 등록 400 — REQ-PRB-001 (Unwanted), FEAT-PRB-001
- **TC-PRB-003** · origin `REACTIVE` / `PROACTIVE` 각각 등록 201, 반응/선제 구분 저장 — @docs/01_analyze/prd/problem.md 범위(반응적/선제적 문제 구분)

### E. 우선순위 산정 (FEAT-PRB-003 / API-PRB-002/003)
- **TC-PRB-004** · 영향도·긴급도 모두 입력(HIGH×HIGH) 등록 → priority 산정 값(P1~P4 중 하나, 비-null) 반환, 상세에서 확인 — REQ-PRB-003 (Event-driven)
- **TC-PRB-005** · 영향도·긴급도 중 하나 누락 등록 → `priority=null`(미산정) 201 — FEAT-PRB-003 (Unwanted, 미산정)

### F. 목록·상세 조회 (API-PRB-001 / API-PRB-003)
- **TC-PRB-006** · 목록 조회 200, `{content[], page, size, totalElements}` 구조, 생성분(TC-PRB-001) 포함(상대검증), content 항목에 ticketKey/status/priority/origin — API-PRB-001
- **TC-PRB-007** · 목록 필터(status / priority / origin) 200, 필터 반영 — API-PRB-001
- **TC-PRB-008** · 상세 조회 200, 필드(status/priority/impact/urgency/rca/workaround/linkedIncidents/linkedChanges/actions) 구조 확인 — API-PRB-003
- **TC-PRB-009** · 존재하지 않는 id 상세 404 — API-PRB-003 404

### G. 6단계 상태 전이 (FEAT-PRB-002 / API-PRB-004)
> 단계: DETECTION → CLASSIFICATION → INVESTIGATION → KNOWN_ERROR → WORKAROUND → RESOLVED_CLOSED
- **TC-PRB-010** · 허용 전이 DETECTION→CLASSIFICATION 200, 상태 갱신 — REQ-PRB-002 (Event-driven)
- **TC-PRB-011** · 순차 전이 CLASSIFICATION→INVESTIGATION→KNOWN_ERROR→WORKAROUND 각 200, 이력 기록 — REQ-PRB-002 (Event-driven)
- **TC-PRB-012** · 순서 어긋난 전이(예: DETECTION→WORKAROUND 단계 건너뜀 또는 역방향) 400 — REQ-PRB-002 (Unwanted), FEAT-PRB-002
- **TC-PRB-013** · 존재하지 않는 id 전이 404 — API-PRB-004 404

### H. 근본 원인 분석 RCA (FEAT-PRB-004 / API-PRB-005)
- **TC-PRB-014** · RCA 작성(rootCause + fiveWhys[] + category) PUT 200, 상세 재조회로 저장 확인 — REQ-PRB-004 (Event-driven)
- **TC-PRB-015** · 개인(사람) 강제 지정 안 함(blameless): 개인이 아닌 근본원인·category 생략으로 저장 성공 200 — REQ-PRB-004 (Ubiquitous), FEAT-PRB-004
- **TC-PRB-016** · 존재하지 않는 id RCA 404 — API-PRB-005 404

### I. 워크어라운드 (FEAT-PRB-006 / API-PRB-006)
- **TC-PRB-017** · 워크어라운드 등록(content) 200, 상세 workaround 반영 — REQ-PRB-006 (Event-driven)
- **TC-PRB-018** · 빈 내용(content 공백/누락) 400 — FEAT-PRB-006 (Unwanted)
- **TC-PRB-019** · 존재하지 않는 id 워크어라운드 404 — API-PRB-006 404

### J. 알려진 오류 / KEDB (FEAT-PRB-005 / API-PRB-007, 008)
- **TC-PRB-020** · KE 생성(title+rootCause+workaround) POST 201, `{id,title}` 반환(KEDB 등록) — REQ-PRB-005 (Event-driven)
- **TC-PRB-021** · KEDB 키워드 검색(생성한 KE title 키워드) 200, 매칭 KE 반환(content에 problemKey 포함) — REQ-PRB-005 (Event-driven)
- **TC-PRB-022** · KEDB 매칭 없는 키워드 검색 200, 빈 목록(content=[]) — FEAT-PRB-005 (Unwanted, 빈 목록)
- **TC-PRB-023** · 존재하지 않는 id KE 생성 404 — API-PRB-007 404

### K. 인시던트 연계 (FEAT-PRB-007 / API-PRB-009)
- **TC-PRB-024** · 문제→인시던트 연계(targetType=INCIDENT, targetId=기존 인시던트) 200, **양방향**: 문제 상세 linkedIncidents 반영 + 인시던트 상세(API-INC-003)에서 문제 링크 노출 — REQ-PRB-007 (Event-driven)
- **TC-PRB-025** · 존재하지 않는 인시던트 연계 400 — REQ-PRB-007 (Unwanted), FEAT-PRB-007
- **TC-PRB-026** · 존재하지 않는 id(문제) 연계 404 — API-PRB-009 404

### L. incident API-INC-012 완성분 검증 (인시던트→문제 연계)
- **TC-INC012-001** · 인시던트→기존 문제 연계(POST /incidents/{id}/links, problemId) 200, `{incidentId, problemId}`, 양방향 링크 확인 — @docs/02_plan/api_spec/incident.md API-INC-012 (스텁→실구현 대체 검증)
- **TC-INC012-002** · createNewProblem=true 신규 문제 생성·연계 200, 신규 문제 조회 가능 — API-INC-012
- **TC-INC012-003** · 존재하지 않는 문제(problemId) 연계 400 — API-INC-012 400

### M. 후속 조치 추적 (FEAT-PRB-009 / API-PRB-010, 011)
- **TC-PRB-027** · 후속조치 등록(description+owner+dueDate) POST 201, `status=IN_PROGRESS` — REQ-PRB-009 (Ubiquitous)
- **TC-PRB-028** · 후속조치 상태 변경 IN_PROGRESS→DONE PATCH 200, 상세 actions에 반영 — REQ-PRB-009 (Ubiquitous)
- **TC-PRB-029** · 조치 항목 없는 문제 상세 조회 시 actions 빈 목록 — FEAT-PRB-009 (Unwanted, 빈 목록)
- **TC-PRB-030** · 존재하지 않는 id 조치 등록 404 — API-PRB-010 404

### N. 문제 종료 (FEAT-PRB-010 / API-PRB-012)
- **TC-PRB-031** · 미해결 후속조치(IN_PROGRESS) 남은 상태로 종료(force=false) → `warning` 반환(경고 표시), 종료 보류 — REQ-PRB-010 (Unwanted), FEAT-PRB-010
- **TC-PRB-032** · force=true 종료 → 200, `status=RESOLVED_CLOSED` — API-PRB-012
- **TC-PRB-033** · 미해결 조치 없는(전부 DONE) 문제 종료 200, `status=RESOLVED_CLOSED`, `warning=null`, 이력 기록 — REQ-PRB-010 (Event-driven)
- **TC-PRB-034** · 존재하지 않는 id 종료 404 — API-PRB-012 404

### O. FE E2E (playwright, http://localhost:5173, 매 항목 새 context/storage)
- **TC-E2E-001** · PM 로그인 → 문제 등록 화면(SCR-PRB-002) 요약 필수·영향도/긴급도 입력 시 우선순위 미리보기·등록 성공 후 상세 이동 — SCR-PRB-002
- **TC-E2E-002** · 문제 목록(SCR-PRB-001) 필터(상태·우선순위·출처·담당자·기간)·상태/우선순위 배지 표시 — SCR-PRB-001
- **TC-E2E-003** · 문제 상세(SCR-PRB-003) 상태 전이(허용 단계)·RCA 저장·워크어라운드·KE 생성·인시던트 연결·후속조치 등록/상태 — SCR-PRB-003
- **TC-E2E-004** · KEDB 검색(SCR-PRB-004) 키워드 검색 결과 표시 및 빈 결과 처리 — SCR-PRB-004
- **TC-E2E-005** · 상세에서 미해결 후속조치 남긴 채 종료 시 경고 다이얼로그 표시, force 종료 시 RESOLVED_CLOSED — SCR-PRB-003 (미해결 후속조치 경고)
- **TC-E2E-006** · 비-PM(agent) 로그인 시 문제 화면/기능 접근 제한(비노출 또는 403) — @docs/02_plan/security/authorization/problem_manager.md 인가

## 범위 제외 (수행하지 않음)
- API-PRB-009 · **CHANGE 연계(targetType=CHANGE) / createNewChange** — change 도메인 미구축(스텁). TC 미작성·미수행. 스텁 400/미지원 응답은 정상 판정.
