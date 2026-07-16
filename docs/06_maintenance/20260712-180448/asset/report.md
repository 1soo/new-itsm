---
date: 20260712-180448
domain: asset
change_type: [new, modified]
keywords: [다국어(i18n), 번역 키 전환, 라벨 헬퍼 결함 수정]
---

# 유지보수 이력 — asset

> 유지보수 일시: 20260712-180448 · 도메인: asset

## 1. 요구사항

다국어 지원 기능이 추가되어야 하며, 한국어/영어 전환이 모든 화면에 적용되어야 한다.
(확정) 날짜/숫자 포맷은 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
자산/CMDB(ITAM) 도메인의 목록·등록/수정·상세·CI·CMDB 관계 뷰·지표 화면 전체가 번역 대상이다.

## 2. 해결 방법

목록/등록·수정/상세/CI·CMDB 관계 뷰/지표 대시보드(SCR-ITAM-001~005) 화면의 하드코딩 텍스트를 번역 키로 전환했다.
결함 수정 3건을 처리했다 — 연결 티켓 유형(`ticketTypeLabel`), CI 관계 유형(`relationTypeLabel`), 생애주기 이력(`statusLabel` 재사용)에서 열거형 원시값이 그대로 노출되던 문제를 각각 라벨 헬퍼로 해결했다.

## 3. 변경 파일

- `source/frontend/src/features/asset/AssetListPage.tsx`
- `source/frontend/src/features/asset/AssetFormPage.tsx`
- `source/frontend/src/features/asset/AssetDetailPage.tsx`
- `source/frontend/src/features/asset/CiRelationPage.tsx`
- `source/frontend/src/features/asset/AssetMetricsPage.tsx`
- `source/frontend/src/features/asset/status.ts`(결함 수정 — `ticketTypeLabel`/`relationTypeLabel`)
- `source/frontend/src/features/asset/format.ts`

## 4. 테스트 결과

통합 테스트 전 항목 PASS했다(재테스트 포함).
커밋 `09340b8`로 반영했다.
