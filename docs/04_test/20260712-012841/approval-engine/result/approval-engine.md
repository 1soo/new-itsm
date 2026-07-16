---
date: 20260712-012841
domain: approval-engine
result: pass
keywords: [COMPLIANCE 시정조치 승인, ESM 부서서비스 승인, 항목단위 독립 승인 인스턴스]
---

# 통합 테스트 결과 — approval-engine (Stage 6: COMPLIANCE + ESM 승인 게이트 연동, 마지막 도메인) (20260712-012841)

## 요약
- 총 19건 · 성공 19 · 실패 0 ✅ **전 항목 통과**
- 핵심 확인(dev-lead 지시): 1) 시정조치 항목(actionId) 단위 독립 승인 인스턴스 확인(A 승인해도 B는 그대로 대기) 2) HR 케이스·체크리스트 하위 작업은 게이트 미적용 회귀 확인 3) 승인 대기함의 도메인 필터(COMPLIANCE/ESM)·라벨(시정조치/부서 서비스) 정상 동작 확인 — 3개 모두 통과
- 전 6단계(SRM/CHANGE/KNOWLEDGE/INCIDENT+PROBLEM/ASSET+VULNERABILITY/COMPLIANCE+ESM) 승인 프로세스 커스텀 기능 마이그레이션 최종 완료

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew build -x test`, `npm run build` 모두 오류 없이 성공 | |
| TC-COMPGATE-001 | PASS | 매칭 규칙 있음(id=7, COMPLIANCE_OFFICER 스코프, AND 1역할 COMPLIANCE_OFFICER) 상태에서 요구사항(COMP-2026-0005)의 시정조치 A(id=5) IN_PROGRESS→RESOLVED 시도 → 409(`APPROVAL_PENDING`) + `approvalRequestId=46`, 인스턴스 IN_PROGRESS 확인(API-COM-004). 요구사항 상세(API-COMP-003)의 해당 시정조치 항목에 `approval` 필드 동일 반영 | |
| TC-COMPGATE-002 | PASS | 동일 요구사항의 시정조치 B(id=6) RESOLVED 시도 → 409 + **A와 다른** `approvalRequestId=47`(별개 인스턴스) | |
| TC-COMPGATE-003 | PASS | tester_comp6_apv로 A 인스턴스(46)만 APPROVE → A 재시도 200(`status="RESOLVED"`) → B 인스턴스(47) 재조회 시 여전히 IN_PROGRESS(A 승인의 영향 없음). 요구사항 상세 재조회로 A=RESOLVED/APPROVED, B=IN_PROGRESS 동시 확인 — **핵심 독립성 검증 통과** | |
| TC-COMPGATE-004 | PASS | SYSTEM_ADMIN(COMPLIANCE_OFFICER 역할 없음)이 요구사항·시정조치 등록 후 DETECTED→IN_PROGRESS→RESOLVED 순차 전이 → 게이트 없이 즉시 200 | |
| TC-COMPUI-001 | PASS | tester_comp6_req로 요구사항 상세(COMP-2026-0005) 진입 시 시정조치 B의 "해결로 전이" 버튼만 `disabled` + `title="승인 완료 전에는 해결 상태로 전이할 수 없습니다"`, 승인 현황 "1차·대기중". A는 이미 해결 상태라 전이 버튼 미노출, 승인 현황 "1차·완료" — 항목별 UI 독립성도 확인 | |
| TC-COMPUI-002 | PASS | 신규 요구사항의 신규 시정조치(Action E) "해결로 전이" 최초 클릭 시 409 유발 → 새로고침 없이 토스트 + 버튼 disabled + 승인 현황 "1차·대기중" 즉시 반영 | |
| TC-COM014-COMP-001 | PASS | tester_comp6_apv로 `/approvals` 진입, 도메인 필터를 "컴플라이언스"로 선택해도 CORRECTIVE_ACTION 건(`CA-8`, `CA-6`)이 정상 노출(내부 ticketType과 무관하게 approval_process.domain 기준 필터링 확인), "티켓 유형" 배지 "시정조치" 라벨 정확 | |
| TC-COMPREG-001 | PASS | 목록 200, 등록 201, 책임자 지정 200, 감사 로그 조회 200, 준수 현황 조회 200(complianceRate=0.63) — 게이트 도입 전과 동일하게 동작 | |
| TC-ESMGATE-001 | PASS | 매칭 규칙 있음(id=8, END_USER 스코프, AND 1역할 DEPT_COORDINATOR) 상태에서 tester-user(END_USER)가 FACILITIES 카탈로그(좌석 배정) 제출, facilities-coord가 SUBMITTED→IN_PROGRESS 전이 후 COMPLETED 시도 → 409 + `approvalRequestId=48`, 인스턴스 IN_PROGRESS 확인, 부서 요청 상세(API-ESM-007) `approval` 필드 동일 반영 | |
| TC-ESMGATE-002 | PASS | facilities-coord(DEPT_COORDINATOR) APPROVE → 200(APPROVED) → COMPLETED 재시도 200 성공, `status="COMPLETED"`, `approval.status="APPROVED"` | |
| TC-ESMGATE-003 | PASS | SYSTEM_ADMIN(END_USER 역할 없음)이 부서 요청 제출 후 SUBMITTED→IN_PROGRESS→COMPLETED 전 구간 → 게이트 없이 즉시 200 | |
| TC-ESMHR-REG-001 | PASS | HR_CASE_MANAGER로 HR 케이스 접수 후 INTAKE→DOCUMENTATION→INVESTIGATION→RESOLUTION 전 구간 게이트 없이 정상 200 전이(409 없음, 회귀 없음) | |
| TC-ESMCHECKLIST-REG-001 | PASS | ONBOARDING 카탈로그 제출로 체크리스트 자동 생성(HR/IT 하위 작업 2건) → 각 부서 담당자(IT는 it-coord, HR은 담당 계정 부재로 SYSTEM_ADMIN 대체)로 순차 완료 처리, 게이트 없이 전부 200, 전체 완료 시 체크리스트 자동 COMPLETED 확인(부서요청 자체 게이트와 무관하게 동작) | |
| TC-ESMUI-001 | PASS | facilities-coord로 승인 대기 중(ESM-2026-0006) 상세 진입 시 "완료" 버튼 `disabled` + `title="승인 완료 전에는 완료 상태로 전이할 수 없습니다"`, 승인 현황 "1차·대기중" 확인 | |
| TC-ESMUI-002 | PASS | 신규 부서 요청(ESM-2026-0007)에서 "완료" 최초 클릭 시 409 유발 → 새로고침 없이 토스트 + 버튼 disabled + 승인 현황 "1차·대기중" 즉시 반영 | |
| TC-COM014-ESM-001 | PASS | facilities-coord로 `/approvals` 진입, 도메인 필터를 "부서 서비스"(ESM)로 선택해도 ESM_REQUEST 건(`ESM-2026-0007`, `ESM-2026-0006`) 정상 노출, "티켓 유형" 배지 "부서 서비스" 라벨 정확, 요청자명("테스터 최종사용자") 정확 | |
| TC-ESMREG-001 | PASS | 카탈로그 목록 200, 요청 목록(scope=mine) 200, 코멘트 등록 201, 지표 조회 200(온보딩 완료율 100%) — 게이트 도입 전과 동일하게 동작 | |
| TC-CROSSREG-001 | PASS | `git status` 기준 `common/approval/**`, `incident/**`, `problem/**`, `change/**`, `srm/**`, `knowledge/**`, `asset/**`, `vulnerability/**` 소스가 Stage6 변경분에 전혀 포함되지 않음 확인(공용 엔진·타 도메인 서비스 미변경, 회귀 없음) | |

## 실패 항목 분석

없음(전 항목 통과).

## 테스트 환경 조성 참고

- COMPLIANCE: 병렬 에이전트 세션 충돌 회피를 위해 전용 계정(비밀번호 `Test@1234`)을 API-AUTH-007로 신규 생성: `tester_comp6_req@itsm.local`/`tester_comp6_apv@itsm.local`(COMPLIANCE_OFFICER, 요청자·승인자 분리).
- ESM: 요청자(END_USER)=`tester-user@itsm.local`, 처리자·승인자(DEPT_COORDINATOR·FACILITIES)=`facilities-coord@itsm.local`(기존 seed 계정 재사용) — 계정 생성 API(API-AUTH-007)에 department 필드가 없어 신규 계정으로는 부서 일치 요건(`EsmRequestService.assertCanProcess`)을 맞출 수 없었음. 온보딩 체크리스트의 HR 하위 작업은 HR 부서 DEPT_COORDINATOR 계정이 존재하지 않아 SYSTEM_ADMIN(부서 검증 우회 허용)으로 대체 완료 처리했다(이 자체는 시나리오상 회귀 확인 목적이라 문제 없음).
- 기존 규칙(id=7 COMPLIANCE, id=8 ESM)은 변경 없이 그대로 재사용했다.
- 테스트용 요구사항(id 5~8, COMP-2026-0005~0008)·시정조치(id 5~8)·부서 요청(id 3~7, ESM-2026-0003~0007)·HR 케이스(id=1)·체크리스트(id=1)가 생성되어 남아있다. dev-lead 확인 후 정리 필요 여부 알려달라.
