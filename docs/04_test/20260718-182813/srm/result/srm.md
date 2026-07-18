---
date: 20260718-182813
domain: srm
result: pass
keywords: [팝업 3분할 미리보기, radio/checkbox 배치방향, guide-text/guide-file 분리, 외부 pre-view 제거]
---

# 통합 테스트 결과 — srm 그리드 폼 빌더 3차 유지보수 (20260718-182813)

## 요약
- 총 12건 · 성공 12 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-GF3-B01 | PASS | BE `./gradlew build`(테스트 포함) BUILD SUCCESSFUL(2m5s), FE `npm run build`(tsc -b && vite build) 성공 | - |
| TC-GF3-001 | PASS | "Form 설정" 팝업 내부에 좌 팔레트/중앙 캔버스/우 전체 레이아웃 미리보기 3영역이 탭 전환 없이 항상 동시 표시. 모달 실측 폭 약 1368px(뷰포트 1536px 기준 약 89%, `w-[90vw] max-w-[1600px]`) — 기존 약 896px 대비 크게 확대 | shots/gf3_3panel_layout.png |
| TC-GF3-002 | PASS | 캔버스에 컴포넌트 배치 시 "적용" 클릭 전에도 우측 미리보기에 즉시 반영(축소 렌더링). guide-file 파일 첨부, guide-text 텍스트 변경, radio 배치방향/여백 변경 모두 적용 전 실시간 반영 확인 | shots/gf3_realtime_preview_after_add.png |
| TC-GF3-003 | PASS | 카탈로그 항목 편집 폼(팝업 바깥)에서 45% 축소 pre-view 렌더링 완전히 제거됨. 필드 미설정 시 "설정된 양식이 없습니다..." 안내, 8개 필드 적용 후 "설정된 필드 8개" 텍스트로 대체 확인 | - |
| TC-GF3-004 | PASS | 좌측 팔레트에 text/textarea/select/radio/checkbox/date/file/label/안내 텍스트(guide-text)/가이드 파일(guide-file) 10종 노출, 통합 `guide` 항목은 존재하지 않음 | shots/gf3_3panel_layout.png |
| TC-GF3-005 | PASS | guide-text Content 설정 팝오버에 "표시 텍스트"+"정렬"(좌/가운데/우)만 존재(default/읽기전용/필수/정규식/첨부파일 없음, label과 동일 구조) | - |
| TC-GF3-006 | PASS | guide-file Content 설정 팝오버에 "첨부 파일(선택)"만 존재(텍스트/정렬 등 다른 설정 없음). 파일(`gf3_guide_file.txt`) 첨부 시 파일명+제거 버튼 노출, 제거 동작 정상 | - |
| TC-GF3-007 | PASS | 요청 제출 화면(SCR-SRM-002)에서 guide-text가 안내 아이콘(Info)+텍스트로 label과 시각적으로 구분되어 렌더링, guide-file은 첨부 시 다운로드 링크(`gf3_guide_file.txt`) 노출·미첨부 시 "첨부된 파일이 없습니다." 빈 상태 안내 확인(3분할 미리보기·pre-view 폐지 후 이 팝업 미리보기로 대체된 위치 포함) | shots/gf3_request_submit_view.png |
| TC-GF3-008 | PASS | guide-text/guide-file만 있고 다른 필수 필드(text) 채운 상태로 실제 `POST /api/v1/service-requests` 제출 → 201 정상 처리, 요청 바디 `formValues`에 `{"field_1":"GF3테스트값"}`만 포함되어 guide-text(field_3)/guide-file(field_4) 등 미입력·정적 컴포넌트가 전혀 포함되지 않음 확인. 400 없음 | - |
| TC-GF3-009 | PASS | radio Content 설정 팝오버에 "옵션 배치 방향"(가로/세로 토글, 기본 "가로" 활성 표시)·"옵션 간 여백"(좁게/보통/넓게, 기본 "좁게" 활성 표시) UI 존재, checkbox도 동일 확인. select Content 설정 팝오버에는 옵션(콤마)/CI연계까지만 있고 배치방향·여백 UI 없음 확인 | shots/gf3_radio_options_layout.png |
| TC-GF3-010 | PASS | radio를 "세로"+"넓게"로 변경 시 개별 미리보기가 즉시 세로 배치+넓은 간격으로 갱신, 실제 요청 제출 화면에도 세로 배치로 반영됨(회귀 렌더러 로직 재사용 확인). 기본값(가로/좁게) 상태에서는 다른 이전 통합테스트와 동일하게 가로 배치 렌더링(코드상 `flex-row flex-wrap`으로 옵션 폭 초과 시 줄바꿈 처리 확인) | shots/gf3_radio_vertical_wide.png |
| TC-GF3-011 | PASS | `FormSubmissionValidatorTest`에 `guideTextTypeComponentIsSkippedEvenWithoutValue`/`guideFileTypeComponentIsSkippedEvenWithoutValue` 2개 테스트 존재, TC-GF3-B01 빌드에서 통과 확인. `grep '"guide"'` 결과 `FormSubmissionValidator.java`·FE 공통 컴포넌트 3개 파일 모두 매치 없음(단일 `guide` 타입 코드 완전 제거) | - |
| TC-GF3-012 | PASS | placeholder(text/textarea/select/date/file만 UI, radio/checkbox 없음) 재확인, 1×1 카드 아이콘 전용 캡션(8개 컴포넌트 모두) 재확인, Content 설정 팝업 내 개별 실시간 미리보기(placeholder 즉시 반영, 로컬 입력이 기본값에 미반영) 재확인, date/file 입력박스+우측아이콘(요청 제출 화면·개별 미리보기 모두 박스 형태 유지, 아이콘 전용 아님) 재확인, label 컴포넌트(텍스트+정렬만) 재확인, 요청 처리함 카테고리 카운트(계정4/하드웨어12/소프트웨어0/기타비품0/미분류19, 미분류 마지막 고정)·신규 요청 반영 정상 | shots/gf3_full_canvas_1x1_captions.png |

## 테스트 데이터 처리
- 카탈로그 항목 "GF3 인터랙티브 테스트"(id=13)를 신규 생성해 인터랙티브 테스트(3분할 실시간 미리보기, guide-text/guide-file, radio 배치방향/여백, date/file 회귀)에 사용 — 카탈로그 항목은 삭제 API가 없어 잔여 테스트 데이터로 유지.
- 요청 제출 테스트로 생성된 SRM-2026-0039 티켓은 기존 세션들과 동일하게 테스트 잔여 데이터로 유지.
- 첨부파일 테스트용 임시 파일(`gf3_guide_file.txt`, `.playwright-mcp/` 하위)은 테스트 종료 후 삭제 완료.

## 실패 항목 분석
없음.
