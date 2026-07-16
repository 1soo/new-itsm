---
date: 20260711-123218
domain: common
result: pass
keywords: [알림확인처리, 사용자별격리, 상세보기이동]
---

# 통합 테스트 결과 — common (알림 확인처리, 보강 검증, 20260711-123218)

> 배경: dev-lead 재요청 항목 중 직전 실행(20260711-122421, 20/20 PASS)에서 다루지 않은 사용자별 이력 격리·상세 보기 이동 회귀에 집중
> 환경: React CSR(:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres)

## 요약

- 총 3건 · 성공 3 · 실패 0 ✅ **전 항목 통과**

## 상세

### A. 사용자별 확인처리 이력 격리

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-ISO-001 | PASS | 사용자A(tester_com_901)가 (ASSET_EXPIRY,777001) 확인처리 후 사용자A 조회에는 포함, 사용자B(tester_com_iso_b) 조회에는 미포함(빈 배열) — 사용자별 이력 완전 격리 확인 | api-evidence/userA_after.json, userB_before.json |
| TC-ISO-002 | PASS | 사용자B가 동일 (ASSET_EXPIRY,777001)를 확인처리 요청 → 200 {dismissedCount:1}(사용자A의 처리와 무관하게 독립적으로 신규 처리, 충돌/오류 없음). DB `UNIQUE(user_id, notification_type, source_id)` 설계대로 사용자별 독립 저장 확인 | api-evidence/r_iso_userB.json |

### B. FE 회귀 — 상세 보기 이동

| TC ID | 결과 | 실제 동작 | 증적 |
|-------|------|-----------|------|
| TC-E2E-101 | PASS | am@itsm.local 로그인(미확인처리 자산 3건) → 알림 팝오버 "Retest Office365 License" 항목의 "상세 보기" 클릭 → `/assets/7`로 정상 이동(개별 X 클릭 시 미이동과 대비되는 정상 라우팅 확인) | shots/e2e-101-detail-navigation.png |

## 실패 항목 분석

없음(전 항목 통과).

## 종합

이번 요청의 나머지 항목(API-COM-001 개별/일괄/멱등/400/401, API-COM-002 이력 없음/조회, 헤더 알림 팝오버의 모두 지우기·개별 X·영구 저장·원본 데이터 불변, 빈 상태 문구)은 직전 실행(`docs/04_test/common/20260711-122421/result/common.md`, 20/20 PASS)에서 이미 검증 완료했다. 이번 3건을 더해 common 도메인(알림 확인처리) 전체 23건 전부 통과.

정렬·8건 상한은 헤더 알림 팝오버의 기존(v1/v2) 기능으로, 이번 유지보수 범위 변경과 무관한 로직(FE는 확인처리 필터링만 추가, 정렬·상한 로직 자체는 미변경)이라 별도 재검증하지 않았다. 코드 리뷰상 `AppLayout.tsx`의 `slice(0, NOTIFICATION_PREVIEW_SIZE)`·`formatRelativeTime` 등 기존 로직은 이번 변경에서 손대지 않았음을 확인했다.
