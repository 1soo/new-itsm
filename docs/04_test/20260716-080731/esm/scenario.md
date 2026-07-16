# 통합 테스트 시나리오 — esm (배치1: 전이버튼 라벨·타임라인 actor, 유지보수 요청 2026-07-16)

## 사전 조건
- 빌드 테스트 통과(service-request 시나리오에서 1회 수행한 결과 공유)
- 계정: po@itsm.local(PROCESS_OWNER, 요청 제출), legal-coord@itsm.local(DEPT_COORDINATOR), hr@itsm.local(HR_CASE_MANAGER) — 공통 비밀번호 `Admin@1234`
- 부서 요청(SCR-ESM-005)만 타임라인 actor 대상, HR 케이스(SCR-ESM-008)는 대상 아님(기존 `changedBy` 필드 유지)

## 시나리오

### TC-ESM-001 · 빌드 테스트(공유)
- 근거: 통합테스트 선행 항목
- 절차: service-request 시나리오 TC-SRM-001 결과 참조(동일 빌드)
- 기대 결과: 오류 없이 성공

### TC-ESM-002 · 부서 요청 전이 버튼 라벨(동작 동사형) 표시
- 근거: @docs/02_plan/screen/esm.md SCR-ESM-005 "전이 버튼 라벨" 표
- 전제: po@itsm.local로 법무 부서 요청 제출, legal-coord@itsm.local 로그인
- 절차:
  1. IN_PROGRESS 버튼 텍스트 확인("처리 시작") 후 클릭
  2. COMPLETED 버튼 텍스트 확인("완료 처리") 후 클릭
- 기대 결과: 각 단계 버튼 텍스트가 도착 상태명이 아닌 표의 동작 동사형 라벨과 정확히 일치

### TC-ESM-003 · 부서 요청 전이 완료 토스트 문구 회귀 확인(도착 상태명 유지)
- 근거: @docs/02_plan/screen/esm.md SCR-ESM-005 "전이 버튼 라벨" 절
- 절차: TC-ESM-002 전이 완료 토스트 문구 확인
- 기대 결과: 토스트는 버튼 라벨이 아닌 기존 도착 상태명(`requestStatusLabel`)을 그대로 사용

### TC-ESM-004 · 부서 요청 타임라인 actor·라벨 표시
- 근거: @docs/02_plan/screen/esm.md SCR-ESM-005 "타임라인 actor 표시" 절, @docs/02_plan/api_spec/esm.md(`timeline[].actor`)
- 전제: TC-ESM-002의 상태 변경 타임라인 존재
- 절차: po@itsm.local(요청자) 또는 legal-coord@itsm.local로 요청 상세 타임라인 확인
- 기대 결과: 각 상태 변경 항목에 행위 수행자 이름(또는 email 폴백)이 표시되고, 메시지가 상태 코드가 아닌 한글 라벨

### TC-ESM-005 · HR 케이스 전이 버튼 라벨(동작 동사형) 표시
- 근거: @docs/02_plan/screen/esm.md SCR-ESM-008 "전이 버튼 라벨" 표
- 전제: hr@itsm.local로 신규 HR 케이스 접수
- 절차:
  1. DOCUMENTATION 버튼 텍스트 확인("기록 시작") 후 클릭
  2. INVESTIGATION 버튼 텍스트 확인("조사 시작") 후 클릭
  3. RESOLUTION 버튼 텍스트 확인("해결 처리") 후 클릭
- 기대 결과: 각 단계 버튼 텍스트가 도착 상태명이 아닌 표의 동작 동사형 라벨과 정확히 일치

### TC-ESM-006 · HR 케이스 토스트·상태 이력 회귀 확인(actor 추가 없음)
- 근거: @docs/02_plan/screen/esm.md SCR-ESM-008 "상태 이력 타임라인" 설명(`changedBy` 별도 필드), @docs/03_develop/plan/esm.md "HrCaseDetailPage.tsx는 변경 없음"
- 절차: TC-ESM-005 전이 완료 토스트 문구 확인 + 상태 이력 타임라인 항목 확인
- 기대 결과: 토스트는 기존 도착 상태명(`hrCaseStatusLabel`) 유지. 상태 이력은 기존과 동일하게 `changedBy` 필드로 행위 주체자 표시(신규 `actor` 필드 추가 없음, 회귀 없음)
