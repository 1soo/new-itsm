---
date: 20260722-042424
domain: approval-engine
result: pass
keywords: [기존 게이트 요청자스코프 보정, 전이버튼 targetState 일반화, 재테스트]
---

# 통합 테스트 결과 — approval-engine (재테스트: TC-REGRESSION-001/TC-FE-001 수정확인) (20260722-042424)

## 요약
- 총 4건 · 성공 4 · 실패 0 ✅ **전 항목 통과**
- 이전 라운드(20260722-040618) FAIL 2건 모두 수정 확인 완료. 전체 통과.

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew build -x test`, `npm run build` 모두 오류 없이 성공 | |
| TC-REGRESSION-001 | PASS | DB 확인: `approval_process.id=1` 요청자 스코프가 SERVICE_DESK_AGENT로 보정됨. 신규 SR(SRM-2026-0046, catalogItemId=1) 생성 후 agent@itsm.local이 SUBMITTED→VALIDATED→ROUTED까지 전이(모두 게이트 없이 통과, 신규 게이트 대상 아님) → IN_FULFILLMENT 전이 시 409(`APPROVAL_PENDING`, approvalRequestId=15) 확인 → cab@itsm.local(APPROVER) APPROVE → 200 → 재시도 시 200(`status=IN_FULFILLMENT`) | |
| TC-FE-001 | PASS | agent@itsm.local로 SRM-2026-0044(VALIDATED 게이트 IN_PROGRESS, approvalRequestId=12 그대로) 상세 재진입 → 이전 라운드에 노출되던 "검증 완료" 버튼이 액션 영역에서 완전히 사라짐(disabled 아닌 미노출로 변경 확인) | |
| TC-NOROUTE-001 | PASS | SRM-2026-0046 상세에서 "이행 완료 처리"(FULFILLED) 버튼 정상 노출(게이트 없는 전이는 영향 없음, 회귀 없음) | |

## 실패 항목 분석
없음(전 항목 통과).

## 참고
- 이전 라운드 아티팩트(규칙 id=22,23, SR 42~45, CHG-2026-0003)는 그대로 유지, 이번 재테스트로 SR 46 신규 생성됨(catalogItemId=1, IN_FULFILLMENT까지 승인 완료 상태).
- 이전 라운드 결과(FAIL 2건 원인 분석 포함)는 `docs/04_test/20260722-040618/approval-engine/result/approval-engine.md` 참조.
