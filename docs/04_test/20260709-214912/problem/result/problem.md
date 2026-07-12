# 통합 테스트 결과 — problem (PRB) 재테스트 (20260709-214912)

> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres, healthy)
> 대상: 직전 실행(20260709-165140) 실패 2건 수정분 + 관련 회귀

## 요약
- 총 **9건** (빌드 2 · API-INC-012 재검증 1 · 연계 회귀 4 · FE 회귀 2) · **성공 9 · 실패 0**
- 직전 실패 2건(TC-BUILD-002, TC-INC012-003) 모두 **수정 확인**

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 (BE gradlew test) | PASS | `BUILD SUCCESSFUL`, 4 actionable tasks up-to-date(기존 통과분 유지, IncidentService.linkProblem 수정분 포함 이미 통과 상태) | |
| TC-BUILD-002 (FE npm run build) | PASS | `tsc -b && vite build` 정상 완료. `dist/` 산출물 생성(index-*.js 692.35kB, index-*.css 36.44kB), TS 오류 없음. ProblemDetailPage.tsx/ProblemListPage.tsx 미사용 import(`ProblemTargetStatus`, `LEVELS`, `priorityTone`)·미사용 변수(`detail`) 제거 확인 | 이전 TC-BUILD-002 FAIL → 이번 PASS |
| TC-INC012-003 (없는 문제 연계 오류코드) | PASS | `POST /incidents/16/links {problemId:999999}` → **400** `{"code":"LINK_TARGET_NOT_FOUND","message":"연계 대상을 찾을 수 없습니다."}` (기존 404 PROBLEM_NOT_FOUND에서 수정됨, 대칭 API-PRB-009와 동일 코드) | 이전 FAIL → 이번 PASS |
| TC-INC012-001-R (기존 문제 연계) | PASS | `POST /incidents/16/links {problemId:21}` → 200 `{"incidentId":16,"problemId":21}` | 회귀 없음 |
| TC-INC012-002-R (createNewProblem=true) | PASS | `POST /incidents/16/links {createNewProblem:true}` → 200 `{"incidentId":16,"problemId":22}`(신규 문제 생성·연계) | 회귀 없음 |
| TC-PRB-024-R (양방향 확인) | PASS | 인시던트(16) 상세 `links=[{"type":"PROBLEM","targetKey":"21"}]`, 문제(21) 상세 `linkedIncidents=[{"id":16,"ticketKey":"INC-2026-0014"}]` 양방향 반영 | 회귀 없음 |
| TC-PRB-025-R (문제→없는 인시던트 연계) | PASS | `POST /problems/21/links {targetType:INCIDENT, targetId:999999}` → 400 `LINK_TARGET_NOT_FOUND`(TC-INC012-003과 오류코드 대칭 일관성 확인) | 회귀 없음 |
| TC-E2E-002-R (문제 목록) | PASS | `/problems` 목록 정상 렌더: 필터(상태/우선순위/출처/담당자/기간), 테이블(식별키/요약/상태/우선순위/출처/담당자/갱신일), 페이지네이션 표시 | 회귀 없음 |
| TC-E2E-003-R (문제 상세) | PASS | `/problems/21` 상세 정상 렌더: RCA(근본원인/5 Whys/카테고리) · 워크어라운드 · KEDB 등록 · 인시던트/변경 연계 패널(연결 인시던트 `INC-2026-0014` 버튼 표시) · 후속조치 패널, 사이드바 분류(우선순위 P1/영향도 높음/긴급도 높음) | 회귀 없음 |
| TC-E2E-006-R (agent RBAC) | PASS | agent 로그인 후 `/problems` 직접 접근 시 `/403` 리다이렉트, `POST /problems`(API) 403 `ACCESS_DENIED` | 회귀 없음 |

## 실패 항목 분석
- 없음 (직전 실패 2건 모두 해소, 회귀 없음)

## 참고 관찰(범위 외, 결함 아님)
- 문제 상세 화면에서 "연결 인시던트" 버튼 클릭 시 인시던트 상세(`/incidents/{id}`)로 이동하는데, 해당 라우트는 `ROLE_INCIDENT_MANAGER`/`ROLE_SERVICE_DESK_AGENT`만 허용되어 PROBLEM_MANAGER(pm) 계정은 `/403`으로 리다이렉트됨. 이는 기존 라우트 인가 설계(`navConfig.tsx`)에 따른 의도된 동작이며 이번 수정 대상(TC-BUILD-002/TC-INC012-003)과 무관해 실패로 판정하지 않음. 정책 의도 여부는 별도 확인 필요 시 dev-lead 판단 요망.

## 결론
- **TC-BUILD-002, TC-INC012-003 수정 확인, 관련 회귀 없음.** problem 도메인 재테스트 전건 통과.
