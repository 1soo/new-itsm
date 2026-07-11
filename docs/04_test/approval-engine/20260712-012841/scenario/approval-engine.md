# 통합 테스트 시나리오 — approval-engine (Stage 6: COMPLIANCE + ESM 승인 게이트 연동, 마지막 도메인)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- Stage 1~5 산출물(공용 승인 엔진, SCR-ADMIN-007/008, SCR-COM-014) 정상 동작 전제
- backend(:8080)에 Stage 6 코드(`ComplianceService`/`EsmRequestService`↔`ApprovalGateService.checkGate`, `CorrectiveActionApprovalTicketSummaryProvider`, `EsmRequestApprovalTicketSummaryProvider`) 반영 확인
- 기존 규칙 재사용: `approval_process.id=7`(domain=COMPLIANCE, tier=3, 요청자 스코프=COMPLIANCE_OFFICER, 1차 AND[COMPLIANCE_OFFICER]), `id=8`(domain=ESM, tier=3, 요청자 스코프=END_USER, 1차 AND[DEPT_COORDINATOR])
- 병렬 에이전트 세션 충돌 회피를 위한 전용 계정(비밀번호 `Test@1234`, `tester-admin@itsm.local`로 생성): `tester_comp6_req@itsm.local`/`tester_comp6_apv@itsm.local`(COMPLIANCE_OFFICER, 요청자·승인자 분리)
- ESM은 요청자=END_USER, 승인/처리자=DEPT_COORDINATOR(부서 일치 필요, `EsmRequestService.assertCanProcess`)라 기존 부서별 테스트 계정 재사용: `tester-user@itsm.local`(END_USER, 요청자), `facilities-coord@itsm.local`(DEPT_COORDINATOR·FACILITIES, 처리자·승인자) — 계정 생성 API에 department 필드가 없어 신규 생성 대신 기존 부서 매칭 계정 재사용
- 핵심 확인 포인트(dev-lead 지시): 1) 시정조치는 항목(actionId) 단위 독립 승인 인스턴스 — A 승인해도 B는 별개로 대기 유지 2) HR 케이스·체크리스트 하위 작업(API-ESM-016)은 게이트 미적용 회귀 확인 3) 승인 대기함(SCR-COM-014)의 ticketType 내부값(CORRECTIVE_ACTION/ESM_REQUEST)에도 도메인 필터(COMPLIANCE/ESM)와 "티켓 유형" 라벨(시정조치/부서 서비스)이 정상 동작하는지 확인

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/api_spec/compliance.md, @docs/02_plan/api_spec/esm.md, @docs/02_plan/api_spec/common.md
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-COMPGATE-001 · 매칭 규칙 있음 — 시정조치 A RESOLVED 전이 시도 시 409+인스턴스 생성
- 근거: @docs/02_plan/api_spec/compliance.md (API-COMP-008 409), @docs/02_plan/api_spec/common.md (0절)
- 전제: tester_comp6_req로 요구사항 등록 후 시정조치 A 등록, DETECTED→IN_PROGRESS 전이
- 절차: 1) `PATCH .../corrective-actions/{actionId}/status {targetStatus:"RESOLVED"}`
- 기대 결과: 409(`APPROVAL_PENDING`) + `approvalRequestId` 반환. API-COM-004 상세 조회 시 인스턴스 IN_PROGRESS. 요구사항 상세(API-COMP-003)의 해당 시정조치 항목에 `approval` 필드 동일 반영

### TC-COMPGATE-002 · 독립성 확인 — 동일 요구사항의 시정조치 B는 별개 인스턴스로 대기
- 근거: dev-lead 지시(시정조치 개별 항목 단위 독립 게이트), `TicketType.CORRECTIVE_ACTION`(ticketId=actionId)
- 전제: TC-COMPGATE-001과 동일 요구사항에 시정조치 B 추가 등록, DETECTED→IN_PROGRESS 전이
- 절차: 1) `PATCH .../corrective-actions/{actionBId}/status {targetStatus:"RESOLVED"}`
- 기대 결과: 409 + **A와 다른** `approvalRequestId` 반환(별개 인스턴스), A의 인스턴스 상태에 영향 없음

### TC-COMPGATE-003 · A만 승인 — B는 그대로 대기 유지(핵심 독립성 검증)
- 근거: 상동
- 전제: TC-COMPGATE-001/002 인스턴스(A, B) 둘 다 IN_PROGRESS
- 절차: 1) tester_comp6_apv로 A의 인스턴스만 APPROVE 2) A `PATCH .../status{RESOLVED}` 재시도 3) B는 재시도 없이 API-COM-004로 B 인스턴스 상태만 재조회
- 기대 결과: 2) A는 200 성공, `status="RESOLVED"` 3) B 인스턴스는 여전히 IN_PROGRESS(A 승인의 영향을 받지 않음 — 항목별 독립 게이트 확인)

### TC-COMPGATE-004 · 매칭 규칙 없음 — 게이트 없이 통과
- 근거: @docs/02_plan/api_spec/common.md (0절 2번)
- 전제: SYSTEM_ADMIN(COMPLIANCE_OFFICER 역할 없음, 요청자 스코프 불일치)이 요구사항·시정조치 등록
- 절차: 1) DETECTED→IN_PROGRESS→RESOLVED 순차 전이
- 기대 결과: 게이트 없이 즉시 전이 허용(409 없음)

### TC-COMPUI-001 · 시정조치 전이 버튼 disabled+tooltip(승인 대기 중)
- 근거: Stage2/4/5 UI 패턴 선례, `source/frontend/src/features/compliance/ComplianceDetailPage.tsx`
- 전제: TC-COMPGATE-002 상태(B 인스턴스 IN_PROGRESS)의 요구사항 상세
- 절차: 1) playwright 새 컨텍스트로 tester_comp6_req 로그인 2) 해당 요구사항 상세 진입
- 기대 결과: 시정조치 B의 RESOLVED 전이 버튼 `disabled` + tooltip 노출, 승인 현황 관련 정보 "1차·대기중" 표시(A는 이미 RESOLVED로 전이 버튼 노출 안 됨)

### TC-COMPUI-002 · 전이 실패(409) 시 승인 패널 즉시 갱신
- 근거: Stage2/4/5 UI 패턴 선례
- 절차: 1) 매칭 규칙이 있는 신규 요구사항의 신규 시정조치에서 RESOLVED 전이 최초 시도(409 유발) 2) 화면 확인
- 기대 결과: 새로고침 없이 토스트 노출 + 해당 시정조치의 승인 상태가 즉시 "대기중"으로 반영

### TC-COM014-COMP-001 · SCR-COM-014 승인 대기함 — 도메인 필터(COMPLIANCE)·라벨(시정조치)·ticketKey(CA-{actionId}) 확인
- 근거: `CorrectiveActionApprovalTicketSummaryProvider.java`, @docs/02_plan/api_spec/common.md (API-COM-003), dev-lead 지시(필터+라벨 실제 확인)
- 전제: 진행 중인 CORRECTIVE_ACTION 승인 인스턴스 존재(TC-COMPGATE-002의 B)
- 절차: 1) playwright로 tester_comp6_apv(COMPLIANCE_OFFICER) 로그인 2) `/approvals` 진입, 도메인 필터를 "COMPLIANCE"로 선택
- 기대 결과: 필터 적용 후에도 해당 건이 정상 노출(내부 ticketType이 CORRECTIVE_ACTION이라도 도메인 필터는 approval_process.domain 기준이라 영향 없음), "티켓 유형" 배지에 "시정조치" 라벨, ticketKey는 `CA-{actionId}` 형태로 노출

### TC-COMPREG-001 · COMPLIANCE 요구사항 목록/등록/책임자지정/변경연계/감사로그/현황 회귀
- 근거: @docs/02_plan/api_spec/compliance.md (API-COMP-001/002/006/009/010)
- 절차: 1) 목록 조회 2) 신규 등록 3) 책임자 지정 4) 감사 로그 조회 5) 준수 현황 조회
- 기대 결과: 전부 정상 응답(200/201), 게이트 도입 전과 동일하게 동작(회귀 없음)

### TC-ESMGATE-001 · 매칭 규칙 있음 — 부서 요청 COMPLETED 전이 시도 시 409+인스턴스 생성
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-008 409), @docs/02_plan/api_spec/common.md (0절)
- 전제: tester-user(END_USER)로 FACILITIES 카탈로그(좌석 배정 요청, 체크리스트 없음) 제출 후 facilities-coord(DEPT_COORDINATOR)가 SUBMITTED→IN_PROGRESS 전이
- 절차: 1) facilities-coord로 `PATCH .../requests/{id}/status {targetStatus:"COMPLETED"}`
- 기대 결과: 409(`APPROVAL_PENDING`) + `approvalRequestId` 반환, 인스턴스 IN_PROGRESS 확인(API-COM-004), 부서 요청 상세(API-ESM-007) `approval` 필드 동일 반영

### TC-ESMGATE-002 · 승인 후 재시도 허용
- 근거: 상동
- 전제: TC-ESMGATE-001 인스턴스
- 절차: 1) facilities-coord(DEPT_COORDINATOR)로 APPROVE 결정 2) `PATCH .../status{COMPLETED}` 재시도
- 기대 결과: 1) 200(requestStatus=APPROVED) 2) 200 성공, `status="COMPLETED"`, 상세 `approval.status="APPROVED"`

### TC-ESMGATE-003 · 매칭 규칙 없음 — 게이트 없이 통과
- 근거: @docs/02_plan/api_spec/common.md (0절 2번)
- 전제: SYSTEM_ADMIN(END_USER 역할 없음, 요청자 스코프 불일치)이 부서 요청 제출
- 절차: 1) facilities-coord가 SUBMITTED→IN_PROGRESS→COMPLETED 순차 전이
- 기대 결과: 게이트 없이 즉시 전이 허용(409 없음)

### TC-ESMHR-REG-001 · HR 케이스는 게이트 대상 아님(회귀)
- 근거: dev-lead 지시, @docs/02_plan/api_spec/esm.md (API-ESM-010/013), `EsmHrCaseService.java`(게이트 미연동)
- 절차: 1) HR_CASE_MANAGER로 HR 케이스 접수 2) INTAKE→DOCUMENTATION→INVESTIGATION→RESOLUTION 순차 전이
- 기대 결과: 전 구간 게이트 없이 정상 200 전이(409 없음, 회귀 없음)

### TC-ESMCHECKLIST-REG-001 · 온보딩 체크리스트 하위 작업은 게이트 대상 아님(회귀)
- 근거: dev-lead 지시, @docs/02_plan/api_spec/esm.md (API-ESM-016), `EsmChecklistService.java`(게이트 미연동)
- 전제: ONBOARDING 카탈로그(신규 입사자 온보딩)로 부서 요청 제출(체크리스트 자동 생성)
- 절차: 1) 생성된 체크리스트의 모든 하위 작업을 배정 부서 계정으로 `PATCH .../checklist-tasks/{taskId}/status {status:"DONE"}` 완료 처리
- 기대 결과: 전 하위 작업 게이트 없이 정상 200 완료, 전체 완료 시 체크리스트 상태 자동 COMPLETED(부서요청 자체의 COMPLETED 게이트와는 무관)

### TC-ESMUI-001 · 부서 요청 COMPLETED 전이 버튼 disabled+tooltip(승인 대기 중)
- 근거: Stage2/4/5 UI 패턴 선례, `source/frontend/src/features/esm/EsmRequestDetailPage.tsx`
- 전제: 승인 대기 중(인스턴스 IN_PROGRESS)인 부서 요청 상세 진입
- 절차: 1) playwright 새 컨텍스트로 facilities-coord 로그인 2) 해당 부서 요청 상세 진입
- 기대 결과: COMPLETED 전이 버튼 `disabled` + tooltip 노출, 승인 현황 패널 "1차·대기중" 표시

### TC-ESMUI-002 · 전이 실패(409) 시 승인 패널 즉시 갱신
- 근거: Stage2/4/5 UI 패턴 선례
- 절차: 1) 매칭 규칙이 있는 신규 부서 요청에서 COMPLETED 전이 최초 시도(409 유발) 2) 화면 확인
- 기대 결과: 새로고침 없이 토스트 노출 + 승인 패널 즉시 "1차·대기중" 반영

### TC-COM014-ESM-001 · SCR-COM-014 승인 대기함 — 도메인 필터(ESM)·라벨(부서 서비스)·ticketKey(ESM-{id}) 확인
- 근거: `EsmRequestApprovalTicketSummaryProvider.java`, 상동, dev-lead 지시(필터+라벨 실제 확인)
- 전제: 진행 중인 ESM_REQUEST 승인 인스턴스 존재
- 절차: 1) playwright로 facilities-coord(DEPT_COORDINATOR) 로그인 2) `/approvals` 진입, 도메인 필터를 "ESM"으로 선택
- 기대 결과: 필터 적용 후에도 해당 건 정상 노출, "티켓 유형" 배지에 "부서 서비스" 라벨, ticketKey `ESM-YYYY-####` 형태 정상 노출

### TC-ESMREG-001 · ESM 카탈로그/목록/코멘트/지표 회귀
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-001/006/009/017)
- 절차: 1) 카탈로그 목록 조회 2) 부서 요청 목록 조회(scope=mine) 3) 코멘트 등록 4) 지표 조회
- 기대 결과: 전부 정상 응답(200/201), 게이트 도입 전과 동일하게 동작(회귀 없음)

### TC-CROSSREG-001 · 타 도메인 승인 흐름 소스 무변경 확인(회귀 스팟)
- 근거: Stage1~5 선례
- 절차: 1) `git status`/`git diff` 기준 `common/approval/**`, `incident/**`, `problem/**`, `change/**`, `srm/**`, `knowledge/**`, `asset/**`, `vulnerability/**` 소스가 Stage6 변경분에 포함되지 않았는지 확인
- 기대 결과: 공용 엔진·타 도메인 서비스 미변경 확인(회귀 없음, 전 도메인 마이그레이션 최종 완료)
