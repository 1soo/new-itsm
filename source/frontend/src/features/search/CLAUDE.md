# CLAUDE.md

통합 검색(SEARCH) 기능. 지식(KM)+티켓(SRM/INC/PRB/CHG) 교차 도메인 검색을 제공한다. 헤더(SCR-COM-002) 검색바에서 Enter 시 전체 결과 화면(SCR-COM-011)으로 이동해 페이지네이션 조회. API 계약은 api_spec/search.md(API-SEARCH-001) 기준. RBAC(도메인 접근·행 단위 스코프)는 서버가 필터링해 반환하므로 FE는 응답을 그대로 렌더링한다.

## 파일
- `types.ts` — SEARCH 도메인 타입(`SearchDomain`/`SearchResultItem`/`SearchQuery`/`PageResponse`).
- `api.ts` — SEARCH API 호출(`searchApi.search`: 통합 검색, keyword/page/size).
- `status.ts` — 결과 도메인 배지 라벨(`domainLabel`)과 상태 배지 라벨/tone(`resultStatusLabel`/`resultStatusTone`, 각 도메인 기존 status.ts 재사용).
- `format.ts` — 일시 표시 포맷터.
- `SearchResultsPage.tsx` — 통합 검색 결과 전체 목록(SCR-COM-011). 재검색 입력·도메인/상태 배지·발췌·갱신일 표, 페이지네이션(size=20).

## 참고
- 헤더 검색바(입력 중 미리보기 드롭다운, SCR-COM-002)의 실제 동작 주입은 `routes/AppLayout.tsx`(FE)가 담당하며, 프레젠테이션(드롭다운 UI)은 `components/layout/header.tsx`(UI 담당)에 있다.
