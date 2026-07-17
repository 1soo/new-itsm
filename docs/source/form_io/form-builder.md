# Form.io Form Builder 구현 가이드 (Embedding & Customization)

> **Form.io Form Builder는 드래그앤드롭으로 폼(입력항목)을 설계하고, 그 결과를 Form JSON Schema로 산출하는 오픈소스 폼 디자이너다.** 순수 JS(`Formio.builder()`)로 임의의 웹 화면에 임베드하거나, React 프로젝트에서는 `@formio/react`의 `<FormBuilder>` 컴포넌트로 통합한다. 컴포넌트 팔레트를 그룹 단위로 노출/제한할 수 있고, 기존 스키마를 불러와 편집 모드로 열 수 있어, ITSM 서비스 카탈로그의 "관리자용 입력항목 커스텀 설계 화면"에 그대로 활용 가능하다.

본 문서는 Form.io 공식 문서([help.form.io](https://help.form.io)), `formio/formio.js`·`formio/react` GitHub README, npm 패키지 문서를 WebFetch로 직접 확인해 재구성한 정리다. 산출물(Form JSON Schema)의 상세 필드 스펙은 별도 문서에서 다루며, 여기서는 Builder 화면 구현에 초점을 둔다.

---

## 📌 두 가지 임베드 방식

Form Builder는 크게 두 경로로 화면에 심을 수 있다. 우리 스택(React/Next 프론트)에서는 **React 컴포넌트 방식**이 1순위이며, 순수 JS 방식은 동작 원리 이해와 옵션 참고용으로 유용하다.

| 방식 | 진입점 | 패키지 | 적합한 경우 |
|---|---|---|---|
| 순수 JS | `Formio.builder(element, form, options)` | `@formio/js` (CDN `formio.full.min.js`) | 프레임워크 무관, 레거시/바닐라 화면 |
| React | `<FormBuilder>` 컴포넌트 | `@formio/react` | React·Next 프론트엔드 (권장) |

> 두 방식 모두 내부적으로 동일한 `@formio/js` 빌더 엔진을 사용한다. React 래퍼는 그 위에 props/콜백 인터페이스를 씌운 것이다.

---

## 🧱 1. 순수 JS — `Formio.builder()`

### 메서드 시그니처

```js
// 반환값: 빌더 인스턴스로 resolve 되는 Promise
Formio.builder(element, [form], [options]);
```

- **element**: 빌더를 붙일 DOM 엘리먼트
- **form** (선택): 초기 폼 JSON 또는 `src` URL — 편집 모드로 열 때 사용
- **options** (선택): 빌더 설정 객체

### 최소 임베드 예시

```html
<html>
  <head>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap/dist/css/bootstrap.min.css" />
    <script src="https://cdn.form.io/js/formio.full.min.js"></script>
  </head>
  <body>
    <div id="builder"></div>
    <script type="text/javascript">
      Formio.builder(document.getElementById('builder'), {}, {});
    </script>
  </body>
</html>
```

> Form.io 빌더는 기본적으로 **Bootstrap 아이콘/CSS**를 전제로 스타일링된다. 우리 디자인 시스템(Atlassian 계열)과 섞으려면 팔레트/모달 영역의 CSS 재정의가 필요하다.

### 기존 스키마 로드(편집 모드)

두 번째 인자에 폼 JSON을 넘기면 해당 스키마가 채워진 상태로 빌더가 열린다.

```js
Formio.builder(document.getElementById('builder'), {
  components: [
    { type: 'textfield', key: 'firstName', label: 'First Name' }
  ]
}).then((builder) => {
  console.log(builder); // 빌더 인스턴스
});
```

URL 소스로부터 로드도 가능하다(단, form.io 서버 형식 엔드포인트를 전제).

```js
Formio.builder(document.getElementById('builder'),
  'https://forms.mysite.com/myproject/myform',
  { /* options */ });
```

---

## 🎛 2. Builder 옵션 (팔레트 커스터마이징 · 미리보기)

`Formio.builder(el, form, options)`의 세 번째 인자로 빌더 동작을 제어한다.

### 주요 최상위 옵션

| 옵션 | 설명 |
|---|---|
| `disabled` | 비활성화할 컴포넌트 key 배열. 예: `['email']` |
| `noDefaultSubmitButton` | `true`면 기본 Submit 버튼을 감춤 |
| `alwaysConfirmComponentRemoval` | 컴포넌트 삭제 시 확인 프롬프트 강제 |
| `editForm` | 컴포넌트 편집 모달(설정 폼) 옵션 |
| `display` | 빌더 형태: `'form'`, `'wizard'`(단계형), `'pdf'` |
| `language` | 빌더 UI 언어 |
| `showFullJsonSchema` | 컴포넌트 전체 JSON 스키마 표시 |

### 컴포넌트 팔레트(사이드바) 그룹 제어

`options.builder` 하위에서 팔레트에 노출할 그룹을 정의한다. 각 그룹은 `title`·`weight`(정렬 순서)·`default`를 가진다.

```js
Formio.builder(document.getElementById('builder'), {}, {
  builder: {
    resource: true,
    basic:    { title: 'Basic',    weight: 0,  default: true },
    advanced: { title: 'Advanced', weight: 10 },
    layout:   { title: 'Layout',   weight: 20 },
    data:     { title: 'Data',     weight: 30 },
    premium:  { title: 'Premium',  weight: 40 }
  }
});
```

특정 그룹을 아예 숨기려면 `false`로 설정한다. ITSM 카탈로그처럼 관리자에게 **꼭 필요한 필드 유형만** 노출하고 싶을 때 유용하다.

```js
builder: {
  resource: false,   // 리소스(외부 데이터) 그룹 숨김
  premium: false     // 프리미엄(서명·파일 등) 그룹 숨김
}
```

### 미리 정의된 필드를 팔레트에 추가

`builder` 안에 커스텀 그룹을 만들고, 그 안에 스키마가 확정된 "즉시 사용 가능한 필드"를 배치할 수 있다. (예: 사내 표준 "요청자 사번" 필드)

```js
builder: {
  custom: {
    title: 'Pre-Defined Fields',
    weight: 10,
    components: {
      firstName: {
        title: 'First Name',
        key: 'firstName',
        icon: 'terminal',
        schema: {
          label: 'First Name',
          type: 'textfield',
          key: 'firstName',
          input: true
        }
      }
    }
  }
}
```

### 미리보기(Preview)

빌더 UI 자체가 캔버스에 실시간 렌더 프리뷰를 포함한다. 저장된 스키마를 **실제 입력 폼으로 렌더**해 확인하려면 빌더가 아닌 렌더러(`Formio.createForm()` / React `<Form>`)를 사용한다 — 즉 "설계 = builder, 미리보기 = form 렌더러"로 분리한다.

### 컴포넌트 편집 폼 재정의

특정 컴포넌트의 설정 모달(라벨/검증 등 편집 UI)을 통째로 커스터마이징할 수 있다.

```js
Formio.Components.components.textfield.editForm = function () {
  return {
    components: [
      { type: 'textfield', key: 'label', label: 'Label' },
      { type: 'checkbox', key: 'validate.required', label: 'Required' }
    ]
  };
};
```

---

## ⚛ 3. React 통합 — `@formio/react`의 `<FormBuilder>`

`@formio/react`는 위 빌더 엔진을 감싼 React 컴포넌트를 제공한다.

### 기본 사용

```jsx
import { FormBuilder } from '@formio/react';
import { useRef } from 'react';

const CatalogFormBuilder = () => {
  const builderRef = useRef(null);

  const handleReady = (instance) => {
    builderRef.current = instance; // 빌더 인스턴스 직접 제어용
  };

  const initialForm = {
    display: 'form',
    components: [
      { type: 'textfield', key: 'firstName', label: 'First Name', input: true },
      { type: 'button', key: 'submit', label: 'Submit', input: true }
    ]
  };

  return (
    <FormBuilder
      initialForm={initialForm}
      onBuilderReady={handleReady}
    />
  );
};
```

### 주요 Props

| Prop | 타입 | 설명 |
|---|---|---|
| `initialForm` | FormType | 빌더에 초기 렌더할 폼 JSON 정의 (편집 모드 진입점) |
| `options` | FormBuilderOptions | 위 순수 JS의 `options`와 동일 계열 설정(팔레트 그룹 등) |
| `onBuilderReady` | `(instance) => void` | 빌더 렌더 완료 시 인스턴스 전달 |
| `onChange` | `(form) => void` | **폼 정의가 바뀔 때마다** 최신 스키마 전달 |
| `onSaveComponent` | `(component, original, parent, path, index, isNew, ...)` | 컴포넌트 설정 저장 시 |
| `onEditComponent` | `(component) => void` | 컴포넌트 편집(설정 모달 오픈) 시 |
| `onUpdateComponent` | `(component) => void` | 컴포넌트 업데이트 시 |
| `onDeleteComponent` | `(component, parent, path, index)` | 컴포넌트 삭제 시 |

> README 상 React 래퍼는 `Formio.builder()`와 달리 **`onChange(form)` 콜백으로 편집 중인 전체 스키마를 그대로 넘겨준다**. 이것이 우리 백엔드 저장 흐름의 핵심 훅이다(아래 4장).
> 또한 README는 `<FormBuilder>`가 **URL로부터 자동 로드/저장을 하지 않는다**고 명시한다 — 스키마의 로드/저장은 우리가 직접 처리하거나 별도 `FormEdit`류 컴포넌트를 써야 한다. (우리처럼 form.io 서버 없이 Spring Boot에 저장하는 구조와 잘 맞는 지점)

### 편집 모드로 열기

기존에 저장해 둔 스키마 JSON을 `initialForm`에 그대로 주입하면 편집 모드가 된다.

```jsx
<FormBuilder
  initialForm={savedSchema}        // 백엔드에서 불러온 기존 스키마
  onChange={(form) => setDraft(form)}
/>
```

---

## 🗂 4. 백엔드(Spring Boot) 저장 흐름

> **결론부터: "form.io 서버 없이 우리 백엔드에 저장"하는 시나리오에서, React `<FormBuilder>`의 `onChange(form)`로 최신 스키마를 받아 우리 API로 POST하는 것이 가장 실용적인 경로다.** 다만 이 "커스텀 백엔드 저장"을 정면으로 다루는 단계별 공식 튜토리얼은 확인되지 않았고, 공식 가이드는 대체로 form.io 자체 서버/Enterprise 모듈을 전제로 서술한다.

공식 문서를 직접 확인한 결과는 다음과 같다.

| 항목 | 확인된 내용 |
|---|---|
| 순수 JS 빌더 이벤트 | `addComponent`, `removeComponent`, `updateComponent`, `builderFormValidityChange` 등 **컴포넌트 단위 이벤트**는 문서화됨 |
| 순수 JS의 "전체 스키마 onChange" | 빌더 레벨의 `saveForm`/`onChange` 단일 이벤트는 **공식 문서에서 명확히 확인되지 않음**. 순수 JS만 쓸 경우 컴포넌트 이벤트를 조합하거나 인스턴스에서 스키마를 읽어와야 함 |
| React 빌더 | `<FormBuilder onChange={(form) => ...}>` 로 **전체 스키마를 직접 수신** 가능(README 명시) |
| Enterprise React `CreateForms` | `onChange(form)` / `onSaveForm(form)` 콜백으로 완성 스키마를 전달 — 백엔드 저장에 적합하다고 공식 언급 |

### 권장 구성 (우리 프로젝트 기준)

```jsx
import { FormBuilder } from '@formio/react';
import { useRef } from 'react';

function CatalogDesigner({ savedSchema, catalogItemId }) {
  const latest = useRef(savedSchema);

  const save = async () => {
    // onChange로 축적한 최신 스키마를 우리 Spring Boot API로 저장
    await apiClient.put(`/api/catalog-items/${catalogItemId}/form-schema`, latest.current);
  };

  return (
    <>
      <FormBuilder
        initialForm={savedSchema}
        onChange={(form) => { latest.current = form; }}
      />
      <button onClick={save}>저장</button>
    </>
  );
}
```

- **로드**: 백엔드에서 저장해 둔 Form JSON을 `initialForm`으로 주입 → 편집 모드.
- **수신**: `onChange`로 편집 중 스키마를 ref/state에 축적(잦은 호출이므로 매 변경 즉시 저장보다 "명시적 저장 버튼" 또는 디바운스 권장).
- **저장**: 우리 `apiClient`로 스키마 JSON을 그대로 PUT/POST. 백엔드는 이 JSON을 그대로 컬럼(JSON/CLOB)에 보관하고, 렌더 시 다시 내려주면 된다.

> 즉 form.io 서버 인프라 없이 **"스키마 JSON을 우리가 소유·저장"**하는 오픈소스 SDK 단독 사용이 가능하며, 이 방식이 ITSM 카탈로그 설계 화면에 부합한다.

---

## 🧬 5. 산출물 — Form JSON Schema (개요)

빌더가 생성/편집하는 결과물은 **Form JSON Schema**다. 상세 필드 스펙은 별도 문서에서 다루고, 여기서는 개략 구조만 정리한다.

- 최상위: `components` 배열(컴포넌트 목록)을 중심으로, `display`(form/wizard/pdf) 등을 가진다.
- 각 컴포넌트 객체의 대표 필드:

| 필드 | 의미 |
|---|---|
| `type` | 컴포넌트 유형 (`textfield`, `email`, `select`, `checkbox`, `button` 등) |
| `key` | 폼 내 고유 식별자(제출 데이터의 키가 됨) |
| `label` | 사용자에게 보이는 라벨 |
| `input` | 입력 필드 여부(boolean) |
| `validate` | 검증 규칙. 예: `{ "required": true }` |
| `conditional` | 다른 필드 값에 따른 표시/숨김 조건 |

간단한 예:

```json
{
  "display": "form",
  "components": [
    {
      "label": "First Name",
      "key": "firstName",
      "type": "textfield",
      "input": true,
      "validate": { "required": true },
      "validateWhenHidden": false
    }
  ]
}
```

> 조건부 로직(`conditional`)은 편집 모달의 **Conditional 탭**에서 Show/Hide 규칙으로 설정되며 검증 동작과 연동된다. 각 필드의 상세 속성(minLength, 다양한 conditional 형식 등)은 스키마 상세 문서를 참조.

---

## 🧩 6. 커스텀 컴포넌트를 팔레트에 추가

기본 컴포넌트로 부족할 때, 새 컴포넌트 클래스를 만들어 팔레트에 등록할 수 있다. (예: ITSM 전용 "담당자 검색" 위젯)

### 개략 절차

**Step 1 — 베이스 클래스 상속**

```js
import { Formio } from '@formio/js';
const Input = Formio.Components.components.input;

class MyComponent extends Input { /* ... */ }
```

**Step 2 — 정적 `schema()` 정의** (고유 `type` 부여)

```js
static schema(...extend) {
  return Input.schema({
    type: 'mycomp',
    label: 'My Component',
    key: 'mycomp',
  }, ...extend);
}
```

**Step 3 — 정적 `builderInfo` 게터** (팔레트 노출 정보)

```js
static get builderInfo() {
  return {
    title: 'My Component',
    icon: 'terminal',
    group: 'basic',   // 노출될 팔레트 그룹
    weight: 0,
    schema: MyComponent.schema()
  };
}
```

**Step 4 — `Formio.use()`로 등록** (빌더 생성 *이전*에)

```js
Formio.use({
  components: { mycomp: MyComponent } // key는 type과 일치해야 함
});
```

**Step 5 — 빌더 생성**

```js
Formio.builder(document.getElementById('builder'));
// mycomp가 지정한 group(basic) 아래 팔레트에 자동 노출됨
```

> React 환경에서도 동일하게 `<FormBuilder>` 렌더 이전에 `Formio.use()`로 전역 등록해두면 팔레트에 반영된다. `group` 값을 커스텀 그룹명으로 주면 위 2장의 `builder.custom` 그룹에 묶을 수 있다.

---

## ✅ 우리 프로젝트 적용 요약

- 프론트가 React/Next이므로 **`@formio/react`의 `<FormBuilder>`**를 1순위로 채택.
- **`options.builder`로 그룹 제한** → 관리자에게 ITSM 카탈로그에 필요한 필드 유형만 노출.
- **`initialForm`으로 편집 모드**, **`onChange`로 최신 스키마 수신** → 우리 `apiClient`로 Spring Boot에 스키마 JSON 저장(form.io 서버 불필요).
- 산출물은 **Form JSON Schema** — 백엔드에 그대로 보관 후 렌더 시 재사용.
- 사내 표준 필드는 **`builder.custom` 프리셋** 또는 **커스텀 컴포넌트 등록**으로 확장.
- 유의: Bootstrap 전제 스타일, 커스텀 백엔드 저장의 단계별 공식 튜토리얼 부재(이벤트/onChange 기반으로 직접 구성), 순수 JS 빌더의 전체 스키마 onChange 미문서화.

---

## 📚 참고 자료

- [Form Builder | Form.io Documentation](https://help.form.io/dev/form-development/form-builder)
- [Form Embedding / Rendering | Form.io Documentation](https://help.form.io/developers/form-development/rendering)
- [formio/formio.js — JavaScript powered Forms with JSON Form Builder (GitHub)](https://github.com/formio/formio.js/)
- [formio/react — JSON powered forms for React.js (GitHub)](https://github.com/formio/react)
- [@formio/js — npm](https://www.npmjs.com/package/@formio/js)
- [@formio/react — npm](https://www.npmjs.com/package/@formio/react)
- [Form Builder Demo (open source)](https://formio.github.io/formio.js/app/builder)
- [Form.io Help Documentation (corpus/ask 인터페이스)](https://help.form.io)
