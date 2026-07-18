---
date: 20260718-184021
domain: srm
change_type: [new, modified, removed]
keywords: [그리드 빌더 팝업 3분할 미리보기, radio/checkbox 배치·여백, guide-text/guide-file 분리]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260718-184021 · 도메인: srm

## 1. 요구사항

직전 유지보수(20260718-175042, guide 컴포넌트 신규·1×1 캡션 아이콘 전환·Content 설정 팝업 확대)의 후속 개선 요청이다.
세팅 화면에 나타나던 미리보기를, 레이아웃을 배치하는 팝업 안에서 바로 볼 수 있도록 수정한다.
배치한 컴포넌트가 모두 보이는 환경에서 확인하기 위함이며, 이를 위해 팝업 크기도 키운다.
radio나 checkbox는 가로 배치 또는 세로 배치를 선택할 수 있도록 하고, 각 option 간 여백 단계를 3단계로 둬서 선택하도록 한다.
기본값은 가로 배치, 여백 1단계다.
guide text와 guide file을 별개의 컴포넌트로 분리한다.

## 2. 해결 방법

### DB

변경 없음.

### BE

`common.form.FormSubmissionValidator`를 수정했다.
기존 guide 타입 스킵 방어 코드를 제거하고, 신규 guide-text/guide-file 두 타입을 제출/검증 대상에서 제외하도록 갱신했다.
관련 테스트를 함께 갱신했다.

### FE

`form-schema.ts`의 guide 컴포넌트 타입을 완전히 제거하고, guide-text(안내 텍스트, label과 동일 구조)/guide-file(첨부 파일만) 두 개의 정적 컴포넌트 타입으로 분리했다.
팔레트 노출 종류를 9종에서 10종으로 갱신했다.
radio/checkbox 전용 옵션으로 `optionsDirection`(row/column, 기본 row)과 `optionsGap`(1~3단계, 기본 1)을 신규 추가했다.
`dynamic-form-builder.tsx`의 Content 설정 팝업에 guide-text/guide-file 각각의 설정 UI(guide-text는 안내 텍스트, guide-file은 첨부 파일 업로드/다운로드)와 radio/checkbox 전용 배치 방향·여백 단계 선택 UI를 추가했다.
`dynamic-form-renderer.tsx`의 radio/checkbox 렌더링을 세로 고정에서 `optionsDirection`/`optionsGap` 값에 따라 렌더링하도록 갱신했다(기존 세로 고정 기본값이 가로로 바뀌는 의도된 변경, 마이그레이션 없이 미지정 시 폴백 기본값 적용).
guide-text/guide-file 렌더링을 각각 분리해 추가하고, 기존 guide 타입 렌더링 로직은 제거했다.
CatalogManagePage(SCR-SRM-007)에 있던 45% 축소 pre-view를 제거하고, "Form 설정" 팝업 내부를 좌 팔레트/중앙 캔버스/우 전체 레이아웃 미리보기 3분할 동시 표시로 통합했다(탭 전환 아님, 편집 중 실시간 반영).
팝업 모달 폭을 `w-[90vw] max-w-[1600px]`로 확대했다.
`index.ts` 배럴 export를 갱신했다.
i18n 리소스(`i18n/locales/ko,en/service-request.json`)를 갱신했다.

### 코드 리뷰

Standards축은 diff를 직접 정독해 발견 사항이 없었다(직전 건에서 지적했던 배럴 export 누락 등의 패턴이 이번엔 처음부터 반영됨).
Spec축은 설계 3건 요구사항 전부 부합했고 누락·범위초과는 없었다.

## 3. 변경 파일

- `source/frontend/src/components/common/form-schema.ts`
- `source/frontend/src/components/common/dynamic-form-builder.tsx`
- `source/frontend/src/components/common/dynamic-form-renderer.tsx`
- `source/frontend/src/components/common/index.ts`
- `source/frontend/src/features/service-request/CatalogManagePage.tsx`
- `source/frontend/src/i18n/locales/ko/service-request.json`
- `source/frontend/src/i18n/locales/en/service-request.json`
- `source/backend/src/main/java/com/itsm/common/form/FormSubmissionValidator.java`(+test)

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260718-182813/srm/result/srm.md`)는 12건 전부 PASS했다.
그리드 빌더 팝업 3분할 동시 표시 및 편집 중 실시간 반영, radio/checkbox 가로/세로 배치·여백 3단계 동작(기본값 가로/1단계 포함), guide-text/guide-file 분리 배치·설정·렌더링을 검증했다.
guide 타입 완전 제거에 따른 회귀는 없었다(운영 데이터 없어 마이그레이션 대상 없음).
커밋 `c5232d5`로 origin/main에 push 완료됐다.
