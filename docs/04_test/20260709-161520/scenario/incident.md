# 통합 테스트 시나리오 — incident (INC)

> 실행 타임스탬프: 20260709-161520 · 도메인: incident
> 범위: API-INC-001~011, 013 (**API-INC-012 문제연계는 범위 제외** — dev-lead 지시)

## 사전 조건

- 빌드 테스트 통과(BE `gradlew test`, FE `npm run build`)
- BE(:8080)·FE dev(:5173) 기동, PostgreSQL(`itsm-postgres`) healthy
- 스키마/시드 적용: `06_incident_schema.sql`, `07_incident_seed.sql` (RBAC 증분: SCR-INC-001~005, screen_role 매핑)
- 역할 테스트 계정(SYSTEM_ADMIN `admin@itsm.local`이 생성·역할부여):
  - `im` (INCIDENT_MANAGER) — 역할 배정(API-INC-006) 전용 권한 보유
  - `agent` (SERVICE_DESK_AGENT) — 인시던트 접수·상세 접근 가능, IM 아님
  - `user` (END_USER) — incident 권한 없음(403 검증용)
- 격리: playwright 매 항목 새 context/storage 초기화. API는 계정당 1회 로그인 세션(jti 단일) 재사용. baseline은 상대 검증(생성 후 포함), 절대 개수 assert 금지.
- 근거 기준: @docs/01_analyze/prd/incident.md, @docs/01_analyze/feature/incident.md, @docs/02_plan/api_spec/incident.md, @docs/02_plan/security/authorization/incident_manager.md

## 시나리오

### A. 빌드
- TC-BUILD-001 · BE `gradlew test` 통과(예외 JUnit 포함) — 근거: @docs/01_analyze/feature/incident.md 전 FEAT
- TC-BUILD-002 · FE `npm run build` 통과 — 근거: @docs/02_plan/screen/incident.md SCR-INC-001~005

### B. 인증 (공통)
- TC-AUTH-001 · 미인증으로 INC API 호출 401 — @docs/02_plan/api_spec/incident.md 공통 규약(Bearer 필요)

### C. 인시던트 등록 (FEAT-INC-001 / API-INC-002)
- TC-INC-001 · 요약·설명·심각도·영향서비스·영향제품 포함 등록 201, `ticketKey=INC-YYYY-####`, `status=NEW`, id 반환 — @docs/01_analyze/prd/incident.md REQ-INC-001 (Event-driven)
- TC-INC-002 · 요약 누락 등록 400 — REQ-INC-001 (Unwanted), FEAT-INC-001
- TC-INC-003 · 심각도 누락 등록 400 — REQ-INC-001 (Unwanted), FEAT-INC-001
- TC-INC-004 · 정의되지 않은 심각도(예: SEV9) 등록 400 — FEAT-INC-002 (Unwanted)

### D. 목록·상세 조회 (API-INC-001 / API-INC-003)
- TC-INC-005 · 목록 조회 200, content/page/size/totalElements 구조, 생성분(TC-INC-001) 포함(상대검증) — API-INC-001
- TC-INC-006 · 목록 필터(status/severity/keyword) 200, 필터 반영 — API-INC-001
- TC-INC-007 · 상세 조회 200, 필드(severity/priority/status/responders/metrics/timeline) 구조 확인 — API-INC-003
- TC-INC-008 · 존재하지 않는 id 상세 404 — API-INC-003

### E. 심각도·우선순위 (FEAT-INC-002 / API-INC-004)
- TC-INC-009 · 심각도/우선순위 변경 200, 갱신 값 반영·이력 기록, SEV와 priority 독립 관리 — REQ-INC-002 (Event-driven/Ubiquitous)
- TC-INC-010 · 정의되지 않은 심각도/우선순위 값 변경 400 — FEAT-INC-002 (Unwanted)
- TC-INC-011 · 존재하지 않는 id severity 변경 404 — API-INC-004

### F. 상태 전이 (FEAT-INC-003 / API-INC-005)
- TC-INC-012 · 허용 전이 NEW→IN_PROGRESS 200 — REQ-INC-003 (Event-driven)
- TC-INC-013 · 허용되지 않은 전이(예: NEW→CLOSED 직접) 400 — REQ-INC-003 (Unwanted)
- TC-INC-014 · 종료(CLOSED)된 인시던트 재전이 400 — FEAT-INC-003 (Unwanted)

### G. 역할 배정 — IM 전용 (FEAT-INC-004 / API-INC-006)
- TC-INC-015 · IM이 TECH_LEAD 배정 200, responders에 반영 — REQ-INC-004 (Event-driven)
- TC-INC-016 · IM이 COMMS/SCRIBE 추가 배정 200 — REQ-INC-004
- TC-INC-017 · **비-IM(SERVICE_DESK_AGENT) 역할 배정 시도 403** — REQ-INC-004 (Unwanted), 인가규칙 API-INC-006 IM 전용
- TC-INC-018 · **비-IM(END_USER) 역할 배정 시도 403** — REQ-INC-004 (Unwanted)
- TC-INC-019 · 존재하지 않는 사용자 배정 400 또는 404 — API-INC-006 400/404
- TC-INC-020 · 존재하지 않는 인시던트 역할 배정 404 — API-INC-006 404

### H. 에스컬레이션 (FEAT-INC-005 / API-INC-007)
- TC-INC-021 · 상위/전문 담당자 에스컬레이션(HIERARCHICAL) 200, 배정·이력 기록 — REQ-INC-005 (Event-driven)
- TC-INC-022 · FUNCTIONAL 에스컬레이션 200 — REQ-INC-005
- TC-INC-023 · 대상 담당자 미존재 400 — FEAT-INC-005 (Unwanted)
- TC-INC-024 · 존재하지 않는 인시던트 에스컬레이션 404 — API-INC-007 404

### I. 상태 업데이트·타임라인 (FEAT-INC-006 / API-INC-008)
- TC-INC-025 · 내부(INTERNAL) 업데이트 등록 201, 타임스탬프·타임라인 반영 — REQ-INC-006 (Event-driven)
- TC-INC-026 · 외부(EXTERNAL) 업데이트 등록 201, visibility 구분 확인 — REQ-INC-006
- TC-INC-027 · 접근 권한 없는 사용자(END_USER) 업데이트 등록 403 — FEAT-INC-006 (Unwanted)
- TC-INC-028 · 존재하지 않는 인시던트 업데이트 404 — API-INC-008 404

### J. 해결·시간 지표 (FEAT-INC-007 / API-INC-009)
- TC-INC-029 · impactStart·detected·impactEnd 포함 해결 200, status=RESOLVED, MTTD/MTTA/MTTR 계산값 반환 — REQ-INC-007 (Event-driven)
- TC-INC-030 · 시각 정보 일부 누락(impactStart 없음) 해결 200, 해당 지표 null=미산정 표시 — REQ-INC-007 (Unwanted)
- TC-INC-031 · 존재하지 않는 인시던트 해결 404 — API-INC-009 404

### K. 포스트모템 (FEAT-INC-008 / API-INC-010, 011)
- TC-INC-032 · 미작성 인시던트 PM 조회 404 — API-INC-010 404
- TC-INC-033 · PM 작성(요약·타임라인·5 Whys·근본원인·조치항목) PUT 200, 인시던트 연결·저장 — REQ-INC-008 (Event-driven)
- TC-INC-034 · 작성 후 PM 조회 200, 저장 내용(rootCause·fiveWhys·actionItems) 확인 — API-INC-010
- TC-INC-035 · **근본원인(rootCause) 누락 제출 400** — REQ-INC-008 (Unwanted), FEAT-INC-008
- TC-INC-036 · PM 수정(재PUT) 200, 갱신 반영 — API-INC-011
- TC-INC-037 · SEV1/SEV2 해결·PM 미작성 인시던트는 목록/상세에서 `postmortemRequired=true` 표시, PM 작성 후 false — REQ-INC-008 (State-driven)

### L. 지표 리포팅 (FEAT-INC-010 / API-INC-013)
- TC-INC-038 · 지표 조회 200, `{count, severityDistribution{SEV1,SEV2,SEV3}, avgMttrMinutes}` 구조 — REQ-INC-010 (Ubiquitous)
- TC-INC-039 · 기간 필터(from/to) 데이터 없는 구간 조회 200, 빈 결과(count 0 등) — FEAT-INC-010 (Unwanted, 빈 결과)

### M. FE E2E (playwright, 매 항목 새 context)
- TC-E2E-001 · IM 로그인 → 인시던트 등록 화면(SCR-INC-002) 필수필드 입력·등록 성공, 상세 이동 — SCR-INC-002
- TC-E2E-002 · 인시던트 목록(SCR-INC-001) 필터·심각도/상태 배지 표시 — SCR-INC-001
- TC-E2E-003 · 인시던트 상세(SCR-INC-003) 심각도/상태 전이·역할 배정 패널·에스컬레이션·상태 업데이트 — SCR-INC-003
- TC-E2E-004 · 상세에서 해결 처리·시간지표(MTTx) 표시, 미산정 표시 — SCR-INC-003
- TC-E2E-005 · 포스트모템 편집(SCR-INC-004) 근본원인 필수·조치항목 저장 — SCR-INC-004
- TC-E2E-006 · 지표 대시보드(SCR-INC-005) KPI 카드·심각도 분포 차트 표시 — SCR-INC-005
- TC-E2E-007 · 비-IM(SERVICE_DESK_AGENT) 상세 화면에서 역할 배정 패널 비노출/403 — SCR-INC-003 인가

## 범위 제외 (수행하지 않음)
- API-INC-012 · 문제 연계(links) — dev-lead 지시로 제외(문제 도메인 미구현 연계). TC 미작성·미수행.
