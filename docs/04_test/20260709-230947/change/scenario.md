# 통합 테스트 시나리오 — change (CHG)

> 실행 타임스탬프: 20260709-230947 · 도메인: change
> 범위: API-CHG-001~012 정상+오류(400/401/403/404/409), 6단계 상태 전이(승인 전 구현 차단), 유형·위험 분류→승인경로(AUTO/PEER_REVIEW/CAB), 표준 변경 사전승인, 승인/반려(역할기반 공유대기함), 구현 결과 기록, 인시던트/문제 양방향 연계, 변경 일정(캘린더), 템플릿, 지표
> **크로스 도메인 회귀(중요)**: problem→CHANGE 연계 완성(`CHANGE_LINK_UNAVAILABLE` 스텁 제거), incident→문제 연계 버튼 활성화(API-INC-012 재검증 겸), 인시던트 상세 `links[].targetKey` 계약(원시 ID → 티켓키) 수정 회귀

## 사전 조건

- 빌드 테스트 통과(BE `gradlew test`, FE `npm run build`)
- BE(:8080)·FE dev(:5173) 기동, PostgreSQL(`itsm-postgres`) healthy
- 테스트 계정:
  - `cm@itsm.local` — CHANGE_MANAGER (변경 등록/전이/분류/구현결과/연계/일정/템플릿/지표)
  - `cab@itsm.local` — APPROVER (CAB 승인/반려, 변경 상세 읽기)
  - `im@itsm.local` — INCIDENT_MANAGER (인시던트 생성, incident→문제 연계 회귀용)
  - `pm@itsm.local` — PROBLEM_MANAGER (문제 생성, problem→CHANGE 연계 회귀용)
  - `agent@itsm.local` — SERVICE_DESK_AGENT (change 권한 없음 → 403 검증용)
- 근거 기준: @docs/01_analyze/prd/change.md, @docs/01_analyze/feature/change.md, @docs/02_plan/api_spec/change.md, @docs/02_plan/api_spec/incident.md(API-INC-012), @docs/02_plan/api_spec/problem.md(API-PRB-009), @docs/02_plan/screen/change.md, @docs/02_plan/security/authorization/change_manager.md, @docs/02_plan/security/authorization/approver.md
- 격리: playwright 매 항목 새 context/storage 초기화. API는 계정당 로그인 세션 재사용(TTL 300s 내 그룹 실행).

## 시나리오

### A. 빌드
- **TC-BUILD-001** · BE `gradlew test` 통과(change 패키지 JUnit 포함) — @docs/01_analyze/feature/change.md 전 FEAT
- **TC-BUILD-002** · FE `npm run build`(tsc + vite build) 통과 — @docs/02_plan/screen/change.md SCR-CHG-001~006

### B. 인증/인가 (401/403)
- **TC-AUTH-001** · 미인증 `GET /api/v1/changes` 401 — 공통 규약
- **TC-CHG-RBAC-001** · agent(권한 없음) `POST /changes` 시도 403 — @docs/02_plan/security/authorization/change_manager.md
- **TC-CHG-RBAC-002** · agent `POST /changes/{id}/approval` 시도 403 — approver.md
- **TC-CHG-RBAC-003** · CHANGE_MANAGER(cm)가 직접 승인(`POST .../approval`) 시도 403 — REQ-CHG-005(승인은 APPROVER 전용, CM 단독 불가)

### C. RFC 생성·분류 (FEAT-CHG-001/002 / API-CHG-002/005)
- **TC-CHG-001** · 정상 RFC 생성(summary+type=NORMAL+risk=HIGH 등) 201, `ticketKey=CHG-YYYY-####`, `status=REQUESTED` — REQ-CHG-001 (Event-driven)
- **TC-CHG-002** · summary 누락 생성 400 — REQ-CHG-001 (Unwanted)
- **TC-CHG-003** · type 누락 생성 400 — FEAT-CHG-001 (Unwanted)
- **TC-CHG-004** · 목록 조회 200 `{content,page,size,totalElements}`, 생성분 포함(상대검증) — API-CHG-001
- **TC-CHG-005** · 상세 조회 200, 필드(type/risk/status/approvalRoute/implementationPlan/rollbackPlan/result/approvals/links) 구조 확인 — API-CHG-003
- **TC-CHG-006** · 존재하지 않는 id 상세 404 — API-CHG-003
- **TC-CHG-007** · 분류 변경(PATCH classification, risk=HIGH) 200, `approvalRoute` 갱신 확인 — API-CHG-005
- **TC-CHG-008** · 위험도 미평가(risk 미지정/null) 분류 시 기본 CAB 경로 — FEAT-CHG-004 (Unwanted, 미평가→CAB)
- **TC-CHG-009** · 정의되지 않은 유형 분류 400 — API-CHG-005 400

### D. 6단계 상태 전이 (FEAT-CHG-003 / API-CHG-004)
> 단계: REQUESTED → REVIEW → PLANNING → APPROVAL → IMPLEMENTATION → CLOSED
- **TC-CHG-010** · 허용 전이 REQUESTED→REVIEW 200 — REQ-CHG-003 (Event-driven)
- **TC-CHG-011** · 순서 어긋난 전이(REQUESTED→IMPLEMENTATION 등 단계 건너뜀) 400 — REQ-CHG-003 (Unwanted)
- **TC-CHG-012** · 승인 완료 전 APPROVAL 단계에서 IMPLEMENTATION 전이 시도 409 — REQ-CHG-003 (Unwanted), API-CHG-004 409
- **TC-CHG-013** · 존재하지 않는 id 전이 404 — API-CHG-004 404

### E. 표준 변경 사전승인 (FEAT-CHG-006)
- **TC-CHG-014** · 표준 변경 템플릿 목록 조회 200, 시드 템플릿(예: "표준 패치 배포") 포함 — API-CHG-011
- **TC-CHG-015** · type=STANDARD + templateId 지정 생성 → `approvalRoute=AUTO`, 승인 없이 PLANNING/IMPLEMENTATION까지 전이 가능 — REQ-CHG-006 (Event-driven)
- **TC-CHG-016** · 표준 템플릿에 없는 templateId로 표준 지정 시 일반 승인 경로(PEER_REVIEW/CAB)로 처리 — FEAT-CHG-006 (Unwanted)

### F. 승인/반려 (FEAT-CHG-005 / API-CHG-006/007)
- **TC-CHG-017** · 고위험(NORMAL+risk=HIGH) 변경 REVIEW 전이 시 승인경로 CAB 결정 — REQ-CHG-004 (Complex, 고위험→CAB)
- **TC-CHG-018** · 승인 대기 목록(cab, scope=mine&type=change) 200, 대상 변경 포함 — API-CHG-007
- **TC-CHG-019** · CAB 승인(decision=APPROVE) 200, `status` 갱신·승인 이력 기록 — REQ-CHG-005 (Event-driven)
- **TC-CHG-020** · 이미 결정된 승인 재처리 409 — API-CHG-006 409
- **TC-CHG-021** · 반려(decision=REJECT) 시 사유(opinion) 누락 400 — approver.md(반려 사유 필수)
- **TC-CHG-022** · 존재하지 않는 id 승인 404 — API-CHG-006 404
- **TC-CHG-023** · 승인 권한 없는 역할(cm) 승인 시도 403 — TC-CHG-RBAC-003과 동일 관점(분류상 별도 유지)

### G. 구현 결과 기록 (FEAT-CHG-008 / API-CHG-008)
- **TC-CHG-024** · 승인 완료 후 구현 결과 기록(outcome=SUCCESS, rolledBack=false) 200 — REQ-CHG-008 (Event-driven)
- **TC-CHG-025** · 승인되지 않은 변경(REQUESTED/REVIEW 등)에 결과 기록 시도 400 — FEAT-CHG-008 (Unwanted)
- **TC-CHG-026** · 존재하지 않는 id 결과 기록 404 — API-CHG-008 404

### H. 인시던트/문제 연계 (FEAT-CHG-009 / API-CHG-009) — 양방향 신규 구현
- **TC-CHG-027** · 변경→인시던트 연계(targetType=INCIDENT, 기존 인시던트) 200, 양방향: 변경 상세 `links`에 INCIDENT 노출 + 인시던트 상세 `links`에 CHANGE 노출 — REQ-CHG-009 (Event-driven)
- **TC-CHG-028** · 변경→문제 연계(targetType=PROBLEM, 기존 문제) 200, 양방향: 변경 상세 `links`에 PROBLEM + 문제 상세 `linkedChanges` 반영 — REQ-CHG-009 (Event-driven)
- **TC-CHG-029** · 존재하지 않는 대상(targetId) 연계 400 — FEAT-CHG-009 (Unwanted)
- **TC-CHG-030** · 존재하지 않는 id(변경) 연계 404 — API-CHG-009 404

### I. 크로스 도메인 회귀 — problem→CHANGE 연계 완성 (연기분 회수)
- **TC-CROSS-001** · `POST /problems/{id}/links {targetType:CHANGE, targetId:기존 변경}` 200(기존 `CHANGE_LINK_UNAVAILABLE` 400 스텁 제거 확인), 문제 상세 `linkedChanges` 반영 + 변경 상세 `links`에 PROBLEM 노출 — 개발계획 change.md §크로스 도메인
- **TC-CROSS-002** · problem→존재하지 않는 변경 연계 400 — 대칭 오류코드 일관성

### J. 크로스 도메인 회귀 — incident 쪽 연계 버튼/계약
- **TC-CROSS-003** · FE 인시던트 상세 "문제 연계" 버튼 활성 상태 확인(비활성/스텁 아님) — API-INC-012 재검증 겸
- **TC-CROSS-004** · 인시던트 상세 `GET /incidents/{id}` 응답 `links[].targetKey`가 원시 ID가 아닌 **티켓키**(예: `PRB-YYYY-####`, `CHG-YYYY-####`) 형식인지 확인(PROBLEM/CHANGE 링크 모두) — 계약 수정 회귀

### K. 변경 일정(캘린더) (FEAT-CHG-007 / API-CHG-010)
- **TC-CHG-031** · 기간 내 예정 변경(scheduledAt 포함) 조회 200, 목록 반영 — REQ-CHG-007 (Event-driven)
- **TC-CHG-032** · 예정 변경 없는 기간 조회 200, 빈 배열 — FEAT-CHG-007 (Unwanted)

### L. 변경 지표 (FEAT-CHG-010 / API-CHG-012)
- **TC-CHG-033** · 지표 조회 200, `{successRate,failureRate,emergencyRate,total}` 구조, 앞서 기록한 구현결과 반영(상대검증) — REQ-CHG-010 (Ubiquitous)
- **TC-CHG-034** · 데이터 없는 기간(from/to 좁게) 조회 200, 빈 결과(0 또는 null) — FEAT-CHG-010 (Unwanted)

### M. FE E2E (playwright, http://localhost:5173, 매 항목 새 context/storage)
- **TC-E2E-001** · CM 로그인 → RFC 생성(SCR-CHG-002) 유형/위험 선택·표준 변경 시 템플릿 선택·승인 생략 안내 → 생성 성공 후 상세 이동 — SCR-CHG-002
- **TC-E2E-002** · 변경 목록(SCR-CHG-001) 필터(유형·상태·위험도·기간)·배지 표시 — SCR-CHG-001
- **TC-E2E-003** · 변경 상세(SCR-CHG-003) 상태 전이(승인 전 구현 차단 UI)·승인경로 배지·구현결과 기록·인시던트/문제 연계 버튼 — SCR-CHG-003
- **TC-E2E-004** · CAB(cab) 로그인 → 승인 대기함(SCR-CHG-004) 승인/반려(의견) 처리 — SCR-CHG-004
- **TC-E2E-005** · 변경 일정 캘린더(SCR-CHG-005) 예정 변경 표시·항목 클릭 시 상세 이동 — SCR-CHG-005
- **TC-E2E-006** · 변경 지표 대시보드(SCR-CHG-006) KPI 카드 표시 — SCR-CHG-006
- **TC-E2E-007** · 비-CM/APPROVER(agent) 로그인 시 변경 메뉴 비노출 + `/changes` 직접 접근 시 `/403` 리다이렉트 — change_manager.md/approver.md 인가
