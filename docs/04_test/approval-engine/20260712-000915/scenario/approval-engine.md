# 통합 테스트 시나리오 — approval-engine (Stage 4 추가 재테스트: API-INC-009 resolve 게이트 보완)

## 배경
- 직전 라운드(20260711-234110)에서 발견한 결함 후보 — API-INC-009(`POST /incidents/{id}/resolve`, 해결 처리·시간 지표)가 승인 게이트를 우회하는 문제 — 를 designer-3가 결함으로 확정, developer-be/fe가 수정 완료.
- 기존 19건(20260711-234110 결과)은 로직 변경이 없어 재확인하지 않는다. 이번 라운드는 API-INC-009 게이트 보완분만 검증한다.

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)에 `IncidentService.resolve()`의 `ApprovalGateService.checkGate` 호출 반영 확인
- docs/02_plan/api_spec/incident.md v0.3(API-INC-009 409 응답 반영) 확인
- 기존 규칙(id=3, domain=INCIDENT, tier=3, 요청자 스코프=SERVICE_DESK_AGENT, 1차 AND[INCIDENT_MANAGER]) 재사용
- 계정: tester_inc4_agent@itsm.local(SERVICE_DESK_AGENT, 요청자), tester_inc4_im@itsm.local(INCIDENT_MANAGER, 승인자) 재활성화 후 재사용(비밀번호 `Test@1234`)

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: docs/02_plan/api_spec/incident.md (API-INC-009 v0.3)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-INCGATE-RESOLVE-001 · 매칭 규칙 있음 — resolve() 직접 호출 시 409 차단
- 근거: docs/02_plan/api_spec/incident.md (API-INC-009 v0.3 409), common.md (0절)
- 전제: tester_inc4_agent로 신규 인시던트 등록 후 IN_PROGRESS 전이(승인 인스턴스 아직 없음)
- 절차: 1) `POST /api/v1/incidents/{id}/resolve` 직접 호출(상태전이 PATCH 경유 없이)
- 기대 결과: 409(`APPROVAL_PENDING`) + `approvalRequestId` 반환, 인스턴스 IN_PROGRESS 생성 확인(API-COM-004). 인시던트 상태는 여전히 IN_PROGRESS(RESOLVED로 바뀌지 않음)

### TC-INCGATE-RESOLVE-002 · 승인 후 resolve() 재시도 허용
- 근거: 상동
- 전제: TC-INCGATE-RESOLVE-001 인스턴스
- 절차: 1) tester_inc4_im으로 APPROVE 결정 2) `POST /api/v1/incidents/{id}/resolve` 재시도
- 기대 결과: 1) 200(requestStatus=APPROVED) 2) 200 성공, 인시던트 상태 RESOLVED, `metrics` 반영, 상세 `approval.status="APPROVED"`

### TC-INCUI-RESOLVE-001 · "해결 처리" 폼 제출 버튼 disabled+tooltip(승인 대기 중)
- 근거: dev-lead 지시(FE ResolveForm 보완), `source/frontend/src/features/incident/IncidentDetailPage.tsx`
- 전제: 승인 대기 중(인스턴스 IN_PROGRESS)인 인시던트 상세 진입
- 절차: 1) playwright 새 컨텍스트로 tester_inc4_agent 로그인 2) 해당 인시던트 상세에서 "해결 처리" 폼의 제출 버튼 상태 확인
- 기대 결과: 제출 버튼 `disabled` + 툴팁("승인 완료 전에는 해결 처리할 수 없습니다") 노출

### TC-INCUI-RESOLVE-002 · resolve() 409 시 승인 패널 즉시 갱신
- 근거: 상동
- 전제: 매칭 규칙은 있으나 아직 게이트가 걸리지 않은(승인 인스턴스 없음) 신규 인시던트
- 절차: 1) "해결 처리" 폼 제출(최초 시도, 409 유발) 2) 승인 패널 확인
- 기대 결과: 새로고침 없이 토스트 노출 + 승인 패널이 즉시 "1차·대기중"으로 표시되고 제출 버튼도 disabled로 전환

### TC-INCGATE-RESOLVE-003 · PATCH 상태전이와 resolve() 간 게이트 인스턴스 공유 확인(회귀)
- 근거: common.md 0절(티켓 단위 인스턴스), 직전 라운드 TC-INCGATE-001~004 로직 변경 없음 재확인
- 전제: TC-INCGATE-RESOLVE-001과 별개의 신규 인시던트
- 절차: 1) `PATCH .../status {targetStatus:"RESOLVED"}`로 먼저 게이트 유발(409+인스턴스 생성) 2) 동일 티켓에 `POST .../resolve` 호출
- 기대 결과: 2)도 동일 인스턴스 기준으로 409(새 인스턴스를 중복 생성하지 않음, 하나의 티켓에 하나의 대기 인스턴스만 존재)
