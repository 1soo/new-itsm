# 통합 테스트 결과 — approval-engine (Stage 3: KNOWLEDGE 게시 승인 게이트) (20260711-213921)

## 요약
- 총 9건 · 성공 9 · 실패 0
- 테스트 중 1회성 이상 동작 관찰(재현 실패, 아래 비고 참조) — 결함으로 판단하지 않음

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | `./gradlew build`, `npm run build` 모두 성공 | |
| TC-KMGATE-001 | PASS | 매칭 규칙 없는 요청자(SYSTEM_ADMIN)가 검토 요청 → 200, `status="PUBLISHED"` 즉시 전환 | |
| TC-KMADM-001 | PASS | 기존 KNOWLEDGE tier=3 규칙(요청자=KNOWLEDGE_CONTRIBUTOR, 1차 AND[GATEKEEPER]) 정상 조회·활용 확인(개발팀이 이미 생성해둔 규칙 재사용, 신규 생성 불필요) | |
| TC-KMGATE-002 | PASS | KNOWLEDGE_CONTRIBUTOR가 검토 요청 → 200(409 아님), `status="IN_REVIEW"`, `approvalRequestId` 반환, 인스턴스 IN_PROGRESS 생성 확인 | |
| TC-KMGATE-003 | PASS | 기사 상세(API-KM-002) 재조회 시 `approval:{approvalRequestId, status:"IN_PROGRESS"}` 정상 유지(새로고침에도 소실 없음), `ticketKey="KM-7"` 형식 정상(이전 undefined 버그 수정 확인) | |
| TC-KMGATE-004 | PASS | 게이트키퍼 APPROVE 결정 직후 별도 재시도 없이 기사 status가 자동 PUBLISHED로 전환 확인 | |
| TC-KMGATE-005 | PASS | REJECT(사유 포함) → 기사 자동 DRAFT 복귀, `approval.status="REJECTED"` + 사유는 API-COM-004 역할별 `reason`에 정상 노출 → "검토 요청" 재클릭 시 신규 `approvalRequestId` 발급 확인(4회 반복 재현, 매번 정상) | 1회 이상 동작 관찰(비고 참조) |
| TC-KMGATE-006 | PASS | 게이트키퍼 `scope=mine&domain=KNOWLEDGE` 조회 결과 전 항목 `ticketKey`="KM-{id}" 형식 정상(KM-15/14/13/12/11/5), undefined 없음 | |
| TC-KMREG-001 | PASS | DB 직접 조회 결과 `screen`/`screen_role`에 SCR-KM-004 레코드 0건(완전 제거 확인) | |
| TC-CROSSREG-001 | PASS | SRM(카탈로그 제출→VALIDATED→ROUTED→IN_FULFILLMENT, 매칭 규칙 없어 게이트 없이 200 통과)·CHANGE(REQUESTED→REVIEW→PLANNING→APPROVAL→IMPLEMENTATION, 매칭 규칙 없어 게이트 없이 200 통과) 모두 정상, 회귀 없음 | |

## 비고 — 1회성 이상 동작(결함 미확정, 참고용 기록)
- TC-KMGATE-005 최초 시도(article id=8) 시, REJECT 처리 후 "검토 요청"을 재클릭했을 때 신규 인스턴스가 생성되지 않고 기존 REJECTED 인스턴스(approvalRequestId=7)가 그대로인 채 기사 status만 `PUBLISHED`로 전환되는 것을 1회 관찰했다(기대: 매칭 규칙이 여전히 활성 상태이므로 신규 인스턴스 생성+IN_REVIEW여야 함).
- 이후 동일 조건으로 **4회 연속 재현을 시도했으나 전부 정상**(매번 신규 `approvalRequestId` 발급, IN_REVIEW 정상 전환)이었다. DB 조회 결과 해당 시간대에 다른 팀원(개발팀)도 KNOWLEDGE 승인 관련 테스트 데이터(예: "Stage3 matched-rule test article B (reject)")를 동시에 생성하고 있었던 흔적이 확인되어, 공유 테스트 환경에서의 동시 조작에 의한 일시적 현상일 가능성이 높다고 판단했다.
- 재현이 안 되어 결함으로 확정하지 않고 기록만 남긴다. 이후 다른 도메인 테스트에서 유사 현상이 다시 관찰되면 그때 정식으로 보고하겠다.
