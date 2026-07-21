---
date: 20260721-163612
domain: common
change_type: [fixed]
keywords: [그리드 폼 빌더, radio, checkbox, align, justify-content, 가운데 정렬 결함]
---

# 유지보수 이력 — Common (그리드 폼 빌더 공용 렌더러)

> 유지보수 일시: 20260721-163612 · 도메인: common

## 1. 요구사항

서비스 카탈로그 관리 화면(SCR-SRM-007 Form 설정)에서 radio/checkbox 컴포넌트의 폭을 2칸으로 넓혀도 정렬(align=center)이 옵션 목록에 반영되지 않는 결함을 확인하고 수정한다.
사용자가 실제 화면(디자인 피드백 도구)에서 radio("남자/여자")·checkbox("옵션1/옵션2") 컴포넌트 스크린샷과 함께 "정렬은 가운데 정렬인데, width에서 가운데 정렬이 되지 않았다"고 보고했다.

## 2. 해결 방법

### 원인 분석

`GridComponentBody`(dynamic-form-renderer.tsx)는 각 컴포넌트를 `widthPercent`(기본 90%) 크기의 wrapper `<div>`로 감싸고, 이 wrapper 자체는 `align` 값에 따라 `mx-auto`/`ml-auto`/`mr-auto`로 가로 위치를 잡는다.
text/textarea/select/date/file은 내부 컨트롤이 wrapper 폭을 그대로 채우므로 wrapper의 margin 정렬만으로 시각적 정렬이 성립한다.
반면 radio/checkbox는 옵션들이 `flex flex-row flex-wrap`(또는 `flex-col`)로 나열되는데, 옵션 묶음 자체의 실제 너비가 wrapper보다 좁은 경우가 대부분이라 옵션 묶음에는 별도의 `justify-content`(row)/`align-items`(column) 지정이 없어 항상 왼쪽(또는 위쪽)에 붙어버렸다. 즉 wrapper 박스는 가운데로 이동해도, 그 안의 좁은 옵션 묶음은 정렬되지 않는 결함이었다.
이 결함은 컴포넌트 폭(w) 상한(1~2칸)과는 무관하며, radio/checkbox 두 타입 모두에서 폭·정렬값에 관계없이 항상 발생하는 문제였다(폭을 넓힐수록 wrapper와 옵션 묶음의 폭 차이가 커져 더 두드러지게 보였을 뿐).

### DB

변경 없음.

### BE

변경 없음.

### FE

`dynamic-form-renderer.tsx`의 `renderControl()` 중 radio/checkbox 분기를 수정했다.
- `OPTIONS_JUSTIFY_CLASS`(row 방향, `justify-start`/`justify-center`/`justify-end`)와 `OPTIONS_ITEMS_CLASS`(column 방향, `items-start`/`items-center`/`items-end`) 매핑을 신설했다(Tailwind JIT 스캔 대상이 되도록 `OPTIONS_GAP_CLASS`와 동일하게 리터럴 매핑 테이블로 작성, 템플릿 문자열로 클래스명을 조립하지 않음).
- radio/checkbox 옵션 컨테이너의 `className`에 `component.input?.align ?? "center"` 값에 따라 위 매핑을 추가로 적용했다 — `optionsDirection`이 `row`(가로 배치)면 주축 정렬(`justify-content`), `column`(세로 배치)이면 교차축 정렬(`align-items`)로 반영된다.
- `GridComponentBody`는 빌더 캔버스(`dynamic-form-builder.tsx`의 `BuilderComponentCard`)와 요청 제출 폼이 공유하므로, 이 한 곳의 수정으로 두 화면 모두에 반영된다(로직 중복 없음).

## 3. 변경 파일

- `source/frontend/src/components/common/dynamic-form-renderer.tsx`

## 4. 테스트 결과

- `tsc -b --noEmit` 통과(타입 오류 없음).
- 실행 중인 dev 서버에서 실제 카탈로그 항목("전체타입테스트")의 Form 설정 팝업을 열어 DOM을 직접 확인 — radio/checkbox 옵션 행에 `justify-center` 클래스가 정상 적용됨을 확인했다(HMR 즉시 반영, 실 데이터는 "취소"로 닫아 변경하지 않음).
- 별도 통합 테스트(tester 에이전트)는 수행하지 않았다 — 사용자가 실제 화면에서 직접 피드백을 준 결함을 Main이 코드 리뷰 + Playwright 실기동 검증으로 원인 확인 후 즉시 수정한 경량 유지보수 건이다.
