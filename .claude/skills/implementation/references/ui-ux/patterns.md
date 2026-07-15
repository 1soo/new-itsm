# UI/UX 디자인 패턴

[conventions.md](conventions.md)를 보완하는 **프레임워크 무관 디자인 패턴** 상세. 컴포넌트 계층·디자인 토큰·접근성 컴포넌트·반응형 패턴을 공식/표준 문서 기반으로 정리한다. 출처의 성격(공식 표준 / 업계 방법론)을 각 섹션에 명시한다.

## 1. 컴포넌트 계층 패턴 (Atomic Design)

화면을 **작은 부품의 조합**으로 설계한다. UI를 5단계로 나누어, 재사용 가능한 최소 단위부터 조립한다. conventions.md "3. 공통 컴포넌트 규격"의 공통 컴포넌트는 아래 atoms~organisms에 해당한다.

| 단계 | 정의 | 예시 |
|------|------|------|
| Atoms | 더 쪼갤 수 없는 최소 UI 요소 | 버튼, 입력, 라벨, 아이콘 |
| Molecules | atoms를 묶은 단순 컴포넌트 | 검색 폼(라벨+입력+버튼) |
| Organisms | molecules/atoms가 모인 화면 구획 | 헤더, 사이드바, 제품 목록 |
| Templates | 컴포넌트를 레이아웃에 배치한 골격 | 페이지 뼈대(콘텐츠 미확정) |
| Pages | 실제 데이터가 채워진 최종 화면 | 사용자가 보는 실 화면 |

- **원칙**: "페이지가 아니라 시스템을 만든다." 화면 단위가 아닌 부품 단위로 설계·재사용한다.
- 선형 절차가 아닌 **사고 모델**이다. 전체와 부분을 동시에 인식하기 위한 분류이므로 단계 이름 자체보다 계층 재사용이 핵심이다.
- conventions.md "3. 공통 컴포넌트 규격"의 shadcn 기반 공통 컴포넌트는 atoms~organisms 계층으로 관리하고, 페이지는 이들을 조립한다.

> 출처: Brad Frost, "Atomic Design" — 업계 방법론(W3C 공식 표준 아님). https://atomicdesign.bradfrost.com/chapter-2/

## 2. 디자인 토큰 포맷 (W3C DTCG)

conventions.md "1. 디자인 토큰" 표의 토큰을 코드로 옮길 때, **W3C Design Tokens Community Group(DTCG) 포맷**을 참고해 도구 간 교환 가능한 형태로 정의한다.

- 토큰은 `$value`(실제 값)와 `$type`(color, dimension, fontFamily 등 값 해석 기준)을 기본 속성으로 갖는다. 선택 속성으로 `$description`, `$deprecated` 등이 있다.
- 모든 표준 속성은 `$` 접두사를 쓴다.
- 값이 여러 하위 값으로 구성되는 shadow·gradient·border·typography 등은 **composite type**으로 정의한다.
- 다른 토큰을 참조하는 **alias**는 중괄호 문법(예: `{color.brand.base}`)을 쓴다. 색상·크기 하드코딩 대신 alias로 참조해 일관성을 유지한다.

```json
{
  "color": {
    "brand": { "base": { "$type": "color", "$value": "#3b82f6" } },
    "button": { "$type": "color", "$value": "{color.brand.base}" }
  }
}
```

> 출처: W3C Design Tokens Community Group, "Design Tokens Format Module" — W3C Community Group 표준(2025.10 첫 안정판). https://www.designtokens.org/tr/drafts/format/

## 3. 접근성 컴포넌트 패턴 (W3C ARIA APG)

동적 위젯은 브라우저가 키보드 지원을 자동 제공하지 않으므로, **role·상태 속성·키보드 상호작용을 직접 구현**한다. conventions.md "6. 접근성"의 키보드 내비게이션·ARIA 부여를 대표 패턴별로 구체화한다.

### 3.1 Dialog (Modal)

| 항목 | 규칙 |
|------|------|
| role/속성 | `role="dialog"`, `aria-modal="true"`, `aria-labelledby`(제목) 또는 `aria-label`, 선택적 `aria-describedby` |
| 포커스 진입 | 열릴 때 다이얼로그 내부 요소로 포커스 이동(보통 첫 포커스 요소, 맥락상 제목·설명일 수 있음) |
| Tab / Shift+Tab | 다이얼로그 내부에서만 순환(포커스 트랩). 마지막→처음, 처음→마지막 wrap |
| Escape | 다이얼로그를 닫는다 |
| 포커스 복귀 | 닫을 때 호출 요소로 포커스 복귀 |
| 배경 | 다이얼로그 밖 콘텐츠는 inert(상호작용 불가) |

### 3.2 Disclosure (표시/숨김)

| 항목 | 규칙 |
|------|------|
| role/속성 | 토글 요소는 `role="button"`, `aria-expanded`(표시 시 true/숨김 시 false), 선택적 `aria-controls`(대상 콘텐츠 참조) |
| Enter / Space | 콘텐츠 표시·숨김 토글(두 키 동일 동작) |

### 3.3 Tabs

| 항목 | 규칙 |
|------|------|
| role | `tablist`(컨테이너), `tab`(개별 탭), `tabpanel`(탭 콘텐츠) |
| 속성 | `aria-selected`(활성 탭 true), `aria-controls`(탭→패널 연결), `aria-labelledby`(패널→탭 연결), 포커스 가능 콘텐츠 없는 패널은 `tabindex="0"`, 세로 배치 시 `aria-orientation="vertical"` |
| Tab | 탭 목록 안팎으로 포커스 이동 |
| 화살표 | 탭 간 이동(가로=Left/Right, 세로=Up/Down) |
| Home / End | 첫/마지막 탭으로 이동(선택) |
| 활성화 방식 | 자동(포커스 시 활성, 사전 로드 콘텐츠에 권장) 또는 수동(Space/Enter로 활성) |

### 3.4 Combobox

| 항목 | 규칙 |
|------|------|
| 정의 | 입력 위젯 + 연관 팝업(목록/그리드/트리/다이얼로그) |
| 팝업 포커스 | 일반 팝업은 `aria-activedescendant`로 포커스 관리, 다이얼로그 팝업은 DOM 포커스가 팝업 안으로 이동 |

> 출처: W3C WAI, "ARIA Authoring Practices Guide (APG)" — W3C 공식 문서. https://www.w3.org/WAI/ARIA/apg/patterns/

## 4. 컴포넌트 캡슐화 원칙 (OOP 관점)

컴포넌트는 **스타일·내부 구조·상태를 내부에 캡슐화**하고, 외부에는 **props(공개 인터페이스)만 노출**한다. 프레임워크와 무관한 설계 원칙이다.

- **원칙**: 내부 구현(마크업 구조, 토큰 참조, 내부 상태)은 감추고, 소비자는 props로만 컴포넌트를 제어한다. OOP의 정보 은닉·캡슐화와 동일하다.
- 내부 구조 변경이 외부 사용처에 영향 주지 않도록 인터페이스(props)를 안정적으로 유지한다.
- 스타일은 컴포넌트 내부에서 토큰(1·2절)을 참조해 적용하고, 색상·크기 값을 props로 직접 넘겨 하드코딩하지 않는다. (테마·상태 variant는 의미 기반 prop으로 노출)
- 접근성 속성(3절)도 컴포넌트 내부에서 관리하고, 필요한 라벨·상태만 props로 받는다.

## 5. 반응형 디자인 패턴 (Mobile-First)

conventions.md "2. 레이아웃 · 그리드"의 Mobile-First·breakpoint 규칙을 구체적인 구현 패턴으로 보강한다. 목표는 모바일 화면을 데스크톱의 축소판이 아니라 **그 뷰포트에 맞춘 별도 레이아웃**으로 제공하는 것이다.

### 5.1 Mobile-First 미디어 쿼리

- 가장 작은 뷰포트(모바일) 스타일을 기본값으로 작성하고, `min-width` 미디어 쿼리로 더 큰 뷰포트에서 레이아웃을 점진적으로 "추가"한다. `max-width`로 큰 화면부터 깎아 내려가지 않는다.
- breakpoint는 특정 기기 크기가 아니라 **콘텐츠·레이아웃이 깨지기 시작하는 지점**을 기준으로 잡고, 절대 px 대신 상대 단위(`em`/`rem`)를 권장한다. 프로젝트 기본값은 conventions.md 표(Mobile/Tablet/Desktop)를 따르되, 화면 설계서에 지정이 있으면 그 값을 우선한다.

```css
/* Mobile 기본 */
.grid { display: grid; grid-template-columns: 1fr; }

/* Tablet 이상 */
@media (min-width: 48em) { /* 768px */
  .grid { grid-template-columns: repeat(2, 1fr); }
}

/* Desktop 이상 */
@media (min-width: 64em) { /* 1024px */
  .grid { grid-template-columns: repeat(4, 1fr); }
}
```

> 출처: MDN, "Responsive web design" / "Media query fundamentals" — 공식 문서. https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/CSS_layout/Responsive_Design , https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/CSS_layout/Media_queries

### 5.2 터치 타겟 크기

모바일에서는 마우스 포인터보다 부정확한 손가락 터치가 입력 수단이 되므로, 탭 가능한 요소(버튼·아이콘·링크)는 충분한 크기를 확보한다.

| 기준 | 최소 크기 | 레벨 |
|------|-----------|------|
| WCAG 2.5.8 Target Size (Minimum) | 24×24 CSS px | AA (필수) |
| WCAG 2.5.5 Target Size (Enhanced) | 44×44 CSS px | AAA (모바일 터치 UI 권장) |

- 시각적 아이콘이 작아도, `padding`으로 탭 가능 영역을 44×44px까지 확보하면 기준을 만족한다.
- 인접한 탭 요소 사이 간격을 충분히 두어 오탭을 방지한다.

> 출처: W3C WAI, "Understanding SC 2.5.8 Target Size (Minimum)" / "Understanding SC 2.5.5 Target Size (Enhanced)" — W3C 공식 문서. https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum.html , https://www.w3.org/WAI/WCAG21/Understanding/target-size.html

### 5.3 Fluid 레이아웃 · 이미지 · 타이포그래피

- 그리드·컨테이너는 고정 px 대신 `%`, `fr`, `minmax()` 등 유동 단위로 폭을 정의해 다양한 뷰포트에 자연스럽게 맞춘다.
- 이미지는 `max-width: 100%; height: auto;`로 부모 폭에 맞춰 축소되게 하고, 해상도가 큰 원본은 `srcset`/`sizes`로 뷰포트에 맞는 크기를 내려받게 한다.
- 타이포그래피는 `clamp(min, preferred, max)`로 뷰포트 폭에 따라 자연스럽게 크기를 조정한다(브레이크포인트마다 폰트 크기를 개별 지정하는 대신).

```css
h1 { font-size: clamp(1.5rem, 4vw, 2.5rem); }
img { max-width: 100%; height: auto; }
```

### 5.4 Container Queries (컴포넌트 단위 반응형)

사이드바·그리드 셀 등 **같은 컴포넌트가 배치되는 위치(컨테이너 폭)에 따라 레이아웃을 바꿔야** 할 때는 뷰포트 기준 미디어 쿼리 대신 **Container Queries**를 쓴다. 컴포넌트가 화면 전체가 아니라 자신이 실제로 차지한 공간에 반응하므로, 공통 컴포넌트를 여러 레이아웃 컨텍스트에서 재사용하기 쉬워진다.

```css
.card-wrapper { container-type: inline-size; }

@container (min-width: 320px) {
  .card { grid-template-columns: auto 1fr; }
}
```

> 출처: MDN, "CSS container queries" — 공식 문서. https://developer.mozilla.org/en-US/docs/Web/CSS/Guides/Containment/Container_queries
