---
date: 20260718-151522
domain: srm
change_type: [new, modified]
keywords: [유효성 첫 위반 1건 순차 표시, label 독립 컴포넌트 분리, 날짜/파일 아이콘 전용 표시, FormSubmissionValidator label 스킵]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260718-151522 · 도메인: srm

## 1. 요구사항

직전 유지보수(20260718-141927, form.io 완전 제거 → 자체 8×n 그리드)의 후속 개선 요청이다.
유효성 실패 메시지를 요청 양식 하단 공통 위치에 표시한다.
한 번에 여러 개를 표시하지 않고 `components` 배열 순서상 첫 번째 위반 1건만 표시한다.
제출할 때마다 순서대로 다시 검증해 다음 오류로 순차 진행하며, 실시간(입력 중) 검증은 아니다.
date input은 달력 아이콘만 표시하고, file input은 파일 아이콘만 표시한다.
선택된 날짜/파일명은 아이콘 옆 텍스트로 표시하고, 아이콘 클릭 시 네이티브 date picker/파일 선택 다이얼로그를 그대로 연다.
label을 입력 컴포넌트에서 완전히 분리해 독립된 팔레트 컴포넌트로 신규 추가한다.
label 컴포넌트는 값 입력이 없는 정적 텍스트이며, 입력 컴포넌트와는 순수하게 시각적으로 인접 배치될 뿐 `for`/`aria-label` 연결은 두지 않는다.
기존 입력 컴포넌트 7종이 갖고 있던 `label`/`labelAlign` 속성은 완전히 제거한다.

## 2. 해결 방법

### DB

컬럼 타입·기본값 등 DB 변경은 없다.
form_schema는 JSON 내부 구조만 조정되며, 이미 전 로우가 빈 그리드 상태라 백필 대상이 없다.

### BE

`common.form.FormSubmissionValidator`가 이미 components 배열을 순서대로 순회하며 첫 위반 즉시 예외를 던지는 구조라, "첫 위반 즉시 반환" 계약을 기존 코드가 이미 만족하고 있음을 확인했다.
`type=label` 컴포넌트를 검증 대상에서 제외하는 방어 코드를 추가했다.
label 스킵, 다중 위반 중 첫 번째만 반환하는 테스트 2건을 추가했다.

### FE

`form-schema.ts`의 `GridComponent`를 판별 유니온으로 재정의했다.
`GridInputComponent`(기존 7종, label/labelAlign 없음)와 `GridLabelComponent`(text/textAlign만 갖는 8번째 팔레트 항목)로 분리했다.
`dynamic-form-builder.tsx`에 label 팔레트 항목과 전용 Content 설정(표시 텍스트·정렬만)을 추가했다.
나머지 7종의 Content 설정에서 라벨 관련 필드를 제거했다.
`dynamic-form-renderer.tsx`에서 입력 컴포넌트의 `<label>` 캡션 렌더링을 제거했다.
`label` 타입의 정적 텍스트 렌더링을 신규 추가했다.
date/file을 아이콘 + 숨김 네이티브 input(`showPicker()`/`click()` 트리거) 방식으로 전환했다.
검증 표시를 필드별 errors map에서 단일 `formError` 문자열(배열 순서상 첫 위반 1건)로 교체했다.

## 3. 변경 파일

- `docs/02_plan/screen/service-request.md`
- `docs/02_plan/api_spec/service-request.md`
- `docs/02_plan/api_spec/common.md`
- `docs/02_plan/database/service-request.md`
- `source/backend/src/main/java/com/itsm/common/form/FormSubmissionValidator.java`
- `source/frontend/src/components/common/form-schema.ts`
- `source/frontend/src/components/common/dynamic-form-builder.tsx`
- `source/frontend/src/components/common/dynamic-form-renderer.tsx`

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260718-145952/srm/result/srm.md`)는 8건 전부 PASS했다.
팔레트 8종(label 포함) 배치, label 전용 설정, 나머지 7종에 라벨 필드가 없는지 확인, date/file 아이콘화 및 네이티브 피커 동작을 검증했다.
그리드 시각 위치가 아니라 배열 순서 기준으로 순차 단일 오류가 표시되는지 직접 구성한 케이스로 검증했다.
서버도 동일 계약(다중 위반 시 첫 번째만 400)임을 확인했다.
요청 처리함 카테고리 필터, 겹침 방지, pre-view 라운드트립 등 기존 기능의 회귀는 없었다.
커밋 `a88cae9`로 origin/main에 push 완료됐다.
