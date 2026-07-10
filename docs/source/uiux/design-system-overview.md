# Atlassian 디자인 시스템 개요 (Design Philosophy & Structure)

> **Atlassian Design System(ADS)은 Jira·Confluence 등 20개 이상의 Atlassian 제품이 공유하는 단일 디자인 언어다.** "Better teamwork by design"이라는 비전 아래 Foundations(토대) · Components(컴포넌트) · Patterns(패턴) · Tokens(토큰)를 하나의 체계로 제공해, 서로 다른 제품에서도 일관되고 익숙한 경험을 만든다.

본 문서는 공식 디자인 시스템 사이트 [atlassian.design](https://atlassian.design)과 Atlassian 개발자 커뮤니티·공식 블로그 게시물을 분석하여 재구성한 종합 정리다.

---

## 📌 ADS란 무엇인가

- **정의**: "디자인 가이드라인, 파운데이션, 도구, 컴포넌트의 집합체." Jira, Confluence를 비롯한 Atlassian 앱에서 고품질 UI를 만들기 쉽게 하는 것이 목표다.
- **대상**: Atlassian 내부 디자이너·개발자·콘텐츠 디자이너뿐 아니라 Marketplace 파트너, 외부 앱 개발자까지 포함한다. `Get started` 섹션이 **Design / Develop / Content design / Figma libraries** 4개 직군별로 나뉘어 있는 것도 이 때문이다.
- **ADG vs AtlasKit**:
  - **ADG(Atlassian Design Guidelines)** = 디자인 원칙·가이드라인 문서 체계 (2012년 시작).
  - **AtlasKit** = ADG를 구현한 실제 React 컴포넌트 라이브러리 (`@atlaskit/*` npm 패키지, [atlaskit.atlassian.com](https://atlaskit.atlassian.com)).
  - 즉 **ADG = 규칙, AtlasKit = 코드**. 2019년부터 두 체계를 하나의 사이트(atlassian.design)로 통합했다.

---

## 🧭 사이트 구조

### Foundations (디자인 토대)

| 항목 | 설명 |
|---|---|
| Tokens | UI에 대한 디자인 결정을 이름과 값으로 저장하는 단일 소스 |
| Accessibility | 모든 능력의 사용자가 상호작용·이해·탐색 가능하도록 보장 |
| Content | 명확하고 간결하며 대화체인 언어 |
| Spacing | 페이지 레이아웃과 UI 구성을 단순화 |
| Grid | 콘텐츠 배치와 일관된 페이지 레이아웃 |
| Color | 브랜드를 구분하고 앱 전반의 경험을 강화 |
| Typography | 폰트와 텍스트 스타일 체계 |
| Motion | 목적 기반의 모션 언어 |
| Iconography | 명령·동작의 시각적 표현 |
| Illustrations | 복잡한 개념을 단순하게 전달 |
| Elevation | 레이어드 서피스를 통한 UI의 기반 |
| Border / Radius | 경계 정의와 모서리 둥글기 표준화 |

### Components (재사용 가능한 빌딩 블록)

| 카테고리 | 대표 컴포넌트 |
|---|---|
| Forms and inputs | Button, Textfield, Select, Checkbox, Radio, Toggle, Datetime picker |
| Navigation | Navigation system(Top nav/Side nav), Breadcrumbs, Menu, Tabs, Pagination |
| Messaging | Banner, Section message, Flag, Empty state |
| Loading | Spinner, Skeleton, Progress bar |
| Labels | Badge, Lozenge, Tag, Date label |
| Overlays and layering | Modal dialog, Popup, Tooltip, Blanket |
| Text and data display | Table, Dynamic table, Avatar, Comment, Heading |
| Layout and structure | Page, Page header, Panel |

> 상세 카탈로그는 [components.md](./components.md) 참고.

---

## 🎯 핵심 가치와 원칙

ADS는 30여 명(디자이너·엔지니어·콘텐츠 전문가)이 참여한 워크숍을 통해 **"이것 vs 저것"의 트레이드오프 형태**로 3가지 가치와 원칙을 정의했다.

| 가치 | 설명 | 짝을 이루는 원칙 |
|---|---|---|
| **Foundational (기초적)** | 모두가 확신을 갖고 쌓아 올릴 수 있는 견고한 기반 제공 | 방대한 패턴보다 **신뢰할 수 있는 기본기**를 우선 |
| **Harmonious (조화로운)** | 응집력 있는 제품군을 이루는 빌딩 블록 | 개별 기능 전달보다 **시스템 전체의 요구**를 우선 |
| **Empowering, for everyone (모두를 위한 역량 강화)** | 직군·숙련도와 무관하게 누구나 시스템에 접근 가능 | 순간적 도움보다 **사람들을 여정에 동참**시키는 것을 우선 |

---

## 🗣 브랜드 톤 (Voice & Personality)

시스템 값과 별개로, Atlassian 콘텐츠/브랜드 보이스는 **"Bold, optimistic, and practical (with a wink)"** — 대담하고 낙관적이며 실용적(약간의 위트 포함)으로 정의된다. 다만 맥락에 따라 강도가 다르다: 마케팅/영업 영역은 "대담함"을 강조하고, 제품 내 실사용 경험에서는 "실용성"에 무게를 둬 사용자가 효율적으로 작업하도록 돕는다.

---

## 🕰 역사와 진화

| 시기 | 사건 |
|---|---|
| 2012 | Jürgen Spangl가 Head of Design으로 합류, ADG(Atlassian Design Guidelines) 시작 |
| ~2014 | ADG 2 — 브랜드와 제품을 더 가깝게 연결 |
| 2016 | **ADG 3** — 현재까지 이어지는 룩앤필의 기반이 된 대규모 리브랜드. **AtlasKit이 함께 출시**되며 구현 기술이 React로 전환 |
| 2017.09 | 브랜드/마케팅 서체로 **Charlie Sans** 도입 (제품 UI 폰트는 아님) |
| 2019 | ADG + AtlasKit 콘텐츠·컴포넌트를 하나의 사이트로 통합하는 리디자인 시작 → 현재의 atlassian.design |
| 2022.10 | **디자인 토큰**, 새 컬러 파운데이션, 다크모드 도입 발표 |
| 2023.04~06 | Jira Cloud 전체에 다크모드 GA(정식 출시) |
| 2022~2023 | Pentagram(Michael Bierut) 협업으로 회사 전체 **비주얼 아이덴티티 리브랜드** 진행, 제품별 로고 개편(예: Jira 마크는 "코드 브래킷" 모티프) |
| 2024~2025 | 제품 UI 서체를 시스템 폰트(SF Pro/Segoe UI)에서 **Atlassian Sans / Atlassian Mono**로 전면 교체, 새 아이콘 시스템 도입 |
| 2026 | ADS를 "AI 시대의 컨텍스트 엔진"으로 재정의하는 전략 발표 — 강한 시맨틱 구조로 AI가 시스템을 이해·추론하도록 지원 |

> ⚠️ 참고: "2023년 리브랜드"로 알려진 경우가 있으나, 실제로는 **2017년(브랜드 서체 Charlie Sans)**과 **2024~2025년(제품 서체·아이콘 전면 개편)**의 두 개 별도 사건이다. 연도를 인용할 때는 이 구분에 유의한다.

---

## 📚 참고 자료

- [atlassian.design](https://atlassian.design/) — 공식 디자인 시스템 홈
- [About Atlassian Design System](https://atlassian.design/get-started/about-atlassian-design-system)
- [Vision, values, and principles](https://atlassian.design/resources/atlassian-design-system-values-principles)
- [Atlassian Design System: Building the context engine for the AI era](https://www.atlassian.com/blog/ai-at-work/atlassian-design-system-building-the-context-engine-for-the-ai-era)
- [Our journey to a reimagined visual system](https://www.atlassian.com/blog/announcements/our-journey-to-a-reimagined-visual-system)
- [Introducing design tokens, new colour foundations, and dark mode](https://community.developer.atlassian.com/t/introducing-design-tokens-new-colour-foundations-and-dark-mode/62258)
