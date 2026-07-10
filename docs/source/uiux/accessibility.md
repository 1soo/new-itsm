# 접근성 (Accessibility)

> **"When apps are accessible, the experience is better for everyone."** ADS 컴포넌트는 키보드 지원과 합리적인 ARIA를 기본 내장하지만, 이것이 제품 전체의 접근성을 보장하지는 않는다 — 패턴·콘텐츠·인터랙션까지 팀이 직접 검토해야 end-to-end 접근성이 완성된다.

본 문서는 [atlassian.design/foundations/accessibility](https://atlassian.design/foundations/accessibility)와 [atlassian.com/accessibility](https://www.atlassian.com/accessibility)를 분석하여 재구성했다.

---

## ✅ 접근성 기준

- **공식 컴플라이언스 목표**: **WCAG 2.2 AA** 준수를 공개적으로 약속.
- Cloud/Data Center 제품군(Jira, Confluence, Bitbucket, JSM, Trello 등)에 대해 **VPAT(Voluntary Product Accessibility Template)**를 정기적으로 발행·갱신.
- 정책 공약: 설계 단계부터 접근성 내장, 엔지니어링 레벨의 자동화된 접근성 테스트, 조직 전체 필수 교육, 분기별 서드파티 감사, 치명적 이슈 **60일** / 높은 심각도 이슈 **120일** 이내 개선 SLA.
- 지원 채널: Accessibility Support Portal(`a11ysupport.atlassian.net`) 또는 `a11y@atlassian.com`.

---

## 🎯 포커스 상태

- 토큰 `border.width.focused` = **2px** — 반드시 `color.border.focused`(색상 토큰)와 **함께** 사용해 포커스 링을 렌더링한다.
- 용도: 키보드로 Tab 이동 시 현재 활성 요소를 시각적으로 표시.
- 구현 가이드:
  - **코드**: 상호작용이 필요한 모든 요소를 `Focusable` 컴포넌트로 감싼다.
  - **디자인**: Figma 라이브러리의 Focus Ring 컴포넌트 사용.
- 포커스 링은 요소에서 2px 오프셋, 모서리 반경은 기본 반경 + 2px (자세한 값은 [layout-and-spacing.md](./layout-and-spacing.md) 참고).

---

## 🎨 색상 대비 기준

| 대상 | 최소 대비율 | WCAG 기준 |
|---|---|---|
| 24px 이상 텍스트/필수 UI 그래픽 | 3:1 | 1.4.11 (Non-text Contrast) |
| 24px 미만 일반 텍스트 | 4.5:1 | 1.4.3 (Contrast Minimum) |

> **색상만으로 의미를 전달하지 않는다.** 상세 내용은 [color.md](./color.md) 참고.

각 색상 토큰은 테마(라이트/다크)별로 다른 값에 매핑되므로, 토큰만 사용하면 테마 전환 시에도 대비 기준을 별도로 재검증할 필요가 없다.

---

## 🏗 시맨틱 HTML / ARIA 가이드

- **시맨틱 요소를 우선 사용**한다 (`header`, `nav`, `footer` 등) — 범용 `div`/`span` 대신 브라우저와 보조기술이 요소의 의미를 정확히 파악하도록 한다.
- ADS 컴포넌트는 "합리적인 ARIA 사용"과 키보드 지원을 기본 내장하지만, 커스텀 인터랙션을 만들 때는 아래 원칙을 따른다 (Pragmatic Drag and Drop 컴포넌트 가이드 기준, 복잡한 인터랙션 전반에 적용 가능한 패턴):
  - **제스처 전용 대신 눈에 보이는 컨트롤 제공**: 버튼/메뉴/폼처럼 보조기술 사용자도 동일한 결과를 트리거할 수 있는 명시적 컨트롤을 함께 제공한다.
  - **접근 가능한 이름에는 대상 객체를 포함**: 예) "Move task 'clean dishes' to top of backlog" — 단순히 "Move"가 아니라 무엇을 어디로 옮기는지 명시.
  - **복잡한 인터랙션을 방향키 전용으로 설계하지 않는다** — 스크린리더 모드 전환·맥락 손실 문제가 있으므로 메뉴 기반 대안을 함께 제공한다.
  - **라이브 리전으로 상태 변화를 알린다**: 예) `announce('Task "Clean dishes" moved to list "Doing" from "Todo".')` — 항목명과 이전/이후 위치를 함께 안내.

---

## 📚 참고 자료

- [Foundations · Accessibility](https://atlassian.design/foundations/accessibility)
- [Atlassian Accessibility Statement](https://www.atlassian.com/accessibility)
- [Foundations · Border (focus ring)](https://atlassian.design/foundations/border)
- [Pragmatic Drag and Drop · Accessibility guidelines](https://atlassian.design/components/pragmatic-drag-and-drop/accessibility-guidelines)
- [Create accessible designs using the Figma A11y Annotation Kit](https://medium.com/designing-atlassian/create-accessible-designs-using-the-figma-a11y-annotation-kit-35371f00dac5)
