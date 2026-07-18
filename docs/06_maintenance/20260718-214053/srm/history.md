---
date: 20260718-214053
domain: srm
change_type: [new, modified, removed]
keywords: [카탈로그 관리 화면 축소 미리보기, 드래그앤드롭 배치, 정규식 text 한정, 9방향 정렬, 라벨 태그 개편]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260718-214053 · 도메인: srm

## 1. 요구사항

직전 유지보수(20260718-192613, 캔버스=미리보기 통합 정정)의 후속 요청 11건이다.
두 화면 영역으로 나뉜다.

[서비스 카탈로그 관리 화면 — CatalogManagePage]
배치한 컴포넌트 UI를 축소된 버전으로 보는 기능을 추가한다(A1).
양식 필드 text와 "Form 설정" 버튼 사이 여백을 늘린다(A2, left 정렬 유지).

[Form custom 팝업 — DynamicFormBuilder]
왼쪽 컴포넌트를 드래그해서 배치 layout에 넣는 기능을 추가한다(B1).
text/date/file/select/guide-file 컴포넌트는 높이 1(그리드 1행)로 제한한다(B2).
text 컴포넌트에 한해 입력값 정규식 제한 기능을 둔다(B3).
컴포넌트 내용 정렬을 기존 좌우(가로)뿐 아니라 세로도 추가해서 9등분(좌상단~우하단 3×3) 정렬로 확장한다(B4).
라벨을 컴포넌트에 "부여"하는 방식으로 바꾼다(B5).
라벨 하나가 컴포넌트 1개에만 부여되면 테두리 없음, 여러 컴포넌트에 부여되면 그 컴포넌트들의 topX/topY/bottomX/bottomY를 계산해 bounding box 테두리를 표시한다(테두리 색상 코드 지정 가능).
기존 팔레트의 "라벨" 컴포넌트 항목을 없애고, 좌측 컴포넌트 목록 최상단에 "라벨 추가" 버튼으로 대체한다(B6).
"라벨 추가" 버튼 클릭 시 미니 팝업을 열어 라벨 텍스트 입력 + 글자색/테두리색 지정(색상코드 또는 color input)을 받는다(B7).
추가한 라벨은 "Form 설정" subtitle과 "컴포넌트 목록+레이아웃 배치 박스" 사이 영역에 표시한다(rounded-square tint 디자인, 텍스트는 글자색, 테두리는 테두리색)(B8).
배치한 컴포넌트의 설정 미니 팝업에서 그 컴포넌트에 라벨을 지정할 수 있게 한다(B9).

## 2. 해결 방법

### DB

`source/db/sql/39_srm_form_schema_label_tag_reset.sql`을 신규 추가했다.
`service_catalog_item.form_schema` 컬럼 기본값을 라벨 태그 개편 스키마 기준으로 갱신했다.
`type:"label"`을 포함한 기존 form_schema 로우를 조건부로 리셋했다(과거 form.io 제거 선례와 동일하게, 그리드 배치형 label 컴포넌트가 완전 폐기되며 구조 비호환이라 마이그레이션 대상 없음).

### BE

`common.form.FormSubmissionValidator`를 수정했다.
정규식(regex) 검증을 `type=text`에만 적용하도록 축소했다(나머지 6종은 값이 있어도 정규식을 평가하지 않음).
관련 테스트를 갱신했다.

### FE

`form-schema.ts`에 최상위 `labels` 배열(id/text/textColor/borderColor)과 컴포넌트별 `labelId` 참조(최대 1개) 필드를 신설하고, 그리드 배치형 `GridLabelComponent`(팔레트 "라벨" 항목)를 완전히 폐기했다.
`gridMaxHeight`를 세분화했다 — text/date/file/select/guide-file은 1칸 고정, radio/checkbox/guide-text는 1~2칸, textarea는 무제한(기존에 2칸으로 저장된 데이터는 강제 축소하지 않음).
`GridAlign`에 가로 정렬과 별개로 `verticalAlign`(input 컴포넌트, 기본 top)/`textVerticalAlign`(guide-text, 기본 top)을 신규 추가해 9방향(3×3) 정렬을 지원하고, 3×3 그리드 앵커 위젯으로 UI를 통일했다.
`dynamic-form-builder.tsx`의 팔레트에서 "라벨" 항목을 제거하고 최상단에 "라벨 추가" 버튼을 추가했다.
"라벨 추가" 클릭 시 라벨 텍스트+글자색+테두리색을 입력받는 미니 팝업을 신규 추가했다.
생성된 라벨은 "Form 설정" subtitle과 팔레트+캔버스 영역 사이에 칩 스트립으로 표시했다(rounded-square tint, 텍스트는 글자색·테두리는 테두리색, 클릭 시 수정 팝업 재오픈, 삭제 시 참조하던 컴포넌트의 `labelId`도 함께 해제).
Content 설정 팝업에 라벨 지정 Select를 추가해 컴포넌트별로 라벨 1개를 지정할 수 있게 했다.
캔버스에 라벨을 참조하는 컴포넌트가 2개 이상이면, 그 컴포넌트들의 topX/topY/bottomX/bottomY로 계산한 사각형 전체를 경계 테두리 오버레이로 표시했다(사각형 안에 라벨 미부여 셀이나 다른 컴포넌트가 있어도 시각적으로 포함되는 것을 허용, 1개만 참조하면 테두리 없음).
팔레트 클릭 배치(기존)에 더해 드래그앤드롭으로 캔버스에 배치하는 기능을 병행 지원하도록 추가했다.
`RequestSubmitPage.tsx`의 타입 호환 문제를 함께 수정했다.
`dynamic-form-renderer.tsx`에 9방향 정렬 렌더링을 반영했다.
CatalogManagePage(SCR-SRM-007)의 "양식 필드" 편집 폼 화면 자체에 축소 미리보기(`disabled`+`hideFooter` 렌더러)를 재도입했다(빌더 팝업 내부의 "캔버스=미리보기 통합"과는 별개 화면).
"양식 필드" 라벨과 "Form 설정" 버튼 사이 여백을 확대했다(좌측 정렬 유지).
`index.ts` 배럴 export를 갱신했다.

### 코드 리뷰 중 발견·수정한 결함

Standards축 수동 리뷰에서 드래그앤드롭 배치 후 클릭 배치가 무시되는 버그 1건을 발견했다.
드래그 종료 시 `justDraggedRef` 플래그가 리셋되지 않아 다음 정상 클릭이 무시되던 문제로, 타임아웃 방식(300ms→0ms)으로 수정해 재확인을 완료했다.
Spec축은 설계 A1/A2/B1~B9 전부 부합했고 누락·범위초과는 없었다.

## 3. 변경 파일

- `source/frontend/src/components/common/form-schema.ts`
- `source/frontend/src/components/common/dynamic-form-builder.tsx`
- `source/frontend/src/components/common/dynamic-form-renderer.tsx`
- `source/frontend/src/components/common/index.ts`
- `source/frontend/src/features/service-request/CatalogManagePage.tsx`
- `source/frontend/src/features/service-request/RequestSubmitPage.tsx`
- `source/backend/src/main/java/com/itsm/common/form/FormSubmissionValidator.java`(+test)
- `source/db/sql/39_srm_form_schema_label_tag_reset.sql`

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260718-210534/srm/`, 재테스트 `docs/04_test/20260718-213750/srm/`)는 19건 중 1차 18건 PASS/1건 FAIL(드래그앤드롭 후 클릭 무시 버그) 후 원인 수정, 재테스트에서 전체 PASS했다.
CatalogManagePage 축소 미리보기, 여백 확대, 드래그앤드롭+클릭 배치 병행, 컴포넌트별 높이 상한 세분화, text 전용 정규식 검증, 9방향 정렬, 라벨 태그 개편(라벨 추가·칩 표시·컴포넌트 지정·2개 이상 참조 시 경계 테두리) 동작을 검증했다.
기존 캔버스=미리보기 통합, radio/checkbox 배치방향·여백, guide-text/guide-file 분리 등 이전 건의 변경 사항은 회귀 없이 그대로 유지됐다.
커밋 `f650e71`로 origin/main에 push 완료됐다.
