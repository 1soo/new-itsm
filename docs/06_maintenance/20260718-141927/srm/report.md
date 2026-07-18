---
date: 20260718-141927
domain: srm
change_type: [new, removed, modified]
keywords: [8×n 그리드 드래그앤드롭 빌더, form.io(@formio) 완전 제거, FormSubmissionValidator required/regex 재작성, file 컴포넌트 base64 변환 결함 수정]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260718-141927 · 도메인: srm

## 1. 요구사항

서비스 카탈로그의 동적 양식 빌더에서 form.io(`@formio/js`, `@formio/react`) 라이브러리를 완전히 제거한다.
좌측 컴포넌트 팔레트, 우측 8칸 고정×n행(스크롤 가능) 그리드 캔버스로 구성된 자체 드래그앤드롭 폼 빌더로 전면 재구현한다.
팔레트는 text(input)/textarea/select/radio/checkbox/date/file 7종만 지원한다.
컴포넌트는 드래그로 배치하고 모서리를 잡아당겨 1칸 단위로 크기를 조절할 수 있다.
크기는 폭·높이 각각 1~2칸 범위로 제한하되, textarea는 높이 제약 예외(무제한)를 둔다.
이미 다른 컴포넌트가 배치된 위치에는 겹쳐 배치할 수 없고, 겹치면 이미 배치되어 있다고 안내한다.
배치한 컴포넌트에 마우스를 올리면 상단에 설정 버튼이 나타나고, 클릭하면 Content 설정 미니 팝업이 뜬다.
미니 팝업에서 label 정렬 위치, input 폭 %, 정렬 위치, 기본값, 읽기 전용(수정 가능) 여부, 필수 여부, 정규식(regex) validation을 설정한다.
select/radio/checkbox 등 옵션 제공 컴포넌트는 콤마(,) 구분 텍스트로 옵션을 설정하며, 향후 CI 연계 확장을 위한 라디오 버튼 자리만 마련한다(로직 미구현).
옵션 텍스트가 길거나 개수가 많을 때는 셀 내부에서 자동 줄바꿈되도록 렌더링한다.
팝업 우측 하단 적용/취소 버튼으로 커스텀 폼 설정을 완료하며, 적용한 폼의 축소판(pre-view)을 Form 설정 버튼 아래에서 확인할 수 있다.
Form 설정 버튼을 다시 클릭하면 이전에 저장한 JSON 값을 기준으로 팝업이 재렌더된다.
기존 form.io 방식으로 저장된 form_schema는 신규 그리드 스키마와 호환되지 않아 빈 값으로 리셋한다.

## 2. 해결 방법

### DB

`source/db/sql/38_srm_form_schema_reset.sql`을 신규 작성했다.
`service_catalog_item.form_schema` 컬럼의 기존 데이터를 전부 `{"components":[]}`로 리셋했다.
컬럼 기본값도 신규 그리드 스키마 기준으로 정정했다.
마이그레이션이 아닌 단순 초기화이며, 기존 서비스 카탈로그 항목의 커스텀 폼은 담당자가 신규 빌더로 재구성해야 한다.

### BE

`common.form.FormSubmissionValidator`를 재작성했다.
기존 form.io 기반의 재귀 순회와 minLength/maxLength/min/max/pattern 검증 로직을 완전히 폐기했다.
신규 그리드 스키마의 평면 배열(`components`)을 순회하며 `validation.required`(REQUIRED_FIELD_MISSING)와 `validation.regex`(FORM_FIELD_INVALID)만 검증하도록 재작성했다.
`FormJsonMapper`는 범용 직렬화 유틸이라 변경하지 않았다.
`ServiceCatalogService`의 `DEFAULT_FORM_SCHEMA` 등 관련 문구를 정리했다.

### FE

`dynamic-form-builder.tsx`, `dynamic-form-renderer.tsx`를 신규 빌드 의존성 없이 자체 구현으로 전면 재작성했다.
Pointer Events 기반 드래그·리사이즈와 CSS Grid 8열 렌더링으로 그리드 빌더/렌더러를 구현했다.
`CatalogManagePage.tsx`의 인라인 임베드 방식을 "Form 설정" 팝업 + pre-view 축소판 방식으로 전환했다.
`@formio/js`, `@formio/react` 의존성과 관련 CSS를 완전히 제거했다(FE 번들 크기 3.94MB → 2.16MB로 감소).
ESM은 이번 유지보수 대상이 아니며, 여전히 레거시 EAV 구조를 그대로 사용하고 있어 `field-builder.tsx`/`dynamic-form.tsx`는 ESM 전용으로 유지했다(삭제하지 않음).

### 코드 리뷰 중 발견·수정한 결함

`dynamic-form-renderer.tsx`의 file 타입 컴포넌트가 raw `File` 객체를 그대로 제출 데이터에 담아, JSON 직렬화 시 값이 `{}`로 유실되는 결함을 발견했다.
`FileReader.readAsDataURL()`로 base64 data URL로 변환한 뒤 전달하도록 수정했다.
실제 POST body를 캡처해 재현하고 수정을 확인했다.

## 3. 변경 파일

- `source/db/sql/38_srm_form_schema_reset.sql`
- `source/backend/src/main/java/com/itsm/common/form/FormSubmissionValidator.java`
- `source/backend/src/main/java/com/itsm/srm/application/ServiceCatalogService.java`
- `source/frontend/src/components/common/dynamic-form-builder.tsx`
- `source/frontend/src/components/common/dynamic-form-renderer.tsx`
- `source/frontend/src/features/service-request/CatalogManagePage.tsx`
- `source/frontend/package.json` (`@formio/js`, `@formio/react` 의존성 제거)
- `docs/02_plan/screen/service-request.md`
- `docs/02_plan/database/service-request.md`
- `docs/02_plan/api_spec/service-request.md`
- `docs/02_plan/api_spec/common.md`
- `docs/02_plan/screen/common.md`
- `docs/00_context/glossary.md`
- `docs/02_plan/screen/esm.md` (ESM 실제 구현 기준 정정)
- `docs/02_plan/database/esm.md` (ESM 실제 구현 기준 정정)
- `docs/02_plan/api_spec/esm.md` (ESM 실제 구현 기준 정정)

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260718-135109/srm/result/srm.md`)는 11건 전부 PASS했다.
팔레트 7종 배치·이동·리사이즈·겹침 방지, Content 설정, pre-view·저장 라운드트립, 서버 재검증(required/regex), 기존 카탈로그 항목 form_schema 리셋 확인, ESM 회귀 없음, `@formio` 코드베이스 전체 미존재 확인을 포함했다.
file 컴포넌트 base64 변환 결함 수정에 대한 재검증도 추가로 통과했다.
커밋 `af6cfa3`로 origin/main에 push 완료됐다.
