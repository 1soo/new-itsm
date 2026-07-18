---
date: 20260718-220645
domain: srm
result: pass
keywords: [라벨 경계 테두리 보정, 4px확장, showBorder, 1개이상기준, 즉시재계산]
---

# 통합 테스트 결과 — srm 그리드 폼 빌더 5차: 라벨 경계 테두리 보정 (20260718-220645)

## 요약
- 총 9건 · 성공 9 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-GF5-B01 | PASS | FE `npm run build`(tsc -b && vite build) 성공(BE/DB 변경 없어 BE 빌드 생략, 계획서 명시) | - |
| TC-GF5-001 | PASS | 신규 카탈로그 항목 "GF5 라벨 테두리 보정 테스트" 생성, 라벨 "우선순위"(기본값)를 text 컴포넌트 1개에만 지정 → 캔버스에 그 컴포넌트 영역에 테두리+텍스트가 즉시 표시됨(직전 4차엔 2개 미만이라 미표시였던 케이스) | shots/gf5_001_single_ref_border.png |
| TC-GF5-002 | PASS | DOM 정밀 측정으로 확인: 컴포넌트 카드 실제 좌표(x=286.4,y=188.1,w=137,h=88) 대비 오버레이 좌표(x=284.4,y=186.1,w=141,h=92) — 좌우상하 각 2px씩, 전체 4px 확장 정확히 일치. 그리드 배치 좌표(`gridColumn`/`gridRow`) 자체는 컴포넌트와 동일 유지 | - |
| TC-GF5-003 | PASS | 오버레이 좌측 상단에 "우선순위" 텍스트가 라벨의 textColor(#1d4ed8, 파란색)로 표시됨. 칩 스트립의 텍스트 표시와 별개로 캔버스 오버레이 내부에도 추가 표시 확인 | shots/gf5_001_single_ref_border.png |
| TC-GF5-004 | PASS | 라벨 생성/수정 팝업에 "테두리 없음" 체크박스 존재. 체크 후 저장 → 참조 컴포넌트 2개(text+select)여도 캔버스 테두리 전혀 안 그려짐. 체크 해제 후 저장 → 즉시 재표시(2개 컴포넌트 모두 포함하는 확장 사각형). 칩 재클릭 시 체크 상태([checked]/[unchecked]) 정확히 프리필됨(양방향 확인) | shots/gf5_004_showborder_off.png, shots/gf5_showborder_toggle_back_on.png |
| TC-GF5-005 | PASS | showBorder=false 상태에서도 팔레트 상단 "우선순위" 칩이 테두리+틴트 배경 스타일 그대로 유지됨(흐려지거나 사라지지 않음) | shots/gf5_004_showborder_off.png |
| TC-GF5-006 | PASS | (1) select 컴포넌트를 다른 셀로 드래그 이동 → 오버레이가 새 위치까지 즉시 확장 재계산(`1/1/5/7`). (2) select를 리사이즈(1칸→2칸 폭)로 확장 → 오버레이가 즉시 재계산되어 새 경계(`1/1/5/8`)로 반영. (3) showBorder 토글 시 즉시 나타남/사라짐(TC-GF5-004에서 확인) | - |
| TC-GF5-007 | PASS | Content 설정 팝업 내 개별 미리보기(text 컴포넌트)에 오버레이 없음(단순 input만 표시). 카탈로그 항목 저장 후 CatalogManagePage A1 축소 미리보기에도 오버레이 없음(DOM 조회로 `-m-[2px]` 클래스 요소 0개 확인). 요청 제출 폼(SCR-SRM-002, id=15)에서도 text/select 모두 오버레이 없이 렌더링됨 | shots/gf5_007_content_preview_scrolled.png, shots/gf5_007_a1_preview_no_overlay.png, shots/gf5_007_request_form_no_overlay.png |
| TC-GF5-R01 | PASS | 팔레트 9종 유지(label 없음) 재확인. DnD 드래그 직후 즉시 클릭(0ms) 시 정상 추가 재확인(라디오 드래그 배치 성공 후 곧바로 클릭해 2번째 라디오도 정상 추가, `input[type=radio]` 개수 0→4로 2개 컴포넌트 모두 반영 — 4차 수정분(JUST_DRAGGED_RESET_MS=0) 회귀 없음). select 컴포넌트 Content 설정 팝업에 정규식 입력 UI 없음(text 전용 유지), 3×3 정렬 위젯(`left-top`/`center-top` 등) 정상 노출 재확인 | - |

## 테스트 데이터 처리
- 신규 카탈로그 항목 "GF5 라벨 테두리 보정 테스트"(id=15) 생성 및 저장(text+select 2개 컴포넌트, 라벨 "우선순위" showBorder=true 최종 상태로 저장).
- 회귀 확인(TC-GF5-R01) 중 임시로 추가한 라디오 2개, select 리사이즈 등은 "취소" 버튼으로 미저장 종료해 저장 데이터에는 반영되지 않음(항목의 최종 저장 상태는 위 text+select 2개 컴포넌트 그대로 유지).

## 실패 항목 분석
없음.
