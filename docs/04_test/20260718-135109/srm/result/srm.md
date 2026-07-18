---
date: 20260718-135109
domain: srm
result: pass
keywords: [8×n 그리드 폼 빌더, form.io 제거, 서버 재검증, 겹침 차단, 라운드트립]
---

# 통합 테스트 결과 — srm form.io 완전 제거 → 자체 8×n 그리드 폼 빌더 전면 재구현 (20260718-135109)

## 요약
- 총 11건 · 성공 11 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-GRID-B01 | PASS | BE `./gradlew clean build`(테스트 포함) BUILD SUCCESSFUL(2m28s), FE `npm run build` 성공(JS 번들 3.94MB→2.16MB, 모듈 3993→2138로 감소 — formio 라이브러리 제거 반영) | - |
| TC-GRID-001 | PASS | `package.json`에 `@formio/js`/`@formio/react` 없음, `src` 전체·`index.css`에 `formio` 문자열 없음(코드 언급은 CLAUDE.md 문서상 "완전 제거됨"이라는 서술뿐), `node_modules/@formio` 디렉토리도 없음 | grep 결과 |
| TC-GRID-002 | PASS | 팔레트 7종 pre-view/빌더에 정상 노출·클릭 배치. Pointer 기반 이동 1칸(92px, gap 포함) 스냅 확인. 리사이즈: textarea는 5행까지 확장 가능(무제한), 일반 컴포넌트(text)는 3행 시도해도 h=2로 캡. 리사이즈 중 겹침 시도는 이동과 동일하게 차단 | 자동화 pointer 이벤트로 검증(스크린샷 grid_builder_popup.png) |
| TC-GRID-003 | PASS | 이미 배치된 컴포넌트 위로 이동 시 `role="alert"` "이미 배치된 컴포넌트와 겹칩니다" 노출, pointerup 후 모든 컴포넌트 위치 원위치(변경 없음) | 자동화 pointer 이벤트로 재현·확인 |
| TC-GRID-004 | PASS | Content 설정 팝오버: 라벨 텍스트/정렬, input 폭%/정렬/기본값/읽기전용/필수, Validation 정규식 입력(placeholder 예시 포함) 전부 노출. select/radio/checkbox는 "옵션(콤마로 구분)" + "일반"/"CI 연계" 라디오 노출, CI 연계 클릭 시 상태만 토글되고 별도 동작 없음(설계대로 자리만) | 스크린샷 grid_content_settings.png, grid_select_settings.png |
| TC-GRID-005 | PASS | 체크박스 필드에 장문+12개 옵션 입력 후 pre-view 확인 — 셀 폭 안에서 줄바꿈, 셀 높이 고정 유지, 내부 스크롤(overflow) 처리로 그리드 레이아웃 붕괴 없음. 렌더러 코드도 `overflow-y-auto` 확인 | 스크린샷 grid_preview_manyoptions.png |
| TC-GRID-006 | PASS | "그리드 빌더 테스트" 항목에 날짜 필드 추가 → 적용 → 저장 → 페이지 새로고침 → 재조회 시 "이름/부서/날짜" 3필드 그대로 유지(라운드트립). 테스트 후 원상 복원(API PATCH로 2필드 원본 스키마 복구) | - |
| TC-GRID-007 | PASS | SCR-SRM-002에서 카탈로그의 그리드 배치 그대로 렌더링(이름*, 부서 select). 필수 미입력 제출 시 인라인 "이름은(는) 필수 항목입니다." 노출·제출 차단. 정상 입력 후 제출 시 SRM-2026-0029 생성, 상세 페이지 이동 확인 | - |
| TC-GRID-008 | PASS | `POST /api/v1/service-requests` 직접 호출: required 필드 누락 → 400 `REQUIRED_FIELD_MISSING`("필수 항목 누락: 이름"), regex 위반 → 400 `FORM_FIELD_INVALID`("부서: 형식이 올바르지 않습니다."), 정상 값 → 201(`form_values`에 그대로 저장). 테스트용 임시 regex는 완료 후 원복 | 최초 curl 시도에서 bash 인라인 문자열 UTF-8 손상으로 오탐(400 VALIDATION_ERROR)이 있었으나 파일 기반 payload로 재시도해 정상 확인됨(앱 결함 아님) |
| TC-GRID-009 | PASS | 기존 카탈로그 항목(id 1~7) `form_schema` 전부 `{"components":[]}`로 리셋 확인. dev-fe가 남긴 테스트 항목(id 8·9)만 populated 그리드 스키마 보유(의도된 상태) | DB 쿼리 결과 |
| TC-GRID-010 | PASS | ESM 부서 카탈로그 관리(`/admin/esm-catalog`)는 여전히 레거시 필드 빌더("필드 추가", 라벨/유형/필수입력 목록) 사용, 부서 서비스 포털 요청 제출(`/esm/portal/requests/new`)도 기존 방식대로 "대상자명*/입사일*/직책*" 정상 렌더링 — 이번 SRM 전용 변경의 영향 없음 | - |

## 실패 항목 분석
없음.
