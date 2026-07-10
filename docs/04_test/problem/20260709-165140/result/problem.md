# 통합 테스트 결과 — problem (PRB) (20260709-165140)

> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres, healthy)
> 계정: pm@itsm.local(PROBLEM_MANAGER) / agent@itsm.local(SERVICE_DESK_AGENT, 403검증) / im@itsm.local(INCIDENT_MANAGER, 인시던트 생성) / admin@itsm.local(SYSTEM_ADMIN)
> 범위: API-PRB-001~012 정상+오류(400/401/403/404), 6단계 전이, 우선순위, RCA, 워크어라운드, KE/KEDB, 후속조치, 종료, API-PRB-009 INCIDENT 연계 + API-INC-012(인시던트→문제) 검증. CHANGE 연계/createNewChange는 범위 제외(스텁).
> access token TTL=300s이라 API 테스트는 세션 만료 회피 위해 도메인 그룹별로 신규 로그인 후 실행.

## 요약

- 총 **52건** (빌드 2 · 인증 2 · 인가 5 · API/도메인 34 · API-INC-012 검증 3 · FE E2E 6) · **성공 50 · 실패 2**
- 실패 2건: **TC-BUILD-002**(FE 프로덕션 빌드 TS 오류 4건) / **TC-INC012-003**(없는 문제 연계 시 404 반환, 계약상 400 기대)

## 상세 — 빌드/인증

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-BUILD-001 (BE gradlew test) | PASS | BUILD SUCCESSFUL (test task up-to-date, 기존 통과분 유지) |
| TC-BUILD-002 (FE npm run build) | **FAIL** | `tsc -b` 4건 오류로 빌드 실패(exit 2). ProblemDetailPage.tsx(27,30 TS6196 'ProblemTargetStatus' 미사용; 405,3 TS6133 'detail' 미사용) / ProblemListPage.tsx(27,3 'LEVELS'; 32,3 'priorityTone' 미사용). ※ Vite dev(:5173)는 기동되어 E2E는 수행 가능 |
| TC-AUTH-001 (미인증 GET /problems 401) | PASS | 401 |
| TC-AUTH-002 (미인증 GET /known-errors 401) | PASS | 401 |

## 상세 — 인가(403, agent)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-PRB-RBAC-001 agent POST /problems | PASS | 403 ACCESS_DENIED |
| TC-PRB-RBAC-002 agent PUT .../rca | PASS | 403 |
| TC-PRB-RBAC-003 agent PATCH .../status | PASS | 403 |
| TC-PRB-RBAC-004 agent POST .../actions | PASS | 403 |
| TC-PRB-RBAC-005 agent POST .../close | PASS | 403 |

## 상세 — API/도메인 (pm/im)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-PRB-001 등록 정상(HIGH×HIGH) | PASS | 201, ticketKey=PRB-2026-0009, status=DETECTION, priority=P1 |
| TC-PRB-002 요약 누락 | PASS | 400 |
| TC-PRB-003 origin PROACTIVE 등록 | PASS | 201, origin=PROACTIVE, priority=P3(MEDIUM×MEDIUM) |
| TC-PRB-004 HIGH×HIGH 우선순위 | PASS | 상세 priority=P1, impact=HIGH, urgency=HIGH |
| TC-PRB-005 긴급도 누락 미산정 | PASS | 201, priority=null(생략) |
| TC-PRB-006 목록 조회 | PASS | 200, {content,page,size,totalElements}, 생성분 포함 |
| TC-PRB-007 필터 origin=PROACTIVE | PASS | 200, PROACTIVE만 반환 |
| TC-PRB-008 상세 구조 | PASS | 200, status/priority/impact/urgency/rca/workaround/linkedIncidents/linkedChanges/actions 확인 |
| TC-PRB-009 없는 id 상세 | PASS | 404 |
| TC-PRB-010 DETECTION→CLASSIFICATION | PASS | 200, status=CLASSIFICATION |
| TC-PRB-011 순차 전이(INVESTIGATION→KNOWN_ERROR→WORKAROUND) | PASS | 각 200, 순차 갱신 |
| TC-PRB-012 순서 어긋난 전이(DETECTION→WORKAROUND) | PASS | 400 INVALID_STATUS_TRANSITION |
| TC-PRB-013 없는 id 전이 | PASS | 404 |
| TC-PRB-014 RCA 작성 | PASS | 200, 상세 재조회 rootCause+fiveWhys[5] 저장 확인 |
| TC-PRB-015 blameless(개인/카테고리 미강제) | PASS | 200(category 생략·시스템 원인 저장 성공) |
| TC-PRB-016 없는 id RCA | PASS | 404 |
| TC-PRB-017 워크어라운드 등록 | PASS | 200, 상세 workaround 반영 |
| TC-PRB-018 빈 내용 | PASS | 400 |
| TC-PRB-019 없는 id 워크어라운드 | PASS | 404 |
| TC-PRB-020 KE 생성 | PASS | 201, {id,title}, KEDB 등록 |
| TC-PRB-021 KEDB 키워드 검색(payment) | PASS | 200, 매칭 KE 반환(problemKey 포함) |
| TC-PRB-022 무매칭 검색 | PASS | 200, content=[] totalElements=0 |
| TC-PRB-023 없는 id KE 생성 | PASS | 404 |
| TC-PRB-024 인시던트 연계(양방향) | PASS | 200. 문제 상세 linkedIncidents=[INC-2026-0013] + 인시던트 상세 links에 PROBLEM 노출 |
| TC-PRB-025 없는 인시던트 연계 | PASS | 400 LINK_TARGET_NOT_FOUND |
| TC-PRB-026 없는 문제 id 연계 | PASS | 404 |
| TC-PRB-027 후속조치 등록 | PASS | 201, status=IN_PROGRESS |
| TC-PRB-028 후속조치 상태 DONE | PASS | 200, 상세 actions 반영 |
| TC-PRB-029 조치 없는 문제 actions | PASS | actions=[] |
| TC-PRB-030 없는 id 조치 등록 | PASS | 404 |
| TC-PRB-031 미해결 조치+force=false 종료 | PASS | 200, status 미변경(DETECTION 유지), warning 반환("미해결 후속 조치 1건...") |
| TC-PRB-032 force=true 종료 | PASS | 200, status=RESOLVED_CLOSED, warning(강제 종료됨) |
| TC-PRB-033 미해결 없음 종료 | PASS | 200, status=RESOLVED_CLOSED, warning=null |
| TC-PRB-034 없는 id 종료 | PASS | 404 |

## 상세 — API-INC-012 (인시던트→문제 연계) 완성분 검증

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-INC012-001 기존 문제 연계 | PASS | 200, {incidentId,problemId}. 인시던트 links + 문제 linkedIncidents 양방향 확인 |
| TC-INC012-002 createNewProblem=true | PASS | 200, 신규 문제 생성·연계(problemId=16) |
| TC-INC012-003 없는 문제 연계 | **FAIL** | **404 PROBLEM_NOT_FOUND 반환**. 계약(api_spec/incident.md API-INC-012)은 "400 존재하지 않는 문제" 명시. 대칭 API-PRB-009(없는 인시던트)는 400 반환하므로 일관성 측면에서도 400이 기대값 |

## 상세 — FE E2E (playwright, localhost:5173, 매 항목 storage 초기화)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-E2E-001 등록(SCR-PRB-002) | PASS | 요약 입력·영향도/긴급도 HIGH 선택 시 우선순위 미리보기 P1, 등록 성공 → 상세(PRB-2026-0016) 이동 |
| TC-E2E-002 목록(SCR-PRB-001) | PASS | 필터(상태·우선순위·출처·담당자·기간)·상태/우선순위 배지·출처(반응/선제)·페이지네이션 표시 |
| TC-E2E-003 상세(SCR-PRB-003) | PASS | RCA 저장(blameless placeholder)·상태 전이(탐지→분류→조사중)·워크어라운드/KEDB/인시던트연계/후속조치 패널; 변경 연계는 "변경 관리 도메인 도입 후" 비활성(범위제외 부합) |
| TC-E2E-004 KEDB(SCR-PRB-004) | PASS | 키워드 검색(payment) 필터·무매칭 시 "조건에 맞는 알려진 오류가 없습니다." |
| TC-E2E-005 종료 경고 | PASS | 미해결 후속조치 남긴 채 종료 → 경고 다이얼로그("미해결 후속 조치 1건...") → 그래도 종료 시 status=종료 |
| TC-E2E-006 비-PM(agent) RBAC | PASS | 문제/KEDB 내비 비노출 + /problems 직접 접근 시 /403 리다이렉트 |

## 실패 항목 분석

- **TC-BUILD-002 (FE 프로덕션 빌드 실패)**: `tsc -b` 미사용 선언 4건으로 빌드 중단.
  - `source/frontend/src/features/problem/ProblemDetailPage.tsx:27` TS6196 `ProblemTargetStatus` 미사용
  - `source/frontend/src/features/problem/ProblemDetailPage.tsx:405` TS6133 `detail` 미사용
  - `source/frontend/src/features/problem/ProblemListPage.tsx:27` TS6133 `LEVELS` 미사용
  - `source/frontend/src/features/problem/ProblemListPage.tsx:32` TS6133 `priorityTone` 미사용
  - 원인: 미사용 import/변수. Vite dev 서버는 기동되어 런타임/E2E는 정상이나 프로덕션 빌드 게이트 실패. 담당: FE.
- **TC-INC012-003 (없는 문제 연계 404 vs 400)**: `POST /api/v1/incidents/{id}/links` `{problemId:99999}` → 404 PROBLEM_NOT_FOUND. 계약 API-INC-012는 없는 문제에 대해 400을 명시하며, 대칭 API-PRB-009(없는 인시던트)는 400(LINK_TARGET_NOT_FOUND)을 반환. 인시던트↔문제 연계 오류코드 불일치. 담당: BE(incident linkProblem — 없는 problemId를 400으로 매핑 권장).

## 결론

- problem 도메인 핵심 기능(등록·6단계 전이·우선순위 P1~P4/미산정·RCA/5Whys/blameless·워크어라운드·KE/KEDB·후속조치·종료 warning/force·인시던트 양방향 연계) 및 인가(PROBLEM_MANAGER 전용, agent 403)·FE 6화면 흐름 **정상 동작**.
- 잔여 실패 2건: FE 프로덕션 빌드 TS 정리(TC-BUILD-002), API-INC-012 없는 문제 오류코드 400 정합(TC-INC012-003). 수정 후 재테스트 필요.
- CHANGE 연계/createNewChange는 지시대로 범위 제외(CHANGE_LINK_UNAVAILABLE 400 스텁 = 정상 판정).
