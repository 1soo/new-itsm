---
date: 20260712-180448
domain: problem
change_type: [new, modified]
keywords: [다국어, i18n, 번역 키, origin 회귀 수정]
---

# 유지보수 이력 — problem

> 유지보수 일시: 20260712-180448 · 도메인: problem

## 1. 요구사항

다국어 지원 기능이 추가되어야 하며, 한국어/영어 전환이 모든 화면에 적용되어야 한다.
(확정) 날짜/숫자 포맷은 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
문제(PRB) 도메인의 목록·등록·상세·KEDB 검색 화면 전체가 번역 대상이다.

## 2. 해결 방법

목록/등록/상세/KEDB 검색(SCR-PRB-001~004) 화면의 하드코딩 텍스트를 번역 키로 전환했다.
통합검색(SCR-COM-011) 상태 배지 전환까지 연동을 완료했다.
개발 중 자체 발견한 회귀를 수정했다 — `origin` 값이 없는 레거시 티켓에서 원시 번역 키 문자열("origin.undefined")이 화면에 그대로 노출되던 문제를, falsy 값 가드를 추가해 해결했다.

## 3. 변경 파일

- `source/frontend/src/features/problem/ProblemListPage.tsx`
- `source/frontend/src/features/problem/ProblemCreatePage.tsx`
- `source/frontend/src/features/problem/ProblemDetailPage.tsx`
- `source/frontend/src/features/problem/KnownErrorSearchPage.tsx`
- `source/frontend/src/features/problem/status.ts`
- `source/frontend/src/features/problem/format.ts`

## 4. 테스트 결과

통합 테스트 10건 전부 PASS했다.
커밋 `0f02072`로 반영했다.
