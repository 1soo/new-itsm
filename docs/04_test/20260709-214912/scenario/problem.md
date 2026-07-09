# 통합 테스트 시나리오 — problem (PRB) 재테스트

> 실행 타임스탬프: 20260709-214912 · 도메인: problem
> 목적: 직전 실행(20260709-165140) 실패 2건(TC-BUILD-002, TC-INC012-003) 수정분 재검증 + 관련 회귀(인시던트↔문제 연계, FE 문제 화면 전반)
> 원본 시나리오: docs/04_test/20260709-165140/scenario/problem.md (전체 커버리지는 원본 유지, 본 문서는 재검증 대상만 다룸)

## 사전 조건
- BE(:8080)·FE dev(:5173) 기동, PostgreSQL(`itsm-postgres`) healthy
- 계정: pm@itsm.local(PROBLEM_MANAGER), im@itsm.local(INCIDENT_MANAGER), agent@itsm.local(SERVICE_DESK_AGENT, 403검증)

## 시나리오

### A. 빌드 (재검증)
- **TC-BUILD-001** · BE `gradlew test` 통과 — @docs/01_analyze/feature/problem.md 전 FEAT
- **TC-BUILD-002** · FE `npm run build`(tsc -b && vite build) 통과 — @docs/02_plan/screen/problem.md SCR-PRB-001~004 (직전 실패: 미사용 import/변수 TS 오류 4건)

### B. API-INC-012 없는 문제 연계 오류코드 (재검증)
- **TC-INC012-003** · `POST /incidents/{id}/links` `{problemId: 존재하지않는id}` → 400 `LINK_TARGET_NOT_FOUND` 기대(직전 실패: 404 `PROBLEM_NOT_FOUND` 반환) — @docs/02_plan/api_spec/incident.md API-INC-012

### C. 관련 회귀 — 인시던트↔문제 연계
- **TC-INC012-001-R** · 기존 문제와 연계(`problemId` 존재) 200, `{incidentId,problemId}` — API-INC-012
- **TC-INC012-002-R** · `createNewProblem=true` 신규 문제 생성·연계 200 — API-INC-012
- **TC-PRB-024-R** · 연계 후 양방향 확인: 인시던트 상세 `links`에 PROBLEM 노출 + 문제 상세 `linkedIncidents` 반영 — API-PRB-009/API-INC-003
- **TC-PRB-025-R** · 문제→존재하지 않는 인시던트 연계 400 `LINK_TARGET_NOT_FOUND`(대칭 오류코드 일관성) — API-PRB-009

### D. 관련 회귀 — FE 문제 화면 전반
- **TC-E2E-002-R** · 문제 목록(SCR-PRB-001) 정상 렌더(필터·배지·페이지네이션) — SCR-PRB-001
- **TC-E2E-003-R** · 문제 상세(SCR-PRB-003) 정상 렌더(RCA/워크어라운드/KEDB/인시던트연계/후속조치 패널, 연결 인시던트 표시) — SCR-PRB-003
- **TC-E2E-006-R** · 비-PM(agent) `/problems` 직접 접근 시 `/403` 리다이렉트(회귀) — 인가 설계

## 범위 제외
- 원본 시나리오의 나머지 항목(TC-PRB-001~034 등, 이전 회차 전건 PASS)은 본 재테스트에서 재수행하지 않음.
