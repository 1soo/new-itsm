# 통합 테스트 시나리오 — change (CHG) 재테스트

> 실행 타임스탬프: 20260709-233129 · 도메인: change
> 목적: 직전 실행(20260709-230947) 실패 1건(TC-CHG-015 표준 변경 AUTO 승인경로 미작동) 수정분 재검증 + 분류 로직 전반(위험도 기반 CAB/PEER_REVIEW) 및 승인 흐름 회귀
> 원본 시나리오: docs/04_test/20260709-230947/scenario/change.md (전체 커버리지는 원본 유지, 본 문서는 재검증 대상만 다룸)

## 사전 조건
- BE(:8080)·FE dev(:5173) 기동, PostgreSQL(`itsm-postgres`) healthy
- 계정: cm@itsm.local(CHANGE_MANAGER), cab@itsm.local(APPROVER)

## 시나리오

### A. 빌드 (재검증)
- **TC-BUILD-001** · BE `gradlew test --rerun-tasks` 통과(수정된 `ChangeService.computeApprovalRoute()` 포함) — @docs/01_analyze/feature/change.md 전 FEAT
- **TC-BUILD-002** · FE `npm run build` 통과(FE 변경 없음, 회귀 확인 차원 재실행) — @docs/02_plan/screen/change.md

### B. TC-CHG-015 수정 재검증 — 표준 변경 사전승인(AUTO)
- **TC-CHG-015** · `type=STANDARD` + 존재하는 `templateId`(id=1) 생성 → 상세 `approvalRoute=AUTO` 확인, REQUESTED→REVIEW→PLANNING→APPROVAL→IMPLEMENTATION **승인 없이** 전이 성공(409 미발생), 구현 결과 기록도 승인 없이 200 — REQ-CHG-006/FEAT-CHG-006/API-CHG-004

### C. 분류 로직 회귀 — 위험도 기반 승인 경로
- **TC-CHG-016-R** · `type=STANDARD` + templateId 미지정(위험도 미평가) → `approvalRoute=CAB`(일반 경로 폴백, AUTO 아님) — FEAT-CHG-006 (Unwanted)
- **TC-CHG-007-R** · `type=NORMAL` + `risk=HIGH` → `approvalRoute=CAB` — REQ-CHG-004 (Complex, 고위험→CAB)
- **TC-CHG-008-R** · `type=NORMAL` + risk 미지정(미평가) → `approvalRoute=CAB`(기본값) — FEAT-CHG-004 (Unwanted)
- **TC-CHG-LOW-R** · `type=NORMAL` + `risk=LOW` → `approvalRoute=PEER_REVIEW` — REQ-CHG-004 (Event-driven, 저위험 동료검토)

### D. 승인 흐름 회귀 — CAB 경로는 여전히 승인 필수
- **TC-CHG-012-R** · CAB 경로(HIGH risk) 변경을 승인 없이 APPROVAL→IMPLEMENTATION 전이 시도 → 409 APPROVAL_PENDING(회귀 없음, AUTO 전용 로직이 CAB/PEER_REVIEW까지 잘못 확장되지 않았는지 확인) — API-CHG-004 409
- **TC-CHG-019-R** · CAB 승인(decision=APPROVE) 후 동일 변경 APPROVAL→IMPLEMENTATION 재시도 → 200 정상 전이 — REQ-CHG-005

### E. FE 회귀
- **TC-E2E-001-R** · CM 로그인 → 변경 상세(SCR-CHG-003)에서 표준 변경(AUTO) 항목의 "승인 경로" 배지가 "자동"으로 표시되고 "승인 이력"이 "이력 없음"으로 정상 표시되는지 확인 — SCR-CHG-003
