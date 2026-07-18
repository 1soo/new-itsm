---
date: 20260719-082912
domain: srm
change_type: [new, modified, removed]
keywords: [라벨 그룹 전 화면 확대, default placeholder 제거, 기본값 UI 타입 동일화, 타이틀 옆 체크박스, form io i18n]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260719-082912 · 도메인: srm

## 1. 요구사항

직전 유지보수(20260719-001132, 미니팝업 위치·순서 보정 7차)에 이은 후속 요청 5건이다.
Form 설정 팝업에선 라벨이 뜨는데, 양식 필드에서 축소된 버전으로 볼 때 라벨이 안 보인다.
축소된 버전에서도 라벨이 보이도록 수정한다.
모든 input의 placeholder를 default로 설정하지 않는다.
날짜/파일 input에 "희망일자 입력", "파일을 선택하세요" 같은 placeholder가 미리 박혀 있는데, 이를 미리 설정해두지 않고 사용자가 직접 입력하도록 한다.
각 컴포넌트별 input의 기본값 설정 시 그에 맞는 input 타입으로 변경한다.
선택/라디오/체크박스는 설정한 옵션 중에서 고르는 input으로, 날짜는 date input으로, 파일은 기본값 없음으로 한다.
설정 팝업의 "읽기 전용"/"필수 여부" 체크박스 위치를 컴포넌트 설정 title 우측에 마진을 두고 배치한다.
form io 관련 파일들에 i18n을 적용한다.

## 2. 해결 방법

### DB

변경 없음.

### BE

변경 없음.
form_schema는 JSONB opaque 컬럼이고 `FormSubmissionValidator`가 defaultValue를 참조하지 않아 코드 변경이 불필요함을 확인했다.

### FE

라벨(태그) 경계 그룹(테두리+legend) 표시를 빌더 캔버스 전용에서 `dynamic-form-renderer.tsx` 자체로 확대했다.
게이팅 없이 요청 제출 폼(SCR-SRM-002)과 A1 축소 미리보기(SCR-SRM-007) 둘 다에 노출되도록 했다.
`GridLabelOverlays`로 캔버스·렌더러가 동일한 라벨 그룹 렌더링 로직을 공유하도록 정리했다(중복 구현 방지).
date/file/select의 하드코딩 기본 placeholder 폴백("날짜를 선택하세요"/"파일을 선택하세요"/"선택")을 완전히 제거했다.
Content 설정 팝업의 기본값 설정 UI를 컴포넌트의 실제 입력 타입과 동일하게 바꿨다.
select/radio는 단일 선택(pill, 재클릭 시 해제)으로, checkbox는 다중 선택 체크박스 그룹으로, date는 네이티브 date input으로 바꾸고, file은 기본값 UI 자체를 제거했다.
`GridComponentInput.defaultValue` 타입을 `string`에서 `string | string[] | null`로 확장했다(checkbox만 배열, 나머지는 기존처럼 단일 문자열).
읽기전용·필수 여부 체크박스를 팝업 본문에서 Content 설정 팝업 타이틀("컴포넌트 설정") 우측으로 이동했다.
공용 `Modal`에 `titleExtra?: ReactNode` 신규 prop을 추가했다(미지정 시 기존 동작 그대로라 다른 Modal 사용처에는 영향이 없다).
`dynamic-form-builder.tsx`/`dynamic-form-renderer.tsx` 내부 UI 텍스트를 i18n 전환 대상에 포함했다(기존 "관리자 전용이라 범위 밖" 방침 해제, 기존 service-request 네임스페이스 재사용).

### 코드 리뷰 중 발견·수정한 결함

개발 중 dev-ui가 자체적으로 file 타입 "기본값" 라벨이 UI 제거 후에도 고아로 남아 표시되던 결함 1건을 발견해 수정했다.
Standards축 수동 리뷰에서는 diff를 직접 정독해 추가 발견 사항이 없었다.
Spec축은 설계 5건 요구사항 전부 부합했다.

## 3. 변경 파일

- `source/frontend/src/components/common/form-schema.ts`
- `source/frontend/src/components/common/modal.tsx`
- `source/frontend/src/components/common/dynamic-form-renderer.tsx`
- `source/frontend/src/components/common/dynamic-form-builder.tsx`
- `source/frontend/src/i18n/locales/ko/service-request.json`
- `source/frontend/src/i18n/locales/en/service-request.json`

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260719-081313/srm/result/srm.md`)는 7건 전부 PASS했다.
라벨 그룹이 요청 제출 폼과 A1 축소 미리보기에도 표시되는지, default placeholder 완전 제거, 기본값 UI 타입별 동일화(select/radio 단일·checkbox 다중·date input·file UI 없음), 타이틀 옆 체크박스 배치, form io 파일 i18n 전환을 검증했다.
직전 1~7차 건(캔버스=미리보기 통합, 라벨 태그 개편, 미니팝업 위치 등)의 나머지 기능은 회귀 없이 그대로 유지됐다.
커밋 `9fdabd1`로 origin/main에 push 완료됐다.
