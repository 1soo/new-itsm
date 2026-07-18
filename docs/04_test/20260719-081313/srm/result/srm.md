---
date: 20260719-081313
domain: srm
result: pass
keywords: [라벨경계표시확대, 요청제출폼오버레이, placeholder폴백제거, 기본값UI동일화, 타이틀우측배치, i18n전환]
---

# 통합 테스트 결과 — srm 그리드 폼 빌더 8차: 라벨 경계 표시 확대·placeholder 폴백 제거·기본값 UI 동일화·타이틀 배치·i18n 전환 (20260719-081313)

## 요약
- 총 7건 · 성공 7 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-GF8-B01 | PASS | FE `npm run build`(tsc -b && vite build) 성공(API/DB 변경 없어 BE 빌드 생략, 계획서 명시) | - |
| TC-GF8-001 | PASS(핵심) | 라벨 "필수그룹"을 select+checkbox 2개 컴포넌트에 지정 후, SCR-SRM-007 A1 축소 미리보기에서 경계 테두리+legend 텍스트 표시 확인(DOM `.-m-[2px]` 오버레이 요소 1개, textContent="필수그룹"). 요청 제출 폼(SCR-SRM-002, id=18)에서도 동일하게 select+checkbox를 감싸는 파란 테두리+"필수그룹" legend 텍스트가 정상 표시됨(직전까지 빌더 캔버스 전용이었던 것이 요청자 화면까지 확대됨을 확인) | shots/gf8_001_a1_preview_label_overlay.png, shots/gf8_001_request_form_label_overlay.png |
| TC-GF8-002 | PASS | 최초 컴포넌트 배치 시(설정 전) 캔버스에서 select 콤보박스·date 버튼·file 버튼 모두 텍스트 없이 완전히 빈 상태로 렌더링됨(하드코딩 "선택"/"날짜를 선택하세요"/"파일을 선택하세요" 문구 없음). file에 placeholder "증빙 파일을 첨부하세요" 지정 → 요청 제출 폼에 그대로 표시. 추가로 배치한 라벨 미지정 select(placeholder도 미지정)는 요청 제출 폼에서 텍스트 없이 화살표만 있는 완전 빈 콤보박스로 렌더링됨(하드코딩 폴백 완전 제거 재확인) | shots/gf8_canvas_placeholder_empty.png, shots/gf8_002_placeholder_blank_vs_set.png |
| TC-GF8-003 | PASS | select/radio 기본값이 옵션 목록 pill 버튼 단일 선택 UI(재클릭 시 해제되어 캔버스 값이 즉시 사라짐, 재선택 시 즉시 반영 확인). checkbox 기본값이 다중 선택 체크박스 그룹(옵션1/옵션2 둘 다 체크 → 캔버스에 즉시 반영). date 기본값이 실제 `<input type="date">`(DOM 확인, 값 입력 시 캔버스 즉시 반영). file 컴포넌트 Content 설정 팝업에는 "기본값" 항목 자체가 없음(Placeholder까지만 존재). 설정한 기본값 전부가 요청 제출 폼에 정확히 반영됨(select="옵션2", checkbox 2개 모두 체크, date="2026-08-15") | shots/gf8_003_select_default_toggle_off.png |
| TC-GF8-004 | PASS | text/select 등 입력 컴포넌트 Content 설정 팝업에서 "읽기 전용"/"필수 여부" 체크박스가 본문이 아니라 팝업 타이틀("컴포넌트 설정") 우측에 위치함을 확인. 다른 Modal 사용처(라벨 생성 팝업, "새 카테고리" 모달)는 타이틀 영역에 `titleExtra` 관련 요소 없이 기존과 동일한 레이아웃 유지(회귀 없음) | shots/gf8_004_category_modal_no_titleextra.png |
| TC-GF8-005 | PASS | 언어를 English로 전환 → "Form Settings" 팝업 전체(팔레트 9종 "Text/Multi-line Text/Select/Radio/Checkbox/Date/File/Guide Text/Guide File", "Add Label"/"Delete Label", "Component Settings"/"Delete Field", "Cancel"/"Apply"/"Close")와 Content 설정 팝업(Read Only/Required/Label (Tag)/Input Width (%)/Input Alignment/Placeholder/Default Value/Options (comma-separated)/Normal/Linked to CI) 모두 영어로 정상 전환. 요청 제출 폼도 "Request Form"/"Cancel"/"Submit"으로 전환. 한국어로 재전환 시 정상 복귀("요청 양식"/"취소"/"제출") | - |
| TC-GF8-R01 | PASS | 캔버스 카드=실제 렌더링 유지. DnD 드래그 직후 즉시 클릭 시 정상 추가 재확인(라디오 드래그 배치 후 곧바로 클릭해 2번째 라디오도 정상 추가, count 6→8). 팔레트 9종(label 없음) 유지. Content 설정 3×3 정렬 위젯("input 정렬") 정상 노출. 라벨 지정/해제(select에 계속 "필수그룹" 유지) 정상. 미니팝업 위치 통일(7차 수정분) 재확인 — Content 설정 팝업 centerX=768/centerY=323.2로 이전 차수와 픽셀 단위까지 동일 | - |

## 테스트 데이터 처리
- 신규 카탈로그 항목 "GF8 라벨확대 placeholder 기본값 테스트"(id=18) 생성 및 저장(select 2개+checkbox 1개+date 1개+file 1개 컴포넌트, 라벨 "필수그룹"을 첫 select+checkbox에 지정). 회귀 확인 중 임시로 추가한 라디오 2개는 "취소" 버튼으로 미저장 종료해 저장 데이터에는 반영되지 않음.

## 실패 항목 분석
없음.
