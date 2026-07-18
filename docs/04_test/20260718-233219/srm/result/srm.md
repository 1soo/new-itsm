---
date: 20260718-233219
domain: srm
result: pass
keywords: [개별미리보기제거, 라벨legend스타일, 5차결함수정확인, showBorder텍스트유지, 테두리색조건부렌더링]
---

# 통합 테스트 결과 — srm 그리드 폼 빌더 6차: 개별 미리보기 제거 + 라벨 표시 보정 (20260718-233219)

## 요약
- 총 9건 · 성공 9 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-GF6-B01 | PASS | FE `npm run build`(tsc -b && vite build) 성공(API/DB 변경 없어 BE 빌드 생략, 계획서 명시) | - |
| TC-GF6-001 | PASS | 신규 카탈로그 항목 "GF6 미리보기 제거 라벨 보정 테스트" 생성, text 컴포넌트 Content 설정 팝업을 끝까지 확인한 결과 "라벨(태그)" Select 이후 "미리보기" 섹션이 완전히 없음(입력 위젯만 존재). 카탈로그 항목 저장 후 SCR-SRM-007 A1 축소 미리보기(disabled 렌더링) 정상 동작, 요청 제출 폼(SCR-SRM-002, id=16)도 정상 렌더링 회귀 없음 | shots/gf6_001_no_preview_section.png, shots/gf6_a1_preview_regression.png, shots/gf6_request_form_no_overlay.png |
| TC-GF6-002 | PASS | 라벨 생성 팝업에서 테두리색을 `#dc2626`으로 지정 → "테두리 없음" 체크 시 테두리색 입력 UI 완전히 사라짐(글자색만 남음) → 체크 해제 시 테두리색 입력 UI 재노출되고 값이 `#dc2626` 그대로 복귀(초기화 안 됨, Playwright `fill()`로 재검증해 정확히 확인) | - |
| TC-GF6-003 | PASS(5차 결함 수정 확인) | "테두리 없음" 체크한 라벨("참고")을 select 컴포넌트에 지정 → 캔버스에 테두리 선은 안 보이지만 "참고" 텍스트는 정상 표시됨(직전 5차의 "오버레이 전체 미표시" 결함 해소 확인). 이후 그 select 컴포넌트를 삭제해 참조를 0개로 만들자 "참고" 텍스트를 포함한 오버레이 전체가 완전히 사라짐(참조 0개 시 렌더링 자체 없음 재확인) | shots/gf6_003_showborder_false_text_only.png, shots/gf6_003_zero_ref_no_overlay.png |
| TC-GF6-004 | PASS | showBorder=true인 "공지" 라벨(테두리색 기본값 `#1d4ed8`)을 text 컴포넌트에 지정 → 라벨 텍스트가 오버레이 내부가 아니라 테두리 선 위에 걸쳐(legend 스타일, 불투명 배경으로 아래 선 가림) 표시됨. showBorder=false인 "참고" 라벨도 텍스트가 (테두리가 있었다면 걸쳤을) 동일 위치에 표시됨(TC-GF6-003 스크린샷과 동일 위치) | shots/gf6_004_legend_style_showborder_true.png |
| TC-GF6-R01 | PASS | 캔버스 카드=실제 렌더링(입력 박스·옵션 그대로 표시) 유지. DnD 드래그 직후 즉시 클릭 시 정상 추가(체크박스 드래그 배치 후 곧바로 클릭해 2번째 체크박스도 정상 추가 — 4차 수정분 회귀 없음). 팔레트 9종(label 없음) 유지. 체크박스 Content 설정에 정규식 UI 없음(text 전용 유지)·9방향 정렬 위젯(`left-top` 등)·옵션 배치 방향(가로/세로) 정상 노출. 칩 스트립은 showBorder 무관 "공지"(빨강 테두리)·"참고"(파랑 테두리) 둘 다 테두리+틴트 스타일 유지 | shots/gf6_r01_chip_style_and_regression.png |

## 테스트 데이터 처리
- 신규 카탈로그 항목 "GF6 미리보기 제거 라벨 보정 테스트"(id=16) 생성 및 저장(text 1개+checkbox 2개 컴포넌트, 라벨 "공지"(showBorder=true, text에 지정)/"참고"(showBorder=false, 생성 후 참조 컴포넌트가 삭제되어 현재 참조 0개 상태로 저장) 2개).

## 실패 항목 분석
없음.
