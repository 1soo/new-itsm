# 통합 테스트 결과 — change (CHG) 재테스트 (20260709-233129)

> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres, healthy)
> 대상: 직전 실행(20260709-230947) 실패 1건(TC-CHG-015) 수정분 + 분류 로직·승인 흐름 회귀

## 요약
- 총 **8건** (빌드 2 · TC-CHG-015 재검증 1 · 분류 회귀 4 · 승인흐름 회귀 2 · FE 회귀 1, 일부 항목은 연속 절차로 함께 확인) · **성공 8 · 실패 0**
- 직전 실패 1건(TC-CHG-015) **수정 확인**

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 (BE gradlew test --rerun-tasks) | PASS | `BUILD SUCCESSFUL`(4 tasks executed, 전체 재실행), `ChangeService.computeApprovalRoute()` 수정분(STANDARD+templateId 우선 체크) 포함 | |
| TC-BUILD-002 (FE npm run build) | PASS | `tsc -b && vite build` 정상 완료(1850 modules, TS 오류 없음). FE 변경 없음 확인 차원 재실행 | |
| TC-CHG-015 (표준 변경 AUTO 승인경로) | PASS | `POST /changes {type:STANDARD, templateId:1}`(id=13) → 상세 `approvalRoute:"AUTO"` 확인. REQUESTED→REVIEW→PLANNING→APPROVAL→IMPLEMENTATION **전부 200**(409 미발생), `POST .../result` 승인 없이 200(`{"outcome":"SUCCESS","rolledBack":false}`) 저장 확인 | 이전 FAIL(CAB로 오분류, 409 발생) → 이번 PASS |
| TC-CHG-016-R (STANDARD, templateId 미지정) | PASS | `POST /changes {type:STANDARD}`(id=14) → `approvalRoute:"CAB"`(AUTO 아님, 일반 경로 폴백 유지) | 회귀 없음 |
| TC-CHG-007-R (NORMAL+risk=HIGH) | PASS | id=15 → `approvalRoute:"CAB"` | 회귀 없음 |
| TC-CHG-008-R (NORMAL, risk 미지정) | PASS | id=17 → `approvalRoute:"CAB"`(기본값) | 회귀 없음 |
| TC-CHG-LOW-R (NORMAL+risk=LOW) | PASS | id=16 → `approvalRoute:"PEER_REVIEW"` | 회귀 없음 |
| TC-CHG-012-R (CAB 경로, 승인 전 IMPLEMENTATION 전이) | PASS | id=15(CAB) REVIEW→PLANNING→APPROVAL 후 승인 없이 IMPLEMENTATION 시도 → **409 APPROVAL_PENDING**(AUTO 전용 수정이 CAB 경로까지 잘못 확장되지 않음 확인) | 회귀 없음 |
| TC-CHG-019-R (CAB 승인 후 전이) | PASS | cab이 id=15 승인(decision=APPROVE) → 이후 APPROVAL→IMPLEMENTATION **200** 정상 전이 | 회귀 없음 |
| TC-E2E-001-R (FE 상세 화면 AUTO 표시) | PASS | `/changes/13` 상세: "승인 경로" 배지 = **자동**, 상태 = 구현, 승인 이력 = "이력 없음", 구현 결과(성공/standard auto approved deploy) 정상 표시 | 회귀 없음 |

## 실패 항목 분석
- 없음 (TC-CHG-015 해소, 분류·승인 흐름 회귀 없음)

## 결론
- **TC-CHG-015(표준 변경 사전승인 AUTO 승인경로) 수정 확인.** `computeApprovalRoute()`에서 STANDARD+유효 templateId 조건을 위험도 평가 여부보다 우선 체크하도록 수정되어, 표준 변경은 위험도와 무관하게 AUTO로 분류되고 승인 없이 IMPLEMENTATION까지 전이·결과 기록이 가능함을 확인했다.
- 위험도 기반 분류(HIGH/미평가→CAB, LOW→PEER_REVIEW) 및 CAB 승인 흐름(승인 전 409, 승인 후 200)은 기존과 동일하게 정상 동작하여 회귀 없음.
- change 도메인 재테스트 전건 통과. 잔여 실패 없음.
