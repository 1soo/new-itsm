# 통합 테스트 결과 — change (CHG) (20260709-230947)

> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres, healthy)
> 계정: cm@itsm.local(CHANGE_MANAGER) / cab@itsm.local(APPROVER, CAB) / im@itsm.local(INCIDENT_MANAGER) / pm@itsm.local(PROBLEM_MANAGER) / agent@itsm.local(SERVICE_DESK_AGENT, 403검증)
> 범위: API-CHG-001~012 정상+오류(400/401/403/404/409), 6단계 전이, 분류→승인경로, 표준 변경 사전승인, 승인/반려, 구현결과 기록, 인시던트/문제 양방향 연계, 일정, 템플릿, 지표 + 크로스 도메인 회귀(problem→CHANGE 연계 완성, incident 문제연계 버튼, targetKey 계약)

## 요약

- 총 **51건** · **성공 50 · 실패 1**
- 실패 1건: **TC-CHG-015**(표준 변경(STANDARD) + 유효한 templateId 지정 시 승인 경로가 AUTO로 산정되지 않고 CAB로 처리되어, 승인 없이는 IMPLEMENTATION 전이 불가 — REQ-CHG-006/FEAT-CHG-006/API-CHG-004 위반)

## 상세 — 빌드

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-BUILD-001 (BE gradlew test) | PASS | `--rerun-tasks`로 강제 재실행, `BUILD SUCCESSFUL`(4 tasks executed, change 패키지 JUnit 포함) |
| TC-BUILD-002 (FE npm run build) | PASS | `tsc -b && vite build` 정상 완료(1850 modules, TS 오류 없음) |

## 상세 — 인증/인가

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-AUTH-001 (미인증 GET /changes) | PASS | 401 UNAUTHENTICATED |
| TC-CHG-RBAC-001 (agent POST /changes) | PASS | 403 ACCESS_DENIED |
| TC-CHG-RBAC-002 (agent POST .../approval) | PASS | 403 ACCESS_DENIED |
| TC-CHG-RBAC-003 (cm 직접 승인 시도) | PASS | 403 ACCESS_DENIED(승인은 APPROVER 전용, CM 단독 불가 확인) |

## 상세 — RFC 생성·분류

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-CHG-001 (정상 생성 NORMAL/HIGH) | PASS | 201, ticketKey=CHG-2026-0002, status=REQUESTED |
| TC-CHG-002 (summary 누락) | PASS | 400 VALIDATION_ERROR |
| TC-CHG-003 (type 누락) | PASS | 400 VALIDATION_ERROR |
| TC-CHG-004 (목록 조회) | PASS | 200 `{content,page,size,totalElements}`, 생성분 포함 |
| TC-CHG-005 (상세 조회) | PASS | 200, type/risk/status/approvalRoute/implementationPlan/rollbackPlan/result/approvals/links 구조 확인 |
| TC-CHG-006 (없는 id 상세) | PASS | 404 CHANGE_NOT_FOUND |
| TC-CHG-007 (분류 변경 risk=HIGH) | PASS | 200, approvalRoute=CAB |
| TC-CHG-008 (위험도 미평가 기본 CAB) | PASS | risk 미지정 생성(id=6) → approvalRoute=CAB(기본값) 확인 |
| TC-CHG-009 (정의되지 않은 유형 분류) | PASS | 400 VALIDATION_ERROR |

## 상세 — 6단계 상태 전이

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-CHG-010 (REQUESTED→REVIEW) | PASS | 200, status=REVIEW |
| TC-CHG-011 (순서 어긋난 전이) | PASS | 400 INVALID_STATUS_TRANSITION(REQUESTED→IMPLEMENTATION 직행 차단) |
| TC-CHG-012 (승인 전 IMPLEMENTATION 전이) | PASS | 409 APPROVAL_PENDING(APPROVAL 단계에서 미승인 상태로 IMPLEMENTATION 시도) |
| TC-CHG-013 (없는 id 전이) | PASS | 404 CHANGE_NOT_FOUND |

## 상세 — 표준 변경 사전승인

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-CHG-014 (템플릿 목록) | PASS | 200, `[{"id":1,"name":"표준 패치 배포",...}]` 시드 확인 |
| **TC-CHG-015 (STANDARD+유효 templateId → AUTO 승인 자동 통과)** | **FAIL** | STANDARD 타입 + 존재하는 templateId=1로 생성(BE API·FE RFC 화면 양쪽에서 재현, id=7/9) 후 상세 조회 시 `approvalRoute`가 **CAB**로 반환됨(AUTO 아님). REQUESTED→REVIEW→PLANNING→APPROVAL까지 전이 후 APPROVAL→IMPLEMENTATION 시도 시 **409 APPROVAL_PENDING** 반환 — 승인 없이 진행되어야 할 사전승인 표준 변경이 일반 변경과 동일하게 승인을 요구함 |
| TC-CHG-016 (템플릿에 없는 표준 변경 처리) | PASS | 존재하지 않는 templateId=999999 지정 시 400(유효하지 않은 템플릿, 명시적 오류 검증) / templateId 미지정 시 approvalRoute=CAB로 처리(일반 승인 경로 폴백 확인). 단, 유효한 템플릿을 지정한 경우도 동일하게 CAB로 처리되는 것은 TC-CHG-015 결함과 연결됨(비고 참조) |

## 상세 — 승인/반려

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-CHG-017 (고위험→CAB 경로) | PASS | risk=HIGH 분류 시 approvalRoute=CAB 확인(TC-CHG-007과 동일 근거) |
| TC-CHG-018 (승인 대기 목록 scope=mine&type=change) | PASS | 200, `[{"changeId":6,...},{"changeId":8,...}]` 대상 포함 |
| TC-CHG-019 (CAB 승인) | PASS | 200, 상태 유지·승인 이력 추가(approver=CAB 승인자, decision=APPROVED) |
| TC-CHG-020 (재승인 시도) | PASS | 409 APPROVAL_ALREADY_DECIDED |
| TC-CHG-021 (반려 사유 누락) | PASS | 400 REJECT_REASON_REQUIRED |
| TC-CHG-022 (없는 id 승인) | PASS | 404 CHANGE_NOT_FOUND |
| TC-CHG-023 (cm 승인 권한 없음) | PASS | 403 ACCESS_DENIED(TC-CHG-RBAC-003과 동일 결과) |

## 상세 — 구현 결과 기록

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-CHG-024 (승인된 변경 결과 기록) | PASS | 200, `{outcome:SUCCESS, rolledBack:false, note:...}` 저장 확인 |
| TC-CHG-025 (미승인 변경 결과 기록) | PASS | 400 CHANGE_NOT_APPROVED |
| TC-CHG-026 (없는 id 결과 기록) | PASS | 404 CHANGE_NOT_FOUND |

## 상세 — 인시던트/문제 연계 (change 발신)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-CHG-027 (change→incident 연계) | PASS | 200, 양방향: 변경 상세 links에 `{"type":"INCIDENT","targetKey":"INC-2026-0017"}` + 인시던트 상세 links에 `{"type":"CHANGE","targetKey":"CHG-2026-0003"}` |
| TC-CHG-028 (change→problem 연계) | PASS | 200, 양방향: 변경 상세 links에 PROBLEM targetKey + 문제 상세 linkedChanges에 반영 |
| TC-CHG-029 (없는 대상 연계) | PASS | 400 LINK_TARGET_NOT_FOUND |
| TC-CHG-030 (없는 change id 연계) | PASS | 404 CHANGE_NOT_FOUND |

## 상세 — 크로스 도메인 회귀

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-CROSS-001 (problem→CHANGE 연계 완성) | PASS | `POST /problems/{id}/links {targetType:CHANGE}` → 200(기존 `CHANGE_LINK_UNAVAILABLE` 400 스텁 제거 확인). 문제 상세 linkedChanges 반영 + 변경 상세 links에 PROBLEM 노출. FE 문제 상세 "변경 연계" 폼도 활성 상태로 정상 동작(스텁 안내 문구 제거, 실제 연계 성공) |
| TC-CROSS-002 (problem→없는 change 연계) | PASS | 400 LINK_TARGET_NOT_FOUND(대칭 오류코드 일관성) |
| TC-CROSS-003 (incident "문제 연계" 버튼 활성) | PASS | FE 인시던트 상세의 "기존 문제 연계"/"신규 문제 생성·연계" 버튼이 스텁 아님(입력값에 따라 활성화되는 정상 폼), 클릭 시 API-INC-012 정상 동작 |
| TC-CROSS-004 (링크 targetKey 계약: 원시ID→티켓키) | PASS | 인시던트 상세 links(PROBLEM/CHANGE 모두), 변경 상세 links(INCIDENT/PROBLEM 모두)에서 targetKey가 `PRB-YYYY-####`/`CHG-YYYY-####`/`INC-YYYY-####` 형식으로 반환됨(이전 problem 도메인 재테스트 시점의 raw id 이슈 해소 확인) |

## 상세 — 변경 일정(캘린더)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-CHG-031 (기간 내 예정 변경 조회) | PASS | 200, 시드 변경(CHG-2026-0001, scheduledAt=2026-07-20) 반영 |
| TC-CHG-032 (예정 없는 기간 조회) | PASS | 200, `[]` |

## 상세 — 변경 지표

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-CHG-033 (지표 조회) | PASS | 200, `{successRate:100.0, failureRate:0.0, emergencyRate:0.0, total:5}` |
| TC-CHG-034 (데이터 없는 기간) | PASS | 200, `{successRate:0.0, failureRate:0.0, emergencyRate:0.0, total:0}` |

## 상세 — FE E2E (playwright, localhost:5173, 매 항목 새 context/storage)

| TC ID | 결과 | 실제 |
|-------|------|------|
| TC-E2E-001 (RFC 생성) | PASS | 유형=표준 선택 시 템플릿 셀렉트+"표준 변경은 승인 단계가 자동으로 생략(사전승인)됩니다" 안내 노출, 템플릿 선택 후 생성 성공 → 상세(CHG-2026-0006) 이동. (단, 생성된 변경의 실제 승인경로가 AUTO로 처리되지 않는 것은 TC-CHG-015 백엔드 결함) |
| TC-E2E-002 (변경 목록) | PASS | 필터(유형/상태/위험도/기간), 유형·상태·위험도 배지, 6건 목록 정상 표시 |
| TC-E2E-003 (변경 상세) | PASS | 프로세스 전이 버튼, 승인 경로 배지, 구현 결과 기록 폼(저장된 값 표시), 승인 이력, 연계 항목 표시 정상 |
| TC-E2E-004 (CAB 승인 대기함) | PASS | 대기 목록(CHG-2026-0005) 노출, 반려 다이얼로그에 사유 입력 후 반려 처리 → 목록에서 제거·"승인 대기 건이 없습니다" 반영 |
| TC-E2E-005 (변경 일정 캘린더) | PASS | 7월 캘린더에 예정 변경(결제 API 서버 패치 배포, 7/20) 칩 표시, 클릭 시 상세(CHG-2026-0001)로 이동 |
| TC-E2E-006 (변경 지표 대시보드) | PASS | KPI 카드(성공률 100%/실패율 0%/긴급 변경 비율 0%) 표시 |
| TC-E2E-007 (agent RBAC) | PASS | 사이드바에 변경 메뉴 비노출(서비스 요청/인시던트만 노출), `/changes` 직접 접근 시 `/403` 리다이렉트 |

## 실패 항목 분석

- **TC-CHG-015 (표준 변경 사전승인 미작동)**: `type=STANDARD`이고 존재하는 `templateId`(시드 "표준 패치 배포", id=1)를 지정해 생성해도, 상세 조회 결과 `approvalRoute`가 항상 `CAB`로 반환되고(AUTO 아님), REQUESTED→REVIEW→PLANNING→APPROVAL까지 정상 전이된 뒤 승인 없이 IMPLEMENTATION으로 전이하면 **409 APPROVAL_PENDING**이 반환된다. 즉 사전승인 표준 변경이 일반(CAB) 변경과 동일하게 취급되어 재승인 없이 진행할 수 없다.
  - 재현: BE API(`POST /changes {type:STANDARD, templateId:1}` → id=7) 및 FE RFC 생성 화면(표준 유형 선택 → 템플릿 "표준 패치 배포" 선택 → 생성 → id=9) 양쪽에서 동일하게 재현됨.
  - 근거 요구사항: @docs/01_analyze/prd/change.md REQ-CHG-006("사전 승인된 표준 변경이 생성되면, 시스템은 별도 승인 없이 계획/구현 단계로 진행할 수 있게 해야 한다"), @docs/01_analyze/feature/change.md FEAT-CHG-006, @docs/02_plan/api_spec/change.md API-CHG-004("200 | 전이 성공(표준 변경은 승인 자동 통과)").
  - 참고: 위험도 기반 분류(HIGH/미평가→CAB, LOW/MEDIUM→PEER_REVIEW)는 정상 동작 확인(TC-CHG-007/008, 및 추가 확인한 LOW/MEDIUM risk 케이스에서 approvalRoute=PEER_REVIEW 정상 반환). 표준 변경 판정 로직만 분류(classification) 로직에 반영되지 않은 것으로 추정됨. 담당: BE(변경 분류 로직에 `type=STANDARD && templateId 유효` 조건 시 `approvalRoute=AUTO` 처리 및 상태 전이 시 승인 자동 통과 로직 추가 필요).

## 결론

- change 도메인 핵심 기능(RFC 생성·목록·상세, 6단계 전이 및 승인 전 구현 차단(409), 위험도 기반 분류(CAB/PEER_REVIEW), CAB 승인/반려(역할기반·재결정 409·반려사유 필수), 구현 결과 기록(미승인 400), 인시던트/문제 양방향 연계, 변경 일정, 지표, RBAC(CHANGE_MANAGER/APPROVER/agent 403)) 및 FE 6+1 화면(RFC 생성/목록/상세/CAB 대기함/캘린더/지표/RBAC) **정상 동작**.
- **크로스 도메인 회귀 전부 정상**: problem→CHANGE 연계 완성(스텁 제거), incident "문제 연계" 버튼 활성화, 링크 targetKey 계약(티켓키 형식) 수정 모두 확인.
- 잔여 실패 1건: **표준 변경(STANDARD) 사전승인 자동 통과(AUTO 승인경로) 미작동**(TC-CHG-015). 수정 후 재테스트 필요.
