# 유지보수 이력 — compliance

> 유지보수 일시: 20260712-180448 · 도메인: compliance

## 1. 요구사항

다국어 지원 기능이 추가되어야 하며, 한국어/영어 전환이 모든 화면에 적용되어야 한다.
(확정) 날짜/숫자 포맷은 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
컴플라이언스(COMP) 도메인의 목록·등록·상세·준수 현황 대시보드 화면 전체가 번역 대상이다.

## 2. 해결 방법

목록/등록/상세/준수 현황 대시보드(SCR-COMP-001~004) 화면의 하드코딩 텍스트를 번역 키로 전환했다.
개발 중 자체 발견한 결함을 수정했다 — 감사 로그 이벤트 코드(COMPLIANCE_REQ_CREATE 등)가 원시값 그대로 노출되던 문제를 `auditEventTypeLabel` 헬퍼로 해결했다.

## 3. 변경 파일

- `source/frontend/src/features/compliance/ComplianceListPage.tsx`
- `source/frontend/src/features/compliance/ComplianceCreatePage.tsx`
- `source/frontend/src/features/compliance/ComplianceDetailPage.tsx`
- `source/frontend/src/features/compliance/ComplianceMetricsPage.tsx`

## 4. 테스트 결과

통합 테스트 전 항목 PASS했다(결함 없음 — 개발 중 자체 발견한 결함은 테스트 이전에 수정 완료).
커밋 `a05cb86`로 반영했다.
