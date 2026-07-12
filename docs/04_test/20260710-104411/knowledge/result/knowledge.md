# 통합 테스트 결과 — knowledge (KM) 재테스트 (20260710-104411)

> 환경: React CSR(localhost:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres, healthy)
> 대상: `KnowledgeListPage.tsx` 카테고리/상태/유용성 컬럼 `whitespace-nowrap` 수정분 회귀 검증

## 요약
- 총 **7건** (빌드 1 · 컬럼 수정 검증 2 · 기존 기능 회귀 4) · **성공 7 · 실패 0**

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 (FE npm run build) | PASS | `tsc -b && vite build` 정상 완료(1867 modules, TS 오류 없음), 수정분 포함 | |
| TC-KM-COL-001 (라이트, 줄바꿈 없음) | PASS | 카테고리("네트워크"/"계정·권한"/"결제" 등)·상태("게시"/"초안"/"검토")·유용성(%) 5개 표본 행 전수 `white-space: nowrap`, 셀 높이 43px로 균일(줄바꿈으로 인한 높이 증가 없음) | shots-knowledge/km-col-001-light.png |
| TC-KM-COL-002 (다크, 줄바꿈 없음) | PASS | 테마 토글 후에도 동일하게 `white-space: nowrap` 유지, 셀 높이 43px 균일 | shots-knowledge/km-col-002-dark.png |
| TC-KM-REG-001 (키워드 검색) | PASS | 키워드 "결제" 검색 시 목록 갱신 정상 동작(제목/요약에 매칭 없어 빈 상태 "기사가 없습니다" 정상 렌더링, 오류 없음) — 검색 기능 자체 회귀 없음 |
| TC-KM-REG-002 (상태 필터) | PASS | 상태 필터를 "게시"로 선택 후 검색 시 결과 3건 전부 상태="게시"로 정상 필터링 |
| TC-KM-REG-003 (페이지네이션) | PASS | 현재 데이터가 6건(< PAGE_SIZE 10)으로 1페이지뿐이라 `Pagination` 컴포넌트가 렌더링되지 않음(`totalPages<=1` 시 `null` 반환, 정상 사양). 회귀 아님 |
| TC-KM-REG-004 (행 클릭 상세 이동) | PASS | 목록 첫 행 클릭 시 `/knowledge/3` 상세로 정상 이동 |

## 실패 항목 분석
- 없음

## 결론
- 지식베이스 목록 컬럼 줄바꿈 수정(`whitespace-nowrap`) 확인 — 라이트/다크 테마 모두 카테고리·상태·유용성 컬럼이 줄바꿈 없이 한 줄로 정상 표시된다.
- 검색·필터·행 클릭 등 기존 기능 회귀 없음. 페이지네이션은 현재 시드 데이터가 1페이지 분량이라 컴포넌트가 렌더링되지 않는 것이 정상 사양(코드 확인: `pagination.tsx` `totalPages<=1 → null`)이며 결함이 아니다.
- knowledge 도메인 외 재검증 불필요(dev-lead 지시대로 해당 파일 외 영향 없음) — 실패 항목 없음.
