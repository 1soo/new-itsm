# 컬러 시스템 (Color)

> **ADS의 컬러는 "중립(neutral) 중심 + 절제된 채도"를 원칙으로 하며, 모든 색은 원시 hex 값이 아니라 의미를 가진 디자인 토큰(`color.*`)으로 소비된다.** 토큰 하나가 라이트/다크 테마별로 다른 값에 매핑되므로, 토큰만 사용하면 다크모드가 자동으로 지원된다.

본 문서는 [atlassian.design/foundations/color](https://atlassian.design/foundations/color)와 관련 Atlassian 개발자 커뮤니티 게시물을 분석하여 재구성했다.

---

## 🎨 컬러 팔레트 구조

팔레트는 4개 카테고리로 구성된다.

1. **Saturated / Accent colors** — gray, red, orange, yellow, green, lime, teal, blue, purple, magenta 등 10개 색상군. 각 색상군은 **100~1000 (12단계, 100/200/250/300/400/500/600/700/800/850/900/1000)**의 비선형 스케일을 가진다.
2. **Neutral colors** — 라이트/다크 모드가 별도 스케일을 가짐.
   - 라이트: 0~1200 (13단계) + alpha 5종
   - 다크: -100~1200 (15단계) + alpha 8종
3. **Alpha (투명) colors** — 배경 위에 겹쳐 쓰는 반투명 색상.
4. **Chart / 데이터 시각화 컬러** — 차트 전용 팔레트 (별도 파운데이션 페이지로 존재).

> Accent 색상군은 **의미를 갖지 않는 장식용**이다. 아바타 색상, 태그, 차트 시리즈처럼 "어떤 색이든 무방한" 곳에 사용하고, 상태를 나타내는 곳에는 아래 시맨틱 토큰을 사용한다.

---

## 🏷 시맨틱 컬러 토큰

### 네이밍 패턴

```
color.[property].[role].[emphasis].[state]
```

| 구성 요소 | 의미 | 값 예시 |
|---|---|---|
| **property** | 적용 대상 | background, border, text, icon, link, blanket(오버레이 딤 처리), chart, skeleton |
| **role** | 시맨틱 의미 | neutral, brand, information, success, warning, danger, discovery, accent(gray/red/green/blue/...), inverse, input |
| **emphasis** | 대비 강도 (3단계) | bold(최대 강조) → default(기본, 접미사 생략) → subtle/subtlest(최소 강조) |
| **state** (선택) | 인터랙션 상태 | hovered, pressed, selected, focused, disabled |

### 대표 토큰 예시

| 토큰 | 설명 |
|---|---|
| `color.background.brand.bold` | 브랜드 강조 배경. **화면당 하나의 주요 액션에만 예약**해서 사용 |
| `color.background.brand.subtlest` / `.boldest` | 브랜드를 약하게/강하게 드러내는 배경 |
| `color.background.danger.bold` (+`.hovered`) | 위험/파괴적 액션 배경 |
| `color.text` | 기본 본문 텍스트 (수정자 없음) |
| `color.text.subtle` | 라벨·메타데이터 전용. **긴 본문에는 사용 금지** |
| `color.icon.success` / `color.icon.accent.green` | 상태·장식용 아이콘 색상 |
| `color.border` / `color.border.focused` | 기본 테두리 / 포커스 링 (반드시 `border.width.focused`와 함께 사용) |

---

## 🌗 다크모드 / 테마

- 토큰 이름은 테마와 무관하게 동일하고, **테마별로 매핑되는 실제 값만 달라진다.** (예: 버튼 색이 라이트에서 700단계라면 다크에서는 400단계로 재매핑되는 식 — 단순 RGB 반전이 아니다.)
- HTML 루트에 `data-color-mode="light|dark|auto"`(사용자 선호), `data-theme="dark"`(현재 적용된 테마) 속성으로 제어.
- 토큰은 CSS 커스텀 프로퍼티로 노출: `var(--ds-surface-raised)`.
- 2022.10 발표 → 2023 Q2 Jira Cloud 전체 GA. 하이 콘트라스트 테마도 동일한 토큰 이름을 공유한다.

---

## 🔵 브랜드 컬러

| 구분 | 값 | 비고 |
|---|---|---|
| 현재 브랜드 블루 (`color.background.brand.bold`) | **#1868DB** (Blue700) | 현행 디자인 토큰 시스템 기준 |
| 레거시 브랜드 블루 | **#0052CC** | 구 AUI(Atlassian UI) 시대부터 널리 알려진 값. 서드파티 브랜드 컬러 사이트에서 여전히 자주 인용됨 |
| Squid Ink (텍스트/다크 뉴트럴) | #172B4D | 브랜드 가이드라인 인용 (2차 출처, 참고용) |

> Jira 전용 별도 브랜드 토큰은 확인되지 않았다 — Jira는 역사적으로 Atlassian 브랜드 블루 계열을 그대로 사용해왔다.

**사용 원칙**: 브랜드 블루(bold)는 화면당 하나의 주요 액션에만 예약해서 사용하고, 남용하지 않는다.

---

## ♿ 접근성 & 대비 기준 (WCAG AA)

| 대상 | 최소 대비율 |
|---|---|
| 24px 이상의 텍스트/필수 UI 그래픽 | **3:1** (WCAG 1.4.11) |
| 24px 미만의 일반 텍스트 | **4.5:1** (WCAG 1.4.3) |

> **색상만으로 의미를 전달하지 않는다.** 색맹·저시력 사용자를 위해 아이콘, 텍스트 라벨, 패턴 등 색 외의 표시 수단을 항상 병행한다.

---

## ✅ 사용 원칙 요약

- **시맨틱 역할(role)에는 고정된 의미가 있다.** brand/danger/warning/success/information/discovery는 의미 전달용, accent 10색상군은 순수 장식용 — 혼용하지 않는다.
- **뉴트럴이 화면의 대부분을 차지한다.** 채도 높은 accent·시맨틱 컬러는 절제해서 사용한다.
- **`color.text.subtle`은 라벨/메타데이터 전용.** 본문 프로즈에는 사용하지 않는다.
- **포커스 링은 반드시 두 토큰을 함께 사용**: `border.width.focused` + `color.border.focused`.

---

## 🧑‍💻 개발 구현

```js
import { token } from '@atlaskit/tokens';

// 두 번째 인자는 폴백 값
color: token('color.background.selected.bold', 'B400')
```

순수 CSS에서는 커스텀 프로퍼티로 직접 참조한다.

```css
background: var(--ds-surface-raised, #eeeeee);
```

---

## 📚 참고 자료

- [Foundations · Color](https://atlassian.design/foundations/color)
- [Foundations · Accessibility](https://atlassian.design/foundations/accessibility)
- [Design tokens](https://atlassian.design/foundations/tokens/design-tokens)
- [Introducing design tokens, new colour foundations, and dark mode](https://community.developer.atlassian.com/t/introducing-design-tokens-new-colour-foundations-and-dark-mode/62258)
- [Start using design tokens in your apps and try dark theme in Jira Cloud](https://community.developer.atlassian.com/t/start-using-design-tokens-in-your-apps-and-try-dark-theme-in-jira-cloud/64147)

> ⚠️ 전체 토큰-hex 매핑표는 `atlassian.design/components/tokens/all-tokens`(JS 렌더링 페이지)에서 실시간으로 확인하는 것을 권장한다. 본 문서의 hex 값은 조사 시점 기준이며 버전에 따라 달라질 수 있다.
