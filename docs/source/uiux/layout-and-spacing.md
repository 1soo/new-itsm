# 레이아웃 & 스페이싱 (Layout, Spacing, Elevation)

> **모든 여백·그리드·그림자·모서리는 8px 기반 스케일의 토큰으로 정의된다.** 값을 직접 하드코딩하지 않고 `space.*`, `elevation.*`, `radius.*` 토큰을 사용하는 것이 원칙이다.

본 문서는 [atlassian.design/foundations](https://atlassian.design/foundations)의 Spacing/Grid/Elevation/Radius 페이지와 Jira 내비게이션 관련 공식 문서를 분석하여 재구성했다.

---

## 📏 스페이싱 스케일

기준 단위: **8px** (`space.100`). 토큰 이름은 기준 단위 대비 백분율이다.

| 토큰 | rem | px | 용도 |
|---|---|---|---|
| `space.0` | 0 | 0px | — |
| `space.025` | 0.125rem | 2px | 매우 좁은 간격 |
| `space.050` | 0.25rem | 4px | 컴포넌트 내부 밀착 간격, 아이콘-텍스트 간격 |
| `space.075` | 0.375rem | 6px | — |
| `space.100` | 0.5rem | **8px (기준)** | 컴포넌트 기본 패딩 |
| `space.150` | 0.75rem | 12px | 여유로운 컴포넌트 패딩, 아바타 간격 |
| `space.200` | 1rem | 16px | 카드 요소 간격 |
| `space.250` | 1.25rem | 20px | — |
| `space.300` | 1.5rem | 24px | 카드/섹션 요소 |
| `space.400` | 2rem | 32px | 페이지 레벨 레이아웃 |
| `space.500` | 2.5rem | 40px | — |
| `space.600` | 3rem | 48px | — |
| `space.800` | 4rem | 64px | — |
| `space.1000` | 5rem | 80px | 페이지 레벨 레이아웃 |

- **음수 토큰**(`space.negative.025` ~ `.400`, -2px~-32px)도 존재하며 오버랩/블리드 효과에 사용한다 (우선 `Bleed` 프리미티브 사용을 권장).
- 사용 구간 가이드: **0~8px** = 컴포넌트 내부 밀착 요소, **12~24px** = 여유로운 컴포넌트 패딩, **32~80px** = 페이지 레벨 레이아웃.

---

## 🧮 그리드 시스템

- **12컬럼 그리드**, 공식 6단계 브레이크포인트에 맞춰 조정.
- 두 가지 그리드 타입:
  - **Fluid grid** — 뷰포트 너비를 꽉 채우며 확장. 칸반 보드처럼 정보 밀도가 높은 화면에 권장.
  - **Fixed grid** — 컨테이너에 최대 너비 적용. `fixed-narrow`/`fixed-wide` 변형 존재. 데이터 테이블처럼 콘텐츠 밀도가 높은 화면에 권장.
- 콘텐츠는 최소 3컬럼 이상, 최대 12컬럼까지 차지하도록 권장.

> ⚠️ 공식 6단계 브레이크포인트의 정확한 px 구간은 페이지가 JS 렌더링이라 직접 확인이 어려웠다. 참고용 근사치: 320px(모바일 최소) / 768px(태블릿) / 1280px(데스크톱), 거터 24px(`space.300`), 페이지 패딩 32px(`space.400`). **실제 구현 전 [breakpoints 페이지](https://atlassian.design/components/primitives/responsive/breakpoints)에서 최신 값을 직접 확인할 것.**

---

## 🧱 Elevation(고도) 시스템

서피스가 쌓이는 순서를 나타내는 5단계(오버플로 포함).

| 레벨 | 설명 | 예시 | 토큰 |
|---|---|---|---|
| **Sunken** | 가장 낮음. 콘텐츠의 "우묵한 배경" | 칸반 보드 컬럼 | `elevation.surface.sunken` (기본 서피스에서만 사용 가능) |
| **Default** | 기준선, 그림자 없음 | Confluence 페이지 | `elevation.surface` |
| **Raised** | 기본보다 살짝 떠 있음 | Jira/Trello 카드 | `elevation.surface.raised` + `elevation.shadow.raised` |
| **Overlay** | 가장 높음. 다른 UI 위에 뜨는 레이어 | 모달, 드롭다운, 툴팁, 팝오버 | `elevation.surface.overlay` + `elevation.shadow.overlay` |
| **Overflow** (특수) | 스크롤로 가려진 콘텐츠 표시 | 테이블 가로 스크롤 | `elevation.shadow.overflow.spread` / `.perimeter` |

> **서로 다른 elevation 레벨의 surface/shadow 토큰을 섞어 쓰지 않는다.**

---

## 🔲 테두리 반경 (Radius)

| 토큰 | 값 | 용도 |
|---|---|---|
| `radius.xsmall` | 2px | 배지, 체크박스, 아바타 라벨, 키보드 단축키 |
| `radius.small` | 4px | 라벨, 로젠지, 타임스탬프, 태그, 툴팁 컨테이너 |
| `radius.medium` | 6px | 버튼, 인풋, 텍스트 영역, 셀렉트, 내비게이션 아이템 |
| `radius.large` | 8px | 카드, 페이지 내 컨테이너, 드롭다운 메뉴 |
| `radius.xlarge` | 12px | 모달, 칸반 컬럼, 테이블 등 큰 컨테이너 |
| `radius.xxlarge` | 16px | 비디오 플레이어 컨테이너 전용 |
| `radius.full` | 999px (pill) | 원형/아바타/사용자 관련 UI |
| `radius.tile` | 25% | 타일 컴포넌트 전용 |

**포커스 링 규칙**: 링은 요소에서 2px 오프셋, 모서리 반경은 **기본 반경 + 2px**. (`radius.focus.xsmall`=4px ~ `radius.focus.xxlarge`=18px)

**테두리 두께**: `border.width`=1px(기본, 인풋/카드/구분선), `border.width.selected`=2px(탭 밑줄 등), `border.width.focused`=2px(포커스 링).

---

## 🧭 레이어링 / Z-index

- 별도의 z-index 숫자 스케일은 공개되어 있지 않다. 대신 **elevation 토큰 + 상대적 스태킹 순서**로 관리한다.
- 암묵적 스태킹 순서(낮음→높음): Navigation → Inline dialogs → Popovers → Modals → Flags → Tooltips.
- 원칙: "같은 elevation 스타일이라도 각 UI는 스택 순서를 나타내기 위해 서로 다른 z-index를 적용해야 한다."

---

## 🧩 Jira 화면 레이아웃 패턴

2020년(Q1~Q2) 새 내비게이션이 Jira Cloud 전체에 롤아웃되었고(구 내비게이션은 2020.09 이후 선택 불가), Server/Data Center는 영향받지 않았다.

### 상단 구조

- **Top nav bar**: 제품 전환기, "For you"/"Recent"/"Starred" 바로가기, 프로젝트 브라우저, Dashboards, 전역 검색 + 생성 액션, 알림, 설정, 계정 메뉴.
- **좌측 글로벌 사이드바**: "For you"(개인화 허브), "Recent"(최근 방문), "Starred"(북마크). "More → Customize sidebar"로 항목 표시/순서 변경 가능. `Ctrl+[`로 접기, 가장자리 드래그로 리사이즈(고정 픽셀 폭 아님).
- **프로젝트 레벨 내비게이션**: 프로젝트 내 가로 탭(관리자가 순서 변경/제거 가능), 프로젝트 설정은 사이드바 하단.

### 앱 확장 지점 (Forge/Connect 개발 시)

상단 "Apps" 서브메뉴(전역, 전체 화면 앱), 프로젝트 사이드바(프로젝트 범위 앱 — **권장 위치**), 프로젝트 설정 메뉴, Jira 관리자 설정, 사용자 프로필 메뉴. 아이콘은 최소 24×24 컬러 SVG.

### 이슈 뷰 레이아웃

- 우측 컨텍스트 필드 패널은 **"Issue Layout"**(관리자 설정)에서 워크아이템 타입별로 필드를 "Details"/"More fields" 그룹으로 드래그 앤 드롭 구성. 넓은 화면에서는 우측 컬럼, 좁은 화면(단일 컬럼)에서는 하단으로 이동.
- "Glances"라는 작은 UI 요소가 우측 사이드바의 상태 필드 아래, 담당자/우선순위/라벨과 함께 표시된다.
- 좌측 글로벌 사이드바를 접으면 디테일 패널 폭이 377px → 321px로 변함(고정폭이 아닌 가변).
- 이슈 뷰 최대 폭은 패딩/여백/스크롤바 포함 약 1680px(커뮤니티 포럼 기준, 비공식이나 구체적).

### 보드/백로그 카드

카드는 3개 정보 레이어로 구성: ① 상단 워크아이템 요약 ② 커스텀 필드 ③ 워크 타입/우선순위/담당자/견적. 보드당 최대 3개의 추가 필드를 구성할 수 있고, 백로그 뷰와 액티브 스프린트 뷰에서 다르게 설정 가능. 워크 타입/우선순위/담당자 등으로 카드에 색상을 입힐 수 있다.

> ADS 컴포넌트 카탈로그에는 "Issue card"/"Board card"라는 별도 컴포넌트가 없다 — 이는 Lozenge(상태), Avatar(담당자), Badge/Tag(라벨), Page/Panel(레이아웃) 등 범용 컴포넌트의 제품 레벨 조합이다.

---

## 📚 참고 자료

- [Foundations · Spacing](https://atlassian.design/foundations/spacing)
- [Foundations · Grid (beta)](https://atlassian.design/foundations/grid-beta)
- [Foundations · Elevation](https://atlassian.design/foundations/elevation)
- [Foundations · Radius](https://atlassian.design/foundations/radius)
- [Jira platform navigation](https://developer.atlassian.com/cloud/jira/platform/navigation/)
- [What is the new navigation in Jira](https://support.atlassian.com/jira-software-cloud/docs/what-is-the-new-navigation-in-jira/)
- [Customize cards (Jira boards)](https://support.atlassian.com/jira-software-cloud/docs/customize-cards/)

> ⚠️ 조사 과정에서 elevation 그림자 hex 값이 출처에 따라 `#091E42` 계열/`#1E1F21` 계열로 갈렸다(브랜드 리브랜드에 따른 뉴트럴 베이스 색 변경으로 추정). 구현 시 실제 배포된 CSS 변수 값을 우선 신뢰할 것.
