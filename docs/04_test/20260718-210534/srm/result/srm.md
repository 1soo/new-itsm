---
date: 20260718-210534
domain: srm
result: partial
keywords: [DnD 배치, 드래그직후클릭씹힘 결함, 높이상한세분화, 정규식text전용, 라벨태그개편, 축소미리보기재도입, 백엔드stale서버]
---

# 통합 테스트 결과 — srm 그리드 폼 빌더 4차: 축소 미리보기 재도입·DnD 배치·높이 고정·정규식 text 전용·라벨 태그 개편 (20260718-210534)

## 요약
- 총 19건 · 성공 18 · 실패 1

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-GF4-B01 | PASS | BE `./gradlew build` BUILD SUCCESSFUL(2m), FE `npm run build`(tsc -b && vite build) 성공 | - |
| TC-GF4-A01 | PASS | 신규 카탈로그 항목 "GF4 배치·라벨 테스트" 생성 후, "Form 설정" 버튼 아래 필드 없을 때 "설정된 양식이 없습니다..." 안내 문구, 적용 후에는 실제 축소 렌더링(모든 컨트롤 `disabled`)이 표시됨. 클릭 시 Form 설정 팝업 재오픈 확인 | shots/gf4_a1_a2_form_field_area.png, shots/gf4_a1_preview_after_apply.png |
| TC-GF4-A02 | PASS | "양식 필드" 라벨과 "Form 설정" 버튼 사이 간격이 "이름"/"설명" 등 다른 필드 라벨-입력 간격보다 육안으로 넓음, 좌측 정렬 유지 | shots/gf4_a1_a2_form_field_area.png |
| TC-GF4-B01a | PASS | 팔레트 9종(텍스트/여러 줄 텍스트/선택(Select)/라디오/체크박스/날짜/파일/안내 텍스트/가이드 파일)만 존재, `label` 항목 없음. 클릭 시 첫 빈 칸 자동 배치 확인 | shots/gf4_builder_initial.png, shots/gf4_click_place_text.png |
| TC-GF4-B01b | PASS | 팔레트 "선택(Select)"을 캔버스 특정 위치(4열 2행)로 드래그→드롭 시 정확히 그 위치에 배치됨(스냅 미리보기 포함) | shots/gf4_dnd_select_placed.png |
| TC-GF4-B01c | PASS | 이미 배치된 select 위치로 "라디오"를 드래그 드롭 시 배치 차단(컴포넌트 개수 불변), 드롭 직후 DOM에 겹침 경고 텍스트("겹칩") 존재 확인 | shots/gf4_dnd_overlap_block.png, shots/gf4_dnd_overlap_warning.png |
| TC-GF4-B01d | PASS | 팔레트 "파일"을 캔버스 바깥(팝업 상단 영역)에 드롭 시 배치 취소(컴포넌트 개수 불변) | shots/gf4_dnd_outside_cancel.png |
| TC-GF4-B01e | **FAIL** | **드래그 배치 직후(300ms 이내) 같은 유형 팔레트 버튼을 클릭하면 컴포넌트가 추가되지 않음.** 코드로 재현·확인: `dynamic-form-builder.tsx`의 `startPaletteDrag`(L282~333)가 드래그 종료 시 `justDraggedRef.current[type]=true`를 설정하고 `JUST_DRAGGED_RESET_MS=300`(L89) 후에만 리셋하는데, `handlePaletteClick`(L335~341)은 이 플래그가 true면 클릭을 무조건 무시(`return`)한다. 이 가드는 "드래그의 pointerup이 우연히 팔레트 버튼 위에서 끝나 발생하는 의도치 않은 click 이벤트"만 걸러야 하는데, 실제로는 **캔버스 안쪽으로 드래그해 드롭한 뒤(pointerup이 팔레트 버튼과 무관한 위치에서 발생) 사용자가 완전히 새로 의도적으로 클릭한 이벤트까지 300ms 동안 통째로 무시**한다. 재현: 팔레트 "체크박스"를 캔버스로 드래그 배치(성공, count 4→5) 직후 지연 없이 같은 "체크박스" 버튼을 클릭 → count 5(불변, 무시됨). 동일 조작을 600ms 지연 후 수행("날짜") → count 6→7(정상 추가). dev-lead가 사전에 지목한 "드래그 후 즉시 클릭 시 씹히는지" 우려가 실제로 재현됨 | shots/gf4_drag_then_click_radio.png(참고, 첫 관찰 당시엔 radio 옵션 input 개수로 오판했던 시행착오 포함 — 최종 확정은 grid 자식 엘리먼트 개수 비교로 재검증) |
| TC-GF4-B02 | PASS | text(1×1 리사이즈 시도해도 span 1 유지), radio(1→2행 리사이즈 성공, 3행 시도는 차단되어 2 유지), textarea(격리된 위치에서 2×2→5행까지 확장 성공, 무제한 확인), select(2행 시도해도 span 1 유지) 각각 코드(`gridMaxHeight`, form-schema.ts L58-62)와 실제 리사이즈 동작 일치 확인. (최초 textarea 확장 시도가 실패했던 것은 select와 겹쳐 `hasOverlap` 차단 때문 — 실제 버그 아님, 격리된 위치에서 재확인해 정상 확인) | - |
| TC-GF4-B03 | PASS | text 컴포넌트 Content 설정 팝업에 "Validation 정규식(선택)" 입력 노출. select/radio 팝업에는 해당 입력 없음(옵션/배치방향/여백만). text에 정규식 `^[0-9]+$`+필수 체크 후 요청 제출 폼에서: 빈 값 제출→"필수 항목을 입력하세요." 1건, "abc" 입력 제출→"입력 형식이 올바르지 않습니다." 1건, "123"으로 수정 제출→정상 접수(SRM-2026-0040) | - |
| TC-GF4-B03b | PASS(재기동 후) | API로 select 컴포넌트(`field_2`)의 `validation.regex`를 `^NOMATCH_PATTERN$`으로 직접 주입 후 값 "옵션1"로 제출 → 재기동 전 백엔드(구 코드 적재 상태)에서는 400(`FORM_FIELD_INVALID`)이 발생했으나, 이는 **테스트 환경 결함**(아래 "테스트 환경 이슈" 참고)으로 확인되어 백엔드 재기동 후 재검증 시 201 정상 접수(SRM-2026-0041) — `FormSubmissionValidator.java` L60의 `"text".equals(type)` 가드가 정상 동작함을 확인 | - |
| TC-GF4-B04 | PASS | Content 설정 팝업의 정렬 UI가 기존 가로 3버튼이 아니라 "left-top~right-bottom" 9개 버튼의 3×3 위젯 하나로 통일됨. text 컴포넌트에 "right-bottom" 적용 → 캔버스 카드 내부 input이 셀의 우측-하단으로 이동해 즉시 반영 확인, 이후 실제 요청 제출 폼(SCR-SRM-002)에도 동일하게 우측-하단 정렬로 렌더링됨을 확인 | shots/gf4_text_anchor_rightbottom.png, shots/gf4_request_submit_render.png |
| TC-GF4-B05 | PASS | 팔레트 목록에 `label` 항목 없음(TC-GF4-B01a와 동일 관찰) | shots/gf4_builder_initial.png |
| TC-GF4-B06 | PASS | 팔레트 최상단 "라벨 추가" 버튼 클릭 → 텍스트/글자색/테두리색 입력 미니 팝업 → "중요" 저장 → 팝업 타이틀-팔레트 사이 스트립에 색상 반영된 칩으로 즉시 표시 | shots/gf4_label_chip_created.png |
| TC-GF4-B07 | PASS | 칩 "중요" 클릭 → 값 프리필된 "라벨 수정" 팝업 재오픈 → "긴급"으로 변경 저장 → 칩 텍스트 갱신 확인. 이후 text/select 컴포넌트에 "긴급" 지정한 상태에서 칩의 ×(라벨 삭제) 클릭 → 칩 사라짐, 캔버스 경계 테두리도 즉시 사라짐, text 컴포넌트는 삭제되지 않고 그대로 유지되며 재오픈한 Content 설정의 "라벨(태그)" 값이 "없음"으로 해제됨 확인 | shots/gf4_label_deleted_border_gone.png |
| TC-GF4-B08 | PASS | text/select 컴포넌트 Content 설정 팝업의 "라벨(태그)" Select에 생성된 라벨과 "없음"이 후보로 노출, 선택 후 재오픈해도 값 유지 확인 | - |
| TC-GF4-B09 | PASS | text(1개)에만 "긴급" 지정 시 캔버스에 테두리 없음 → select에도 같은 라벨 지정(2개 참조)하자 두 컴포넌트를 포함하는 사각형 테두리가 즉시 오버레이로 표시됨(중간의 다른 컴포넌트 포함 허용 확인). select를 다른 위치로 이동 → 테두리 즉시 재계산되어 새 위치까지 확장됨. 라벨 삭제(참조 0개화) → 테두리 즉시 사라짐. Content 설정 개별 미리보기·A1 축소 미리보기·요청 제출 폼(SCR-SRM-002) 렌더링에는 이 오버레이가 전혀 나타나지 않음(아키텍처상 별도 오버레이 레이어라 `GridComponentBody` 재사용 화면에는 애초에 렌더링되지 않음) | shots/gf4_label_1ref_no_border.png, shots/gf4_label_2ref_border.png, shots/gf4_label_border_recalc_after_move.png, shots/gf4_label_deleted_border_gone.png |
| TC-GF4-DB01 | PASS | 과거 label 타입을 포함했을 것으로 추정되는 "라벨아이콘테스트"(id=10) 조회 결과 `type:"label"` 컴포넌트 없음·`labels:[]`로 정상 상태(마이그레이션 SQL `39_srm_form_schema_label_tag_reset.sql`의 조건부 리셋 로직 검토 결과 `jsonb_array_elements`+`EXISTS`로 정확히 `type='label'` 포함 로우만 판별해 리셋, 미사용 로우는 미대상). 신규 생성한 "GF4 배치·라벨 테스트" 카탈로그 항목은 처음부터 `{"components":[],"labels":[]}` 기본값으로 시작함을 확인(컬럼 기본값 갱신 확인) | - |
| TC-GF4-R01 | PASS | 요청 제출 폼(SCR-SRM-002)에서 text/select/radio×2/checkbox/date×2/textarea×2/file/guide-text/guide-file 12개 컴포넌트 모두 실제 렌더링 정상(placeholder, guide-text 안내아이콘+텍스트, guide-file "첨부된 파일이 없습니다.", date/file 입력박스+우측 아이콘). 제출 시 순차 단일 오류(필수→형식 순으로 한 번에 1건씩 표시) 정상. 빌더 팝업 캔버스=미리보기 통합(카드가 실제 렌더링 모습, hover 시 설정/삭제 오버레이) 이번 세션 전체에서 정상 동작(회귀 없음) | shots/gf4_request_submit_render.png |

## 테스트 환경 이슈(도메인 결함 아님)

TC-GF4-B03b 최초 시도 시 400(`FORM_FIELD_INVALID`)이 발생해 서버가 여전히 비-text 타입의 regex를 평가하는 것처럼 보였으나, 원인 조사 결과 **실행 중이던 백엔드 프로세스(PID 32164, 기동 시각 15:04:36)가 이번 4차 유지보수 코드 변경(`FormSubmissionValidator.java` 최종 수정 20:22:39)보다 먼저 기동되어 있던 stale 프로세스**였음을 확인(코드 자체는 `"text".equals(type)` 가드가 정상 작성되어 있었음 — `source/backend/src/main/java/com/itsm/common/form/FormSubmissionValidator.java` L60). 백엔드를 재기동(`./gradlew bootRun`)한 뒤 동일 시나리오 재실행 시 201로 정상 접수되어, **실제 배포/정상 기동 환경에서는 결함이 아님**을 확인했다. dev-lead는 이번 유지보수 커밋 반영 후 실서비스/스테이징 환경에서 백엔드가 최신 빌드로 재기동됐는지만 확인하면 된다(코드 수정 불필요).

## 테스트 데이터 처리
- 신규 카탈로그 항목 "GF4 배치·라벨 테스트"(id=14) 생성 및 저장(양식 12개 컴포넌트, 라벨은 최종적으로 삭제해 0개 상태로 저장됨).
- 이 항목으로 서비스 요청 2건 생성: SRM-2026-0040(정상 접수 검증용), SRM-2026-0041(서버 regex 무시 검증용, API 직접 호출로 생성).
- API 직접 호출(PATCH)로 select 컴포넌트(`field_2`)에 `validation.regex="^NOMATCH_PATTERN$"`를 임시 주입했던 것은 검증 목적이며, 이후 원복하지 않았으므로 이 항목을 재사용할 경우 유의(운영 데이터 아님, 통합테스트 전용 항목).
- 이 과정에서 백엔드 프로세스를 재기동함(기존 stale 프로세스 종료 후 `./gradlew bootRun`으로 재기동, 현재 정상 기동 상태).

## 실패 항목 분석
- **TC-GF4-B01e**: 드래그앤드롭으로 컴포넌트를 배치한 직후(300ms 이내) 같은 유형의 팔레트 버튼을 클릭하면 컴포넌트가 추가되지 않는다.
  - **원인**: `source/frontend/src/components/common/dynamic-form-builder.tsx`
    - L89 `JUST_DRAGGED_RESET_MS = 300`
    - L282~333 `startPaletteDrag`: 드래그가 실제로 발생했으면(`dragging=true`) `onUp`에서 `justDraggedRef.current[type] = true`로 설정하고, `setTimeout(..., 300)`으로만 다시 `false`로 되돌림.
    - L335~341 `handlePaletteClick`: `justDraggedRef.current[type]`이 true면 그 값을 `false`로 되돌리기만 하고 `handleAddComponent`를 호출하지 않은 채 `return`.
  - 이 가드는 드래그 종료 시 브라우저가 우연히 같은 팔레트 버튼 위에서 `click` 이벤트를 합성 발생시키는 경우(드래그 시작 지점=버튼, 드롭도 그 근처)만 걸러내야 하는데, **드롭 위치가 캔버스 안쪽으로 완전히 이동한 경우에도 300ms 동안 그 타입의 팔레트 버튼 클릭 전체를 무시**해 사용자가 이어서 같은 유형을 빠르게 한 번 더 추가하려는 정당한 클릭까지 막는다.
  - **재현 조건**: 드래그로 컴포넌트 1개 배치 → 300ms 이내 동일 유형 팔레트 버튼 클릭 → 무시됨(2번째 컴포넌트 미추가). 600ms 이상 대기 후 클릭하면 정상 추가됨.
  - **관련 요구사항**: `docs/03_develop/plan/service-request.md` B1(팔레트 클릭+드래그앤드롭 병행 지원), dev-lead 사전 지정 확인 포인트("드래그로 배치한 직후, 같은 타입 팔레트 버튼을 다시 클릭했을 때 정상적으로 컴포넌트가 추가되는지 반드시 확인").
  - **제안(참고용, 최종 수정은 dev-ui 판단)**: `justDraggedRef`를 타입별이 아니라 "드래그 종료 시 pointerup의 대상 엘리먼트가 시작 버튼과 동일한 경우"에만 클릭을 억제하도록 판정 기준을 좁히거나, 실제 click 이벤트의 `event.target`이 드래그를 시작한 그 버튼 엘리먼트와 동일한지 확인하는 방식으로 변경.
