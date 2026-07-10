# 아이콘 시스템 (Iconography)

> **"Atlassian icons"는 2025년 6년 만에 전면 개편된 아이콘 시스템이다.** 24px 캔버스·2px 스트로크의 구형 세트(350개+)를 16px 캔버스·1.5px 스트로크로 재설계해, 새 타이포그래피(Atlassian Sans)의 둥근 형태와 시각적으로 조화를 이루도록 만들었다.

본 문서는 [atlassian.design/foundations/iconography](https://atlassian.design/foundations/iconography)와 Atlassian 디자인 블로그를 분석하여 재구성했다.

---

## 🖼 시스템 개요

- 명칭: **Atlassian icons** (Foundations → Iconography, 컴포넌트: `/components/icon`). React 컴포넌트, Figma 라이브러리, 아이콘 탐색기 형태로 제공.
- **개편 규모** (2025.09 블로그 기준): 1,000개 이상의 커스텀 아이콘을 감사, 275개 이상의 신규 아이콘을 내부 "Icon Lab"(전용 Figma 플러그인)에 기여, 16,000개 이상의 사용처에 피처 플래그로 순차 롤아웃.
- **캔버스/스트로크**: 코어 세트는 **16px 캔버스 / 1.5px 스트로크**(스트로크 가장자리를 픽셀 그리드에 정렬해 저해상도에서도 선명). 좁은 공간(태그·메타데이터)용 **12px 유틸리티 세트**도 별도 존재.

---

## 📐 스타일 규칙

- **외곽은 둥글게, 내부 코너는 날카롭게** — 둥근 외곽은 새 타이포그래피/UI 요소의 둥근 형태와 조화를 이루고, 날카로운 내부는 대담함을 더한다.
- **선 끝은 각진(square) 캡** 사용 — 둥근 끝이 아님.
- 정면에서 바라본 형태로 그리며, 3D/사선 원근은 지양한다.

**Do**: 기존 아이콘 재사용(일관성), 널리 통용되는 시각적 은유 사용
**Avoid**: 이미 있는 아이콘을 새로 만드는 것, 작은 크기에서 읽히지 않는 과도한 디테일, 3D/사선 형태, 둥근 내부 앵커 포인트

---

## 📏 크기

| 크기 | 픽셀 | 용도 |
|---|---|---|
| Medium (기본) | 16px | 본문 텍스트/앱 밀도와 균형을 이루는 기본 크기 |
| Small | 12px | 절제해서 사용 — 셰브런, 필드 유효성 검사, 좁은 공간, 보조 액션 |

> 개발 컴포넌트 API(`@atlaskit`/Forge UI Kit)에서는 `size="small"`(16px)/`size="medium"`(24px, 기본)로 약간 다르게 명명되어 있으니 실제 구현 시 해당 패키지 문서를 확인한다. `size="large"`와 `primaryColor`/`secondaryColor` prop은 2025년 말 제거 예정인 **deprecated** 옵션이다.

---

## 🎯 사용 원칙

4가지 핵심 원칙:

1. **보편적 이해(Universal understanding)** — 널리 통용되는 기호와 확립된 시각적 은유 사용
2. **단순함과 디테일의 균형** — 빠르게 인식되면서도 의미를 전달할 만큼의 디테일 유지
3. **시각적 조화** — 세트 전체에서 일관된 크기·형태·스타일
4. **의도적 사용** — "아이콘은 강력한 신호이지만, 잘못 쓰면 혼란과 시각적 잡음을 더한다. 가능하면 텍스트 라벨로 아이콘을 보완하라."

> 아이콘은 텍스트를 **대체**하지 않고 **보강**한다. 공간이 매우 제한적이고 관습적으로 확립된 경우가 아니라면 항상 텍스트 라벨과 함께 사용한다.

---

## ♿ 접근성

- Icon 컴포넌트의 `label` prop이 접근성 이름을 담당한다: **"눈에 보이는 텍스트 라벨이 없을 때 아이콘을 설명하는 텍스트가 필요하다."**
- 아이콘이 이미 라벨이 있는 요소(예: 라벨 붙은 버튼) 안/옆에 있다면 **별도 라벨이 필요 없다** — 아이콘은 장식용으로 처리된다.
- `label`에 **빈 문자열**을 전달하면 명시적으로 "장식용, 스크린 리더에서 숨김" 처리된다.
- 아이콘 단독 사용 시: "기존 텍스트 라벨이나 접근성 텍스트가 없다면 `label` prop으로 명확한 라벨을 제공하라."

---

## 🎨 컬러

- **기본적으로 아이콘은 현재 텍스트 색상을 상속**한다 (`fill="currentcolor"`) — 별도 지정이 없으면 주변 텍스트와 자동으로 맞춰진다.
- 명시적 색상 지정 시 raw hex가 아닌 **`color.` 접두 토큰**만 허용 (예: `color.icon.*`, `color.text.*` 공유 토큰).
- 상태 표시용 시맨틱 토큰 존재: `color.icon.success`, `color.icon.warning`, `color.icon.danger` 등 — 앱 전반에서 동일한 토큰 패밀리로 상태를 표현한다.
- 대비를 위한 예외: 경고(warning) 배경(`background.warning.bold`) 위에서는 흰색이 아닌 **`icon/text-warning-inverse`(어두운 색)**를 짝지어 사용해야 읽기 쉽다.

---

## 📚 참고 자료

- [Foundations · Iconography](https://atlassian.design/foundations/iconography)
- [Components · Icon](https://atlassian.design/components/icon)
- [Behind the screens: building Atlassian's new icon system](https://www.atlassian.com/blog/design/behind-the-screens-building-atlassians-new-icon-system)
- [What's new — Typography and iconography updates](https://atlassian.design/whats-new/typography-and-iconography-updates)
