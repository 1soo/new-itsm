# 기능 명세서 — UI/UX 개편 (Jira/Atlassian Design System 기반)

> 도메인: ui-revamp · 버전: 0.1 · 작성일: 2026-07-10

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-10 | 최초 작성 |

## 1. 개요

디자인 토큰(색상·타이포·스페이싱·radius·elevation·모션) 체계를 ADS 기준으로 재정의하고, 공통 UI 프리미티브·패턴 컴포넌트·레이아웃 셸의 비주얼을 개편한다. 새 비즈니스 로직은 추가하지 않으며 프레젠테이션 레이어만 다룬다.

## 2. 기능 목록

| ID | 기능명 | 관련 요구사항 | 설명 |
|----|--------|---------------|------|
| FEAT-UIX-001 | 색상 토큰 체계(라이트/다크) | REQ-UIX-001 | 시맨틱 색상 토큰과 테마별 값 세트 정의 |
| FEAT-UIX-002 | 다크모드 토글 | REQ-UIX-002 | 테마 전환 UI 및 선택 유지 |
| FEAT-UIX-003 | 타이포그래피 스케일 정렬 | REQ-UIX-003 | Heading/Body/Metric 크기·줄높이·굵기 토큰화 |
| FEAT-UIX-004 | 스페이싱·Radius 토큰 | REQ-UIX-004 | 8px 기반 스페이싱, 컴포넌트별 radius 정의 |
| FEAT-UIX-005 | Elevation 토큰 | REQ-UIX-005 | 서피스 단계별 배경·그림자 토큰 정의 |
| FEAT-UIX-006 | 모션 토큰 정렬 | REQ-UIX-006 | duration/easing 토큰화 및 reduced-motion 대응 |
| FEAT-UIX-007 | Lozenge 배지 개편 | REQ-UIX-007 | 상태/우선순위 배지를 4px radius 규격으로 전환 |
| FEAT-UIX-008 | 공통 컴포넌트 비주얼 개편 | REQ-UIX-008 | ui/·common/ 컴포넌트가 신규 토큰만 참조하도록 갱신 |
| FEAT-UIX-009 | 레이아웃 셸 비주얼 개편 | REQ-UIX-009 | 헤더·사이드바·푸터 색상/스페이싱/elevation 갱신 |
| FEAT-UIX-010 | 아이콘 정렬 | REQ-UIX-010 | lucide 아이콘 크기·라벨링 원칙 정렬 |
| FEAT-UIX-011 | 포커스 링 접근성 | REQ-UIX-011 | 포커스 표시 2px+색상 토큰 동시 적용 |
| FEAT-UIX-012 | 색상 대비 검증 | REQ-UIX-012 | 신규 토큰의 WCAG AA 대비 충족 확인 |
| FEAT-UIX-013 | 개별 화면 이슈 식별·에스컬레이션 | REQ-UIX-013 | 하드코딩 색상 화면 식별 및 dev-lead 보고 |

## 3. 기능 상세

### FEAT-UIX-001 · 색상 토큰 체계(라이트/다크)

- **설명**: 뉴트럴 중심 팔레트와 시맨틱 역할(brand/success/warning/danger/information)을 라이트·다크 두 테마의 CSS 커스텀 프로퍼티 값으로 정의한다.
- **관련 요구사항**: REQ-UIX-001
- **입력**: 없음(정적 토큰 정의)
- **출력**: `index.css`의 `:root`(라이트)와 `[data-theme="dark"]`(다크) 커스텀 프로퍼티, `@theme inline` 시맨틱 매핑
- **처리 흐름**:
  1. 뉴트럴/시맨틱 role별 라이트 값 정의
  2. 동일 토큰 이름의 다크 값 정의(단순 반전이 아닌 재매핑)
  3. 기존 `--base`/`--status-*` 원자 변수를 신규 토큰 구조로 대체
- **인수 기준 (EARS)**:
  - (Ubiquitous) 시스템은 라이트/다크 테마에서 동일한 토큰 이름으로 서로 다른 값을 제공해야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 컴포넌트가 토큰이 아닌 raw hex를 참조하면, **THEN** 개편 검수 시 결함으로 처리해야 한다.

### FEAT-UIX-002 · 다크모드 토글

- **설명**: 헤더 영역에 라이트/다크 전환 컨트롤을 제공하고 선택을 저장한다.
- **관련 요구사항**: REQ-UIX-002
- **입력**: 사용자의 토글 클릭
- **출력**: `data-theme` 속성 전환, 로컬 저장된 테마 선호값
- **처리 흐름**:
  1. 저장된 선호 테마(없으면 라이트 기본) 로드 후 `data-theme` 적용
  2. 토글 클릭 시 반대 테마로 전환하고 저장
- **인수 기준 (EARS)**:
  - (Event-driven) **WHEN** 사용자가 테마 토글을 클릭하면, 시스템은 즉시 테마를 전환해야 한다.
  - (Ubiquitous) 시스템은 마지막 선택 테마를 재방문 시에도 유지해야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 저장된 테마 값이 없거나 유효하지 않으면, **THEN** 시스템은 라이트 테마를 기본값으로 적용해야 한다.

### FEAT-UIX-003 · 타이포그래피 스케일 정렬

- **설명**: Pretendard 서체를 유지하며 ADS의 Heading(XXL~XXS)/Body(L/M/S)/Metric 크기·줄높이·굵기 스케일을 토큰화해 적용한다.
- **관련 요구사항**: REQ-UIX-003
- **입력**: 없음(정적 스타일 토큰)
- **출력**: 타이포 유틸리티 클래스/토큰(예: `font.heading.large`, `font.body`)
- **처리 흐름**:
  1. 현재 텍스트 스타일(페이지 타이틀·본문·메트릭)을 ADS 스케일에 매핑
  2. 컴포넌트별 적용(페이지 타이틀→heading.large, 카드 헤더→heading.medium 등)
- **인수 기준 (EARS)**:
  - (Ubiquitous) 시스템은 헤딩/본문/메트릭 텍스트에 정의된 크기·줄높이·굵기 토큰을 적용해야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 본문 텍스트가 12px 미만으로 축소되면, **THEN** 시스템은 이를 결함으로 처리해야 한다.

### FEAT-UIX-004 · 스페이싱·Radius 토큰

- **설명**: 8px 기반 스페이싱 스케일과 컴포넌트 유형별(배지 2px/버튼·인풋 6px/카드 8px/모달 12px/pill 999px) radius 토큰을 정의한다.
- **관련 요구사항**: REQ-UIX-004
- **입력**: 없음
- **출력**: `space.*`, `radius.*` 토큰(CSS 변수/Tailwind 매핑)
- **처리 흐름**:
  1. 현재 단일 `--radius`(0.5rem 기반 sm/md/lg/xl) 구조를 컴포넌트별 차등 radius로 확장
  2. 컴포넌트별 radius 재매핑(배지→xsmall/small, 버튼/인풋→medium, 카드→large, 모달→xlarge)
- **인수 기준 (EARS)**:
  - (Ubiquitous) 시스템은 컴포넌트 유형에 규정된 radius 토큰을 적용해야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 컴포넌트가 규정 외 임의 radius 값을 사용하면, **THEN** 개편 검수 시 결함으로 처리해야 한다.

### FEAT-UIX-005 · Elevation 토큰

- **설명**: default/raised/overlay 3단계 서피스에 대응하는 배경·그림자 토큰을 정의하고 카드·모달·드롭다운에 적용한다.
- **관련 요구사항**: REQ-UIX-005
- **입력**: 없음
- **출력**: `elevation.surface.*`, `elevation.shadow.*` 토큰
- **처리 흐름**:
  1. 카드=raised, 모달/드롭다운/팝오버/툴팁=overlay로 매핑
  2. 각 레벨의 서피스 배경색과 그림자 값을 라이트/다크 별로 정의
- **인수 기준 (EARS)**:
  - (Ubiquitous) 시스템은 카드에는 raised, 모달/팝오버/드롭다운에는 overlay elevation 토큰을 적용해야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 서로 다른 elevation의 서피스·그림자 토큰이 한 컴포넌트에 혼용되면, **THEN** 개편 검수 시 결함으로 처리해야 한다.

### FEAT-UIX-006 · 모션 토큰 정렬

- **설명**: 모달·드롭다운·토스트의 진입/퇴장 전환에 duration/easing 토큰을 적용하고, transform/opacity만 애니메이션한다.
- **관련 요구사항**: REQ-UIX-006
- **입력**: 없음
- **출력**: 모션 duration/easing 토큰, reduced-motion 미디어쿼리 처리
- **처리 흐름**:
  1. 모달 진입=long(250ms)+ease-in-out bold, 퇴장=medium(200ms) 등 컴포넌트별 매핑
  2. 사이드바 폭 전환처럼 layout 속성(width)을 애니메이션하는 기존 코드를 transform 기반으로 전환 검토
  3. `prefers-reduced-motion: reduce` 시 duration을 0으로 처리
- **인수 기준 (EARS)**:
  - (Event-driven) **WHEN** 모달/드롭다운/토스트가 열리거나 닫히면, 시스템은 정의된 duration·easing으로 전환해야 한다.
  - (State-driven) **WHILE** 모션 감소 설정이 활성화된 동안, 시스템은 전환을 즉시 처리해야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 전환에 `width`/`height` 등 레이아웃 속성이 애니메이션되면, **THEN** 개편 검수 시 결함으로 처리해야 한다.

### FEAT-UIX-007 · Lozenge 배지 개편

- **설명**: `StatusBadge`/`PriorityBadge`를 pill(999px) 형태에서 Lozenge 규격(4px radius, 테두리 유지)으로 전환한다.
- **관련 요구사항**: REQ-UIX-007
- **입력**: 없음(기존 `tone`/`priority` prop 유지)
- **출력**: 개편된 배지 렌더링(전 도메인 목록/상세 화면에 자동 반영)
- **처리 흐름**:
  1. `badge.tsx`의 `rounded-full`을 `radius.small`(4px)로 변경, 테두리 스타일 추가
  2. `StatusBadge`/`PriorityBadge`는 API 변경 없이 스타일만 상속
- **인수 기준 (EARS)**:
  - (Ubiquitous) 상태/우선순위 배지는 4px radius와 테두리를 가진 Lozenge 스타일로 렌더링되어야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 배지가 텍스트 라벨 없이 색상만으로 상태를 표시하면, **THEN** 이를 결함으로 처리해야 한다.

### FEAT-UIX-008 · 공통 컴포넌트 비주얼 개편

- **설명**: `components/ui/*`(Button/Input/Select/Checkbox/Card/Dialog/AlertDialog/Table/Popover/DropdownMenu/Avatar/Skeleton/Sonner 등)와 `components/common/*`(데이터 테이블·빈 상태·페이지네이션·타임라인·KPI 카드 등)이 신규 토큰만 참조하도록 스타일을 갱신한다.
- **관련 요구사항**: REQ-UIX-008
- **입력**: 없음(기존 컴포넌트 API 유지)
- **출력**: 신규 토큰 기반으로 재스타일링된 컴포넌트(전 도메인 화면에 자동 반영)
- **처리 흐름**:
  1. 컴포넌트별로 하드코딩된 색상/크기 값을 신규 토큰 참조로 교체
  2. Button은 variant/size prop 구조를 그대로 두고 색상·radius·아이콘 정렬만 갱신(5종 세분화는 범위 제외)
  3. Dialog/AlertDialog 오버레이·콘텐츠는 overlay elevation 토큰 적용
- **인수 기준 (EARS)**:
  - (Ubiquitous) 공통 컴포넌트는 신규 색상·타이포·radius·elevation 토큰만 참조해야 한다.
  - (Ubiquitous) Button 컴포넌트의 public prop 인터페이스는 개편 전후 동일해야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 컴포넌트 스타일 변경으로 기존 호출부(props)가 깨지면, **THEN** 이를 결함으로 처리하고 API 호환성을 복구해야 한다.

### FEAT-UIX-009 · 레이아웃 셸 비주얼 개편

- **설명**: 헤더·사이드바·푸터·앱셸의 색상·스페이싱·elevation을 신규 토큰으로 갱신하되 상단 헤더+좌측 사이드바+하단 푸터 구조는 유지한다.
- **관련 요구사항**: REQ-UIX-009
- **입력**: 없음(기존 `AppShellProps`/`HeaderProps`/`SidebarProps` 유지)
- **출력**: 개편된 레이아웃 셸(전 도메인 화면에 자동 반영)
- **처리 흐름**:
  1. 사이드바 배경(현재 Neutral Dark 고정)을 라이트/다크 테마 대응 토큰으로 전환
  2. 헤더 검색바·알림 벨·사용자 메뉴에 신규 radius/elevation 적용
  3. 사이드바 접힘 전환은 모션 토큰 정책(FEAT-UIX-006)에 따라 재검토
- **인수 기준 (EARS)**:
  - (Ubiquitous) 레이아웃 셸은 신규 토큰 기준으로 렌더링되어야 한다.
  - (Ubiquitous) 레이아웃 셸의 컴포넌트 구조(헤더/사이드바/콘텐츠/푸터)는 개편 전후 동일해야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 다크 테마에서 사이드바·헤더의 색상 대비가 기준 미달이면, **THEN** 이를 결함으로 처리해야 한다.

### FEAT-UIX-010 · 아이콘 정렬

- **설명**: lucide-react 아이콘의 기본 크기를 16px로 정렬하고, 텍스트 라벨 없는 아이콘 단독 사용처에 접근성 라벨을 보강한다.
- **관련 요구사항**: REQ-UIX-010
- **입력**: 없음
- **출력**: 정렬된 아이콘 크기 클래스, `aria-label` 보강
- **처리 흐름**:
  1. 공통 컴포넌트(Button icon 사이즈 등)의 아이콘 기본 크기를 16px 기준으로 통일
  2. 아이콘 전용 버튼(헤더 알림·사이드바 토글 등)에 라벨 존재 여부 점검
- **인수 기준 (EARS)**:
  - (Ubiquitous) 아이콘은 기본 16px 크기로 렌더링되어야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 아이콘 단독 버튼에 접근성 라벨이 없으면, **THEN** 이를 결함으로 처리해야 한다.

### FEAT-UIX-011 · 포커스 링 접근성

- **설명**: 키보드 포커스 시 2px 두께 링과 포커스 색상 토큰을 함께 렌더링한다.
- **관련 요구사항**: REQ-UIX-011
- **입력**: 키보드 Tab 이동
- **출력**: 포커스 링 시각 표시
- **처리 흐름**:
  1. `:focus-visible` 규칙에 `border.width.focused`(2px)와 `color.border.focused` 토큰 적용
  2. 포커스 링 radius = 요소 radius + 2px 규칙 반영
- **인수 기준 (EARS)**:
  - (Ubiquitous) 시스템은 포커스 상태에서 2px 링과 색상 토큰을 함께 표시해야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 포커스 링이 색상만 있고 두께 토큰이 없거나 그 반대이면, **THEN** 이를 결함으로 처리해야 한다.

### FEAT-UIX-012 · 색상 대비 검증

- **설명**: 신규 색상 토큰 값이 WCAG 2.2 AA 대비 기준을 충족하는지 검증한다.
- **관련 요구사항**: REQ-UIX-012
- **입력**: 신규 정의된 텍스트/배경 색상 토큰 쌍
- **출력**: 대비 검증 결과(통과/미달 목록)
- **처리 흐름**:
  1. 라이트/다크 테마 각각의 텍스트-배경 조합 대비율 계산
  2. 24px 미만 4.5:1, 24px 이상/그래픽 3:1 기준 충족 여부 확인
- **인수 기준 (EARS)**:
  - (Ubiquitous) 신규 색상 토큰은 정의된 최소 대비율을 충족해야 한다.
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 특정 토큰 조합이 기준에 미달하면, **THEN** 해당 값을 조정하고 재검증해야 한다.

### FEAT-UIX-013 · 개별 화면 이슈 식별·에스컬레이션

- **설명**: 토큰/공통 컴포넌트 개편만으로 자동 반영되지 않는 화면(raw 색상 하드코딩)을 식별해 보고한다.
- **관련 요구사항**: REQ-UIX-013
- **입력**: 소스 grep 결과(raw Tailwind 색상 클래스 사용 화면)
- **출력**: 개별 화면 조정 필요 목록 — **재검증 결과(2026-07-10): 해당 없음**(4절 도메인 영향 범위 참고)
- **처리 흐름**:
  1. `-{color}-{100~900}`(예: `text-red-500`) 및 hex(`#xxxxxx`) 패턴으로 raw 클래스 grep
  2. 대상 화면·도메인 매핑
  3. dev-lead에게 목록 전달, dev-lead가 Main에게 FE 소집 필요 여부 에스컬레이션
- **인수 기준 (EARS)**:
  - (Ubiquitous) 분석 산출물은 raw 색상 하드코딩 화면 목록을 포함해야 한다(발견되지 않으면 "해당 없음"으로 명시).
- **예외/오류 처리 (EARS · Unwanted Behaviour)**:
  - **IF** 개편 진행 중 하드코딩 화면이 새로 발견되고 이번 이니셔티브(dev-ui 단독) 범위에서 수정되지 않으면, **THEN** dev-lead는 Main에게 FE 담당자 추가 소집 필요 여부를 에스컬레이션해야 한다.

> **정정 (2026-07-10)**: 최초 분석의 grep 근거(5개 파일)는 오탐으로 확인됐다. 상세 근거는 PRD REQ-UIX-013 정정 노트 참고.

## 4. 도메인별 영향 범위 매핑

토큰·공통 컴포넌트 개편은 모든 화면에 **자동 반영**된다(각 도메인 화면은 `components/ui`, `components/common`, `components/layout`을 조합해 구성되어 있기 때문). **2026-07-10 재검증 결과, raw Tailwind 색상 클래스 하드코딩 화면은 발견되지 않았다** — 최초 분석의 5개 파일 지목은 grep 오탐이었다(정정 근거: PRD REQ-UIX-013 정정 노트).

| 도메인 | 영향 유형 | 근거 |
|--------|-----------|------|
| auth | 자동 반영 | raw 색상 하드코딩 미발견(재검증) |
| auth(관리자 화면) | 자동 반영 | raw 색상 하드코딩 미발견(재검증) |
| service-request | 자동 반영 | raw 색상 하드코딩 미발견(재검증) |
| incident | 자동 반영 | raw 색상 하드코딩 미발견(재검증) |
| problem | 자동 반영 | raw 색상 하드코딩 미발견 |
| change | 자동 반영 | raw 색상 하드코딩 미발견 |
| knowledge | 자동 반영 | raw 색상 하드코딩 미발견 |
| asset | 자동 반영 | raw 색상 하드코딩 미발견 |

> **에스컬레이션 불필요**: 개별 화면 FE 수정 대상이 없으므로, 이번 이니셔티브는 dev-ui 단독 범위로 충분하다. 단, 개편 검수 중 신규 하드코딩이 발견되면 REQ-UIX-013/FEAT-UIX-013 절차에 따라 dev-lead가 Main에게 에스컬레이션한다.
