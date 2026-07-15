# 통합 테스트 시나리오 — common (승인 대상자 역할 기반 동적 상세조회 권한, 유지보수 요청 2026-07-15)

## 사전 조건
- 빌드 테스트 통과(TC-SRM-001과 공용, service-request 결과 참조)
- 각 도메인 요청/티켓 1건 이상 존재(요청자=일반 계정), 매니저/에이전트 계정, cab@itsm.local(APPROVER, 비밀번호 `Admin@1234`)
- SYSTEM_ADMIN(admin@itsm.local)으로 `docs/02_plan/api_spec/auth.md` API-AUTH-023~026(승인 프로세스 생성)을 이용해 도메인별 규칙 생성 가능

## 시나리오

### TC-COM-001 · SRM 정적 APPROVER 전체조회 폐지 확인
- 근거: @docs/02_plan/api_spec/common.md 0-1절 "SRM/CHANGE ... 정적 권한을 폐지"
- 전제: 요청자·담당자·큐 무관, 매칭되는 승인 프로세스 규칙이 없는 서비스 요청 1건, cab@itsm.local(APPROVER, 어느 규칙에도 매칭 안 됨) 로그인
- 절차: `GET /api/v1/service-requests/{id}` 호출
- 기대 결과: 403(구버전에서는 APPROVER면 전체조회 가능했으나 이제 매칭 규칙 없으면 거부)

### TC-COM-002 · SRM 동적 매칭 시 APPROVER 조회 허용
- 근거: 위와 동일
- 전제: SYSTEM_ADMIN이 domain=SERVICE_REQUEST, requestSubtypeKey=해당 catalogItemId, 승인자역할=APPROVER인 규칙 생성(차수 1개 이상)
- 절차: cab@itsm.local로 동일 요청 상세 조회
- 기대 결과: 200(규칙 매칭으로 동적 허용), 승인 인스턴스 생성 여부와 무관(상세조회는 게이트 전이 아님)

### TC-COM-003 · CHANGE 정적 APPROVER 전체조회 폐지 확인
- 근거: @docs/02_plan/api_spec/common.md 0-1절
- 전제: 매칭 규칙 없는 변경 요청 1건, cab@itsm.local 로그인
- 절차: `GET /api/v1/changes/{id}` 호출
- 기대 결과: 403

### TC-COM-004 · CHANGE 동적 매칭 시 APPROVER 조회 허용
- 전제: SYSTEM_ADMIN이 domain=CHANGE, requestSubtypeKey=해당 변경유형, 승인자역할=APPROVER 규칙 생성
- 절차: cab@itsm.local로 동일 변경 상세 조회
- 기대 결과: 200

### TC-COM-005 · INCIDENT 결함 정리 확인 — 무역할 사용자 403
- 근거: @docs/02_plan/api_spec/common.md 0-1절 "INCIDENT ... 결함성 상태를 정리"
- 전제: SERVICE_DESK_AGENT/INCIDENT_MANAGER/매칭 규칙 어디에도 해당하지 않는 계정(예: user@itsm.local, END_USER)으로 임의 인시던트 상세 조회
- 절차: `GET /api/v1/incidents/{id}` 호출
- 기대 결과: 403(구 결함에서는 인증된 사용자면 전체조회 가능했음 — 이번에 신규 역할체크로 차단됨)

### TC-COM-006 · INCIDENT 기존 역할(SERVICE_DESK_AGENT/INCIDENT_MANAGER) 정상 조회
- 전제: agent@itsm.local(SERVICE_DESK_AGENT) 또는 im@itsm.local(INCIDENT_MANAGER)
- 절차: 동일 API 호출
- 기대 결과: 200(기존 매니저/에이전트 조회 회귀 없음)

### TC-COM-007 · INCIDENT 동적 매칭 OR 결합 확인
- 전제: SYSTEM_ADMIN이 domain=INCIDENT, 승인자역할=APPROVER 규칙 생성. cab@itsm.local(APPROVER만 보유, SERVICE_DESK_AGENT/INCIDENT_MANAGER 없음)
- 절차: 해당 인시던트 상세 조회
- 기대 결과: 200(정적 역할 조건 불충족이어도 동적 매칭 OR로 허용)

### TC-COM-008 · PROBLEM 동적 매칭 신규 허용
- 근거: @docs/02_plan/api_spec/common.md 0-1절 "PROBLEM ... 신규로 추가"
- 전제: SYSTEM_ADMIN이 domain=PROBLEM, 승인자역할=APPROVER 규칙 생성, 매칭되는 문제 티켓 1건
- 절차: cab@itsm.local로 상세 조회 / PROBLEM_MANAGER(pm@itsm.local)로 별도 상세 조회
- 기대 결과: 둘 다 200(매니저 전용 조건 유지 + APPROVER 동적 허용 추가), 매칭 안 되는 다른 문제 티켓은 cab@ 403

### TC-COM-009 · VULNERABILITY 동적 매칭 신규 허용
- 전제: SYSTEM_ADMIN이 domain=VULNERABILITY 규칙(승인자역할=APPROVER) 생성, 매칭되는 취약점 1건
- 절차: cab@itsm.local 상세 조회 / vm@itsm.local(VULNERABILITY_MANAGER) 상세 조회
- 기대 결과: 둘 다 200, 매칭 안 되는 취약점은 cab@ 403

### TC-COM-010 · COMPLIANCE 동적 매칭 신규 허용(시정조치 경유 판정)
- 근거: @docs/03_develop/plan/common.md "COMPLIANCE ... requirement의 action들을 순회"
- 전제: SYSTEM_ADMIN이 domain=COMPLIANCE 규칙(승인자역할=APPROVER) 생성. 시정조치(corrective_action) 1건 이상 보유한 요구사항 1건(그 액션의 requester가 매칭 대상), 시정조치 0건인 요구사항 1건
- 절차: cab@itsm.local로 두 요구사항 각각 상세 조회 / co@itsm.local(COMPLIANCE_OFFICER)로 상세 조회
- 기대 결과: 시정조치 보유 요구사항은 cab@ 200, 시정조치 0건 요구사항은 cab@ 403(매칭 문맥 없음). co@itsm.local은 둘 다 200(매니저 전용 조건 유지)

### TC-COM-011 · ESM 동적 매칭 신규 허용
- 전제: SYSTEM_ADMIN이 domain=ESM 규칙(승인자역할=APPROVER) 생성, 매칭되는 ESM 요청 1건(요청자 부서 무관)
- 절차: cab@itsm.local 상세 조회 / 요청자 본인 또는 같은 부서 DEPT_COORDINATOR로 상세 조회
- 기대 결과: 셋 다 200(본인/DEPT_COORDINATOR 기존 조건 유지 + APPROVER 동적 허용 추가)

### TC-COM-012 · ASSET 변경 없음 확인(no-op)
- 근거: @docs/02_plan/api_spec/common.md 0-1절 "ASSET ... 백엔드 코드 변경 불필요"
- 전제: 임의 역할 계정(예: kc@itsm.local, 자산 관련 역할 없음)
- 절차: `GET /api/v1/assets/{id}` 상세 조회
- 기대 결과: 200(기존처럼 인증된 사용자 전반 허용, 회귀 없음 — 좁히는 변경 없음 확인)

### TC-COM-013 · FE 라우트 가드 7개 도메인 ROLE_APPROVER 확인
- 근거: @docs/02_plan/api_spec/common.md 0-1절 "FE 라우트 가드"
- 전제: cab@itsm.local(APPROVER만 보유) 로그인
- 절차: SRM/CHANGE/PROBLEM/ASSET/VULNERABILITY/COMPLIANCE/ESM 7개 도메인 상세 라우트로 각각 직접 이동(navigate)
- 기대 결과: 7개 모두 라우트 가드 통과(화면 진입 자체는 허용, 실제 데이터 조회 가능 여부는 백엔드 매칭 결과로 별도 결정 — 매칭 규칙 없는 티켓 진입 시 403 처리 화면 노출)

### TC-COM-014 · INCIDENT는 FE 라우트 가드 변경 없음 확인
- 근거: 위와 동일 "INCIDENT 제외"
- 전제: cab@itsm.local(APPROVER만 보유, SERVICE_DESK_AGENT/INCIDENT_MANAGER 없음) 로그인
- 절차: 인시던트 상세 라우트로 직접 이동
- 기대 결과: 라우트 가드 자체에서 차단(403 화면 또는 접근 불가) — 라우트 가드에 ROLE_APPROVER가 추가되지 않았음을 확인
