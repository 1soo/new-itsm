# Form.io Form Renderer 통합 가이드 (저장된 JSON 스키마 → 실제 입력폼 렌더링)

> **핵심 요약**: Form.io Renderer는 백엔드(Form.io 서버) 없이도 동작하는 **순수 클라이언트 사이드 SDK**다. 카탈로그 관리자가 저장한 JSON 스키마를 `Formio.createForm()`(vanilla) 또는 `@formio/react`의 `<Form>` 컴포넌트에 그대로 넘기면 입력폼이 렌더링되고, 클라이언트 검증이 자동 실행되며, `onSubmit` 콜백으로 **우리 자체 API에 제출 데이터를 보낼 수 있다**. src가 Form.io 서버 URL이 아니면 `onSubmit`이 곧 최종 제출 이벤트가 된다.

Form.io의 모든 폼은 하나의 JSON 문서로 표현되며, 이 스키마가 "폼이 어떻게 보이고, 동작하고, 검증되고, 데이터를 제출하는지"를 완전히 정의한다. 렌더링·검증 로직·조건부 동작은 모두 브라우저에서 실행되고, 서버는 폼 정의를 가져오거나 완성된 데이터를 제출받을 때만 통신한다. 우리는 Form.io 서버를 쓰지 않으므로 **로컬 JSON 스키마를 직접 주입**하고 **제출은 커스텀 핸들러로 우리 백엔드**에 보낸다.

## 1. Renderer를 웹 화면에 임베드하기

### 1-1. Vanilla JS — `Formio.createForm()`

`Formio.createForm`은 `FormioForm`(또는 display 타입에 따라 `FormioWizard`/`FormioPDF`) 인스턴스를 생성하고 **Promise를 반환**하는 팩토리 메서드다.

```js
// 시그니처: Formio.createForm([DOM element], [src | JSON form], [options])
Formio.createForm(document.getElementById('formio'), {
  components: [
    { type: 'textfield', key: 'firstName', label: 'First Name', input: true },
    { type: 'textfield', key: 'lastName',  label: 'Last Name',  input: true },
    { type: 'button', action: 'submit', label: 'Submit', theme: 'primary' }
  ]
}).then((form) => {
  // form 인스턴스 준비 완료 — 여기서 이벤트 바인딩
});
```

> 두 번째 인자에 Form.io Embed URL(`'https://examples.form.io/example'`)을 넣는 방식도 있으나, 우리는 저장된 로컬 JSON 스키마를 그대로 넘긴다.

### 1-2. React — `@formio/react`의 `<Form>` 컴포넌트

```jsx
import { createRoot } from 'react-dom/client';
import { Form } from '@formio/react';

const formDefinition = {
  type: 'form',
  display: 'form',
  components: [
    { type: 'textfield', key: 'firstName', label: 'First Name', input: true },
    { type: 'textfield', key: 'lastName',  label: 'Last Name',  input: true },
    { type: 'button',    key: 'submit',    label: 'Submit',     input: true }
  ]
};

root.render(<Form src={formDefinition} />);
```

`<Form>` 컴포넌트 주요 props (공식 README 기준):

| Prop | 타입 | 설명 |
|------|------|------|
| `src` | `Webform \| string` | **JSON 폼 정의(객체)** 또는 소스 URL. 우리는 객체를 넘긴다 |
| `url` | `string` | JSON 정의 사용 시 파일 업로드/OAuth 용도 |
| `submission` | `JSON` | 사전 채움(pre-fill) 제출 데이터 |
| `options` | `FormOptions` | 렌더러 설정 옵션 |
| `onFormReady` | `function` | 폼 렌더 완료 시 인스턴스 전달 |
| `onSubmit` | `function` | 제출 시작 시 호출 (서버 URL 아니면 최종 제출 이벤트) |
| `onChange` | `function` | 값 변경 시 호출 |
| `onError` | `function` | 검증 오류 시 호출 |

## 2. 저장된 JSON 스키마를 주입하는 흐름

- **서버 없이 로컬 JSON 그대로 주입 가능**하다. vanilla는 `createForm`의 두 번째 인자로 `{ components: [...] }` 객체를, React는 `<Form src={formDefinition} />`로 스키마 객체를 넘긴다.
- 우리 ITSM 시나리오: 카탈로그 관리자가 저장한 스키마 JSON을 API/DB에서 조회 → 그 객체를 그대로 `src`/두 번째 인자에 전달하면 입력폼이 렌더링된다. Form.io 서버 왕복이 필요 없다.
- 초기값이 있으면 vanilla는 `form.submission = { data: {...} }`, React는 `submission` prop으로 주입한다.

```js
// vanilla: 렌더 후 초기값 주입
Formio.createForm(el, formSchema).then((form) => {
  form.submission = { data: { firstName: 'Joe', lastName: 'Smith' } };
});
```

## 3. 폼 제출(submit) — 자체 백엔드로 전송

**지원된다.** src가 Form.io 서버 URL이 아니면 `onSubmit`(React) / `form.on('submit')`(vanilla)이 **최종 제출 이벤트**이므로, 여기서 우리 API로 값을 보낸다.

```jsx
// React
<Form
  src={formSchema}
  onSubmit={(submission) => {
    // submission.data 에 입력값이 담긴다
    fetch('/api/service-requests', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(submission)
    });
  }}
/>
```

```js
// vanilla
Formio.createForm(el, formSchema).then((form) => {
  form.on('submit', (submission) => {
    // Form.io 서버가 없으므로 이 이벤트가 최종 제출
    // submission.data 를 우리 API로 전송
  });
});
```

> `@formio/react` README: "onSubmit is called when the submission has started. **If src is not a Form.io server URL, this will be the final submit event.**"

## 4. 입력값 검증(validation) 실행 시점과 방식

- **클라이언트 사이드 검증이 renderer에서 자동 동작한다.** 스키마의 각 컴포넌트 `validate` 객체에 정의된 규칙(required, min/max, pattern 등)이 브라우저에서 실행된다.
- 검증은 **제출 시도 시** 트리거되며, 실패하면 `submit` 이벤트 대신 `error`(vanilla `form.on('error')` / React `onError`) 콜백이 호출되고 화면에 오류가 표시된다. 값 변경 중에도 재검증된다.
- React `onChange` 콜백은 `submission.isValid`(전체 검증 통과 여부 boolean)와 `submission.changed`(변경 항목)를 제공하므로, 제출 버튼 활성화 제어 등에 쓸 수 있다.
- 참고: 스키마의 `validate` 규칙은 클라이언트/서버 양쪽에 적용되도록 설계돼 있다. 우리는 Form.io 서버를 안 쓰므로 **서버 측 검증은 우리 백엔드에서 별도로 구현**해야 하며, 클라이언트 검증만 신뢰해선 안 된다.

## 5. 실시간 값 동기화 (onChange)

```jsx
// React — 상위 컴포넌트 상태와 동기화
const [formData, setFormData] = useState({});

<Form
  src={formSchema}
  onChange={(submission) => {
    // submission.data: 현재 전체 값, submission.changed: 방금 바뀐 항목,
    // submission.isValid: 검증 통과 여부
    setFormData(submission.data);
  }}
/>
```

```js
// vanilla
form.on('change', (changed) => {
  // 값 변경 시마다 호출 — 상위 상태로 반영
});
```

React에서 폼 인스턴스에 직접 접근해 프로그래밍적으로 값을 세팅하려면 `onFormReady`로 인스턴스를 잡아 `instance.getComponent('firstName')?.setValue('John')` 형태로 조작한다.

## 주요 이벤트 요약 (vanilla `form.on()`)

| 이벤트 | 시점 | 반환 |
|--------|------|------|
| `submit` | 폼 제출 (서버 없으면 최종) | submission 객체 |
| `error` | 검증 실패 | 오류 배열 |
| `submitDone` | 서버 저장 완료 (Form.io 서버 사용 시) | 저장된 submission |
| `change` | 필드 값 변경 | 변경 데이터 |
| `render` | 렌더링 완료 | form 인스턴스 |

## ITSM 적용 관점 정리

- 서비스 요청 화면: DB에 저장된 카탈로그 스키마 JSON → `<Form src={schema} onChange={syncState} onSubmit={postToOurApi} />`
- 제출 데이터는 `submission.data`(key-value)로 우리 API가 그대로 수신 가능
- 클라이언트 검증은 자동이나, **백엔드 재검증 필수**

---

## 📚 참고 자료

- [formio/react README (@formio/react 공식)](https://github.com/formio/react/blob/master/README.md)
- [@formio/react - npm](https://www.npmjs.com/package/@formio/react)
- [Formio.createForm / Form Renderer — formio.js Wiki](https://github.com/formio/formio.js/wiki/Form-Renderer)
- [Form JSON Schema — formio.js Wiki](https://github.com/formio/formio.js/wiki/Form-JSON-Schema)
- [Forms From JSON: Schema-Driven Architecture — form.io](https://form.io/features/form-from-json-schema/)
- [formio/formio.js GitHub](https://github.com/formio/formio.js/)

> 비고: `help.form.io/developers/form-development/form-renderer`는 현재 404이며, 렌더러 문서가 재편된 상태다. 위 내용은 formio.js Wiki, @formio/react README(공식), npm 페이지, form.io 기능 소개 페이지에서 교차 확인했다. `submitDone`은 Form.io 자체 서버 사용 시의 이벤트로, 우리처럼 서버를 쓰지 않는 구성에서는 발생하지 않는다.
