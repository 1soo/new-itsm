# 통합 테스트 결과(재테스트) — service-request (SRM) (20260709-124548)

> 대상: SRM 승인 재처리 결함(TC-APR-008) 수정본 재테스트 · BE PID 21816(재기동)
> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres)

## 요약

- TC-APR-008 + 승인/상태전이 회귀 재수행 — **전 항목 통과(실패 0)**.
- 직전 유일 결함(이미 결정된 승인 재처리) 해결 확인. 상태 오염(되돌림/뒤집힘) 재발 없음.
- SRM 도메인 통합테스트 종합: 실패 0.

## 수정 확인 (TC-APR-008)

- **소스**: `ServiceRequestService.decideApproval()`에 `if (!approval.isPending()) throw APPROVAL_ALREADY_DECIDED(409)` 가드 추가, `ErrorCode.APPROVAL_ALREADY_DECIDED(409)` 신설.
- **BE 테스트**: `gradlew test` BUILD SUCCESSFUL, 73테스트 0 failures(신규 `SrmApprovalIntegrationTest` 포함 — 실 DB 재-결정 차단 검증).

## 상세

### TC-APR-008 · 이미 결정된 승인 재처리 차단
| 검증 | 결과 | 실제 |
|------|------|------|
| 승인→IN_FULFILLMENT 후 재-APPROVE | PASS | 409, status IN_FULFILLMENT **불변**, approval APPROVED **불변** |
| 이어서 재-REJECT | PASS | 409, status/approval 불변(뒤집힘 없음) |
| 반려(REJECTED) 후 재-APPROVE | PASS | 409, status REJECTED 유지 |

### 승인 회귀 (API-SRM-011/012)
| TC | 결과 | 실제 |
|----|------|------|
| 승인대기 중 이행 | PASS | 409 |
| 비승인자(END_USER) 승인 | PASS | 403 |
| 반려 사유 누락 | PASS | 400 |
| 최초 승인 | PASS | 200, status ROUTED |
| 승인 후 이행→종료 | PASS | IN_FULFILLMENT→FULFILLED→CLOSED 200 |
| 승인 대기 목록 | PASS | APPROVER 200 / END_USER 403 |

### 상태 전이 회귀 (API-SRM-010)
| TC | 결과 | 실제 |
|----|------|------|
| 허용되지 않은 전이(SUBMITTED→IN_FULFILLMENT) | PASS | 400 |
| 권한 없는 전이(END_USER VALIDATED) | PASS | 403 |
| 정상 전이 전 구간 | PASS | VALIDATED→ROUTED→IN_FULFILLMENT→FULFILLED→CLOSED 200 |
| 종료 재전이 | PASS | 400 |

### 빌드
| TC-BUILD-001 | PASS | gradlew test 73테스트 0 failures(SrmApprovalIntegrationTest 포함) |

### FE E2E
- 이번 수정은 **백엔드 한정**(decideApproval 가드 + ErrorCode)이며 FE 변경 없음. 직전 사이클(20260709-122918)의 SRM FE E2E 9/9 PASS가 유효(포털→제출→목록/큐→상세·코멘트→승인함→CSAT→카탈로그관리→지표→RBAC).

## 결론
- SRM 도메인 통합테스트 **전 항목 통과**. 승인 재처리 결함 해소·회귀 없음. 미결 항목 없음.
</content>
