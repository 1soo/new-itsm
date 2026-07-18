---
date: 20260718-172626
domain: srm
result: pass
keywords: [placeholder, guide 컴포넌트, 1x1 캡션 아이콘화, 개별 실시간 미리보기, date/file 입력박스 롤백]
---

# 통합 테스트 결과 — srm 그리드 폼 빌더 후속 개선 (20260718-172626)

## 요약
- 총 11건 · 성공 11 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-GF2-B01 | PASS | BE `./gradlew build`(테스트 포함) BUILD SUCCESSFUL(3m21s), FE `npm run build`(tsc -b && vite build) 성공 | - |
| TC-GF2-001 | PASS | 팔레트 9종(text/textarea/select/radio/checkbox/date/file/label/guide) 노출 확인. text/date/select Content 설정 팝오버에 Placeholder 입력 항목 존재, radio/checkbox 팝오버에는 Placeholder 항목 없음(옵션 콤마 입력만) — `hasPlaceholderUi` 계약대로 동작 | shots/gf2_builder_canvas.png |
| TC-GF2-002 | PASS | text에 placeholder "예: 홍길동" 지정 후 요청 제출 화면·pre-view에서 값 미입력 시 placeholder 표시, 입력 후 실제 값 표시. select는 placeholder 미지정 시 기존과 동일하게 "선택" 폴백 표시(팝오버 미리보기로 확인) | shots/gf2_request_submit.png |
| TC-GF2-003 | PASS | guide Content 설정 팝오버에 "안내 텍스트"(여러 줄)+"첨부 파일"(선택)만 존재(정렬/default/읽기전용/필수/정규식 없음). 파일 첨부(`gf2_test_guide.txt`) 시 파일명+제거 버튼 노출, 제거 동작 확인. 1×1→2×1 리사이즈 시 폭 변경 정상(1~2×1~2 범위, label과 동일 규칙) | shots/gf2_guide_popover.png |
| TC-GF2-004 | PASS | 요청 제출 화면·pre-view 모두 안내 텍스트+다운로드 링크(`gf2_test_guide.txt`) 렌더링. `POST /api/v1/service-requests` 실제 요청 바디 확인 결과 `formValues`에 guide 컴포넌트의 key(field_3)가 포함되지 않음(`{"field_4":"2026-08-20","field_5":"data:...","field_1":"테스트값","field_2":"옵션1"}`) | shots/gf2_request_submit.png |
| TC-GF2-005 | PASS | 위 TC-GF2-004의 실제 제출(guide 값 없음)이 201 Created로 정상 처리됨(guide로 인한 400 없음). 백엔드 단위 테스트(`FormSubmissionValidatorTest.guideTypeComponentIsSkippedEvenWithoutValue`)도 TC-GF2-B01 빌드에서 통과 확인 | - |
| TC-GF2-006 | PASS | 캔버스에 배치한 3개 컴포넌트(1×1) 모두 아이콘만 표시(텍스트 숨김) 확인. guide 컴포넌트를 2×1로 리사이즈 후 아이콘+"안내/가이드" 텍스트 함께 표시 확인 | shots/gf2_builder_canvas.png, shots/gf2_canvas_after_resize.png |
| TC-GF2-007 | PASS | Content 설정 팝업 실제 렌더링 폭 측정 결과 약 420px(스크린샷 좌표 기준 563~983px) — 설계 요구(최소 400px 이상) 충족, guide 텍스트+파일+미리보기 영역이 잘리지 않고 표시됨 | shots/gf2_guide_popover.png |
| TC-GF2-008 | PASS | text 컴포넌트 Placeholder 입력 변경 시 팝업 하단 미리보기(textbox placeholder)가 입력 즉시 갱신됨. 미리보기 textbox에 임의 값("미리보기전용값") 입력 후 "기본값" 입력란은 그대로 빈 값 유지(로컬 상태 미반영 확인) — date 컴포넌트 placeholder 변경 시에도 미리보기 버튼 라벨 즉시 갱신 확인 | - |
| TC-GF2-009 | PASS | 요청 제출 화면·pre-view 모두 date/file이 입력 박스(테두리)+박스 우측 아이콘(캘린더/파일) 형태로 렌더링(아이콘 전용 아님). `HTMLInputElement.prototype.showPicker` 계측 결과 박스 클릭 시 1회 호출 확인(네이티브 피커 트리거). 날짜 값 설정 시 박스 안에 "2026-08-20" 표시, 파일 선택(박스 클릭→파일 선택) 시 박스 안에 "gf2_test_guide.txt" 파일명 표시, 값 없을 때 박스 안에 placeholder("희망일자 입력"/유형별 기본 안내 "파일을 선택하세요") 표시 | shots/gf2_request_submit.png |
| TC-GF2-010 | PASS | 서비스 카탈로그 카테고리 관리 탭(계정/하드웨어/소프트웨어/기타비품) 정상 로드. 요청 처리함(SCR-SRM-004) 카테고리별 건수(계정4/하드웨어12/소프트웨어0/기타비품0/미분류18, 미분류 마지막 고정) 정상, 신규 제출 요청(SRM-2026-0038) 목록에 정상 반영. 회귀 없음 | - |

## 테스트 데이터 처리
- 카탈로그 항목 "GF2 인터랙티브 테스트"(id=12)를 신규 생성해 인터랙티브 테스트(placeholder/guide 파일첨부/1×1 리사이즈/select 폴백)에 사용 — 카탈로그 항목은 삭제 API가 없어(다른 도메인 티켓과 동일 패턴) 잔여 테스트 데이터로 유지.
- 기존 "가이드폼 테스트 항목"(dev 팀이 남긴 픽스처, guide+date+radio 사전 구성)은 읽기 전용으로만 열람(변경 없음, 팝업은 "닫기"로 미저장 종료).
- 요청 제출 테스트로 생성된 SRM-2026-0038 티켓은 기존 세션들과 동일하게 테스트 잔여 데이터로 유지.
- 첨부파일 테스트용 임시 파일(`gf2_test_guide.txt`, `.playwright-mcp/` 하위)은 테스트 종료 후 삭제 완료.

## 실패 항목 분석
없음.

## 참고 사항(결함 아님)
- 카탈로그 항목 저장(`저장` 버튼) 직후 콘솔에 `401 Unauthorized`(`/api/v1/auth/me`, `/api/v1/service-catalog/items` 등) 로그가 일시적으로 출현했으나, 이후 토큰 재발급 재시도로 저장은 정상 완료(신규 항목이 목록·조회에 즉시 반영됨). 이번 5개 요구사항(placeholder/guide/1×1 캡션/개별 미리보기/date-file 롤백)과 무관한 기존 인증 인터셉터 동작으로 판단되며, 실제 기능 실패로 이어지지 않아 실패 항목에 포함하지 않음(참고용으로만 기록).
