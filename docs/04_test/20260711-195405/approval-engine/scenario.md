# 통합 테스트 시나리오 — approval-engine (Stage 1)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- 로컬 docker-compose DB fresh 재기동 완료(볼륨 삭제 후 재기동, 26/27번 SQL 최초 실행 확인)
- 테스트 계정(공통 비밀번호 `Admin@1234`): `admin@itsm.local`(SYSTEM_ADMIN), `user@itsm.local`(END_USER), `cab@itsm.local`(APPROVER), `cm@itsm.local`(CHANGE_MANAGER), `po@itsm.local`(PROCESS_OWNER)
- 서비스 카탈로그 항목(05_srm_seed.sql): "노트북 신청"(IT 서비스 큐), "비밀번호 초기화"

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/api_spec/common.md, @docs/02_plan/api_spec/auth.md
- 절차: 1) `./gradlew build`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-ADM-001 · 승인 프로세스 대상 도메인 목록 조회(SYSTEM_ADMIN)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-023)
- 전제: admin@itsm.local 로그인
- 절차: 1) `GET /api/v1/admin/approval-processes/domains`
- 기대 결과: 200, 9개 도메인(SERVICE_REQUEST/CHANGE/KNOWLEDGE/INCIDENT/PROBLEM/ASSET/VULNERABILITY/COMPLIANCE/ESM)만 포함, AUTH/COMMON/INFRA_MONITORING 제외. SERVICE_REQUEST/CHANGE는 `hasRequestSubtype=true`

### TC-ADM-002 · 도메인별 요청유형 후보 조회
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-024)
- 절차: 1) `GET /api/v1/admin/approval-processes/domains/SERVICE_REQUEST/request-subtypes` 2) `GET .../CHANGE/request-subtypes` 3) `GET .../INCIDENT/request-subtypes`(하위유형 없음)
- 기대 결과: SERVICE_REQUEST → 카탈로그 항목 목록(id+name), CHANGE → STANDARD/NORMAL/EMERGENCY 고정 코드, INCIDENT → 빈 배열(200)

### TC-ADM-003 · 승인 프로세스 생성 — tier=2(요청유형 전용), OR 1차
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-027)
- 절차: 1) `POST /api/v1/admin/approval-processes` body: domain=SERVICE_REQUEST, requestSubtypeKey="노트북 신청" 카탈로그 id, requesterRoleIds=[], name="노트북 신청 승인", steps=[{decisionMode:"OR", roleIds:[APPROVER, CHANGE_MANAGER 역할id]}]
- 기대 결과: 201, priorityTier=2로 저장, steps 1개(OR, 역할 2개)

### TC-ADM-004 · 우선순위 충돌 409 — 동일 tier=2 스코프 재생성
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-027 409)
- 전제: TC-ADM-003 규칙 존재
- 절차: 1) 동일 domain=SERVICE_REQUEST, requestSubtypeKey=동일 카탈로그 id로 재생성 시도(requesterRoleIds 없음)
- 기대 결과: 409

### TC-ADM-005 · 승인 프로세스 목록/상세 조회
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-025/026)
- 절차: 1) `GET /api/v1/admin/approval-processes?domain=SERVICE_REQUEST` 2) `GET /api/v1/admin/approval-processes/{id}`
- 기대 결과: 200, 목록에 TC-ADM-003 항목 포함(requestSubtypeLabel resolve), 상세에 steps/roleIds 정확히 반영

### TC-ADM-006 · 승인 프로세스 수정(steps 전체 교체 → AND 2차)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-028)
- 절차: 1) `PATCH /api/v1/admin/approval-processes/{id}` body: steps=[{decisionMode:"AND", roleIds:[APPROVER,CHANGE_MANAGER]}] (1차만, AND로 변경)
- 기대 결과: 200, 차수 1개 decisionMode=AND로 갱신

### TC-ADM-007 · 승인 프로세스 삭제(soft delete)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-029)
- 절차: 별도 테스트용 규칙(TC-SRM 시나리오와 무관한 임시 규칙) 생성 후 `DELETE /api/v1/admin/approval-processes/{tempId}`
- 기대 결과: 200 `{deleted:true}`, 목록 조회 시 제외됨

### TC-ADM-008 · SYSTEM_ADMIN 아닌 계정 403
- 근거: @docs/02_plan/security/authorization/system_admin.md, @docs/02_plan/api_spec/auth.md
- 전제: user@itsm.local(END_USER) 로그인
- 절차: 1) `GET /api/v1/admin/approval-processes` 2) `POST /api/v1/admin/approval-processes`
- 기대 결과: 둘 다 403

### TC-SRM-001 · 승인 게이트 — 매칭 규칙 있음(AND, 1차) → 이행 전이 409 + 인스턴스 생성
- 근거: @docs/02_plan/api_spec/common.md (0절 게이트, API-COM-003~005), @docs/02_plan/api_spec/service-request.md
- 전제: TC-ADM-006에서 "노트북 신청" 카탈로그에 AND(APPROVER+CHANGE_MANAGER) 1차 규칙 존재
- 절차: 1) user@itsm.local로 "노트북 신청" 서비스요청 제출 2) SERVICE_DESK_AGENT 등으로 VALIDATED→ROUTED 진행 3) `PATCH .../status {targetStatus:"IN_FULFILLMENT"}` 시도
- 기대 결과: 409, 응답에 `approvalRequestId` 포함, `approval_request` IN_PROGRESS 인스턴스 생성 확인(API-COM-004 상세 조회)

### TC-SRM-002 · 공용 승인 대기함 노출 — 역할 보유자만
- 근거: @docs/02_plan/screen/common.md (SCR-COM-014), @docs/02_plan/api_spec/common.md (API-COM-003)
- 절차: 1) cab@itsm.local(APPROVER)로 `GET /api/v1/approvals?scope=mine` 2) po@itsm.local(PROCESS_OWNER, 무관 역할)로 동일 조회
- 기대 결과: APPROVER 목록에 TC-SRM-001 인스턴스 포함, PROCESS_OWNER 목록에는 미포함(빈 배열 또는 무관)

### TC-SRM-003 · AND 결정 — 반려 사유 누락 400
- 근거: @docs/02_plan/api_spec/common.md (API-COM-005 400)
- 절차: 1) cab@itsm.local로 `POST /api/v1/approvals/{id}/decisions {decision:"REJECT"}`(reason 누락)
- 기대 결과: 400

### TC-SRM-004 · AND 결정 — 역할별 승인, 모두 채워야 차수 완료
- 근거: @docs/02_plan/api_spec/common.md (0절 결정 처리, API-COM-005)
- 절차: 1) cab@itsm.local(APPROVER)로 APPROVE 결정 2) 상세 조회 → stepStatus=PENDING(CHANGE_MANAGER 슬롯 미결) 3) cm@itsm.local(CHANGE_MANAGER)로 APPROVE 결정
- 기대 결과: 1) 200, requestStatus=IN_PROGRESS 유지 2) CHANGE_MANAGER 결정 후 stepStatus=APPROVED, 마지막 차수라 requestStatus=APPROVED

### TC-SRM-005 · 이미 결정된 슬롯 재처리 409
- 근거: @docs/02_plan/api_spec/common.md (API-COM-005 409)
- 절차: 1) cab@itsm.local로 같은 인스턴스에 재차 결정 시도
- 기대 결과: 409(이미 종료된 인스턴스 또는 슬롯 재처리)

### TC-SRM-006 · 전체 승인 완료 후 이행 전이 재시도 허용
- 근거: @docs/02_plan/api_spec/service-request.md (API-SRM-... 상태전이)
- 전제: TC-SRM-004에서 인스턴스 APPROVED
- 절차: 1) `PATCH .../status {targetStatus:"IN_FULFILLMENT"}` 재시도
- 기대 결과: 200, 전이 허용

### TC-SRM-007 · OR 결정 — 최초 1건으로 차수 확정
- 근거: @docs/02_plan/api_spec/common.md (0절 결정 처리 OR)
- 전제: 신규 서비스요청 제출 후 "노트북 신청" 규칙을 OR 2역할로 재구성(steps PATCH) 또는 별도 OR 전용 규칙으로 재현
- 절차: 1) 신규 요청 제출·이행 전이 시도로 신규 인스턴스 생성(409) 2) cab@itsm.local(APPROVER)만 APPROVE
- 기대 결과: OR 차수는 1건 결정으로 즉시 stepStatus=APPROVED, 마지막 차수면 requestStatus=APPROVED

### TC-SRM-008 · REJECT 시 인스턴스 종료, 재전이 409 유지
- 근거: @docs/02_plan/api_spec/common.md (0절 3번, API-COM-005)
- 절차: 1) 신규 요청/인스턴스 생성 2) REJECT(사유 포함) 결정
- 기대 결과: 200, requestStatus=REJECTED. 이후 이행 전이 재시도 시 409 유지

### TC-SRM-009 · 매칭 규칙 없음/0차 규칙 — 게이트 없이 즉시 통과
- 근거: @docs/02_plan/api_spec/common.md (0절 2번)
- 절차: 1) "비밀번호 초기화"(승인 규칙 미설정) 카탈로그로 요청 제출 후 이행 전이 시도
- 기대 결과: 200(승인 인스턴스 생성 없이 즉시 전이 허용)

### TC-NOTI-001 · 헤더 알림 — 승인 대기 표시(API-COM-003 기준)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002), @docs/02_plan/api_spec/common.md (API-COM-003)
- 절차: 1) playwright 새 컨텍스트로 cab@itsm.local 로그인 2) 헤더 알림 벨 클릭
- 기대 결과: TC-SRM-001/007에서 생성된 미결 인스턴스가 알림 드롭다운에 "서비스요청 승인" 라벨로 노출

### TC-NOTI-002 · 헤더 알림 확인처리(dismiss) 반영
- 근거: @docs/02_plan/api_spec/common.md (API-COM-001)
- 절차: 1) TC-NOTI-001 알림 항목 개별 X 클릭
- 기대 결과: 목록에서 즉시 제거, 재조회(5초 polling) 후에도 재노출되지 않음

### TC-CHG-001 · CHANGE 목록/생성/상세 회귀
- 근거: @docs/02_plan/api_spec/change.md, @docs/02_plan/screen/change.md
- 절차: 1) `GET /api/v1/changes` 2) 변경 요청 생성 3) `GET /api/v1/changes/{id}` 상세 조회
- 기대 결과: 정상 동작, 상세 응답에 `approval:{approvalRequestId:null, status:null}` 포함(매칭 승인 프로세스 없음)

### TC-CHG-002 · CHANGE 상태전이 — 게이트 미연동으로 구현(IMPLEMENTATION) 전이 통과
- 근거: dev-lead-2 지시(Stage 1은 CHANGE 게이트 미연동), @docs/02_plan/api_spec/change.md (API-CHG-004)
- 절차: 1) REVIEW→PLANNING→APPROVAL→IMPLEMENTATION 순차 전이
- 기대 결과: 승인 없이 IMPLEMENTATION 전이까지 정상 통과(409 없음)

### TC-CHG-003 · 구 CAB 승인 대기함 제거 확인
- 근거: @docs/02_plan/screen/change.md (SCR-CHG-004 제거), 27_approval_engine_seed.sql
- 절차: 1) `GET /api/v1/approvals/changes`(구 API) 호출 2) FE에서 `/changes`(구 CAB 대기함 경로 추정) 접근
- 기대 결과: 구 API 404(라우트 없음), screen_role에서 SCR-CHG-004 제거되어 사이드바/라우팅에서 노출 없음

### TC-PERM-001 · PROCESS_OWNER 카탈로그 승인필드 제거 확인
- 근거: @docs/02_plan/security/authorization/process_owner.md, @docs/02_plan/api_spec/service-request.md
- 전제: po@itsm.local 로그인
- 절차: 1) `POST /api/v1/service-catalog/items` 승인 관련 필드(approvalRequired/approverRole) 없이 생성 요청
- 기대 결과: 201 정상 생성(응답에 승인 필드 없음), DB에도 해당 컬럼 없음(schema 확인)

### TC-PERM-002 · APPROVER/CHANGE_MANAGER 동적 판정 — 무관 역할 403
- 근거: @docs/02_plan/security/authorization/approver.md (4절)
- 전제: TC-SRM-007 등에서 생성된 진행 중 인스턴스 존재
- 절차: 1) po@itsm.local(무관 역할)로 `POST /api/v1/approvals/{id}/decisions {decision:"APPROVE"}`
- 기대 결과: 403(현재 대기 차수 필요 역할 미보유)
