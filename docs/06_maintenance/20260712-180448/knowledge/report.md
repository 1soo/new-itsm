# 유지보수 이력 — knowledge

> 유지보수 일시: 20260712-180448 · 도메인: knowledge

## 1. 요구사항

다국어 지원 기능이 추가되어야 하며, 한국어/영어 전환이 모든 화면에 적용되어야 한다.
(확정) 날짜/숫자 포맷은 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
지식(KM) 도메인의 검색·목록·기사 열람·작성/편집·지표 화면이 번역 대상이다(SCR-KM-004는 SCR-COM-014로 이미 대체되어 제외).

## 2. 해결 방법

검색/목록/기사 열람/작성·편집/지표 대시보드(SCR-KM-001~003,005) 화면의 하드코딩 텍스트를 번역 키로 전환했다.
통합검색(SCR-COM-011) 상태 배지 전환까지 연동을 완료했다.

## 3. 변경 파일

- `source/frontend/src/features/knowledge/KnowledgeListPage.tsx`
- `source/frontend/src/features/knowledge/ArticleViewPage.tsx`
- `source/frontend/src/features/knowledge/ArticleEditPage.tsx`
- `source/frontend/src/features/knowledge/KnowledgeMetricsPage.tsx`
- `source/frontend/src/features/knowledge/status.ts`
- `source/frontend/src/features/knowledge/format.ts`

## 4. 테스트 결과

통합 테스트 전 항목 PASS했다(결함 없음).
커밋 `f4fd2c8`로 반영했다.
