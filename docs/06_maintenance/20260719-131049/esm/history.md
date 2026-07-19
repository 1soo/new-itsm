---
date: 20260719-131049
domain: esm
change_type: [new, modified, removed]
keywords: [ESM 그리드 폼 빌더 전환, EAV 폐지, DynamicFormBuilder/Renderer 공용화, JSONB 전환]
---

# 유지보수 이력 — ESM (엔터프라이즈 서비스 관리)

> 유지보수 일시: 20260719-131049 · 도메인: esm

## 1. 요구사항

ESM 부서요청의 동적 양식을 기존 EAV(`esm_catalog_form_field`/`esm_request_form_value`)에서, SRM이 이미 사용 중인 자체 8×n 그리드 드래그앤드롭 폼 빌더(JSONB `form_schema`/`form_values`, 공용 컴포넌트 `dynamic-form-builder.tsx`/`dynamic-form-renderer.tsx`, 공용 백엔드 `com.itsm.common.form.FormSubmissionValidator`)로 전환한다.
공용 컴포넌트 자체는 변경하지 않고 그대로 소비만 한다.
사용자 확정 결정은 3건이다.
기존 카탈로그 스키마는 자동 배치 없이 빈 그리드로 리셋한다.
기존 제출 데이터는 `form_values`로 백필하고, 신규/과거 구분 없이 단일 공용 렌더러를 사용한다.
number 타입은 팔레트에 별도로 추가하지 않고 text+정규식으로 흡수한다.

## 2. 해결 방법

### DB

`source/db/sql/40_esm_form_schema_jsonb.sql`을 신규 추가했다.
`esm_catalog_item.form_schema` JSONB 컬럼을 신규 추가했다(기본값 `{"components":[],"labels":[]}`, EAV와 그리드 구조가 비호환이라 자동 마이그레이션 불가 — 사용자 결정대로 백필 없이 빈 그리드로 신규 추가).
`esm_request.form_values` JSONB 컬럼을 신규 추가했다(기본값 `{}`), 기존 `esm_request_form_value`(EAV) 데이터를 `esm_request_id`별 `{field_key:field_value}` 객체로 조립해 백필했다.
`esm_catalog_form_field`/`esm_request_form_value`(EAV) 테이블을 DROP했다.
`source/db/sql/CLAUDE.md` 인덱스를 갱신했다.
부수 정리로, 통합테스트 중 발생한 더미 요청(ticket_key=ESM-2026-0008, 빈 값 제출)을 관례대로 soft delete 처리했다(`esm_request.id=8`, `timeline_event.id=134`, `is_deleted=true`).

### BE

`com.itsm.esm.*`를 수정했다.
`domain/EsmCatalogItem.java`에 `formSchema` 필드를, `domain/EsmRequest.java`에 `formValues` 필드를 추가했다.
`application/EsmCatalogService.java`가 공용 `FormJsonMapper`로 `formSchema`를 직렬화/역직렬화하도록 바꾸고 EAV `saveFields` 로직을 제거했다.
`application/EsmRequestService.java`가 공용 `FormSubmissionValidator`로 제출을 재검증하고 `formValues`를 그대로 저장/조회하도록 바꿨다.
`application/dto/CreateCatalogItemRequest.java`·`UpdateCatalogItemRequest.java`·`CatalogItemDetailResponse.java`의 `formSchema` 타입을 `List<FormFieldDto>`에서 `Map<String,Object>`로 전환했다.
`domain/EsmCatalogFormField.java`, `domain/EsmRequestFormValue.java`, `domain/repository/EsmCatalogFormFieldRepository.java`·`EsmRequestFormValueRepository.java`, `infrastructure/persistence/EsmCatalogFormFieldJpaRepository.java`·`EsmRequestFormValueJpaRepository.java`, `application/dto/FormFieldDto.java`를 삭제했다.
`common/form/CLAUDE.md`를 "SRM 전용"에서 "SRM/ESM 공용"으로 갱신했다.
다른 11개 도메인(Asset/Auth×2/Change/Compliance/Incident/Infra/Knowledge/Problem/Search/SrmApproval/Vulnerability) 통합테스트의 Testcontainers 마운트 목록에 `40_esm_form_schema_jsonb.sql`을 추가해, esm 신규 컬럼으로 인한 스키마 검증 실패를 해결했다.
관련 `CLAUDE.md`(`esm/CLAUDE.md`, `application/CLAUDE.md`, `application/dto/CLAUDE.md`, `domain/CLAUDE.md`, `domain/repository/CLAUDE.md`, `infrastructure/persistence/CLAUDE.md`)를 전체 갱신했다.

### FE

`source/frontend/src/features/esm/*`, `components/common/*`를 수정했다.
`EsmCatalogManagePage.tsx`를 `FieldBuilder` 인라인 편집에서 "Form 설정" 버튼+Modal(`DynamicFormBuilder`)+축소 미리보기(`PREVIEW_SCALE=0.45`)로 전환했다.
`DeptRequestSubmitPage.tsx`를 `DynamicForm`+`validateForm`에서 `DynamicFormRenderer`로 전환했다(온보딩/오프보딩 대상자명은 별도 필드로 유지).
`types.ts`의 `formSchema` 타입을 `FormFieldSchema[]`에서 `GridFormSchema`로 전환했다.
`components/common/field-builder.tsx`, `dynamic-form.tsx`를 삭제하고, `form-schema.ts`/`index.ts`의 레거시 export(`FormFieldSchema`/`FormValues`/`FormErrors`/`validateForm`/`hasOptions`)를 제거했다.
i18n `esm.json`(ko/en)의 `formFieldsLabel`을 `formSchema`+`formBuilder.{openButton,modalTitle,previewEmpty}`로 바꾸고, `common.json`(ko/en)의 `fieldBuilder.*` 키를 제거했다.
관련 `CLAUDE.md`(`components/common/CLAUDE.md`, `features/esm/CLAUDE.md`)를 갱신했다.

### 부수 이슈(task #45)

테스트 중 8080 포트에 어제 시작된 stale 백엔드 프로세스(ESM 마이그레이션 반영 전 코드)가 떠 있던 문제가 발견됐다.
코드 결함이 아니며, dev-be가 프로세스를 종료하고 최신 코드로 재기동해 해결했다.

### 코드 리뷰

Standards축(SRM 패턴 재사용 충실도, 레거시 완전 삭제 확인)·Spec축(`docs/02_plan` 대조) 모두 발견 사항이 없었다(재작업 없이 1회 통과).

## 3. 변경 파일

- `source/db/sql/40_esm_form_schema_jsonb.sql`
- `source/db/sql/CLAUDE.md`
- `com.itsm.esm.domain.EsmCatalogItem`, `EsmRequest`
- `com.itsm.esm.application.EsmCatalogService`, `EsmRequestService`
- `com.itsm.esm.application.dto.CreateCatalogItemRequest`, `UpdateCatalogItemRequest`, `CatalogItemDetailResponse`
- (삭제) `EsmCatalogFormField`, `EsmRequestFormValue`, 관련 리포지토리·JPA 구현체, `FormFieldDto`
- `source/backend/src/main/java/com/itsm/common/form/CLAUDE.md`
- `source/frontend/src/features/esm/EsmCatalogManagePage.tsx`
- `source/frontend/src/features/esm/DeptRequestSubmitPage.tsx`
- `source/frontend/src/features/esm/types.ts`
- (삭제) `source/frontend/src/components/common/field-builder.tsx`, `dynamic-form.tsx`
- `source/frontend/src/components/common/form-schema.ts`, `index.ts`(레거시 export 제거)
- `source/frontend/src/i18n/locales/{ko,en}/esm.json`, `common.json`

## 4. 테스트 결과

통합 테스트(`docs/04_test/20260719-125019/esm/result/esm.md`, 시나리오: `docs/04_test/20260719-125019/esm/scenario.md`)는 7건 전부 PASS했다.
TC-ESM-B01(빌드/레거시참조 0건), 001(Form 설정 팔레트 9종 배치·저장·재조회), 002(요청 제출 렌더링·성공), 003(클라이언트 첫 위반만 표시+서버 400 재검증), 004(온보딩 대상자명 검증+체크리스트 자동생성 회귀 없음), 005(기존 카탈로그 리셋/기존 제출건 백필 표시 확인), 006(레거시 참조 0건)을 검증했다.
