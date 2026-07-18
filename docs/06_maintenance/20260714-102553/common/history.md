---
date: 20260714-102553
domain: common
change_type: [modified]
keywords: [사이드바축소, 컬럼폭고정, PAGE_SIZE재산정]
---

# 유지보수 이력 — common

> 유지보수 일시: 20260714-102553 · 도메인: common

## 1. 요구사항

사이드바 폭/폰트를 축소해 콘텐츠 영역을 확보해야 한다.
목록 표의 컬럼 폭이 페이지 이동 시 흔들리는 문제를 방지하기 위해 모든 목록 표의 컬럼 폭을 고정해야 한다.
1920×1080 해상도, 뷰포트 937px 기준으로 100% 확대 시 스크롤이 나타나지 않도록 페이지당 아이템 수를 재산정해야 한다.

## 2. 해결 방법

`sidebar.tsx`에서 사이드바 폭을 펼침 240px→190px, 접힘 64px→48px로 축소하고, 메뉴 라벨 14px→12px, 그룹 헤더 12px→10px로 축소했다.
`index.css`에 scrollbar-hide 유틸리티를 추가했다.
`data-table.tsx`의 `Column<T>` 타입에 `width?: number`를 추가하고, colgroup + table-fixed 구조로 컬럼 폭 고정 인프라를 도입했다(DataTable을 거치지 않는 표는 영향 없음).
17개 목록 화면(admin 3·service-request 2·incident 1·problem 1·change 1·knowledge 1·asset 1·esm 4·vulnerability 1·compliance 1·search 1)에 도메인별로 확정한 px 값을 적용하고, truncate/line-clamp-1을 보강했다.
1920×1080/뷰포트 937px 기준 산정에 따라 PAGE_SIZE를 화면 패턴별로 10/20에서 13/14/16으로 재산정해 반영했다.
problem 도메인의 KnownErrorSearchPage(SCR-PRB-004)는 카드 리스트 구조라 컬럼 폭 고정을 적용하지 않았고, 카드 높이가 가변적이라 페이지당 아이템 수 산정이 불가해 기존 10건을 유지했다.

## 3. 변경 파일

- `source/frontend/src/components/layout/sidebar.tsx`
- `source/frontend/src/index.css`
- `source/frontend/src/components/common/data-table.tsx`
- `source/frontend/src/features/admin/MenuManagementPage.tsx`
- `source/frontend/src/features/admin/AuditLogPage.tsx`
- `source/frontend/src/features/admin/UserListPage.tsx`
- `source/frontend/src/features/service-request/RequestQueuePage.tsx`
- `source/frontend/src/features/service-request/RequestListPage.tsx`
- `source/frontend/src/features/incident/IncidentListPage.tsx`
- `source/frontend/src/features/problem/ProblemListPage.tsx`
- `source/frontend/src/features/change/ChangeListPage.tsx`
- `source/frontend/src/features/knowledge/KnowledgeListPage.tsx`
- `source/frontend/src/features/asset/AssetListPage.tsx`
- `source/frontend/src/features/esm/EsmRequestQueuePage.tsx`
- `source/frontend/src/features/esm/MyEsmRequestsPage.tsx`
- `source/frontend/src/features/esm/HrCaseListPage.tsx`
- `source/frontend/src/features/esm/MyChecklistTasksPage.tsx`
- `source/frontend/src/features/vulnerability/VulnerabilityListPage.tsx`
- `source/frontend/src/features/compliance/ComplianceListPage.tsx`
- `source/frontend/src/features/search/SearchResultsPage.tsx`

## 4. 테스트 결과

통합 테스트 23건 전부 PASS했다.
커밋 `4610582`(main, push 완료)로 반영됐다.
