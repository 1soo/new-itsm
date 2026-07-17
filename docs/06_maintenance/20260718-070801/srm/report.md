---
date: 20260718-070801
domain: srm
change_type: [modified, removed]
keywords: [form.io 폼 빌더, JSONB form_schema, FormSubmissionValidator, 드래그앤드롭 레이아웃]
---

# 유지보수 이력 — SRM (서비스 카탈로그/서비스 요청)

> 유지보수 일시: 20260718-070801 · 도메인: srm

## 1. 요구사항

서비스 카탈로그 항목의 동적 양식 구성 방식을 기존 "필드 반복 입력"(필드를 하나씩 순서대로 추가하는 방식)에서 form.io 스타일로 전환한다.
컬럼/패널/탭 레이아웃을 포함한 완전 자유배치 드래그앤드롭 폼 빌더를 지원해야 한다.

## 2. 해결 방법

### DB

`source/db/sql/36_srm_form_schema_jsonb.sql`을 신규 작성했다.
기존 EAV(Entity-Attribute-Value) 구조인 `catalog_form_field`, `service_request_form_value` 테이블을 폐기했다.
`service_catalog_item.form_schema`, `service_request.form_values` 컬럼을 JSONB 타입으로 신규 추가했다.
마이그레이션 시 기존 데이터를 Form.io의 Form JSON(스키마)/submission.data(제출값) 형태로 백필한 뒤 구 EAV 테이블을 DROP했다.

### BE

`common.form.FormSubmissionValidator`를 신규 작성했다.
formSchema를 재귀 순회하며 required·minLength·maxLength·min·max·pattern 조건을 서버 측에서 재검증한다.
`common.form.FormJsonMapper`를 신규 작성했다.
JSON Map과 String 간 직렬화를 담당하는 공용 유틸이며, 코드 리뷰 중 BE 내 중복 직렬화 로직을 발견해 이 클래스로 추출·통합했다.
`ServiceCatalogService`, `ServiceRequestService`를 JSONB 스키마 기반으로 리팩터링했다.
EAV 관련 클래스인 `FormFieldDto`, `CatalogFormField`, `ServiceRequestFormValue` 등은 삭제했다.

### FE

`dynamic-form-builder.tsx`를 신규 작성해 `@formio/react`의 FormBuilder를 래핑했다.
기존 `field-builder.tsx`를 대체한다.
`dynamic-form-renderer.tsx`를 신규 작성해 `@formio/react`의 Form을 래핑했다.
기존 `dynamic-form.tsx`를 대체한다.
폼 빌더 팔레트는 Basic, Advanced, Layout 그룹만 노출하고 Data, Premium, Resource 그룹은 숨겼다.
렌더러에는 자체 하단 제출/취소 푸터를 추가했다.
동시에 form.io 기본 Submit 버튼과 중복 노출되지 않도록 `noAddSubmitButton`/`noDefaultSubmitButton` 옵션과 CSS 숨김 처리를 함께 적용했다.
`.formio-scope`, `.formio-dialog` 스코프를 두어 Bootstrap 전제로 만들어진 form.io 기본 CSS를 ADS(디자인 시스템) 토큰 기반으로 재구현했다(`index.css`).

### 패키지

`@formio/js`, `@formio/react` 의존성을 신규 추가했다.

### 결정 사항(known limitation)

Form.io FormBuilder가 필드 라벨로부터 key(Property Name)를 자동 생성할 때 영숫자만 허용하기 때문에, 한글 라벨을 입력하면 저장이 차단되는 제약이 있다.
우회 방법은 컴포넌트 편집 모달의 API 탭에서 영문 key를 직접 입력하는 것이다.
dev-lead 판단과 designer 동의를 거쳐, 이는 Form.io 라이브러리 고유 동작이며 우회 가능(비차단)하므로 자동 한글→영문 변환 로직 추가는 이번 유지보수 범위 밖으로 결론지었다.
이 내용은 `docs/02_plan/screen/common.md` 8.1절에 known limitation으로 문서화했으며 코드 변경은 하지 않았다.

## 3. 변경 파일

- `source/db/sql/36_srm_form_schema_jsonb.sql`
- `source/backend/src/main/java/com/itsm/common/form/FormSubmissionValidator.java`
- `source/backend/src/main/java/com/itsm/common/form/FormJsonMapper.java`
- `source/backend/src/main/java/com/itsm/srm/application/ServiceCatalogService.java`
- `source/backend/src/main/java/com/itsm/srm/application/ServiceRequestService.java`
- `source/backend/src/main/java/com/itsm/srm/application/dto/FormFieldDto.java` (삭제)
- `source/backend/src/main/java/com/itsm/srm/domain/CatalogFormField.java` (삭제)
- `source/backend/src/main/java/com/itsm/srm/domain/ServiceRequestFormValue.java` (삭제)
- `source/backend/src/main/java/com/itsm/srm/domain/repository/CatalogFormFieldRepository.java` (삭제)
- `source/backend/src/main/java/com/itsm/srm/domain/repository/ServiceRequestFormValueRepository.java` (삭제)
- `dynamic-form-builder.tsx` (신규, `@formio/react` FormBuilder 래핑)
- `dynamic-form-renderer.tsx` (신규, `@formio/react` Form 래핑)
- `docs/02_plan/screen/common.md` (8.1절 known limitation 문서화)

## 4. 테스트 결과

통합 테스트는 총 3회전에 걸쳐 진행됐다.
1차(20260717-145302, 10건 중 3건 FAIL): 폼 빌더 팔레트 구성 불일치(Premium 그룹 노출, File 위치 문제)와, `FormSubmissionValidator`가 pattern 빈 문자열("")을 정규식으로 취급해 모든 텍스트 필드 제출이 항상 400으로 거부되는 critical 버그가 발견됐다.
2차(20260717-152015, 5건 중 1건 FAIL): 1차에서 발견된 2건은 수정 확인됐다.
신규로 요청 제출 화면에서 신규 제출/취소 푸터와 Form.io 기본 Submit 버튼이 중복 노출되는 결함이 발견됐다.
3차(20260717-153547, 1건 PASS): 중복 Submit 버튼 결함 수정을 확인했고 최종적으로 전건 PASS했다.

코드 리뷰 결과, Standards축에서는 JSON 직렬화 로직 중복이 발견되어 `FormJsonMapper`로 추출 완료를 확인했다(BE/FE 재빌드 성공).
Spec축에서는 `docs/02_plan`(api_spec, screen/database service-request.md, screen/common.md 8절)과 구현을 대조했으며 불일치는 없었다.

커밋 `4ebdb3a`로 origin/main에 push 완료됐다.
ESM 도메인은 이번 유지보수 범위에서 제외되었으며, 사용자가 별도로 요청할 예정이다.
