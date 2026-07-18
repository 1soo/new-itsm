---
date: 20260718-175042
domain: srm
change_type: [new, modified]
keywords: [placeholder, guide 컴포넌트 신규, 1×1 캡션 아이콘 전환, Content 설정 개별 미리보기, date/file 입력박스 롤백]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260718-175042 · 도메인: srm

## 1. 요구사항

직전 유지보수(20260718-151522, label 컴포넌트 분리·date/file 아이콘 전용화)의 후속 개선 요청이다.
input에 placeholder를 추가한다.
안내사항 텍스트나 가이드 파일을 첨부할 수 있는 컴포넌트를 신규로 둔다.
파일은 컴포넌트 설정 시 업로드하고, 요청 양식에서는 다운로드할 수 있어야 한다.
빌더 캔버스에서 카드가 1×1 크기일 때 컴포넌트명이 잘려 보이는 문제를 해결한다.
아이콘만 표시해도 되고, 팝업 크기를 늘려 텍스트가 안 잘리게 해도 된다.
컴포넌트 설정 시 팝업에서 실제로 어떻게 렌더링되는지도 나타나야 한다.
현재는 어떤 컴포넌트인지만 나타난다.
달력 input과 파일 input은 입력 박스가 길게 있고, 그 박스 우측 끝에 아이콘이 위치하도록 되돌린다.
처음엔 입력 박스가 비어있고, 값을 입력하면 달력은 날짜(yyyy-MM-dd), 파일은 파일명을 박스 안에 나타낸다.

## 2. 해결 방법

### DB

변경 없음.
form_schema는 JSONB 컬럼 내부 구조만 확장되며 별도 마이그레이션은 없다.

### BE

`common.form.FormSubmissionValidator`를 수정했다.
신규 9번째 팔레트 컴포넌트(guide)를 제출/검증 대상에서 제외하도록 방어 코드를 추가했다.
관련 테스트를 추가했다.

### FE

`form-schema.ts`에 7개 입력 컴포넌트 공통 `placeholder` 필드를 추가했다.
`GridComponentType`에 `guide`를 추가하고, label과 마찬가지로 값 입력이 없는 정적 컴포넌트로 정의했다.
guide 컴포넌트는 안내 텍스트와 첨부 파일(base64 인라인)을 갖는다.
`dynamic-form-builder.tsx`에 guide 팔레트 항목(9번째)과 전용 Content 설정(안내 텍스트, 파일 업로드)을 추가했다.
placeholder는 text/textarea/select/date/file 5종의 Content 설정 UI에만 노출하고, radio/checkbox는 제외했다.
빌더 캔버스 카드가 1×1 크기일 때 캡션을 텍스트 대신 아이콘 전용으로 표시하도록 수정했다.
Content 설정 팝업 폭을 320px에서 420px로 확대했다.
Content 설정 팝업 내부에 편집 중인 컴포넌트 단독의 실시간 렌더링 미리보기를 신규로 추가했다(렌더러 로직을 재사용, 별도 구현 없음).
`dynamic-form-renderer.tsx`에서 date/file의 "아이콘 전용 표시"(직전 유지보수 20260718-151522, a88cae9)를 롤백했다.
입력 박스(길게)를 부활시키고 박스 우측 끝에 아이콘을 배치하는 형태로 변경했다.
박스가 비어있으면 빈 상태로, 값이 있으면 박스 안에 날짜(yyyy-MM-dd)/파일명을 텍스트로 표시한다.
박스 전체를 클릭 영역으로 두어 클릭 시 네이티브 date picker/파일 선택 다이얼로그를 연다.
label 컴포넌트 분리, 유효성 실패 첫 1건 순차 표시 등 직전 유지보수의 나머지 변경 사항은 그대로 유지했다.
`index.ts` 배럴 export를 정리했다.

### 코드 리뷰 중 발견·수정한 결함

Standards축 수동 리뷰에서 3건을 발견해 담당 개발 에이전트가 수정했다.
label/guide 셀의 중복 패딩을 정리했다.
options 메모이제이션 누락을 수정했다.
`index.ts` 배럴 export 누락을 추가했다.
Spec축은 설계 5건 요구사항 전부 부합했고 누락·범위초과는 없었다.

## 3. 변경 파일

- `source/frontend/src/components/common/form-schema.ts`
- `source/frontend/src/components/common/dynamic-form-builder.tsx`
- `source/frontend/src/components/common/dynamic-form-renderer.tsx`
- `source/frontend/src/components/common/index.ts`
- `source/backend/src/main/java/com/itsm/common/form/FormSubmissionValidator.java`(+test)

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260718-172626/srm/result/srm.md`)는 11건 전부 PASS했다.
placeholder 노출 범위(5종만), guide 컴포넌트 배치·안내텍스트·파일 업로드/다운로드, 1×1 캡션 아이콘 전환, Content 설정 팝업 확대 및 개별 미리보기, date/file 입력박스+우측 아이콘 롤백 동작을 검증했다.
직전 유지보수의 label 분리·순차 단일 오류 표시 등 기존 기능의 회귀는 없었다.
커밋 `fa01413`으로 origin/main에 push 완료됐다.
