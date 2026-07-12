# 통합 테스트 결과 — approval-engine (Stage 4 추가 재테스트: API-INC-009 resolve 게이트 보완) (20260712-000915)

## 요약
- 총 6건 · 성공 6 · 실패 0 ✅ **전 항목 통과** — 직전 라운드(20260711-234110) 발견 사항 해소 확인

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew build -x test`, `npm run build` 모두 오류 없이 성공 | |
| TC-INCGATE-RESOLVE-001 | PASS | 매칭 규칙 있음 상태에서 IN_PROGRESS 인시던트(INC-2026-0004, id=10)에 `POST /incidents/{id}/resolve` 직접 호출 → 409(`APPROVAL_PENDING`) + `approvalRequestId=30` 반환, 인스턴스 IN_PROGRESS 생성 확인. 인시던트 상태는 IN_PROGRESS로 그대로 유지(RESOLVED로 바뀌지 않음) — 우회 경로 차단 확인 | |
| TC-INCGATE-RESOLVE-002 | PASS | tester_inc4_im APPROVE 결정(200, APPROVED) → `resolve()` 재시도 200 성공, `status="RESOLVED"`, 상세 `approval.status="APPROVED"` | |
| TC-INCUI-RESOLVE-001 | PASS | 승인 대기 중(id=11) 상세에서 "해결 처리" 제출 버튼 `disabled` + `title="승인 완료 전에는 해결 처리할 수 없습니다"` 확인, 승인 현황 패널도 "1차" 노출 | |
| TC-INCUI-RESOLVE-002 | PASS | tester_inc4_im으로 신규 인시던트(id=12) "해결 처리" 최초 제출 시 409 유발 → 새로고침 없이 토스트 "승인 대기 중에는 이행할 수 없습니다." + 승인 현황 패널 "1차·대기중" 즉시 표시. 동일 승인 상태를 공유하는 "해결"(PATCH 전이) 버튼도 함께 disabled로 전환되는 것까지 확인(두 진입점이 같은 게이트 상태를 일관되게 반영) | |
| TC-INCGATE-RESOLVE-003 | PASS | 신규 인시던트(id=11)에서 `PATCH .../status{RESOLVED}`로 먼저 게이트 유발(409+인스턴스 id=31) 후 동일 티켓에 `POST .../resolve` 호출 → 동일 인스턴스(id=31) 기준 409 재확인, 신규 인스턴스 중복 생성 없음 | |

## 실패 항목 분석

없음(전 항목 통과). 직전 라운드(20260711-234110)에서 보고한 API-INC-009 게이트 우회 결함이 완전히 해소되었다.

## 테스트 환경 조성 참고

- 이전 라운드에서 INACTIVE 처리했던 tester_inc4_agent@itsm.local / tester_inc4_im@itsm.local 계정을 ACTIVE로 재전환해 재사용했다.
- 신규 생성된 테스트 인시던트(id 10~12, ticket_key는 이전 라운드 정리로 시퀀스가 재사용되어 INC-2026-0004~0006로 표기됨 — 결함 아님, 연도별 카운트 기반 채번)와 승인 인스턴스(id 30, 31)가 남아있다. dev-lead 확인 후 정리 필요 여부 알려달라.
