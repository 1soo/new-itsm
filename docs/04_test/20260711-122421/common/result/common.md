---
date: 20260711-122421
domain: common
result: pass
keywords: [알림확인처리, 모두지우기, 개별삭제]
---

# 통합 테스트 결과 — common (알림 확인처리: 모두 지우기·개별 X, 20260711-122421)

> 범위: API-COM-001/002, SCR-COM-002 헤더 알림 팝오버 확인처리
> 환경: React CSR(:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres)
> 계정: `cab@itsm.local`(APPROVER, 승인 대기 SRM-2026-0007/requestId=8 + CHG-2026-0006/changeId=9), `am@itsm.local`(ASSET_MANAGER, 만료 임박 자산 4건), `admin@itsm.local`/신규 END_USER 계정(BE API 격리 테스트용)

## 요약

- 총 20건 · 성공 20 · 실패 0 ✅ **전 항목 통과**

## 상세

### A. 빌드

| TC ID | 결과 | 비고 |
|-------|------|------|
| TC-BUILD-001 | PASS | `gradlew clean test build` BUILD SUCCESSFUL |
| TC-BUILD-002 | PASS | `npm run build` 성공 |

### B. 알림 확인처리 (API-COM-001)

| TC ID | 결과 | 실제 동작 |
|-------|------|-----------|
| TC-COM-001 | PASS | 개별 1건(ASSET_EXPIRY,999001) → 200 {dismissedCount:1} |
| TC-COM-002 | PASS | 서로 다른 유형 혼합 8건 일괄 → 200 {dismissedCount:8} |
| TC-COM-003 | PASS | TC-COM-001과 동일 항목 재요청 → 200 {dismissedCount:0}(멱등, 오류 아님) |
| TC-COM-004 | PASS | 기존 1건+신규 1건 혼합 요청 → 200 {dismissedCount:1}(신규분만 카운트) |
| TC-COM-005 | PASS | items:[] → 400 VALIDATION_ERROR("items: 비어 있을 수 없습니다") |
| TC-COM-006 | PASS | 요청 바디 {} (items 필드 자체 누락) → 400 VALIDATION_ERROR |
| TC-COM-007 | PASS | Authorization 없이 요청 → 401 |

### C. 확인처리 이력 조회 (API-COM-002)

| TC ID | 결과 | 실제 동작 |
|-------|------|-----------|
| TC-COM-101 | PASS | 확인처리 이력 없는 신규 END_USER 계정 조회 → 200 {items:[]} |
| TC-COM-102 | PASS | B절에서 확인처리한 admin 계정 조회 → 200, (ASSET_EXPIRY,999001)·(ASSET_EXPIRY,999002) 포함, 각 항목 dismissedAt 포함(총 12건) |
| TC-COM-103 | PASS | Authorization 없이 조회 → 401 |

### D. FE E2E — 헤더 알림 팝오버 확인처리 (playwright, 매 항목 새 context)

| TC ID | 결과 | 실제 동작 | 증적 |
|-------|------|-----------|------|
| TC-E2E-001 | PASS | cab@itsm.local 로그인, 알림 2건(서비스요청 승인·변경 승인) 중 "노트북 신청"(서비스요청 승인) 개별 X 클릭 → 해당 1건만 즉시 제거, URL은 "/"에 그대로 유지(상세 이동 없음, stopPropagation 정상), 벨 뱃지 2→1, 변경 승인 1건은 유지 | shots/e2e-001-before-dismiss.png |
| TC-E2E-002 | PASS | 남은 변경 승인 1건에서 "모두 지우기" 클릭 → 목록 즉시 빈 상태("새로운 알림이 없습니다"), 벨 뱃지 배지 사라짐(카운트 0) | shots/e2e-002-before-clear-all.png, shots/e2e-002-after-clear-all-empty.png |
| TC-E2E-003 | PASS | storage 초기화 후 cab@itsm.local 재로그인 → 벨에 카운트 배지 없음("알림"), 팝오버 오픈 시 "새로운 알림이 없습니다" — 확인처리 2건 모두 영구 저장되어 재노출되지 않음 확인 | (스냅샷 확인) |
| TC-E2E-004 | PASS | cab 토큰으로 원본 API 재조회 — `GET /approvals?type=service-request`에 requestId=8(SRM-2026-0007), `GET /approvals?type=change`에 changeId=9(CHG-2026-0006) 그대로 존재. 확인처리가 원본 승인 대기 데이터를 변경하지 않음 확인 | api-evidence/verify_srm_approval.json, verify_chg_approval.json |
| TC-E2E-005 | PASS | am@itsm.local 로그인(자산 만료 4건) → "Retest AWS Reserved Instance" 개별 X 클릭 → 해당 1건만 제거(4→3), 나머지 3건("Retest Office365 License"·"Retest Laptop Dell XPS v2"·"개발팀 노트북 Dell XPS15")은 그대로 유지 | shots/e2e-005-asset-individual-dismiss.png |

## 실패 항목 분석

없음(전 항목 통과).

## 참고

- BE API 격리 테스트(B/C절)는 실제 승인·자산 데이터와 충돌하지 않도록 합성 sourceId(999001·999002·998001~998008)를 사용했다.
- FE E2E(D절)는 데모 계정의 실제 대기 항목(cab의 SRM-2026-0007/CHG-2026-0006, am의 자산 4건 중 1건)을 확인처리했다. API-COM-001은 append-only·영구 저장 설계이므로 이 항목들은 이후 다른 테스트에서도 해당 계정 기준으로는 계속 숨김 처리된 상태로 남는다(원본 데이터 자체는 삭제되지 않음 — TC-E2E-004로 확인). 추후 동일 계정으로 알림 팝오버 회귀 테스트가 필요하면 새로운 승인 대기/자산 데이터를 생성하거나 DB에서 `notification_dismissal` 이력을 초기화해야 한다.
