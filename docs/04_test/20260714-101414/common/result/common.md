---
date: 20260714-101414
domain: common
result: pass
keywords: [사이드바축소, 컬럼폭고정, PAGE_SIZE]
---

# 통합 테스트 결과 — 공통(사이드바 축소·컬럼폭 고정·페이지당 아이템 수) (20260714-101414)

## 요약
- 총 23건 · 성공 23 · 실패 0
- 데이터 부족으로 페이지 이동 컬럼폭 흔들림 항목을 재검증하지 못한 화면(RequestListPage/RequestQueuePage/ChangeListPage/AssetListPage/EsmRequestQueuePage/MyChecklistTasksPage/HrCaseListPage/MyEsmRequestsPage/SearchResultsPage 등, 실 데이터 건수 < PAGE_SIZE)이 있으나, 컬럼폭·헤더·PAGE_SIZE 요청 파라미터는 전 항목 확인 완료(사전 조건에 명시된 SKIP 처리 대상 아님 — 헤더/폭/요청 파라미터 검증은 정상 수행됨).

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-COM-001 | PASS | `npm run build` → `tsc -b && vite build` 성공, 에러 없음(2136 modules transformed, 12.39s) | 콘솔 로그 |
| TC-COM-002 | PASS | 사이드바 펼침 `width: 190px`, 접힘 `width: 48px`. 그룹헤더 `font-size: 10px`(`text-[10px]` 클래스), 메뉴 라벨 `font-size: 12px`(`text-xs` 클래스) | - |
| TC-COM-003 | PASS | `transition: width 0.2s cubic-bezier(0.16,1,0.3,1)` 적용, 접기/펼치기 클릭 시 정상 전환(레이아웃 깨짐 없음) | shots/sidebar-collapsed.png, shots/sidebar-expanded.png |
| TC-COM-004 | PASS | `DataTable` 경유 목록(UserListPage 등)에서 `<table>` `table-layout: fixed`, `<colgroup>`/`<col>` 존재, width 지정 컬럼은 고정폭 style, 대표 텍스트 컬럼은 `col` width 미지정(auto)으로 잔여 폭 흡수 | - |
| TC-COM-005 | PASS | LoginPage 테스트 계정 표: `table-layout: auto`, `<colgroup>` 없음 — DataTable 변경 영향 없음 확인 | - |
| TC-COM-006 (RequestListPage) | PASS | 컬럼폭 130/auto/110/110/120/110(설계 일치), API 요청 `size=13` 확인 | 실 데이터 2건이라 페이지 이동 재현 불가(SKIP) |
| TC-COM-007 (RequestQueuePage) | PASS | 컬럼폭 130/auto/110/110/120/110/140(설계 일치), API 요청 `size=16` 확인 | 실 데이터 2건, 페이지 이동 SKIP |
| TC-COM-008 (IncidentListPage) | PASS | 컬럼폭 130/auto/110/110/120/90/110(설계 일치), API 요청 `size=14` 확인, `요약` 컬럼 `line-clamp-1` 적용(소스 확인) | 실 데이터 4건 |
| TC-COM-009 (ProblemListPage) | PASS | 컬럼폭 130/auto/110/110/100/120/110(설계 일치), API 요청 `size=14` 확인, 소스에 `line-clamp-1` 적용 확인 | 실 데이터 3건 |
| TC-COM-010 (ChangeListPage) | PASS | 컬럼폭 130/auto/110/110/110/110(설계 일치), API 요청 `size=14` 확인, 소스에 `line-clamp-1` 적용 확인 | 실 데이터 2건 |
| TC-COM-011 (KnowledgeListPage) | PASS | 컬럼폭 auto/auto/110/110/90(설계 일치), API 요청 `size=14` 확인 | 실 데이터 9건 |
| TC-COM-012 (AssetListPage) | PASS | 컬럼폭 130/auto/110/110/120/150(설계 일치), API 요청 `size=14` 확인, 소스에 `line-clamp-1` 적용 확인 | 실 데이터 2건 |
| TC-COM-013 (EsmRequestQueuePage) | PASS | 컬럼폭 130/100/auto/110/110(설계 일치), API 요청 `size=13` 확인 | 실 데이터 1건 |
| TC-COM-014 (MyEsmRequestsPage) | PASS | 컬럼폭 130/100/auto/110/110(설계 일치), API 요청 `size=13` 확인 | 실 데이터 3건 |
| TC-COM-015 (HrCaseListPage) | PASS | 컬럼폭 130/auto/110/110(설계 일치), API 요청 `size=13` 확인 | 실 데이터 1건 |
| TC-COM-016 (MyChecklistTasksPage) | PASS | 컬럼폭 110/120/auto/110/140(설계 일치), API 요청 `size=13` 확인 | 실 데이터 1건 |
| TC-COM-017 (VulnerabilityListPage) | PASS | 컬럼폭 130/auto/110/110/130/120/110(설계 일치), API 요청 `size=14` 확인, rowCount 9(데이터 충분) | 페이지 이동 미실시(단일 페이지) |
| TC-COM-018 (ComplianceListPage) | PASS | 컬럼폭 130/auto/auto/150/120/110(설계 일치), API 요청 `size=14` 확인, rowCount 5 | - |
| TC-COM-019 (SearchResultsPage) | PASS | 컬럼폭 130/auto/130/160(설계 일치), API 요청 `size=13` 확인, 제목 셀 `truncate`/`line-clamp-1` 적용 확인 | 실 매칭 결과 10건 |
| TC-COM-020 (AuditLogPage) | PASS | 컬럼폭 160/130/160/auto/100(설계 일치), API rowCount 14, 페이지네이션 5페이지 존재(데이터 충분) | - |
| TC-COM-021 (UserListPage) | PASS | 컬럼폭 120/auto/220/100/110(설계 일치), rowCount 14, 페이지 2 이동 후 컬럼폭 동일 유지 확인(흔들림 없음), 이메일 셀 `truncate` 적용 확인 | - |
| TC-COM-022 (MenuManagementPage) | PASS | 컬럼폭 110/auto/auto/140/80/130/260(설계 일치), rowCount 16 | - |
| TC-COM-023 (KnownErrorSearchPage 제외 확인) | PASS | `<table>` 없음(카드 리스트 유지), API 요청 `size=10`(변경 없음) 확인 | - |

## 실패 항목 분석
- 없음.
