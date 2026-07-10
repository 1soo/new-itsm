# 컴포넌트 카탈로그 (Components)

> **AtlasKit(`@atlaskit/*`) 컴포넌트는 ADS의 코드 구현체다.** 아래는 [atlassian.design/components](https://atlassian.design/components)의 공식 카테고리 분류를 기준으로 정리한 핵심 컴포넌트 목록이다.

본 문서는 atlassian.design 컴포넌트 카탈로그와 Atlassian 디자인 블로그를 분석하여 재구성했다. 개별 컴포넌트의 상세 props/variant는 각 컴포넌트 페이지에서 최신 상태를 확인한다.

---

## 🔘 Actions (버튼류)

| 컴포넌트 | 설명 |
|---|---|
| **Button** | 이벤트/액션을 트리거하는 기본 버튼 |
| **Icon Button** | 공간이 제한적일 때 사용하는 아이콘 전용 버튼 |
| **Link Button** | 액션이 아닌 페이지 이동(네비게이션) 의미의 버튼 |
| **Link Icon Button** | 공통 링크/페이지로 이동하는 아이콘 전용 버튼 |
| **Split Button** | 보조 액션 메뉴가 붙은 버튼 |
| **Button Group** | 관련 버튼들을 묶어 배치하는 컨테이너 |

> 구 **Button(legacy)**는 위 5종 목적별 버튼으로 세분화되며 deprecated 처리되었다 (접근성/시맨틱/성능 개선 목적).

---

## 📝 Forms and inputs

| 컴포넌트 | 설명 |
|---|---|
| **Text field** | 텍스트를 입력/수정하는 인풋. `isDisabled`/`isInvalid`/`isReadOnly`/`isRequired`/`spacing="compact"` 등 지원 |
| **Text area** | 여러 줄에 걸친 장문 텍스트 입력 |
| **Select** | 단일/다중 선택. Standard, Multi, Async, Async Creatable, Creatable, Checkbox, Radio, Popup, Country 등 다양한 변형 존재 |
| **Checkbox** / **Checkbox group** | 하나 이상의 옵션 선택 |
| **Radio** / **Radio group** | 여러 선택지 중 단 하나만 선택 |
| **Toggle** | 활성/비활성 스위치 |
| **Form** | 정보 입력을 위한 레이아웃/유효성 검사 래퍼 |
| **Datetime picker** | 날짜와 시간을 함께 선택 (모달 내 변형 포함) |
| **Range** | 슬라이더로 근사값 선택 |
| **Focus ring** | 키보드 포커스가 있는 항목을 명확히 표시 (접근성 헬퍼) |

---

## 🧭 Navigation

새로운 **Navigation system**이 구 Atlassian navigation / Side navigation을 대체한다. 구성:

| 컴포넌트 | 설명 |
|---|---|
| **Top navigation** | 상단 바의 전역 액션(검색, 생성 등) |
| **Side nav items** | 사이드바 메뉴 아이템 |
| **Layout** | 내비게이션 + 콘텐츠 영역의 전체 구조 정의 |
| **Breadcrumbs** (+ item, stateless) | 사이트/앱 내 현재 위치 표시 |
| **Menu** / **Dropdown menu** | 탐색/액션을 위한 옵션 목록 |
| **Tabs** (+ Tab content) | 같은 페이지 내 유사 정보를 그룹화 |
| **Pagination** | 대량 콘텐츠를 페이지 단위로 분할 |
| **Link** | 앱/사이트 내 새 위치로 이동 |

> 디자인 근거(Atlassian 블로그): 제품 내비게이션을 상단 바에서 **사이드바로 이동**시키고, 상단 바는 검색·생성 같은 전역 액션 전용으로 남겨 업계 표준적인 "사이드바 우선" 멘탈 모델에 맞췄다.

**Deprecated (마이그레이션 대상)**: Atlassian navigation(구 상단 내비) / Side navigation(구 사이드바) / Layout grid / Page layout(구 레이아웃) → 모두 **Navigation system**으로 이전 권장.

---

## 💬 Feedback & Status (메시징)

| 컴포넌트 | 설명 |
|---|---|
| **Banner** | 화면 상단에 나타나 콘텐츠를 밀어내는 배너. **시스템 레벨의 치명적 메시지 전용**(데이터/기능 손실 경고, 에러) — 일반 안내에는 사용하지 않음 |
| **Section message** | 특정 영역 위에 표시되는 알림 (예: Jira 이슈 상단). `appearance`로 error/warning 등 지정 |
| **Flag** | 확인/알림/승인 등 최소한의 상호작용이 필요한 토스트형 알림. `Flags provider`/`Flag group`으로 관리 |
| **Empty state** | 데이터가 없을 때 표시. 스캔하기 쉬운 제목 + 이유 + 다음 단계 CTA(명령형 동사) 권장 |
| **Spinner** | 로딩 중임을 나타내는 애니메이션 아이콘. xsmall~xlarge 크기, 항상 설명 라벨과 함께 사용 |
| **Skeleton** (+ Avatar skeleton) | 로딩 중 콘텐츠 자리표시자. **실험적 컴포넌트**로 잦은 브레이킹 체인지 있음 |
| **Progress bar** | 시스템 프로세스의 진행 상태 |
| **Progress tracker** | 여정의 모든 단계와 전체 진행률 표시 |
| **Progress indicator** | 여정 중 현재 위치를 강조 (전체 단계보다 현재 위치 중심) |

> **메시지 타입 공통 팔레트**: information / success / warning / danger(error) / discovery(new) — Banner, Section message, Flag가 이 팔레트를 공유한다.

---

## 🏷 Labels

| 컴포넌트 | 설명 |
|---|---|
| **Badge** | 숫자(집계값) 표시. `max` prop으로 상한(기본 99, 초과 시 "99+") 설정 |
| **Lozenge** | 상태를 빠르게 인식시키는 알약형 라벨. Success/Removed/In progress/New/Moved 등 시맨틱 색상, subtle/bold 스타일, 항상 테두리 유지, 기본 최대 폭 200px |
| **Tag** / **Tag group** | 콘텐츠 분류용 라벨. 제거 가능한 태그(`removeButtonLabel`) 지원 |
| **Date label** | 날짜와 상태를 함께 보여주는 비상호작용 라벨 |

---

## 🪟 Overlays and layering

| 컴포넌트 | 설명 |
|---|---|
| **Modal dialog** (+ Modal body) | 페이지 위 레이어에서 상호작용이 필요한 콘텐츠 표시. **Drawer의 권장 대체 컴포넌트** |
| **Popup** | 짧은 콘텐츠를 오버레이로 표시. `placement`로 15가지 위치 지정, `shouldFitContainer`로 트리거 폭에 맞춤. **Inline dialog의 권장 대체 컴포넌트** |
| **Tooltip** | 상호작용 불가능한 설명 라벨. `position`으로 위치 지정(마우스 추적 포함), 화면 경계 근처에서 자동 재배치 |
| **Inline dialog** | 소량의 정보/컨트롤을 담는 팝업 컨테이너. **Deprecated → Popup으로 이전 권장** |
| **Drawer** | 좌측에서 슬라이드되는 패널. **Deprecated → Modal로 이전 권장** |
| **Blanket** | 모달/드로어 뒤에서 하위 UI를 가리는 딤(dim) 처리 |

---

## 📊 Data display

| 컴포넌트 | 설명 |
|---|---|
| **Avatar** (+ presence, status, skeleton) | 사용자/개체의 시각적 표현 |
| **Avatar group** | 여러 아바타를 스택으로 표시. 4개 초과 시 숫자 배지 표시 |
| **Table** / **Dynamic table** | 데이터 표시. Dynamic table은 페이지네이션·정렬·드래그 재정렬·로딩 상태를 내장 |
| **Table tree** | 계층 구조를 위한 확장형 테이블 |
| **Calendar** | 월 단위 날짜 표시 |
| **Comment** | 스레드형 토론/피드백 UI |
| **Page** / **Page header** / **Panel** | 페이지 그리드 구성, 타이틀+브레드크럼+버튼 조합, 컨텍스트 콘텐츠 컨테이너 |
| **Grid** | Page 내 레이아웃/간격 제어 |
| **Inline edit** | 같은 화면에서 읽기/편집 모드 전환 |
| **Code** | 본문 내 짧은 코드 스니펫 강조 |
| **Heading / Text / MetricText** | 토큰 기반 타이포그래피 프리미티브 |
| **Visually hidden** | 화면에는 숨기되 스크린 리더에는 노출 (접근성 유틸리티) |

---

## 🧩 Jira 전용 패턴

atlassian.design 카탈로그에는 "이슈 카드"/"보드 카드" 전용 컴포넌트가 없다 — 이는 developer.atlassian.com의 제품 문서에서 다룬다.

- **이슈 뷰 확장 가이드**(Forge/Connect 앱): 기본 Jira 룩앤필과 일치시키는 것을 강조.
- **보드/백로그 카드**: Lozenge(상태), Avatar(담당자), Badge/Tag(라벨) 등 범용 컴포넌트의 조합으로 구성 (상세는 [layout-and-spacing.md](./layout-and-spacing.md) 참고).

---

## 🧱 기타 참고 (Primitives)

Box, Pressable, Anchor, Inline, Stack, Flex, Grid, Bleed, Text, MetricText, Focusable 등 저수준 토큰 기반 레이아웃 프리미티브가 별도 카테고리로 존재한다. 또한 **Spotlight**(사용자 온보딩/코치마크)도 주목할 만하다.

---

## 📚 참고 자료

- [atlassian.design/components](https://atlassian.design/components) — 전체 컴포넌트 카탈로그
- [Designing Atlassian's new navigation](https://www.atlassian.com/blog/design/designing-atlassians-new-navigation)
- [Evolving button and links](https://atlassian.design/whats-new/evolving-button-and-links)

> **마이그레이션 체크리스트**: Drawer → Modal dialog / Inline dialog → Popup / 구 Atlassian navigation·Side navigation → Navigation system.
