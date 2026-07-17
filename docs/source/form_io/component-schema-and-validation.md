# Form.io 컴포넌트 스키마 · Validation · Conditional Logic

> Form.io는 **폼 전체를 하나의 JSON 스키마(Component JSON Schema)로 선언**하는 스키마 주도(schema-driven) 방식이다. 각 입력항목은 `type`/`key`/`label`/`validate`/`conditional` 등을 가진 컴포넌트 객체이며, 폼은 이들의 배열(`components`)로 구성된다. 검증과 조건부 로직 역시 **코드가 아닌 스키마 필드**로 표현되므로, 우리 ITSM의 EAV(`catalog_form_field`: field_key/label/field_type/required/options/sort_order) 방식을 Form.io 수준으로 확장하려면 이 스키마 모델을 기준으로 삼는 것이 적합하다.

---

## 1. Component JSON Schema 공통 구조

폼은 최상위에 `title`/`display`/`type`/`components`를 갖고, `components`는 컴포넌트 객체 배열이다. 각 컴포넌트는 아래 공통 필드를 공유한다.

```json
{
  "title": "Person",
  "display": "form",
  "type": "form",
  "name": "person",
  "path": "person",
  "components": [
    {
      "label": "First Name",
      "key": "firstName",
      "type": "textfield",
      "input": true,
      "validate": { "required": true },
      "applyMaskOn": "change",
      "validateWhenHidden": false
    },
    {
      "label": "Email",
      "key": "email",
      "type": "email",
      "input": true
    }
  ]
}
```

### 핵심 공통 필드

| 필드 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| **`type`** | string | ✅ | 컴포넌트별 | 컴포넌트 종류 식별자 (`textfield`, `select`, `email` 등) |
| **`key`** | string | ✅ | — | 제출 데이터(`submission.data`)의 속성명이 되는 API 식별자. 우리 `field_key`에 대응 |
| **`label`** | string | | — | 화면에 표시되는 라벨(HTML label) |
| **`input`** | boolean | ✅ | `true` | 사용자 입력을 받는 컴포넌트인지 여부 (Layout/Display 계열은 `false`) |
| **`placeholder`** | string | | — | 입력 전 힌트 텍스트 |
| **`defaultValue`** | 타입별 | | — | 초기 값 |
| **`validate`** | object | | `{required:false}` | 검증 규칙 객체 (§3) |
| **`conditional`** | object | | — | 표시/숨김 조건 로직 (§4) |
| **`multiple`** | boolean | | `false` | 값을 배열로 수집(다중 입력) |
| **`tableView`** | boolean | | `true` | 데이터 테이블/목록 출력에 노출 여부 |
| **`hidden`** | boolean | | `false` | 초기 숨김 |
| **`clearOnHide`** | boolean | | `true` | 숨겨질 때 값 초기화 |
| **`persistent`** | boolean | | `true` | 제출 데이터로 영구 저장 여부 |
| **`protected`** | boolean | | `false` | API 응답에서 값 숨김(비밀번호 등) |
| **`unique`** | boolean | | `false` | 제출 간 값 유일성 강제 |
| **`prefix`** / **`suffix`** | string | | — | 입력창 앞/뒤 부착 텍스트 |
| **`errors`** | object | | — | 커스텀 에러 메시지 |
| **`logic`** | array | | — | 트리거 기반 반응형 컴포넌트 변경(Advanced Logic) |

> `key`는 스키마의 중심축이다. 제출 데이터가 `data[key]`로 저장되고, 조건부·계산식이 다른 필드를 참조할 때도 `data.<key>`로 접근한다.

---

## 2. 지원 컴포넌트 타입

Form.io 공식 문서는 컴포넌트를 아래 카테고리로 분류한다(빌더 좌측 팔레트 기준). 우리 시스템의 6종(text/textarea/select/number/date/file)은 이 중 극히 일부에 해당한다.

### Basic (기본 입력)

| 타입(`type`) | 설명 | 현재 우리 시스템 대응 |
|---|---|---|
| **`textfield`** | 한 줄 텍스트 | `text` |
| **`textarea`** | 여러 줄 텍스트 | `textarea` |
| **`number`** | 숫자 | `number` |
| **`password`** | 비밀번호 | 없음 |
| **`checkbox`** | 단일 체크박스(불리언) | 없음 |
| **`selectboxes`** | 다중 선택 체크박스 그룹 | 없음 |
| **`select`** | 드롭다운(단일/다중) | `select` |
| **`radio`** | 라디오(단일 선택) | 없음 |
| **`button`** | 버튼(제출/커스텀) | 없음 |

### Advanced (고급 입력)

| 타입(`type`) | 설명 |
|---|---|
| **`email`** | 이메일(형식 검증 내장) |
| **`url`** | URL |
| **`phoneNumber`** | 전화번호(마스킹) |
| **`tags`** | 태그 입력 |
| **`address`** | 주소(외부 지오코딩 연동) |
| **`datetime`** | 날짜+시간 선택 |
| **`day`** | 일/월/년 분리 입력 |
| **`time`** | 시간 |
| **`currency`** | 통화(로케일 포맷) |
| **`survey`** | 설문(행×척도) |
| **`signature`** | 서명 캔버스 |

### Layout (레이아웃/컨테이너, `input: false`)

| 타입(`type`) | 설명 |
|---|---|
| **`panel`** | 제목 있는 패널(접기 가능) |
| **`columns`** | 다단(컬럼) 배치 |
| **`table`** | 정적 표 레이아웃 |
| **`tabs`** | 탭 |
| **`well`** | 시각적 그룹 박스 |
| **`fieldset`** | 필드 묶음 |
| **`container`** | 하위 컴포넌트를 중첩 객체로 저장하는 논리 컨테이너 |

### Data (반복/구조화 데이터)

| 타입(`type`) | 설명 |
|---|---|
| **`datagrid`** | 행 반복 그리드(배열 데이터) |
| **`editgrid`** | 인라인 편집형 반복 그리드 |
| **`datamap`** | 키-값 맵 |
| **`tree`** | 트리 구조 데이터 |

### Display / 기타

| 타입(`type`) | 설명 |
|---|---|
| **`content`** | 정적 콘텐츠(리치 텍스트) |
| **`htmlelement`** | 임의 HTML 요소 |
| **`hidden`** | 숨김 값 필드 |
| **`file`** | 파일 업로드 → 우리 `file` 대응 |
| **`form`** / **`resource`** | 다른 폼/리소스 중첩 |
| **`recaptcha`** | reCAPTCHA |
| **`custom`** | 커스텀 컴포넌트 |

> 공식 문서는 대략 33종의 코어 컴포넌트를 기술한다. 위 표는 카탈로그성으로 대표 타입만 정리했으며, 세부 프로퍼티는 각 컴포넌트 페이지에서 최신 상태를 확인해야 한다.

---

## 3. Validation 스펙

검증 규칙은 컴포넌트의 **`validate` 객체**에 선언한다. Form.io의 핵심 특징은 **동일 스키마가 클라이언트·서버 양쪽에서 동일하게 적용**된다는 점이다(같은 규칙이 서버에서도 재검증됨).

```json
{
  "type": "textfield",
  "key": "userId",
  "label": "User ID",
  "input": true,
  "validate": {
    "required": true,
    "minLength": 4,
    "maxLength": 20,
    "pattern": "^[a-zA-Z0-9_]+$",
    "customMessage": "영문/숫자/밑줄 4~20자여야 합니다",
    "custom": "valid = (input.length !== 5) ? true : '5자는 사용할 수 없습니다';",
    "customPrivate": false
  }
}
```

| `validate` 필드 | 대상 | 설명 |
|---|---|---|
| **`required`** | 전체 | 필수 입력 여부 (우리 `required`에 대응) |
| **`minLength`** / **`maxLength`** | 문자열 | 최소/최대 글자 수 |
| **`min`** / **`max`** | 숫자 | 최소/최대 값 |
| **`minWords`** / **`maxWords`** | textarea | 최소/최대 단어 수 |
| **`pattern`** | 문자열 | 정규식 패턴 |
| **`custom`** | 전체 | JavaScript 표현식 기반 커스텀 검증. `valid` 변수에 `true` 또는 에러 메시지 문자열을 할당. `input`/`data`/`row` 등 변수 사용 가능 |
| **`customMessage`** | 전체 | 검증 실패 시 표시할 커스텀 메시지 |
| **`customPrivate`** | 전체 | 커스텀 검증을 서버에서만(클라이언트 비노출) 수행할지 |
| **`json`** | 전체 | JSON Logic 기반 검증 규칙(JavaScript 대안) |

> **JSON Logic 검증**은 규칙을 DB에 저장하거나 프로그램적으로 생성해야 할 때, 또는 보안 정책상 JavaScript 실행을 피해야 하는 환경에서 JavaScript `custom`의 대안으로 쓰인다.

---

## 4. Conditional Logic (조건부 표시/숨김)

특정 필드 값에 따라 컴포넌트를 표시/숨김한다. **Simple(단순)** 과 **Advanced(고급)** 두 방식이 있다.

### 4-1. Simple Conditions

컴포넌트의 **`conditional` 객체**로 표현하며, 코드 없이 "언제 보이거나 숨길지"를 선언한다. 스키마 표현:

```json
{
  "type": "textfield",
  "key": "otherReason",
  "label": "기타 사유",
  "input": true,
  "conditional": {
    "show": true,
    "when": "category",
    "eq": "other"
  }
}
```

| `conditional` 필드 | 설명 |
|---|---|
| **`show`** | 조건 충족 시 `true`=표시 / `false`=숨김 |
| **`when`** | 조건을 평가할 기준 컴포넌트의 `key` |
| **`eq`** | `when` 필드가 이 값과 같을 때 조건 충족 |
| **`json`** | (고급) JSON Logic 조건식. 아래 참고 |

> 빌더 UI 최신 버전의 Simple 탭은 **When(One/All)** + 다중 **Conditions(When 컴포넌트 · Is 연산자 · Value)** 조합으로 `equals`, `not equals`, `empty`, `not empty`, `less than`, `greater than` 등 다변수 조건도 지원한다. 단일 필드 기준의 단순 조건에는 Simple 방식을 우선 사용하는 것이 공식 권장이다.

### 4-2. Advanced Conditions

여러 필드를 조합한 복잡한 조건은 **JavaScript(`customConditional`)** 또는 **JSON Logic(`conditional.json`)** 으로 작성한다.

**JavaScript 방식** — `show`에 `true`/`false`를 할당. `data`, `form`, `submission`, `component`, `value`, `moment`, `_`(Lodash), `utils` 변수 사용 가능:

```javascript
show = (data.income < 45000) && (data.maritalStatus == 'single' || data.maritalStatus == 'widowed');
```

스키마상으로는 문자열로 저장된다:

```json
{
  "type": "textfield",
  "key": "assistanceReason",
  "input": true,
  "customConditional": "show = data.income < 45000;"
}
```

**JSON Logic 방식** — JavaScript 실행이 제한된 환경/DB 저장용 대안:

```json
{
  "conditional": {
    "json": {
      "and": [
        { "<": [{ "var": "data.income" }, 45000] },
        { "in": [{ "var": "data.maritalStatus" }, ["single", "widowed"]] }
      ]
    }
  }
}
```

| 방식 | 표현 위치 | 적합한 상황 |
|---|---|---|
| **Simple** | `conditional.{show,when,eq}` | 단일(또는 소수) 필드 값 기준의 단순 표시/숨김 |
| **Advanced - JS** | `customConditional`(문자열) | 다중 필드 조합, 계산·함수가 필요한 복잡 조건 |
| **Advanced - JSON Logic** | `conditional.json` | 규칙을 DB에 저장/프로그램 생성, JS 실행 회피 환경 |

---

## 5. Calculated Value (자동 계산 필드)

다른 필드 값을 기반으로 값을 **자동 계산**하는 필드를 지원한다. JavaScript 또는 JSON Logic으로 작성하며, 폼 값이 바뀔 때마다 자동 재계산된다(별도 watch 불필요).

- 사용 가능 변수: **`data`**(폼 전체 데이터), **`row`**(DataGrid 등 반복 컨텍스트의 현재 행)
- `value` 변수에 결과를 할당하면 해당 필드 값이 됨

**JavaScript 방식** (`calculateValue`):

```json
{
  "type": "number",
  "key": "total",
  "label": "합계",
  "input": true,
  "calculateValue": "value = data.quantity * data.unitPrice;"
}
```

**JSON Logic 방식**:

```json
{
  "type": "number",
  "key": "total",
  "input": true,
  "calculateValue": {
    "*": [{ "var": "data.quantity" }, { "var": "data.unitPrice" }]
  }
}
```

> 주 용도: (1) 숫자 필드 간 수식 계산(수량×단가=합계 등), (2) 한 필드 값을 다른 필드로 보간(interpolate). 폼 값 변경 시마다 재계산되므로 파생 값 관리에 적합하다.

---

## 📚 참고 자료

- [Components JSON Schema · formio/formio.js Wiki](https://github.com/formio/formio.js/wiki/Components-JSON-Schema)
- [Form JSON Schema · formio/formio.js Wiki](https://github.com/formio/formio.js/wiki/Form-JSON-Schema)
- [Logic and Conditions | Form.io Documentation](https://help.form.io/form-building/logic-and-conditions)
- [Form JSON | Form Building | Form.io Documentation](https://help.form.io/form-building/form-json)
- [Forms From JSON: Schema-Driven Architecture | Form.io](https://form.io/features/form-from-json-schema/)
- [Form Conditional Logic and Validation That Runs Everywhere | Form.io](https://form.io/features/form-conditional-logic-form-validation/)
- [Form.io Concepts | Getting Started](https://help.form.io/start/form.io-concepts)
- [JavaScript Powered Forms — Conditions Example](https://formio.github.io/formio.js/app/examples/conditions.html)
