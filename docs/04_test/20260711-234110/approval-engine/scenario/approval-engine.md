# 통합 테스트 시나리오 — approval-engine (Stage 4: INCIDENT + PROBLEM 승인 게이트 연동)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- Stage 1~3 산출물(공용 승인 엔진, SCR-ADMIN-007/008, SCR-COM-014) 정상 동작 전제
- backend(:8080)에 Stage 4 코드(`IncidentService`/`ProblemService`↔`ApprovalGateService.checkGate`, `IncidentApprovalTicketSummaryProvider`, `ProblemApprovalTicketSummaryProvider`) 반영 확인
- 기존 규칙 재사용: `approval_process.id=3`(domain=INCIDENT, tier=3, 요청자 스코프=SERVICE_DESK_AGENT, 1차 AND[INCIDENT_MANAGER]), `id=4`(domain=PROBLEM, tier=3, 요청자 스코프=PROBLEM_MANAGER, 1차 AND[PROBLEM_MANAGER])
- 병렬 에이전트 세션 충돌 회피를 위한 전용 계정(비밀번호 `Test@1234`, `tester-admin@itsm.local`로 생성): `tester_inc4_agent@itsm.local`(SERVICE_DESK_AGENT, 요청자), `tester_inc4_im@itsm.local`(INCIDENT_MANAGER, 승인자), `tester_prb4_req@itsm.local`(PROBLEM_MANAGER, 요청자), `tester_prb4_apv@itsm.local`(PROBLEM_MANAGER, 승인자 — 요청자와 분리해 자가승인 모호성 회피)
- 공용 승인 엔진 내부 로직(OR/AND 집계, 대기함, 규칙 CRUD)은 Stage 1~3에서 이미 검증됨 — 본 라운드는 INCIDENT/PROBLEM 도메인 연동 지점(게이트 호출 위치, 상세 응답 `approval` 필드, FE 버튼/패널, 대기함 ticketKey)만 신규 검증한다.

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/api_spec/incident.md, @docs/02_plan/api_spec/problem.md, @docs/02_plan/api_spec/common.md
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-INCGATE-001 · 매칭 규칙 있음 — RESOLVED 전이 시도 시 409+인스턴스 생성
- 근거: @docs/02_plan/api_spec/incident.md (API-INC-005 409), @docs/02_plan/api_spec/common.md (0절)
- 전제: tester_inc4_agent로 신규 인시던트 등록 후 IN_PROGRESS 전이
- 절차: 1) `PATCH .../status {targetStatus:"RESOLVED"}`
- 기대 결과: 409(`APPROVAL_PENDING`) + `approvalRequestId` 반환. API-COM-004 상세 조회 시 인스턴스 IN_PROGRESS. 인시던트 상세(API-INC-003) 재조회 시 `approval.approvalRequestId` 동일 값, `status=IN_PROGRESS`

### TC-INCGATE-002 · 승인(APPROVE) 결정 → 재전이 허용
- 근거: @docs/02_plan/api_spec/common.md (API-COM-005), @docs/02_plan/api_spec/incident.md (API-INC-005)
- 전제: TC-INCGATE-001 인스턴스
- 절차: 1) tester_inc4_im으로 `POST /api/v1/approvals/{id}/decisions {decision:"APPROVE"}` 2) `PATCH .../status {targetStatus:"RESOLVED"}` 재시도
- 기대 결과: 1) 200, requestStatus=APPROVED 2) 200 전이 성공, 인시던트 상세 재조회 시 `status="RESOLVED"`, `approval.status="APPROVED"`

### TC-INCGATE-003 · 반려(REJECT) 결정 → 재전이 409 유지(SRM/CHANGE와 동일 설계)
- 근거: @docs/02_plan/api_spec/common.md (0절 checkGate 방식), Stage1 TC-SRM-008 선례
- 전제: 신규 인시던트로 새 인스턴스 생성(TC-INCGATE-001 반복)
- 절차: 1) tester_inc4_im으로 REJECT(사유 포함) 결정 2) `PATCH .../status {targetStatus:"RESOLVED"}` 재시도
- 기대 결과: 1) 200, requestStatus=REJECTED 2) 409 유지(동일 인스턴스 재사용, 신규 인스턴스 생성 안 함 — checkGate 방식은 KNOWLEDGE와 달리 재요청 개념이 없음)

### TC-INCGATE-004 · 매칭 규칙 없음 — 게이트 없이 통과
- 근거: @docs/02_plan/api_spec/common.md (0절 2번)
- 전제: 기존 규칙(id=3)의 requester 스코프를 임시로 무관 역할로 변경(매칭 회피) 후 확인 즉시 원복
- 절차: 1) SYSTEM_ADMIN으로 인시던트 등록(요청자 스코프 불일치) 후 IN_PROGRESS→RESOLVED 순차 전이
- 기대 결과: 게이트 없이 즉시 전이 허용(409 없음)

### TC-INCUI-001 · RESOLVED 버튼 disabled+tooltip(승인 대기 중)
- 근거: Stage2 TC-CHGUI-001 선례, `source/frontend/src/features/incident/IncidentDetailPage.tsx`
- 전제: TC-INCGATE-001 상태(인스턴스 IN_PROGRESS)의 인시던트
- 절차: 1) playwright 새 컨텍스트로 tester_inc4_agent 로그인 2) 해당 인시던트 상세 진입
- 기대 결과: "해결" 버튼이 `disabled` + 툴팁("승인 완료 전에는 해결 상태로 전이할 수 없습니다") 노출

### TC-INCUI-002 · 전이 실패(409) 시 승인 패널 즉시 갱신
- 근거: Stage2 TC-CHGUI-002 선례
- 절차: 1) 매칭 규칙이 있는 신규 인시던트에서 상태 전이 버튼으로 RESOLVED 시도(409 유발, allowedTransitions에 없을 경우 직접 API 유발 후 화면 재진입으로 대체 확인) 2) 승인 패널 확인
- 기대 결과: 새로고침 없이 승인 패널이 "1차·대기중" 등 진행 상태를 즉시 반영

### TC-COM014-INC-001 · SCR-COM-014 승인 대기함 — INCIDENT ticketKey(INC-{id}) 노출
- 근거: `IncidentApprovalTicketSummaryProvider.java`, @docs/02_plan/api_spec/common.md (API-COM-003)
- 전제: 진행 중인 INCIDENT 승인 인스턴스 존재
- 절차: 1) playwright로 tester_inc4_im(INCIDENT_MANAGER) 로그인 2) `/approvals` 진입
- 기대 결과: "인시던트" 유형 배지 + `INC-{id}` 형태 ticketKey, 요약, 요청자명(tester_inc4_agent) 정확히 노출

### TC-INCREG-001 · INCIDENT 등록/목록/에스컬레이션/포스트모템 회귀
- 근거: @docs/02_plan/api_spec/incident.md (API-INC-001/002/004/007/009)
- 절차: 1) 목록 조회 2) 신규 등록 3) 심각도 변경 4) 에스컬레이션 5) 해결 처리(API-INC-009, 매칭 규칙 없는 케이스로)
- 기대 결과: 전부 정상 응답(200/201), 게이트 로직 도입 전과 동일하게 동작(회귀 없음)

### TC-PRBGATE-001 · 매칭 규칙 있음 — RESOLVED_CLOSED 전이 시도 시 409+인스턴스 생성
- 근거: @docs/02_plan/api_spec/problem.md (API-PRB-004 409), @docs/02_plan/api_spec/common.md (0절)
- 전제: tester_prb4_req로 신규 문제 등록 후 WORKAROUND까지 순차 전이
- 절차: 1) `PATCH .../status {targetStatus:"RESOLVED_CLOSED"}`
- 기대 결과: 409(`APPROVAL_PENDING`) + `approvalRequestId` 반환, 인스턴스 IN_PROGRESS 확인(API-COM-004), 문제 상세(API-PRB-003) `approval` 필드 동일 반영

### TC-PRBGATE-002 · 승인(APPROVE) 결정 → 재전이 허용
- 근거: 상동
- 전제: TC-PRBGATE-001 인스턴스
- 절차: 1) tester_prb4_apv로 APPROVE 결정 2) `PATCH .../status {targetStatus:"RESOLVED_CLOSED"}` 재시도
- 기대 결과: 1) 200, requestStatus=APPROVED 2) 200 전이 성공, 문제 상세 `status="RESOLVED_CLOSED"`, `approval.status="APPROVED"`

### TC-PRBGATE-003 · 반려(REJECT) 결정 → 재전이 409 유지
- 근거: 상동, Stage1 TC-SRM-008 선례
- 전제: 신규 문제로 새 인스턴스 생성(TC-PRBGATE-001 반복)
- 절차: 1) tester_prb4_apv로 REJECT(사유 포함) 결정 2) `PATCH .../status {targetStatus:"RESOLVED_CLOSED"}` 재시도
- 기대 결과: 1) 200, requestStatus=REJECTED 2) 409 유지(동일 인스턴스, 신규 생성 없음)

### TC-PRBGATE-004 · 매칭 규칙 없음 — 게이트 없이 통과
- 근거: @docs/02_plan/api_spec/common.md (0절 2번)
- 전제: 기존 규칙(id=4)의 requester 스코프를 임시로 무관 역할로 변경(매칭 회피) 후 확인 즉시 원복
- 절차: 1) SYSTEM_ADMIN으로 문제 등록(요청자 스코프 불일치) 후 WORKAROUND→RESOLVED_CLOSED 전이
- 기대 결과: 게이트 없이 즉시 전이 허용(409 없음)

### TC-PRBGATE-005 · 종료(force close, API-PRB-012)는 게이트 대상 아님
- 근거: dev-lead 지시(API-PRB-012는 게이트 대상 아님), @docs/02_plan/api_spec/problem.md (API-PRB-012)
- 전제: TC-PRBGATE-001처럼 RESOLVED_CLOSED 전이가 게이트로 막혀 있는(승인 대기 중) 문제
- 절차: 1) 해당 문제에 `POST .../close {force:true}` 호출
- 기대 결과: 승인 대기 상태와 무관하게 200 성공, `status="RESOLVED_CLOSED"`(전이 게이트를 우회하는 별도 종료 경로임을 확인)

### TC-PRBUI-001 · RESOLVED_CLOSED 버튼 disabled+tooltip(승인 대기 중)
- 근거: Stage2 TC-CHGUI-001 선례, `source/frontend/src/features/problem/ProblemDetailPage.tsx`
- 전제: TC-PRBGATE-001과 별개의 새 인스턴스(IN_PROGRESS) 문제(TC-PRBGATE-005에서 close로 소모되지 않은 건)
- 절차: 1) playwright 새 컨텍스트로 tester_prb4_req 로그인 2) 해당 문제 상세 진입
- 기대 결과: "종료" 계열 버튼이 `disabled` + 툴팁("승인 완료 전에는 종료 상태로 전이할 수 없습니다") 노출

### TC-PRBUI-002 · 전이 실패(409) 시 승인 패널 즉시 갱신
- 근거: Stage2 TC-CHGUI-002 선례
- 절차: 1) 매칭 규칙이 있는 신규 문제에서 RESOLVED_CLOSED 전이 시도(409 유발) 2) 승인 패널 확인
- 기대 결과: 새로고침 없이 승인 패널이 진행 상태를 즉시 반영

### TC-COM014-PRB-001 · SCR-COM-014 승인 대기함 — PROBLEM ticketKey(PRB-{id}) 노출
- 근거: `ProblemApprovalTicketSummaryProvider.java`, @docs/02_plan/api_spec/common.md (API-COM-003)
- 전제: 진행 중인 PROBLEM 승인 인스턴스 존재
- 절차: 1) playwright로 tester_prb4_apv(PROBLEM_MANAGER) 로그인 2) `/approvals` 진입
- 기대 결과: "문제" 유형 배지 + `PRB-{id}` 형태 ticketKey, 요약, 요청자명(tester_prb4_req) 정확히 노출

### TC-PRBREG-001 · PROBLEM 등록/RCA/워크어라운드/KEDB 회귀
- 근거: @docs/02_plan/api_spec/problem.md (API-PRB-001/002/005/006/008)
- 절차: 1) 목록 조회 2) 신규 등록 3) RCA 작성 4) 워크어라운드 등록 5) KEDB 검색
- 기대 결과: 전부 정상 응답(200/201), 게이트 로직 도입 전과 동일하게 동작(회귀 없음)

### TC-CROSSREG-001 · SRM/CHANGE/KNOWLEDGE 승인 흐름 회귀 스팟 체크
- 근거: Stage1~3 선례, 공용 게이트 서비스에 변경이 없음을 재확인
- 절차: 1) 기존 CHANGE 승인 완료 건 상세 조회 2) 기존 KNOWLEDGE 승인 완료 건 상세 조회
- 기대 결과: 두 도메인 모두 기존 상태·approval 필드 값 그대로 유지, 오류 없음
