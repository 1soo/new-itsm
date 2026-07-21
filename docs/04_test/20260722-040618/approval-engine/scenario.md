# 통합 테스트 시나리오 — approval-engine (상태별 승인자 지정 확장, 2026-07-22 유지보수)

## 사전 조건

- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend 로컬 실행 중, DB(`itsm-postgres`) 41번 마이그레이션(`target_state` 컬럼/백필) 반영 확인
- 기존 활성 규칙 재사용(회귀 검증 대상): `approval_process.id=1`(domain=SERVICE_REQUEST, request_subtype_key='1'(카탈로그 "노트북 신청"), 요청자 스코프=END_USER(role_id=2), 1차 AND[END_USER 아님 — 승인자 역할은 미확인], target_state=IN_FULFILLMENT(41번 백필값), tier=55) — 41번 마이그레이션 전부터 존재하던 유일한 실사용 규칙(다른 8개 도메인은 로컬 DB에 활성 규칙 없음, 마이그레이션 파일 주석 확인).
- 테스트 계정(공통 비밀번호 `Admin@1234`): `admin@itsm.local`(SYSTEM_ADMIN, 관리자 CRUD), `user@itsm.local`(END_USER, 요청자), `agent@itsm.local`(SERVICE_DESK_AGENT, 전이 수행자), `po@itsm.local`(PROCESS_OWNER), `cab@itsm.local`(APPROVER — AGENT/PROCESS_OWNER 아님, canApproverView 동적 권한 전용 검증 계정)
- 서비스 카탈로그: item id=1(노트북 신청, 기존 규칙 스코프) / item id=2(비밀번호 초기화, 기존 규칙 없음 — 신규 게이트 격리 테스트용)
- dev-lead 지시(핵심 확인 포인트): 1) 기존 게이트 지점 회귀 없음 2) 신규 상태별 게이트 정상 동작 3) 생성 시점 게이트에서 마스터 레코드 롤백 없음 4) 반려 후 재승인요청 흐름 5) 담당자(호출자)에 따라 승인요청자 역할 매칭이 달라지는 시나리오

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/api_spec/common.md (0절), @docs/02_plan/api_spec/auth.md (API-AUTH-031)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-STATES-001 · 도메인별 적용 상태 후보 목록 조회(API-AUTH-031)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-031)
- 전제: admin@itsm.local 로그인
- 절차: `GET /api/v1/admin/approval-processes/domains/SERVICE_REQUEST/states`
- 기대 결과: 200, SUBMITTED/VALIDATED/ROUTED/IN_FULFILLMENT/FULFILLED/CLOSED 6개 값+라벨 반환

### TC-ADMVALID-001 · targetState 지정 시 requesterRoleIds 빈 배열이면 400
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-027 확정 방침 6), `ApprovalProcessAdminService.validateTargetStateRequiresRole`
- 절차: `POST /api/v1/admin/approval-processes` body `{domain:"SERVICE_REQUEST", targetState:"VALIDATED", requestSubtypeKey:"2", requesterRoleIds:[], steps:[{stepNo:1,decisionMode:"AND",roleIds:[4]}]}`
- 기대 결과: 400(VALIDATION_ERROR류), 규칙 생성 안 됨

### TC-ADMVALID-002 · targetState가 해당 도메인 후보 외 값이면 400
- 근거: `ApprovalProcessAdminService.validateTargetStateIsValidOption`
- 절차: 위와 동일하되 `targetState:"NOT_A_REAL_STATE"`, `requesterRoleIds:[2]`
- 기대 결과: 400

### TC-ADM-CREATE-001 · 생성 시점 게이트 규칙 신규 등록(SUBMITTED, END_USER)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-027)
- 절차: `POST /api/v1/admin/approval-processes` `{domain:"SERVICE_REQUEST", targetState:"SUBMITTED", requestSubtypeKey:"2", requesterRoleIds:[2], steps:[{stepNo:1,decisionMode:"AND",roleIds:[4]}]}`
- 기대 결과: 201, tier=55(4축 모두 지정)

### TC-ADM-CREATE-002 · 신규 상태 게이트 규칙 등록(VALIDATED, SERVICE_DESK_AGENT)
- 근거: 상동
- 절차: `POST /api/v1/admin/approval-processes` `{domain:"SERVICE_REQUEST", targetState:"VALIDATED", requestSubtypeKey:"2", requesterRoleIds:[3], steps:[{stepNo:1,decisionMode:"AND",roleIds:[4]}]}`
- 기대 결과: 201

### TC-REGRESSION-001 · 기존 게이트 지점(백필 target_state) 마이그레이션 후 회귀 확인 — 카탈로그 item 1, IN_FULFILLMENT
- 근거: @docs/02_plan/api_spec/common.md (0절, 요청자 산출 규칙), 마이그레이션 41번 백필 대상 규칙(id=1)
- 전제: user@itsm.local이 item 1로 신규 요청 제출, agent@itsm.local이 배정
- 절차: 1) agent가 SUBMITTED→VALIDATED→ROUTED→IN_FULFILLMENT 순차 전이
- 기대 결과(사전 예상 — 마이그레이션 전 기준): IN_FULFILLMENT 전이 시 규칙(요청자 스코프=END_USER) 매칭돼 409(`APPROVAL_PENDING`)
- **검증 포인트**: 방침 5(요청자=현재 호출자)가 적용되면 IN_FULFILLMENT 전이는 오직 SERVICE_DESK_AGENT만 호출 가능(`assertTransitionRole`, END_USER는 CLOSED 외 전이 호출 권한 자체가 없음)한데, 기존 규칙의 요청자 스코프는 END_USER로 그대로 남아있다 — 실제 호출자(agent, SERVICE_DESK_AGENT)가 이 스코프와 매칭되는지 반드시 확인

### TC-NEWGATE-001 · 생성 시점 게이트(TC-ADM-CREATE-001) — 마스터 레코드 롤백 없음
- 근거: @docs/02_plan/api_spec/common.md (0절), `TicketCreationGateSupport`
- 전제: TC-ADM-CREATE-001 규칙 활성
- 절차: 1) user@itsm.local이 item 2로 신규 요청 제출(`POST /api/v1/service-requests`)
- 기대 결과: 1) 409(`APPROVAL_PENDING`) + `approvalRequestId` 반환 2) `GET /api/v1/approvals/{approvalRequestId}`로 ticketId 확인 후 po@itsm.local(PROCESS_OWNER, 조회 권한 보유)로 `GET /api/v1/service-requests/{ticketId}` 조회 시 200 + `status=SUBMITTED`(마스터 레코드 정상 존재, 생성 롤백 없음)

### TC-VIEWAUTH-001 · canApproverView — 승인자 역할(APPROVER, AGENT/PROCESS_OWNER 아님)의 동적 상세조회
- 근거: @docs/02_plan/api_spec/common.md (0-1절), TC-ADM-CREATE-001의 승인자 차수 역할=APPROVER(id 4)
- 전제: TC-NEWGATE-001의 티켓, cab@itsm.local(APPROVER 단독 보유)
- 절차: `GET /api/v1/service-requests/{ticketId}`(cab@itsm.local 토큰)
- 기대 결과: 200(매칭 규칙의 승인자 역할이라 동적 조회 권한 인정, 기존엔 SRM은 AGENT/PROCESS_OWNER만 정적 허용이었음)

### TC-REJECT-001 · 생성 시점 승인 반려
- 근거: @docs/02_plan/api_spec/common.md (2절 API-COM-005/006)
- 전제: TC-NEWGATE-001의 approvalRequestId
- 절차: cab@itsm.local(APPROVER, 1차 승인자)로 `POST /api/v1/approvals/{approvalRequestId}/decisions` `{decision:"REJECT", reason:"테스트 반려"}`
- 기대 결과: 200, 인스턴스 상태 REJECTED

### TC-RESUBMIT-001 · 반려 후 재승인요청(API-COM-006) — 생성 시점 게이트
- 근거: @docs/02_plan/api_spec/common.md (2절 API-COM-006)
- 전제: TC-REJECT-001로 REJECTED된 인스턴스
- 절차: 1) user@itsm.local로 `POST /api/v1/approvals/resubmit` `{ticketType:"SERVICE_REQUEST", ticketId}` 2) 생성된 새 인스턴스를 cab@itsm.local로 APPROVE
- 기대 결과: 1) 200, 새 `approvalRequestId`(REJECTED 인스턴스와 다른 id), `status=IN_PROGRESS` 2) 200, APPROVED. 상세(API-COM-004)의 `targetState=SUBMITTED` 유지 확인(재매칭 대상 targetState 불변)

### TC-ACTORROLE-001 · 담당자(호출자)별 승인요청자 역할 매칭 — 신규 상태 게이트(VALIDATED, TC-ADM-CREATE-002)
- 근거: @docs/02_plan/api_spec/common.md (0절 요청자 산출 규칙), dev-lead 지시(담당자별 매칭 차이 시나리오)
- 전제: TC-RESUBMIT-001로 생성 게이트(SUBMITTED) 승인 완료된 티켓, agent@itsm.local이 배정
- 절차: 1) agent@itsm.local로 `PATCH .../status {targetStatus:"VALIDATED"}`
- 기대 결과: 409(`APPROVAL_PENDING`) + 신규 `approvalRequestId`(TC-ADM-CREATE-002 규칙(요청자 스코프=SERVICE_DESK_AGENT)이 호출자 agent와 매칭돼 신규 인스턴스 생성) — **같은 티켓의 SUBMITTED 게이트는 요청자(user, END_USER)로, VALIDATED 게이트는 전이자(agent, SERVICE_DESK_AGENT)로 서로 다른 역할이 매칭됨을 확인**

### TC-ACTORROLE-002 · VALIDATED 게이트 승인 후 전이 재시도 성공
- 근거: 상동
- 전제: TC-ACTORROLE-001의 인스턴스
- 절차: 1) cab@itsm.local(APPROVER)로 APPROVE 2) agent@itsm.local로 VALIDATED 재시도
- 기대 결과: 1) 200 APPROVED 2) 200, `status=VALIDATED`

### TC-NOROUTE-001 · 매칭 규칙 없는 전이는 게이트 없이 통과(회귀)
- 근거: @docs/02_plan/api_spec/common.md (0절 2번)
- 전제: TC-ACTORROLE-002 티켓(item 2, ROUTED/IN_FULFILLMENT/FULFILLED에는 규칙 없음), agent 배정 완료
- 절차: 1) agent로 ROUTED→IN_FULFILLMENT→FULFILLED 순차 전이
- 기대 결과: 전 구간 게이트 없이 200(409 없음)

### TC-CROSSDOMAIN-001 · 타 도메인 최소 회귀 스팟체크(규칙 없는 도메인, 게이트 없이 통과)
- 근거: 마이그레이션 41번 주석(SERVICE_REQUEST 외 도메인은 로컬 DB에 활성 규칙 없음)
- 절차: 1) CHANGE 도메인에서 RFC 1건 생성 후 상태 전이 1단계 시도(cm@itsm.local)
- 기대 결과: 활성 규칙이 없으므로 게이트 없이 200(구조적 회귀 위험은 TC-REGRESSION-001 결과로 전 도메인 공통 적용, 별도 도메인별 심층 재현은 생략)

### TC-FE-001 · SRM 상세화면 승인대기/반려 배지 + targetStateLabel 노출(playwright, 새 컨텍스트)
- 근거: @docs/02_plan/screen/admin.md, `deriveApprovalStatusDisplay`/`ApprovalPanel`
- 전제: TC-ACTORROLE-001 상태(VALIDATED 게이트 IN_PROGRESS)
- 절차: 1) 새 브라우저 컨텍스트로 agent@itsm.local 로그인 2) 해당 요청 상세 진입
- 기대 결과: 상태 배지 "{VALIDATED 라벨}(승인대기)" 표시, 승인 패널에 대상 상태 라벨 노출

### TC-FE-002 · 반려 배지 + 재승인요청 버튼(playwright, 새 컨텍스트)
- 근거: 상동, 5절 재승인요청 버튼
- 전제: TC-REJECT-001 직후(재승인요청 전) 상태 재현 필요 시 신규 반려 건 생성, 또는 이미 승인 완료된 흐름이라면 신규로 규칙 위반 1건 재현
- 절차: 1) 새 컨텍스트로 user@itsm.local 로그인 2) 반려된 티켓 상세 진입 3) "재승인요청" 버튼 클릭
- 기대 결과: "{targetStateLabel}(반려됨)" 배지 + 재승인요청 버튼 노출, 클릭 시 API-COM-006 호출 성공 후 화면이 "승인대기"로 갱신

### TC-FE-003 · 관리자 화면 적용 상태 지정 시 요청자 역할 필수 검증(playwright, 새 컨텍스트)
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-008, 확정 방침 6)
- 절차: 1) 새 컨텍스트로 admin@itsm.local 로그인 2) `/admin/approval-processes/new` 진입 3) 도메인 선택 4) 적용 상태를 "전체 상태 공통" 아닌 구체적 상태로 선택 5) 요청자 역할 박스를 비운 채 저장 시도
- 기대 결과: 인라인 에러("이 상태에서 요청할 역할을 지정하세요" 류) + 저장 버튼 비활성 또는 저장 차단

### TC-FE-004 · 관리자 목록 "적용 상태" 컬럼(playwright, 새 컨텍스트)
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-007)
- 절차: 1) 새 컨텍스트로 admin@itsm.local 로그인 2) `/admin/approval-processes` 진입
- 기대 결과: TC-ADM-CREATE-001/002로 생성한 규칙이 목록에 "적용 상태" 컬럼(SUBMITTED/VALIDATED 라벨)과 함께 노출
