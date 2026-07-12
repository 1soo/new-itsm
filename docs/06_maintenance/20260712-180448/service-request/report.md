# 유지보수 이력 — service-request

> 유지보수 일시: 20260712-180448 · 도메인: service-request

## 1. 요구사항

다국어 지원 기능이 추가되어야 하며, 한국어/영어 전환이 모든 화면에 적용되어야 한다.
(확정) 날짜/숫자 포맷은 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
서비스요청(SRM) 도메인의 포털·요청 제출·목록·큐·상세·카탈로그·지표 화면 전체가 번역 대상이다.

## 2. 해결 방법

서비스 포털/요청 제출/내 요청 목록/요청 큐/요청 상세/카탈로그 관리/지표 대시보드(SCR-SRM-001~005,007~008) 화면의 하드코딩 텍스트를 번역 키로 전환했다.
`status.ts`의 상태 라벨을 `t` 함수 기반으로 전환했다.
통합검색(SCR-COM-011) 상태 배지 전환까지 연동을 완료했다.
개발 중 공용 컴포넌트 결함 3건을 발견해 수정했다 — `field-builder.tsx`(양식 필드 빌더 전체가 번역되지 않았던 문제), `rating.tsx`(별점 aria-label 미전환), `form-schema.ts`(`validateForm`의 필수 항목 오류 메시지 미전환).

## 3. 변경 파일

- `source/frontend/src/features/service-request/PortalPage.tsx`
- `source/frontend/src/features/service-request/RequestSubmitPage.tsx`
- `source/frontend/src/features/service-request/RequestListPage.tsx`
- `source/frontend/src/features/service-request/RequestQueuePage.tsx`
- `source/frontend/src/features/service-request/RequestDetailPage.tsx`
- `source/frontend/src/features/service-request/CatalogManagePage.tsx`
- `source/frontend/src/features/service-request/MetricsPage.tsx`
- `source/frontend/src/features/service-request/status.ts`
- `source/frontend/src/components/common/field-builder.tsx`(결함 수정)
- `source/frontend/src/components/common/rating.tsx`(결함 수정)
- `source/frontend/src/components/common/form-schema.ts`(결함 수정)

## 4. 테스트 결과

통합 테스트 15건 전부 PASS했다(재테스트 포함).
커밋 `2146282`로 반영했다.
