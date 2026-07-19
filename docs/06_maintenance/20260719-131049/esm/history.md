---
date: 20260719-131049
domain: esm
change_type: [new, modified, removed]
keywords: [ESM 그리드 폼 빌더 전환, EAV 폐지, DynamicFormBuilder/Renderer 공용화, JSONB 전환]
---

# 유지보수 이력 — ESM (엔터프라이즈 서비스 관리)

> 유지보수 일시: 20260719-131049 · 도메인: esm

## 1. 요구사항

ESM 카탈로그 관리 화면과 부서 요청 제출 화면의 동적 양식을 레거시 EAV 방식(`FieldBuilder`/`DynamicForm`, `esm_catalog_form_field`/`esm_request_form_value` 테이블)에서, SRM에서 이미 사용 중인 그리드 폼 빌더(`DynamicFormBuilder`/`DynamicFormRenderer`, JSONB 스키마) 방식으로 전환한다.
SRM과 동일한 공용 컴포넌트·검증 로직을 재사용해 두 도메인의 동적 양식 구현을 일원화한다.

## 2. 해결 방법

### DB

`source/db/sql/40_esm_form_schema_jsonb.sql`을 신규 추가했다.
`esm_catalog_item.form_schema` JSONB 컬럼을 신규 추가했다(기본값 `{"components":[],"labels":[]}`, EAV와 구조가 비호환이라 백필 없이 빈 그리드로 시작).
`esm_request.form_values` JSONB 컬럼을 신규 추가했다(기본값 `{}`), 기존 `esm_request_form_value`(EAV) 행은 `{field_key:field_value}` 객체로 조립해 백필했다.
`esm_catalog_form_field`/`esm_request_form_value`(EAV) 테이블을 DROP했다.

### BE

`com.itsm.esm.*` 패키지를 수정했다.
`EsmCatalogItem.formSchema`, `EsmRequest.formValues`(JSONB) 필드를 추가했다.
`EsmCatalogService`/`EsmRequestService`가 SRM의 `ServiceCatalogService`/`ServiceRequestService`와 동일한 패턴으로 공용 `com.itsm.common.form.FormJsonMapper`/`FormSubmissionValidator`를 재사용하도록 전환했다.
`EsmCatalogFormField`/`EsmRequestFormValue` 엔티티·리포지토리(도메인+JPA 구현체)와 `FormFieldDto`를 삭제했다.
`CreateCatalogItemRequest`/`UpdateCatalogItemRequest`/`CatalogItemDetailResponse.formSchema` 타입을 `List<FormFieldDto>`에서 `Map<String,Object>`로 전환했다.
다른 11개 도메인 통합테스트의 Testcontainers 마운트 목록에 `40_esm_form_schema_jsonb.sql`을 추가해, esm 신규 컬럼으로 인한 스키마 검증 실패를 방지했다.
`common/form/CLAUDE.md`를 "SRM 전용"에서 "SRM/ESM 공용"으로 갱신했다.

### FE

`source/frontend/src/features/esm/*`, `components/common/*`를 수정했다.
`EsmCatalogManagePage.tsx`를 `FieldBuilder`(레거시 EAV)에서 "Form 설정" 버튼+Modal(`DynamicFormBuilder`)+축소 미리보기(`PREVIEW_SCALE=0.45`, `disabled`+`hideFooter` `DynamicFormRenderer`)로 전환했다(SRM `CatalogManagePage.tsx`와 동일 패턴).
`DeptRequestSubmitPage.tsx`를 `DynamicForm`+`validateForm`(레거시)에서 공용 `DynamicFormRenderer`로 전환했다.
온보딩/오프보딩 대상자명 필드는 그리드 폼과 별도 필드로 유지하고, 렌더러 자체 검증 통과 후 `onSubmit` 콜백에서 함께 검사하도록 했다.
`types.ts`의 `formSchema` 타입을 `FormFieldSchema[]`에서 `GridFormSchema`로 전환했다.
`components/common/field-builder.tsx`, `dynamic-form.tsx`와 `form-schema.ts`/`index.ts`의 레거시 export(`FormFieldSchema`/`FormValues`/`FormErrors`/`validateForm`/`hasOptions`)를 완전히 삭제했다.

### 코드 리뷰

Standards축·Spec축 모두 발견 사항이 없었다.

## 3. 변경 파일

- `source/db/sql/40_esm_form_schema_jsonb.sql`
- `com.itsm.esm.*`(BE 도메인 전 계층 — `EsmCatalogItem`/`EsmRequest`, `EsmCatalogService`/`EsmRequestService`, DTO)
- `source/frontend/src/features/esm/EsmCatalogManagePage.tsx`
- `source/frontend/src/features/esm/DeptRequestSubmitPage.tsx`
- `source/frontend/src/features/esm/types.ts`
- `source/frontend/src/components/common/field-builder.tsx`(삭제)
- `source/frontend/src/components/common/dynamic-form.tsx`(삭제)
- `source/frontend/src/components/common/form-schema.ts`(레거시 export 제거)
- `source/frontend/src/components/common/index.ts`(레거시 export 제거)
- `source/backend/src/main/java/com/itsm/common/form/CLAUDE.md`

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260719-125019/esm/result/esm.md`)는 7건 전부 PASS했다.
Form 설정 팔레트 9종 배치·저장·재조회, 요청 제출 렌더링과 클라이언트/서버 검증, 온보딩 체크리스트 자동생성 회귀, 기존 데이터 리셋/백필, 레거시 참조 0건(FieldBuilder/DynamicForm/EAV 흔적 없음)을 확인했다.
