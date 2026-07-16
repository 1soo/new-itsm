---
date: 20260712-180448
domain: infra-monitoring
change_type: [modified]
keywords: [다국어(i18n) 전환, 번역 키 적용, 지표/임계치/용량계획/리포팅 화면]
---

# 유지보수 이력 — infra-monitoring

> 유지보수 일시: 20260712-180448 · 도메인: infra-monitoring

## 1. 요구사항

다국어 지원 기능이 추가되어야 하며, 한국어/영어 전환이 모든 화면에 적용되어야 한다.
(확정) 날짜/숫자 포맷은 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
인프라 모니터링·용량관리(IOM) 도메인의 지표 등록·대시보드·임계치 설정/알림·용량 계획·리포팅 화면 전체가 번역 대상이다(전체 12개 도메인 중 마지막 순서로 진행).

## 2. 해결 방법

지표 등록/대시보드/임계치 설정·알림/용량 계획/리포팅(SCR-IOM-001~005) 화면의 하드코딩 텍스트를 번역 키로 전환했다.

## 3. 변경 파일

- `source/frontend/src/features/infra-monitoring/InfraMetricRegisterPage.tsx`
- `source/frontend/src/features/infra-monitoring/InfraMetricDashboardPage.tsx`
- `source/frontend/src/features/infra-monitoring/InfraThresholdAlertPage.tsx`
- `source/frontend/src/features/infra-monitoring/InfraCapacityPlanPage.tsx`
- `source/frontend/src/features/infra-monitoring/InfraReportPage.tsx`

## 4. 테스트 결과

통합 테스트 전 항목 PASS했다(결함 없음).
커밋 `3017562`로 반영했다(12개 도메인 중 마지막 커밋).
