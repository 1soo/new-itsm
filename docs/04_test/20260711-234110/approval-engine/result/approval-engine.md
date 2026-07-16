---
date: 20260711-234110
domain: approval-engine
result: pass
keywords: [INCIDENT 승인 게이트, PROBLEM 승인 게이트, resolve() 게이트 우회 발견]
---

# 통합 테스트 결과 — approval-engine (Stage 4: INCIDENT + PROBLEM 승인 게이트 연동) (20260711-234110)

## 요약
- 총 19건 · 성공 19 · 실패 0 ✅ **전 항목 통과**
- 시나리오 범위 밖 발견 사항 1건(결함 후보, dev-lead 확인 필요) — 아래 "발견 사항" 참조

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew build -x test -q`, `npm run build` 모두 오류 없이 성공 | |
| TC-INCGATE-001 | PASS | 매칭 규칙 있음(id=3, SERVICE_DESK_AGENT 스코프, AND 1역할 INCIDENT_MANAGER) 상태에서 tester_inc4_agent가 IN_PROGRESS→RESOLVED 시도 → 409(`APPROVAL_PENDING`) + `approvalRequestId=21` 반환. API-COM-004 상세 조회 시 인스턴스 IN_PROGRESS 확인. 인시던트 상세(API-INC-003) `approval:{approvalRequestId:21,status:"IN_PROGRESS"}` 일치(INC-2026-0003) | |
| TC-INCGATE-002 | PASS | tester_inc4_im(INCIDENT_MANAGER) APPROVE 결정 → 200(requestStatus=APPROVED) → RESOLVED 재시도 200 성공, 상세 재조회 `status="RESOLVED"`, `approval.status="APPROVED"` | |
| TC-INCGATE-003 | PASS | 신규 인시던트(INC-2026-0004)로 새 인스턴스(id=22) 생성 후 REJECT(사유 포함) → 200(requestStatus=REJECTED) → RESOLVED 재시도해도 동일 인스턴스로 409 유지(checkGate 방식은 KNOWLEDGE의 evaluateAndCreateIfNeeded와 달리 재요청 시 신규 인스턴스를 만들지 않음 — Stage1 TC-SRM-008과 동일 설계 확인) | |
| TC-INCGATE-004 | PASS | SYSTEM_ADMIN(SERVICE_DESK_AGENT 역할 없음, 요청자 스코프 불일치)이 인시던트 등록 후 IN_PROGRESS→RESOLVED 순차 전이 → 게이트 없이 즉시 200(INC-2026-0005) | 규칙 데이터 직접 변경 없이 스코프 밖 계정으로 자연스럽게 검증(다른 에이전트 데이터 영향 없음) |
| TC-INCUI-001 | PASS | tester_inc4_agent로 승인 대기 중(INC-2026-0006) 상세 진입 시 "해결" 버튼 `disabled` + `title="승인 완료 전에는 해결 상태로 전이할 수 없습니다"` 확인 | |
| TC-INCUI-002 | PASS | 신규 인시던트(INC-2026-0007, 최초 게이트 미확정 상태)에서 "해결" 버튼 최초 클릭 시 409 유발 → 페이지 이동/새로고침 없이 토스트 "승인 대기 중에는 이행할 수 없습니다." 노출 + 승인 현황 패널이 즉시 "1차·대기중"으로 나타남(버튼도 동시에 disabled로 전환) | 콘솔에 409 fetch 오류 로그 1건 노출되나 의도된 요청 실패로 결함 아님 |
| TC-COM014-INC-001 | PASS | tester_inc4_im으로 `/approvals` 진입 시 "인시던트" 유형 배지 + `INC-2026-0007`/`INC-2026-0006` ticketKey·요약, 요청자명("Stage4 Incident Requester") 정확히 노출 | |
| TC-INCREG-001 | PASS | 목록(GET .../incidents) 200, 등록 201, 심각도 변경(IM) 200, 에스컬레이션(AGENT) 200, 해결 처리(API-INC-009, IM) 200, 지표 조회 200 — 게이트 도입 전과 동일하게 동작 | **발견 사항 있음**(아래 참조) |
| TC-PRBGATE-001 | PASS | 매칭 규칙 있음(id=4, PROBLEM_MANAGER 스코프, AND 1역할 PROBLEM_MANAGER) 상태에서 tester_prb4_req가 WORKAROUND→RESOLVED_CLOSED 시도 → 409 + `approvalRequestId=25`, 인스턴스 IN_PROGRESS 확인(API-COM-004), 문제 상세(API-PRB-003) `approval` 필드 일치(PRB-2026-0002) | 요청자·승인자 역할이 동일(PROBLEM_MANAGER)이라 자가승인 모호성 회피 위해 별도 계정(tester_prb4_req/apv) 사용 |
| TC-PRBGATE-002 | PASS | tester_prb4_apv APPROVE 결정 → 200(APPROVED) → RESOLVED_CLOSED 재시도 200 성공, 상세 `status="RESOLVED_CLOSED"`, `approval.status="APPROVED"` | |
| TC-PRBGATE-003 | PASS | 신규 문제(PRB-2026-0003)로 새 인스턴스(id=26) 생성 후 REJECT(사유 포함) → 200(REJECTED) → 재시도해도 동일 인스턴스로 409 유지 | |
| TC-PRBGATE-004 | PASS | SYSTEM_ADMIN(PROBLEM_MANAGER 역할 없음)이 문제 등록 후 DETECTION~RESOLVED_CLOSED 전 구간 전이 → 게이트 없이 즉시 200(PRB-2026-0004) | |
| TC-PRBGATE-005 | PASS | TC-PRBGATE-003에서 REJECTED로 영구 차단된 문제(PRB-2026-0003, id=4)에 `POST .../close {force:true}` 호출 → 승인 대기 상태와 무관하게 200, `status="RESOLVED_CLOSED"`(종료는 게이트 우회 경로로 정상 동작, dev-lead 지시대로 API-PRB-012는 게이트 미대상) | |
| TC-PRBUI-001 | PASS | tester_prb4_req로 승인 대기 중(PRB-2026-0005) 상세 진입 시, 두 "종료" 버튼 중 상태전이용(bg-primary)만 `disabled` + `title="승인 완료 전에는 종료 상태로 전이할 수 없습니다"`, force close용(bg-success, "종료" 버튼)은 항상 활성 상태로 구분 확인 | |
| TC-PRBUI-002 | PASS | 신규 문제(PRB-2026-0006)에서 상태전이 "종료" 버튼 최초 클릭 시 409 유발 → 새로고침 없이 버튼 disabled 전환 + 승인 현황 패널 "1차·대기중" 즉시 표시 | |
| TC-COM014-PRB-001 | PASS | tester_prb4_apv로 `/approvals` 진입 시 "문제" 유형 배지 + `PRB-2026-0006`/`PRB-2026-0005` ticketKey·요약, 요청자명("Stage4 Problem Requester") 정확히 노출 | |
| TC-PRBREG-001 | PASS | 목록 200, 등록 201, RCA 저장 200, 워크어라운드 등록 200, KEDB 검색 200(매칭 없으면 빈 목록) — 게이트 도입 전과 동일하게 동작 | |
| TC-CROSSREG-001 | PASS | `git status`/`git diff` 기준 `common/approval/**`, `change/**`, `srm/**` 소스가 Stage4 변경분에 전혀 포함되지 않음(공용 엔진·타 도메인 서비스 미변경) 확인. CHANGE/SRM 상세 API 자체도 200 정상 응답 | DB에는 현재 CHANGE/SRM 도메인의 과거 승인 인스턴스·프로세스 규칙이 남아있지 않아(다른 세션에서 정리된 것으로 추정) 라이브 데이터 기반 재확인은 제한적이었음 — 대신 소스 diff 기준으로 무변경을 확인해 회귀 없음으로 판단 |

## 발견 사항 (결함 후보 — dev-lead 확인 필요)

- **API-INC-009(해결 처리·시간 지표, `POST /incidents/{id}/resolve`)가 승인 게이트를 완전히 우회한다.** `IncidentService.resolve()`는 `IncidentStateMachine` 검증과 `ApprovalGateService.checkGate` 호출 없이 `Incident.resolve()`를 직접 호출해 상태를 곧바로 `RESOLVED`로 설정한다(`source/backend/src/main/java/com/itsm/incident/application/IncidentService.java:281-297`). 반면 `PATCH /incidents/{id}/status`(API-INC-005)로 `RESOLVED` 전이 시도 시에는 게이트가 걸린다. 실제로 INCIDENT_MANAGER 권한 계정으로 승인 대기 여부와 무관하게 `/resolve` 호출만으로 200 응답과 함께 즉시 RESOLVED 전환되는 것을 확인했다(회귀 테스트 중 발견, 재현 100%).
  - 이 메서드는 Stage4 이전부터 동일한 형태로 존재했다(git 이력상 `ed2adf0`(최초 인시던트 도메인 구현)부터 상태를 직접 설정하는 구조 — Stage4 개발자가 새로 만든 코드는 아님).
  - 다만 PROBLEM 도메인의 동일한 우회 가능 지점(API-PRB-012 강제 종료)은 dev-lead가 "게이트 대상 아님"으로 명시적으로 설계 의도를 확인해 준 반면, INCIDENT의 이 지점은 별도 언급이 없어 의도된 예외인지 누락인지 불명확하다.
  - 시나리오에 명시된 항목은 아니라 FAIL로 처리하지 않았으나, 요구사항("IN_PROGRESS→RESOLVED 전이에 승인 게이트 연동")의 의도를 완전히 충족하려면 dev-lead 판단(PRB-012처럼 의도된 예외로 문서화 vs `resolve()`에도 게이트 추가)이 필요해 보고한다.

## 테스트 환경 조성 참고

- 병렬 에이전트 세션 충돌 회피를 위해 전용 계정(비밀번호 `Test@1234`)을 API-AUTH-007로 신규 생성해 사용: `tester_inc4_agent@itsm.local`(SERVICE_DESK_AGENT), `tester_inc4_im@itsm.local`(INCIDENT_MANAGER), `tester_prb4_req@itsm.local`/`tester_prb4_apv@itsm.local`(PROBLEM_MANAGER, 요청자·승인자 분리).
- 기존 규칙(id=3 INCIDENT, id=4 PROBLEM)은 변경 없이 그대로 재사용했다(매칭없음 케이스는 규칙 스코프 밖 계정으로 자연스럽게 검증해 다른 에이전트 데이터에 영향 없음).
- 테스트용 인시던트(id 3~8, INC-2026-0003~0008)·문제(id 3~8, PRB-2026-0002~0007)가 생성되어 남아있다. 실제 업무 데이터가 아니므로 정리가 필요하면 dev-lead에 확인 요청.
- curl로 REJECT 사유에 한글을 직접 `-d`로 전달하면 Windows Git Bash 인코딩 문제로 `VALIDATION_ERROR`가 발생하는 것을 확인했다(`--data-binary` 또는 ASCII 사유로 재시도 시 정상) — 백엔드 결함이 아니라 로컬 curl 테스트 도구의 인코딩 이슈로 판단(python urllib으로 UTF-8 재확인 완료).
