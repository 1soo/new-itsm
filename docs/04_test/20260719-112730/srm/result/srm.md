---
date: 20260719-112730
domain: srm
result: pass
keywords: [defaultalign, center-middle, 소급적용없음, GridLabelOverlays회귀없음, DnD회귀]
---

# 통합 테스트 결과 — srm 유지보수: 신규 컴포넌트 default align을 center-center로 변경 (20260719-112730)

## 요약
- 총 4건 · 성공 4 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-ALIGN-B01 | PASS | FE `npm run build`(tsc -b && vite build) 성공 | - |
| TC-ALIGN-001 | PASS | SCR-SRM-007 Form 설정 팝업에서 text/select/checkbox를 신규 추가 후 각 컴포넌트 설정(Content 설정) 팝업의 "input 정렬" 위젯이 3종 모두 `center-middle`로 기본 선택됨(DOM class `border-primary bg-primary` 확인). 캔버스 카드에서도 text input이 `mx-auto`(가로 중앙)+상위 `items-center`(세로 중앙) 컨테이너에 렌더링됨을 computed style로 확인. 저장 후 END_USER 요청 제출 폼(`/portal/requests/new?item=19`)에서도 동일하게 중앙 정렬로 표시됨 | shots/align_001_canvas_center_middle.png, shots/align_001_request_form_center_middle.png |
| TC-ALIGN-002 | PASS | 같은 팝업에서 안내 텍스트(guide-text)를 신규 추가 시 정렬 위젯이 `center-middle` 기본 선택(기존 `left-top`에서 변경 확인), 텍스트 `textAlign: center`+상위 flex 컨테이너 `justify-center`(세로 중앙)로 렌더링됨을 computed style로 확인 | - |
| TC-ALIGN-003 | PASS | (a) 신규 항목("GF9 정렬 기본값 테스트", id=19) 저장 후 페이지 재로드해도 컴포넌트 배치·정렬이 서버에 정상 영속화됨을 확인. (b) **소급 적용 없음 확인**: 이전 라운드(8차)에 저장된 기존 항목 "GF8 라벨확대 placeholder 기본값 테스트"(id=18)를 재편집 없이 열어보니, 기존 input 컴포넌트들의 래퍼가 여전히 `items-start`(세로 top 정렬)로 남아있어 이번 기본값 변경이 소급 적용되지 않음을 확인(회귀 없음) | shots/align_003_a1_preview_saved.png, shots/align_003_a1_preview_reloaded.png, shots/align_003_gf8_no_retroactive.png |
| TC-ALIGN-004 | PASS | 팔레트 9종 중 대표로 text/select/checkbox/guide-text 4종을 캔버스에 정상 추가(팔레트 나머지 5종은 `buildNewComponent`의 동일한 "input" 분기를 공유하므로 코드 레벨로 회귀 없음 확인). DnD로 text 컴포넌트를 새 위치(row1→row3 영역)로 재배치 시 정상 이동하고 라벨 오버레이도 함께 따라 이동함. "회귀테스트라벨" 라벨을 신규 생성해 text 컴포넌트에 연결, 경계 테두리+legend 텍스트가 캔버스·요청 제출 폼 양쪽에서 정상 표시됨(`GridLabelOverlays` 회귀 없음). checkbox 컴포넌트 설정의 기본값 UI가 8차와 동일하게 다중 토글 버튼(옵션1/옵션2)으로 정상 동작 | shots/align_004_label_overlay_before_dnd.png |

## 테스트 데이터 처리
- 신규 카탈로그 항목 "GF9 정렬 기본값 테스트"(id=19)를 생성해 테스트에 사용(정리 없이 유지, 이후 회귀 테스트 자산으로 활용 가능).
- 기존 항목("GF8 라벨확대 placeholder 기본값 테스트", id=18)은 조회만 하고 수정하지 않음.

## 실패 항목 분석
없음. 세션 전반의 콘솔 에러는 기존부터 존재하던 무관한 401(auth/me, service-catalog/items)·404(favicon)뿐이며, 이번 변경과 관련된 TypeError·크래시는 없음.
