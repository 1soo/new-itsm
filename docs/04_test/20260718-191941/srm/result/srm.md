---
date: 20260718-191941
domain: srm
result: pass
keywords: [3분할 폐기, 캔버스=미리보기 통합, pointer-events-none, hover 오버레이 액션, Content 설정 개별 미리보기]
---

# 통합 테스트 결과 — srm 그리드 폼 빌더 3차 정정 재검증 (20260718-191941)

## 요약
- 총 8건 · 성공 8 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-GF3R-B01 | PASS | BE `./gradlew build` UP-TO-DATE(BE 코드 변경 없음, 직전 빌드 결과 유지) BUILD SUCCESSFUL, FE `npm run build`(tsc -b && vite build) 성공 | - |
| TC-GF3R-001 | PASS | "Form 설정" 팝업 내부에 좌 팔레트/우 캔버스 2영역만 존재, 별도 "미리보기" 라벨의 우측 패널 완전히 사라짐(직전 20260718-182813 테스트의 3분할 우측 패널과 비교) | shots/gf3r_2panel_layout.png |
| TC-GF3R-002 | PASS | 캔버스에 배치된 8개 컴포넌트(text/radio/guide-text/guide-file/select/date/file/label) 카드 모두 유형 아이콘·캡션 텍스트 없이 실제 렌더링 모습(빈 input, 옵션1/옵션2 라디오, 안내 아이콘+텍스트, 다운로드 링크, "선택" 콤보박스, 날짜/파일 입력박스+아이콘, "텍스트" 라벨)을 그대로 표시 | shots/gf3r_2panel_layout.png |
| TC-GF3R-003 | PASS | Playwright로 캔버스 카드 내부 text input에 실제 클릭+입력을 시도한 결과 클릭 자체가 타임아웃(요소가 액셔너블하지 않음, `pointer-events: none` computed style로 재확인) — 값 변경 없음. 반면 카드를 포인터로 드래그(mousedown→move→up)하면 정상적으로 다른 셀로 이동(위치 y=135→411로 이동 확인) | shots/gf3r_after_drag.png |
| TC-GF3R-004 | PASS | text 컴포넌트 Content 설정에서 Placeholder를 "여기에입력"으로 변경 → 적용 버튼을 누르지 않은 상태에서 캔버스 카드 렌더링(및 Content 설정 팝업 하단 개별 미리보기)에 즉시 반영됨을 확인 | shots/gf3r_canvas_live_update.png |
| TC-GF3R-005 | PASS | 카드 hover 시 우측 상단에 설정(Settings2)·삭제(Trash2) 버튼이 반투명 배경 오버레이로 노출. 설정 버튼 클릭 시 드래그로 오인되지 않고 Content 설정 팝오버가 정상 오픈(이전 테스트에서 설정한 "필수 여부" 체크 상태도 유지되어 있음을 확인 — `onPointerDown` stopPropagation으로 드래그와 분리 처리 정상 동작) | - |
| TC-GF3R-006 | PASS | Content 설정 팝업 하단 개별 미리보기 textbox에 "미리보기값입력"을 실제로 입력 성공(상호작용 가능) — 동시에 "기본값" 필드는 빈 값 그대로 유지(로컬 상태 격리), 캔버스 카드(placeholder "여기에입력"만 표시)에도 이 입력값이 전혀 반영되지 않아 캔버스 카드(비상호작용)와 팝업 내 미리보기(상호작용 가능)가 명확히 구분됨을 확인 | shots/gf3r_preview_interactive_vs_canvas.png |
| TC-GF3R-007 | PASS | 요청 처리함(SCR-SRM-004) 카테고리별 건수(계정4/하드웨어12/소프트웨어0/기타비품0/미분류19, 미분류 마지막 고정)·기존 요청(SRM-2026-0039 등) 목록 정상 표시, 이번 유지보수(빌더 UI 전용 변경)와 무관한 기능 회귀 없음. `dynamic-form-renderer.tsx`는 이번 변경분에 포함되지 않아(git status 확인) 요청 제출 화면(SCR-SRM-002) 렌더링은 직전 통합 테스트(`20260718-182813`)에서 이미 확인한 그대로 영향 없음 | - |

## 테스트 데이터 처리
- 직전 통합 테스트(`20260718-182813`)에서 생성한 카탈로그 항목 "GF3 인터랙티브 테스트"(id=13)를 재사용해 재검증(변경사항은 "취소" 버튼으로 미저장 종료, 항목 스키마 원본 그대로 유지).
- 신규 생성한 티켓·첨부파일 없음.

## 실패 항목 분석
없음.
