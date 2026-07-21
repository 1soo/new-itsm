# 통합 테스트 시나리오 — approval-engine (재테스트: TC-REGRESSION-001/TC-FE-001 수정확인)

이전 라운드(`docs/04_test/20260722-040618/approval-engine/`) 결과 FAIL 2건에 대한 수정 확인 재테스트. dev-lead 지시에 따라 해당 2건만 재검증하고, 나머지는 짧은 회귀 스팟체크(TC-NOROUTE-001 성격)만 수행한다.

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- developer-db: `source/db/sql/42_approval_process_id1_requester_role_fix.sql`(approval_process.id=1 요청자 스코프 END_USER→SERVICE_DESK_AGENT) 반영
- developer-fe: `RequestDetailPage.tsx` 전이 버튼 필터를 `target === detail.approval.targetState`로 일반화, `fallbackTransitions`의 ROUTED 하드코딩 제거

## 시나리오

### TC-BUILD-001 · 빌드 재확인
- 근거: @docs/02_plan/api_spec/common.md
- 절차: `./gradlew build -x test`, `npm run build`
- 기대 결과: 오류 없이 성공

### TC-REGRESSION-001(재검증) · 기존 게이트(id=1) 요청자 스코프 보정 확인
- 근거: 42번 마이그레이션, @docs/02_plan/api_spec/common.md (0절)
- 전제: approval_process.id=1 요청자 스코프=SERVICE_DESK_AGENT로 보정됨
- 절차: 1) user@itsm.local이 catalogItemId=1로 신규 SR 제출 2) agent@itsm.local 배정 3) SUBMITTED→VALIDATED→ROUTED→IN_FULFILLMENT 순차 전이
- 기대 결과: IN_FULFILLMENT 전이 시 409(`APPROVAL_PENDING`), 승인 후 재시도 시 200

### TC-FE-001(재검증) · SRM 상세화면 전이 버튼 targetState 일반화 확인
- 근거: `RequestDetailPage.tsx` 수정분
- 전제: SRM-2026-0044(VALIDATED 게이트 IN_PROGRESS, approvalRequestId=12, 직전 라운드에서 의도적으로 미승인 유지)
- 절차: 1) 새 컨텍스트로 agent@itsm.local 로그인 2) 상세 진입
- 기대 결과: "검증 완료"(VALIDATED 전이) 버튼이 disabled가 아니라 목록에서 완전히 숨겨짐

### TC-NOROUTE-001(스팟체크) · 게이트 없는 전이 정상 동작 회귀 확인
- 근거: 상동
- 전제: SRM-2026-0046(TC-REGRESSION-001 재검증으로 IN_FULFILLMENT까지 승인 완료된 상태)
- 절차: 1) agent@itsm.local로 상세 진입 2) FULFILLED 전이 버튼 노출·클릭 가능 확인
- 기대 결과: 게이트 없는 전이는 버튼이 정상 노출되고 클릭 가능(회귀 없음)
