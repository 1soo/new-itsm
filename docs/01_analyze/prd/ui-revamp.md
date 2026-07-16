# 요구사항 정의서 — UI/UX 개편 (Jira/Atlassian Design System 기반)

> 도메인: ui-revamp · 버전: 0.1 · 작성일: 2026-07-10

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-10 | 최초 작성 |

## 1. 개요

기존 7개 업무 도메인(auth·service-request·incident·problem·change·knowledge·asset) 개발이 완료된 상태에서, 전체 UI/UX를 Atlassian Design System(ADS, Jira 스타일)에 가깝게 개편하는 **cross-cutting 디자인 개편 이니셔티브**다. 새 비즈니스 로직·API는 추가하지 않으며, 디자인 토큰과 공통 컴포넌트·레이아웃 셸의 시각적 언어를 ADS 기준으로 재정의한다.

## 2. 범위

### 포함 (In Scope)

- 색상 디자인 토큰 체계 개편: 뉴트럴 중심 팔레트, `color.[property].[role].[emphasis].[state]` 시맨틱 네이밍, **라이트+다크 두 테마 값 세트** 정의 및 실사용 가능한 **다크모드 토글 기능**
- 타이포그래피 스케일(Heading/Body/Metric) 정렬. 서체는 Pretendard 유지, 크기·줄높이·굵기만 ADS 기준 정렬
- 스페이싱(8px 기반)·그리드·Radius(컴포넌트별 차등)·Elevation(sunken/default/raised/overlay) 토큰 정의
- 모션 토큰(duration/easing) 정의 및 `transform`/`opacity` 중심 인터랙션 정렬, `prefers-reduced-motion` 대응
- 아이콘 정렬: lucide-react 유지, 기본 크기(16px)·정사각 선 끝 등 스타일 원칙 정렬, 텍스트 라벨 병행 원칙 적용
- 공통 UI 프리미티브(`components/ui/*`) 및 공통 패턴 컴포넌트(`components/common/*`) 비주얼 개편 — Button은 **단일 API 유지, 스타일만 갱신**, 상태/우선순위 배지는 **Lozenge 규격(4px radius, 테두리 유지)으로 전 도메인 일괄 변경**
- 레이아웃 셸(`components/layout/*`: 헤더·사이드바·푸터·앱셸) 색상·스페이싱·엘리베이션 비주얼 정렬 (구조는 유지)
- 접근성 기준 정렬: 포커스 링(2px + 색상 토큰 동시 적용), WCAG AA 색상 대비, 아이콘 라벨링 규칙

### 미포함 (Out of Scope)

- 개별 도메인 화면(auth~asset)의 레이아웃/정보구조 재설계 — 공통 컴포넌트·토큰 반영만으로 자동 적용되지 않는 화면 단위 변경은 본 이니셔티브 범위 밖이며, 발견 시 별도 식별 대상으로만 명시한다
- Button의 5종 세분화(Icon/Link/Split Button 등) — 전 화면 호출부 수정이 필요해 범위 제외
- Atlassian Sans/Mono, Atlassian 자체 아이콘 세트의 실제 자산 이식 — 독점 자산이라 이식 불가
- 신규 비즈니스 요구사항, API 스펙 변경, 백엔드/DB 변경
- 네비게이션 정보구조 변경(예: Jira식 "For you/Recent/Starred" 개인화 사이드바 도입) — 현재의 도메인 메뉴 사이드바 구조는 유지

## 3. 요구사항 목록

| ID | 유형 | 요구사항 | 우선순위 | 출처 |
|----|------|----------|----------|------|
| REQ-UIX-001 | 기능 | 시스템은 라이트/다크 두 테마의 값을 갖는 색상 디자인 토큰 체계를 제공해야 한다 | High | docs/source/uiux/color.md |
| REQ-UIX-002 | 기능 | 사용자는 라이트/다크 테마를 토글할 수 있어야 한다 | High | docs/source/uiux/color.md (다크모드 실장 승인) |
| REQ-UIX-003 | 기능 | 시스템은 ADS 기준 타이포그래피 스케일(Heading/Body/Metric)을 적용해야 한다 | High | docs/source/uiux/typography.md |
| REQ-UIX-004 | 기능 | 시스템은 8px 기반 스페이싱 토큰과 컴포넌트별 Radius 토큰을 적용해야 한다 | High | docs/source/uiux/layout-and-spacing.md |
| REQ-UIX-005 | 기능 | 시스템은 Elevation(sunken/default/raised/overlay) 단계별 서피스·그림자 토큰을 적용해야 한다 | Med | docs/source/uiux/layout-and-spacing.md |
| REQ-UIX-006 | 기능 | 시스템은 모션 토큰(duration/easing)을 적용하고 `prefers-reduced-motion`을 존중해야 한다 | Med | docs/source/uiux/motion-and-interaction.md |
| REQ-UIX-007 | 기능 | 상태/우선순위 배지는 Lozenge 규격(4px radius, 테두리 유지)으로 전 도메인에 일괄 적용되어야 한다 | High | docs/source/uiux/layout-and-spacing.md (Main 승인) |
| REQ-UIX-008 | 기능 | 공통 UI 프리미티브·패턴 컴포넌트는 색상·타이포·radius·elevation을 신규 토큰으로 참조해야 한다 (Button은 API 변경 없이 스타일만) | High | docs/source/uiux/components.md |
| REQ-UIX-009 | 기능 | 레이아웃 셸(헤더·사이드바·푸터)은 신규 토큰 기준으로 비주얼이 갱신되어야 하되 구조는 유지해야 한다 | High | docs/source/uiux/layout-and-spacing.md |
| REQ-UIX-010 | 기능 | 아이콘은 기본 16px 크기·정사각 선 끝 원칙으로 정렬되고, 단독 사용 시 접근성 라벨을 가져야 한다 | Med | docs/source/uiux/iconography.md |
| REQ-UIX-011 | 비기능 | 시스템은 포커스 표시 시 `border.width.focused`(2px)와 색상 토큰을 함께 적용해야 한다 | High | docs/source/uiux/accessibility.md |
| REQ-UIX-012 | 비기능 | 시스템은 WCAG 2.2 AA 색상 대비 기준(24px 미만 텍스트 4.5:1, 24px 이상/그래픽 3:1)을 충족해야 한다 | High | docs/source/uiux/accessibility.md |
| REQ-UIX-013 | 비기능 | 토큰 개편만으로 자동 반영되지 않는 화면(raw 색상 하드코딩 등)은 식별되어 에스컬레이션되어야 한다 | High | 재검증 결과 하드코딩 미발견(2026-07-10) — 최초 grep은 오탐으로 정정 |

## 4. 인수 기준 (Acceptance Criteria · EARS)

### REQ-UIX-001

- (Ubiquitous) 시스템은 색상 토큰을 `color.[property].[role].[emphasis].[state]` 패턴의 시맨틱 이름으로 노출하고, 라이트/다크 테마별 값을 CSS 커스텀 프로퍼티로 매핑해야 한다.
- (Ubiquitous) 시스템은 원시 hex 값을 컴포넌트에 직접 하드코딩하지 않고 시맨틱 토큰만 참조해야 한다.

### REQ-UIX-002

- (Event-driven) **WHEN** 사용자가 테마 토글 컨트롤을 클릭하면, 시스템은 즉시 라이트↔다크 테마를 전환해야 한다.
- (Ubiquitous) 시스템은 사용자가 선택한 테마를 저장하여 재방문 시에도 유지해야 한다.
- (State-driven) **WHILE** 다크 테마가 적용된 동안, 시스템은 모든 화면의 배경·텍스트·테두리·상태 색상을 다크 값으로 렌더링해야 한다.

### REQ-UIX-003

- (Ubiquitous) 시스템은 페이지 타이틀·컴포넌트 헤더·본문·메트릭 텍스트에 각각 정의된 폰트 크기·줄높이·굵기 토큰을 적용해야 한다.
- (Ubiquitous) 시스템은 본문 최소 폰트 크기를 12px 이상으로 유지해야 한다.

### REQ-UIX-004

- (Ubiquitous) 시스템은 컴포넌트 내부·요소 간 여백에 8px 기반 스페이싱 토큰을 적용해야 한다.
- (Ubiquitous) 시스템은 배지·라벨·버튼·인풋·카드·모달 등 컴포넌트 유형별로 규정된 Radius 토큰을 적용해야 한다.

### REQ-UIX-005

- (Ubiquitous) 시스템은 기본/상승(raised)/오버레이(overlay) 서피스에 각각 대응하는 elevation 토큰(서피스+그림자)을 적용해야 한다.
- (Unwanted) **IF** 서로 다른 elevation 레벨의 서피스·그림자 토큰이 혼용되면, **THEN** 개편 검수 시 이를 결함으로 처리해야 한다.

### REQ-UIX-006

- (Event-driven) **WHEN** 모달·드롭다운·토스트가 열리거나 닫히면, 시스템은 정의된 duration·easing 토큰으로 진입/퇴장 애니메이션을 적용해야 한다.
- (Ubiquitous) 시스템은 애니메이션에 `transform`/`opacity` 속성만 사용하고 레이아웃을 유발하는 속성(`width`/`height`)은 애니메이션하지 않아야 한다.
- (State-driven) **WHILE** 사용자의 운영체제가 모션 감소(prefers-reduced-motion)를 설정한 동안, 시스템은 모든 전환을 즉시(instant) 처리해야 한다.

### REQ-UIX-007

- (Ubiquitous) 상태 배지(StatusBadge)·우선순위 배지(PriorityBadge)는 4px radius와 테두리를 유지하는 Lozenge 스타일로 렌더링해야 한다.
- (Ubiquitous) 배지는 색상만으로 상태를 구분하지 않고 텍스트 라벨을 함께 표시해야 한다.

### REQ-UIX-008

- (Ubiquitous) 공통 UI 프리미티브(Button/Badge/Table/Dialog/Input/Select/Card 등)는 신규 색상·타이포·radius·elevation 토큰만 참조해야 한다.
- (Ubiquitous) Button 컴포넌트는 기존 단일 API(variant/size prop)를 유지한 채 스타일 값만 갱신해야 한다.

### REQ-UIX-009

- (Ubiquitous) 헤더·사이드바·푸터·앱셸은 신규 토큰 기준의 색상·스페이싱·elevation으로 렌더링되어야 한다.
- (Ubiquitous) 헤더·사이드바·앱셸의 현재 구조(상단 고정 헤더, 접기 가능한 좌측 사이드바, 하단 푸터)는 변경하지 않아야 한다.

### REQ-UIX-010

- (Ubiquitous) 아이콘은 기본 16px 크기와 정사각(square) 선 끝 스타일 원칙을 따라야 한다.
- (Unwanted) **IF** 아이콘이 텍스트 라벨 없이 단독으로 사용되면, **THEN** 시스템은 접근성 라벨(`aria-label` 등)을 제공해야 한다.

### REQ-UIX-011

- (Ubiquitous) 키보드 포커스가 상호작용 요소에 위치하면, 시스템은 2px 두께의 포커스 링과 포커스 색상 토큰을 함께 렌더링해야 한다.

### REQ-UIX-012

- (Ubiquitous) 시스템은 24px 미만 텍스트에 4.5:1, 24px 이상 텍스트/필수 UI 그래픽에 3:1 이상의 명도 대비를 충족해야 한다.

### REQ-UIX-013

- (Ubiquitous) 시스템은 raw Tailwind 색상 클래스가 하드코딩된 화면이 발견되는 경우 "개별 화면 FE 수정 필요 대상"으로 최종 산출물에 명시해야 한다.
- (Unwanted) **IF** 개편 진행 중 위와 같은 하드코딩 화면이 새로 발견되고 이번 이니셔티브(dev-ui 단독) 범위에서 수정되지 않으면, **THEN** dev-lead는 이 사실을 Main에게 에스컬레이션해야 한다.

> **정정 (2026-07-10)**: 최초 분석 시 근거로 든 grep 결과(UserCreatePage, UserDetailPage, LoginPage, IncidentDetailPage, PortalPage 5개 화면)는 재검증 결과 오탐으로 확인됐다. `-{color}-{100~900}` 패턴과 hex 패턴으로 `source/frontend/src` 전체를 재grep한 결과 0건이며, 해당 5개 파일을 직접 열람해도 전부 시맨틱 토큰(`bg-danger`/`text-warning`/`text-muted-foreground` 등)만 사용 중이다. 최초 grep이 `gap-2`/`size-3`/`w-28`/`text-xl` 등 숫자를 포함한 일반 유틸리티 클래스를 색상 클래스로 오인한 것으로 추정된다. 현재 코드 기준으로는 raw 색상 하드코딩 화면이 **없다**.
