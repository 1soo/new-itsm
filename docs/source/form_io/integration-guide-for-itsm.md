# 우리 ITSM에 Form.io 적용하기 — 통합 가이드

> 이 문서는 [overview.md](./overview.md)·[form-builder.md](./form-builder.md)·[form-renderer.md](./form-renderer.md)·[component-schema-and-validation.md](./component-schema-and-validation.md)에서 정리한 Form.io 리서치를, 우리 프로젝트(`source/`)의 **현재 서비스 카탈로그 입력항목 구현**에 매핑한 종합 정리다. 실제 도입 여부·설계는 이 문서가 아니라 `docs/01_analyze`·`docs/02_plan`에서 정식으로 결정되어야 하며, 이 문서는 그 논의의 출발점(참고 자료)이다.

---

## 1. 현재 구현 스냅샷

| 계층 | 현재 방식 | 위치 |
|---|---|---|
| 필드 타입 | `text`/`textarea`/`select`/`number`/`date`/`file` 6종 고정 | `source/frontend/src/components/common/form-schema.ts` |
| 설계 화면(관리자) | `FieldBuilder` — 행 단위 추가/삭제, key/label/type/required/options 입력 | `source/frontend/src/components/common/field-builder.tsx` |
| 렌더 화면(요청자) | `DynamicForm` — 필드 타입별 분기 렌더 + `validateForm()` | `source/frontend/src/components/common/dynamic-form.tsx` |
| 저장 구조(DB) | **EAV**: `catalog_form_field`(field_key, label, field_type, required, options JSONB, sort_order), UNIQUE(catalog_item_id, field_key) | `docs/02_plan/database/service-request.md`, `CatalogFormField.java` |
| 제출 데이터(DB) | **EAV**: `service_request_form_value`, UNIQUE(service_request_id, field_key) | `docs/02_plan/database/service-request.md` |
| 도메인 중복 | SRM(`CatalogFormField`)과 ESM(`EsmCatalogFormField`)에 **동일 설계가 두 벌** 존재 | `com.itsm.srm.domain.CatalogFormField`, `com.itsm.esm.domain.EsmCatalogFormField` |

즉 지금은 "필드 하나 = DB row 하나"인 **행 기반 EAV**이고, Form.io는 "폼 하나 = JSON 문서 하나"인 **문서 기반 스키마**다. 이 차이가 도입 시 가장 중요한 설계 갈림길이다.

---

## 2. 개념 매핑

| Form.io 개념 | 우리 시스템의 대응(현재) | 비고 |
|---|---|---|
| Form (JSON Schema, `components[]`) | `catalog_form_field` 행들의 집합(`catalog_item_id` 기준) | Form.io는 이 전체를 **하나의 JSON**으로 다룸 |
| Component (`type`/`key`/`label`/`validate`/`conditional`) | `catalog_form_field` 한 행(`field_key`/`label`/`field_type`/`required`/`options`) | Form.io는 컴포넌트당 필드가 훨씬 풍부(§3) |
| Submission (`data.<key>`) | `service_request_form_value` 행들(`field_key`→값) | 구조는 유사(키-값), 다만 Form.io는 통째로 JSON 하나로 옴 |
| Form Builder (`@formio/react` `<FormBuilder>`) | `FieldBuilder` 컴포넌트 | 드래그앤드롭·레이아웃(컬럼/패널/탭) 없음, 행 추가/삭제만 |
| Form Renderer (`@formio/react` `<Form>`) | `DynamicForm` 컴포넌트 | 필드 타입 6종만 분기, 조건부 표시·계산 필드 없음 |

**현재 부족한 것 vs Form.io가 기본 제공하는 것**: checkbox/radio/selectboxes/email/phone 등 컴포넌트 타입, 조건부 표시(`conditional`), 계산 필드(`calculateValue`), 레이아웃 컴포넌트(columns/panel/tabs), 정규식·custom validation. 이 항목들이 "form.io처럼 커스텀 화면"이라는 요구사항의 실질적 갭이다.

---

## 3. 핵심 설계 갈림길 — EAV 유지 vs JSON 스키마 전환

### 옵션 A. 저장 구조를 JSON Schema 통째로 전환

- `catalog_form_field` 테이블을 없애고, `service_catalog_item`에 `form_schema JSONB` 컬럼 하나로 Form.io Form JSON을 그대로 저장.
- **장점**: Form.io의 Builder/Renderer를 거의 수정 없이 그대로 활용(스키마 형태가 일치). 조건부·계산 필드 등 고급 기능을 스키마 안에 자연스럽게 표현.
- **단점**: 기존 `catalog_form_field`/`service_request_form_value` 행 기반 조회(관리 화면 목록, 통계 등)를 활용하는 코드가 있다면 마이그레이션 필요. `service_request_form_value`(제출 데이터)도 `submission.data` JSON 통째 저장으로 바꿀지, 기존처럼 키별 행으로 분해해 저장할지 별도 결정 필요.

### 옵션 B. 기존 EAV 구조를 유지하되 컴포넌트 표현력만 확장

- `catalog_form_field`에 `component_schema JSONB` 같은 컬럼을 추가해 Form.io 스타일의 `validate`/`conditional`/`calculateValue` 조각을 필드 단위로 저장, 렌더 시 각 행을 모아 하나의 Form.io Form JSON으로 조립.
- **장점**: 기존 테이블·API·마이그레이션 리스크 최소화, SRM/ESM 기존 조회 로직 재사용 가능.
- **단점**: 컬럼(columns)·패널(panel)처럼 **필드 간 배치·중첩 구조**를 표현하는 레이아웃 컴포넌트는 "행의 집합"만으로 표현하기 어려움(부모-자식 트리 구조가 필요). 순수 EAV로는 한계가 뚜렷.

> **분석 포인트**: "form.io와 같이 custom 하는 화면"이 어느 수준을 의미하는지(단순 컴포넌트 타입 확장 정도 vs 레이아웃까지 자유 배치하는 완전한 빌더)에 따라 A/B 선택이 갈린다. 레이아웃 자유도까지 필요하면 옵션 A(JSON Schema 통째 저장)가 Form.io의 설계 의도에 부합한다. 이 결정은 `docs/01_analyze`(또는 유지보수 워크플로우라면 `maintainer`)에서 요구사항으로 확정한 뒤 `docs/02_plan/database`에 반영해야 한다.

---

## 4. 아키텍처 권장 — "라이브러리만, 서버는 없이"

[overview.md](./overview.md)에서 확인했듯, Form.io Server(MongoDB 기반 별도 백엔드)는 선택 사항이며 우리처럼 자체 Spring Boot + 기존 DB를 가진 시스템에는 **불필요**하다. 권장 구성:

```
[관리자] → @formio/react <FormBuilder> (source/frontend, FieldBuilder 대체/확장)
              → onChange(form)로 Form JSON Schema 수신
              → 기존 apiClient → Spring Boot API → DB (옵션 A/B 중 택1로 저장)

[요청자] → Spring Boot API에서 form_schema(JSON) 조회
              → @formio/react <Form src={schema}> (DynamicForm 대체/확장)
              → onSubmit(submission) → 기존 apiClient → Spring Boot API → service_request_form_value
```

- **프론트**: `@formio/js` + `@formio/react`를 npm 의존성으로 추가. `FieldBuilder`는 `<FormBuilder>`로, `DynamicForm`은 `<Form>`으로 교체하거나 병행.
- **백엔드**: 검증은 클라이언트([form-renderer.md](./form-renderer.md) §4 참고)만 신뢰하지 않고 **서버 재검증 필수** — 현재 `ServiceRequestService`의 값 저장 로직에 Form.io `validate` 규칙(required/pattern/min/max 등)을 동일하게 적용하는 서버 검증기를 두어야 한다.
- **SRM/ESM 중복 문제**: 현재 `CatalogFormField`/`EsmCatalogFormField`가 별도 구현이므로, 도입 시 공통 모듈(예: `com.itsm.common.form`)로 통합할지 여부도 함께 검토 대상.

---

## 5. 스타일 통합 이슈

Form.io Builder/Renderer는 기본적으로 **Bootstrap CSS**를 전제로 렌더링된다([form-builder.md](./form-builder.md) 주의사항 참고). 우리 UI가 Atlassian 계열 디자인 시스템(`docs/source/uiux`)을 쓰고 있다면, Form.io 컴포넌트의 CSS를 우리 디자인 토큰으로 재정의(override)하거나, Form.io의 커스텀 템플릿/렌더 훅으로 마크업을 우리 컴포넌트로 치환하는 작업이 별도로 필요하다. 이 부분은 `designer`가 UI/UX 설계 단계에서 구체화해야 한다.

---

## 6. 다음 단계 제안

이 문서는 리서치 참고 자료이며, 실제 도입은 아래 순서로 정식 진행하는 것을 제안한다.

1. **요구사항 확정**(analyzer 또는 maintainer): "커스텀 화면"의 범위 — 지원할 컴포넌트 타입 목록, 레이아웃 자유도(컬럼/패널 필요 여부), 조건부 로직/계산 필드 필요 여부를 확정.
2. **설계**(designer): 위 §3의 옵션 A/B 중 선택, `docs/02_plan/database`에 스키마 변경 반영, `docs/02_plan/screen`에 Builder/Renderer 임베드 화면 설계, `docs/02_plan/api_spec`에 스키마 저장/조회 API 갱신.
3. **개발**(dev-lead + UI/FE/BE): `@formio/js`·`@formio/react` 도입, SRM/ESM 공통화 여부 결정, 서버 검증 로직 구현.

---

## 📚 관련 리서치 문서

- [overview.md](./overview.md) — Form.io 개요·아키텍처·라이선스
- [form-builder.md](./form-builder.md) — Form Builder 임베드·팔레트 커스터마이징·백엔드 저장 흐름
- [form-renderer.md](./form-renderer.md) — Form Renderer 임베드·제출·검증·onChange 동기화
- [component-schema-and-validation.md](./component-schema-and-validation.md) — Component JSON Schema·Validation·Conditional Logic·Calculated Value
