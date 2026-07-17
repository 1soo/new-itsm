# Form.io 개요 및 아키텍처 (Overview & Architecture)

> **Form.io는 "폼(Form)을 코드 없이 드래그앤드롭으로 설계하고, 그 결과를 JSON Schema로 저장·렌더링·데이터 수집"까지 연결하는 폼 플랫폼이다.** 핵심 프론트엔드 라이브러리 `formio.js`(MIT)는 서버 없이도 Builder + Renderer만 임베드해 쓸 수 있고, 데이터 저장·API·권한 관리가 필요할 때만 별도의 Form.io Server(오픈소스 커뮤니티 ~ 상용 Enterprise)를 얹는 구조다.

본 문서는 공식 사이트 [form.io](https://form.io)와 공식 문서([help.form.io](https://help.form.io)), 공식 저장소([github.com/formio](https://github.com/formio))를 조사해 재구성한 첫 리서치 산출물이다. 우리 ITSM(서비스 카탈로그/서비스 요청)에 커스텀 폼 빌더를 도입하기 위한 참고 자료로, 특히 **"서버는 쓰지 않고 라이브러리만 가져다 쓰는 것이 가능한가"**를 중심으로 정리했다.

---

## 📌 Form.io란 무엇인가

- **정의**: 드래그앤드롭 폼 빌더가 만들어낸 **JSON Schema**를 기반으로 폼을 렌더링하고, 사용자가 입력한 데이터(제출물)를 수집·관리하는 폼 개발 플랫폼이다. 빌더는 "폼을 설계하는 동시에 그 폼을 다룰 API까지 함께 생성"하는 것을 지향한다.
- **핵심 가치**: 폼을 코드로 일일이 작성하지 않고 시각적으로 만들며, 각 필드를 캔버스에 끌어다 놓으면 해당 컴포넌트의 JSON Schema가 실시간으로 생성된다. 공식 문서는 폼 빌더를 **"glorified JSON schema builder(잘 포장된 JSON 스키마 빌더)"**라고 표현한다. ([Form Builder 문서](https://help.form.io/dev/form-development/form-builder.md))
- **두 갈래 제공 방식**:

| 구분 | 내용 | 라이선스/과금 |
|---|---|---|
| **오픈소스 (`formio.js`)** | 프론트엔드 JavaScript 렌더러 + SDK + 폼 빌더 라이브러리 | MIT, 무료 |
| **오픈소스 서버 (`formio`)** | 폼 정의·제출 데이터 저장 + 자동 생성 REST API (Core API Engine) | OSL-3.0, 커뮤니티 무료 |
| **상용 SaaS/Enterprise** | 위 오픈소스 위에 Developer Portal·권한·환경 승격·PDF·감사 로그 등 추가 | 라이선스 키 기반 유료 |

> Form.io는 단순한 오픈소스 폼 빌더가 아니라, 폼과 그 데이터를 **사용자 자신의 환경에 네이티브로 배포**하는 데이터 관리 플랫폼을 표방한다. 배포하면 가동률·성능이 Form.io나 제3자에 의존하지 않는다. ([Open Source](https://form.io/open-source/))

---

## 🏗️ 전체 아키텍처 구성요소

Form.io는 아래 4개 구성요소로 나뉘며, **각각 독립적으로 사용 가능**하다. 특히 클라이언트 라이브러리(Builder + Renderer)와 서버는 완전히 분리되어 있다.

| 구성요소 | 역할 | 형태 | 서버 필요? |
|---|---|---|---|
| **Form Builder** | 드래그앤드롭으로 폼을 설계하는 UI. 컴포넌트를 캔버스에 놓으면 JSON Schema를 실시간 생성 | `formio.js` 라이브러리 (`Formio.builder()`) | ❌ 불필요 |
| **Form Renderer** | JSON Schema를 실제 입력 폼으로 렌더링하고 검증·조건부 로직 수행 | `formio.js` 라이브러리 (`Formio.createForm()`) | ❌ 불필요 |
| **Form.io Server / API** | 폼 정의·제출 데이터 저장, 자동 생성 REST API, 권한 관리 | `formio` (Node.js + MongoDB, Docker) | ✅ (선택) |
| **PDF Server** | 제출물을 PDF로 생성·저장 | 별도 서버 (Enterprise 기능) | ✅ (선택, 상용) |

### 구성요소 간 관계 (개념)

```
[Form Builder] --설계--> Form JSON Schema
                              |
                              v
[Form Renderer] --렌더--> 실제 HTML 폼 --사용자 입력--> Submission(JSON)
                                                          |
                              (선택) --------------------> [Form.io Server / API] --> MongoDB
```

### 핵심 포인트 — 서버 없이 라이브러리만 쓰기

- **Form Builder는 Renderer의 확장이다.** 빌더는 렌더러 위에 드래그앤드롭 기능을 얹은 것으로, 본질은 JSON Schema를 생성하는 UI다. ([Form Builder 문서](https://help.form.io/dev/form-development/form-builder.md))
- **Renderer는 원시 JSON Schema만으로 동작한다.** `formio.js`는 Form.io 클라우드/서버 없이도 로컬 JSON 정의를 받아 폼을 그리고, 클라이언트 측 검증·렌더링 도구로만 쓸 수 있다. 제출물(Submission)의 JSON 구조를 우리 백엔드가 직접 받아 처리하는 커스텀 백엔드 방식이 명시적으로 지원된다. ([formio.js README](https://github.com/formio/formio.js/))
- 즉 **폼 설계 UI(Builder)와 폼 표시·검증(Renderer)만 필요하고, 저장은 우리 백엔드가 맡는다면 Form.io Server는 전혀 필요 없다.**

---

## 🧩 핵심 데이터 모델: Form · Component · Submission

Form.io의 데이터 모델은 세 개념의 관계로 요약된다. ([Form JSON](https://help.form.io/form-building/form-json.md), [Submissions](https://help.form.io/form-building/submissions.md))

| 개념 | 정의 | 형태 |
|---|---|---|
| **Form** | 폼 전체의 구조·외형·동작을 정의하는 JSON Schema. `components` 배열을 포함 | JSON 객체 |
| **Component** | 폼을 구성하는 개별 필드/요소(Textfield, Select, Button, Layout 등). 각각 자신의 JSON Schema를 가짐 | Form 안의 요소 |
| **Submission** | 렌더링된 폼에 사용자가 입력해 제출한 데이터. Form의 Schema 구조를 따라 `data` 객체에 담김 | JSON 객체 |

### 관계 요약

- **Form = Component의 집합** — 폼 하나는 여러 Component를 `components` 배열에 담은 JSON Schema다. 빌더에서 필드를 하나 끌어다 놓을 때마다 그 필드의 Component Schema가 Form에 추가된다.
- **Submission = Form Schema를 따라 채워진 값** — 사용자가 입력한 값은 각 Component의 `key`를 키로 하는 `data` 객체로 수집된다. 즉 Form이 "틀"이고 Submission이 그 틀에 부어진 "값"이다.

### 컴포넌트 카테고리

빌더는 드래그 가능한 컴포넌트를 카테고리로 묶어 제공한다. 오픈소스 코어는 기본·특수·레이아웃 계열 약 33개 컴포넌트를 포함한다. ([Open Source vs Enterprise](https://form.io/form-io-open-source-core-vs-enterprise/))

| 카테고리 | 예시 컴포넌트 |
|---|---|
| **Basic** | Text Field, Text Area, Number, Checkbox, Select, Radio, Button |
| **Advanced** | Email, Url, Phone Number, Date/Time, Input Mask, Signature |
| **Layout** | Columns, Panel, Table, Well, Tabs, Field Set |
| **Data** | Container, Data Grid, Edit Grid, Data Map |
| **Premium** | File 업로드, Wizard(다단계), reCAPTCHA, Data Table 등 (상용) |

---

## ⚖️ 라이선스 / 오픈소스 구조

Form.io 생태계는 **클라이언트 라이브러리(MIT)**와 **서버(OSL-3.0)**로 라이선스가 나뉜다.

### 클라이언트 (npm 패키지, 모두 MIT)

| 패키지 | 역할 | 비고 |
|---|---|---|
| `@formio/js` (구 `formio.js`) | 코어 — Vanilla JS 렌더러 + 빌더 + SDK | 프레임워크 무관, jQuery 의존 없음 |
| `@formio/react` (구 `react-formio`) | React 바인딩 | `Form`, `FormBuilder`, `FormEdit`, `FormGrid`, `SubmissionGrid`, `FormioProvider` 등 export |
| `@formio/angular` (구 `angular-formio`) | Angular 바인딩 | — |
| `@formio/vue` (구 `vue-formio`) | Vue 바인딩 | — |

> 코어는 순수 Vanilla JS이며 React/Angular/Vue 바인딩이 그 위의 래퍼다. `@formio/react`의 `Form` 컴포넌트는 **JSON 폼 정의 또는 Form.io URL 둘 다** 받을 수 있어, 서버 없이 로컬 JSON Schema만으로 React 앱에서 렌더링이 가능하다. ([@formio/react](https://github.com/formio/react))

### 서버 및 보조 도구

| 패키지/제품 | 역할 | 라이선스 |
|---|---|---|
| `formio` | Core API Engine (폼·제출 저장 + REST API). MongoDB 필수 | **OSL-3.0** (copyleft) |
| `formio-cli`, `formio-upload`, `formio-workers`, `formio-webhook-receiver` | 보조 유틸리티 | MIT |
| Enterprise Server / SaaS | Developer Portal, 환경 승격, 감사 로그 등 | 라이선스 키 유료 |

### 오픈소스 코어 vs Enterprise

| 영역 | 오픈소스 코어 (무료) | Enterprise (유료) |
|---|---|---|
| Form Builder / Renderer | ✅ (`formio.js`, MIT) | ✅ |
| React/Angular 라이브러리 | ✅ | ✅ |
| 기본 컴포넌트(~33종) | ✅ | ✅ |
| API 서버 + 데이터 내보내기(JSON/CSV) | ✅ (`formio`, OSL-3.0) | ✅ |
| Developer Portal / Projects(샌드박스) | ❌ | ✅ |
| Premium 컴포넌트(File, Wizard, Signature, reCAPTCHA) | ❌ | ✅ |
| 인증(OAuth/SAML/JWT), RBAC, 환경 승격(dev/stg/prod) | ❌ | ✅ |
| PDF 생성 서버, 감사 로그, 필드 암호화 | ❌ | ✅ |

출처: [Open Source vs Enterprise](https://form.io/form-io-open-source-core-vs-enterprise/), [Licenses 문서](https://help.form.io/deployments/licenses)

---

## 🎯 우리 프로젝트 관점: "서버 없이 Builder + Renderer만"

우리처럼 **자체 백엔드(Spring Boot)와 프론트(React)를 이미 갖춘 시스템**이라면, Form.io Server는 도입하지 않고 `formio.js`의 Builder + Renderer만 라이브러리로 임베드하는 것이 일반적이고 공식적으로 지원되는 패턴이다.

- **근거 1 — 라이브러리 독립성**: `formio.js`는 Form.io 클라우드 인프라와 무관하게 동작하며, 원시 JSON Schema만으로 렌더링/검증하거나 **커스텀 백엔드를 붙여 JSON 데이터 구조로 직접 다루는 사용을 명시적으로 지원**한다. ([formio.js README](https://github.com/formio/formio.js/))
- **근거 2 — 서버는 선택**: `formio` 서버는 MongoDB를 필수로 요구하는 별도 배포물이다. 저장·API를 우리 Spring Boot + 기존 DB가 맡으면 이 의존성을 도입할 이유가 없다.
- **근거 3 — React 통합 준비됨**: `@formio/react`의 `FormBuilder`로 관리자가 폼을 설계 → 그 JSON Schema를 우리 DB에 저장, `Form` 컴포넌트로 JSON Schema를 불러와 렌더링 → 제출물을 우리 API로 전송하는 흐름이 자연스럽게 구성된다.

### 권장 통합 흐름 (개념)

```
[관리자] --@formio/react FormBuilder--> Form JSON Schema
                                            |
                                            v
                              Spring Boot API --> 기존 DB (폼 정의 저장)
                                            |
[요청자] <---- Form JSON Schema 조회 --------+
     |
     v
@formio/react Form(렌더) --입력·검증--> Submission(JSON) --> Spring Boot API --> DB (제출 데이터)
```

> 정리하면, **Form.io의 "서버"는 우리에게 선택 사항이고, 실제로 차용할 핵심 자산은 MIT 라이선스의 Builder + Renderer(JSON Schema 생성·렌더링) 라이브러리다.** 저장·인증·권한·API는 우리 기존 스택이 그대로 담당한다.

---

## 📚 참고 자료

- [Form.io 공식 사이트](https://form.io/) — 플랫폼 소개
- [Form.io Open Source](https://form.io/open-source/) — 오픈소스 구성요소·라이선스 개요
- [Form.io Open Source Core vs. Enterprise](https://form.io/form-io-open-source-core-vs-enterprise/) — 무료/유료 기능 구분
- [formio/formio.js (GitHub)](https://github.com/formio/formio.js/) — 코어 프론트엔드 라이브러리(MIT), 데이터 모델·독립 사용
- [formio/react (GitHub)](https://github.com/formio/react) — React 바인딩(`Form`, `FormBuilder` 등)
- [Form.io GitHub 조직](https://github.com/formio) — 전체 패키지 생태계
- [Form Builder 문서 (Developer)](https://help.form.io/dev/form-development/form-builder.md) — "glorified JSON schema builder", 컴포넌트 카테고리, `Formio.builder()`
- [Form JSON 문서](https://help.form.io/form-building/form-json.md) — Form/Component JSON Schema 구조
- [Submissions 문서](https://help.form.io/form-building/submissions.md) — 제출 데이터 모델
- [Licenses 문서](https://help.form.io/deployments/licenses) — OSL-3.0 및 Enterprise 라이선스 키
