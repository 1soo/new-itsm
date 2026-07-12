# 유지보수 이력 — esm

> 유지보수 일시: 20260712-180448 · 도메인: esm

## 1. 요구사항

다국어 지원 기능이 추가되어야 하며, 한국어/영어 전환이 모든 화면에 적용되어야 한다.
(확정) 날짜/숫자 포맷은 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
엔터프라이즈 서비스 관리(ESM) 도메인의 부서 포털·요청 제출·내 요청 목록·처리 큐·요청 상세·카탈로그 관리·HR 케이스 목록/상세·체크리스트 상세·내 하위 작업·지표 화면(11개 화면, 최대 규모) 전체가 번역 대상이다.

## 2. 해결 방법

부서 포털/요청 제출/내 요청 목록/처리 큐/요청 상세/카탈로그 관리/HR 케이스 목록·상세/체크리스트 상세/내 하위 작업/지표 대시보드(SCR-ESM-001~011) 화면의 하드코딩 텍스트를 번역 키로 전환했다.
개발 중 자체 발견한 결함을 수정했다 — 체크리스트 유형(온보딩/오프보딩) 라벨이 여러 화면에 중복 하드코딩돼 있던 것을 `checklistTypeLabel` 공용 헬퍼로 통합하고, 카탈로그 템플릿 유형 헬퍼를 신설했다.
`DeptRequestSubmitPage.tsx`에 `validateForm`의 `t`(번역 함수) 인자를 추가했다(service-request 단계에서 남겨뒀던 부분을 이번에 반영).

## 3. 변경 파일

- `source/frontend/src/features/esm/DeptPortalPage.tsx`
- `source/frontend/src/features/esm/DeptRequestSubmitPage.tsx`
- `source/frontend/src/features/esm/MyEsmRequestsPage.tsx`
- `source/frontend/src/features/esm/EsmRequestQueuePage.tsx`
- `source/frontend/src/features/esm/EsmRequestDetailPage.tsx`
- `source/frontend/src/features/esm/EsmCatalogManagePage.tsx`
- `source/frontend/src/features/esm/HrCaseListPage.tsx`
- `source/frontend/src/features/esm/HrCaseDetailPage.tsx`
- `source/frontend/src/features/esm/ChecklistDetailPage.tsx`
- `source/frontend/src/features/esm/MyChecklistTasksPage.tsx`
- `source/frontend/src/features/esm/EsmMetricsPage.tsx`

## 4. 테스트 결과

통합 테스트 전 항목 PASS했다(결함 없음 — 개발 중 자체 발견한 결함은 테스트 이전에 수정 완료).
커밋 `e0b3616`로 반영했다.
