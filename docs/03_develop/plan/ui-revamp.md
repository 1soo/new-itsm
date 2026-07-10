# 개발 계획 — ui-revamp (UI/UX 개편, ADS 기반)

> 이니셔티브: ui-revamp · cross-cutting(7개 업무 도메인 완료 후) · 작성: dev-lead · 2026-07-10

## 1. 목표

기존 7개 업무 도메인(auth·service-request·incident·problem·change·knowledge·asset) 개발 완료 후, 새 비즈니스 로직/API/DB 변경 없이 디자인 토큰(색상 라이트/다크·타이포·스페이싱/Radius·Elevation·모션·아이콘·접근성)과 공통 컴포넌트·레이아웃 셸의 비주얼만 Atlassian Design System(ADS, Jira 스타일) 기준으로 개편한다.

## 2. 설계 근거 (docs/01_analyze, docs/02_plan)

- 요구사항: `01_analyze/prd/ui-revamp.md`(REQ-UIX-001~013), `01_analyze/feature/ui-revamp.md`(FEAT-UIX-001~013)
- 화면: `02_plan/screen/common.md` v0.2 (2절 디자인 토큰, 3~4절 SCR-COM-001~010, 5절 개별 화면 에스컬레이션 목록)

## 3. 담당별 범위

### UI (dev-ui) — `source/frontend/src/index.css`, `components/ui/*`, `components/common/*`, `components/layout/*`
- **토큰 정의**(`index.css`): 2.1절 색상 라이트/다크 CSS 변수(`--primary`, `--primary-hover`, `--primary-foreground`, `--background`, `--card`, `--popover`, `--foreground`, `--muted-foreground`, `--border`, `--ring`, `--sidebar`, `--sidebar-primary` 등) 재매핑, `[data-theme="dark"]` 값 세트 추가. 2.2절 상태 시맨틱(Success/Warning/Danger/Info/Muted) subtle/bold 값. 2.3절 타이포 스케일(`font.heading.*`/`font.body.*`/`font.metric.*`). 2.4절 스페이싱(`space.050~400`)·Radius(`radius.xsmall~full`, 컴포넌트별 차등). 2.5절 Elevation(default/raised/overlay 서피스+그림자). 2.6절 모션(duration/easing, `prefers-reduced-motion` 대응).
- **배지**(`components/ui/badge.tsx` 및 `components/common/StatusBadge`/`PriorityBadge`): pill(`rounded-full`)→Lozenge(`radius.small` 4px + 테두리)로 전환. `tone`/`priority` prop 인터페이스는 변경 금지, 스타일만 교체. subtle 기본, P1/SEV1 등 강조 필요 시에만 bold 허용. 텍스트 라벨 항상 병행(색상 단독 금지).
- **공통 프리미티브**(`components/ui/*`: Button/Input/Select/Checkbox/Card/Dialog/AlertDialog/Table/Popover/DropdownMenu/Avatar/Skeleton/Sonner 등): 하드코딩 색상/radius를 신규 토큰 참조로 교체. **Button은 variant/size prop 구조·public API 그대로 유지, 스타일 값만 갱신**(5종 세분화 범위 제외). Dialog/AlertDialog/Popover/DropdownMenu는 overlay elevation 토큰 적용, 진입/퇴장 모션은 2.6절 컴포넌트별 duration/easing 적용(transform/opacity만, width/height 애니메이션 금지).
- **공통 패턴**(`components/common/*`: 데이터 테이블·빈 상태·페이지네이션·타임라인·KPI 카드 등): 신규 토큰만 참조하도록 스타일 갱신. 구조/API 변경 없음.
- **레이아웃 셸**(`components/layout/*`: Header/Sidebar/Footer/AppShell): 헤더·사이드바·푸터·앱셸 구조(상단 고정 헤더/좌측 접기 가능 사이드바/하단 푸터)는 유지, 색상·스페이싱·elevation만 신규 토큰으로 교체. 사이드바 배경은 고정색이 아니라 `--sidebar`(라이트/다크 대응)로 전환. `AppShellProps`/`HeaderProps`/`SidebarProps` 등 기존 prop 인터페이스 변경 금지.
- **SCR-COM-010 테마 토글 신설**: 헤더 우측(알림 벨 좌측)에 아이콘 버튼 추가(라이트=달 아이콘, 다크=해 아이콘, `aria-label="테마 전환"`). 클릭 시 `data-theme` 속성 즉시 전환 + 로컬 저장(localStorage 등), 최초 진입 시 저장값 로드(없거나 유효하지 않으면 라이트 기본).
- **아이콘 정렬**: lucide-react 기본 크기 16px 통일(좁은 공간 12px 허용). 텍스트 라벨 없는 아이콘 단독 사용처(헤더 알림 벨, 사이드바 토글, 테마 토글 등)에 `aria-label` 보강.
- **접근성**: `:focus-visible`에 2px 링(`border.width.focused`) + `--ring` 색상 토큰 동시 적용(radius=요소 radius+2px, 오프셋 2px). 신규 색상 조합의 WCAG AA 대비(24px 미만 4.5:1, 24px 이상/그래픽 3:1) 실제 렌더링 값 기준 재검증(FEAT-UIX-012) — 미달 시 dev-ui가 값 조정 후 재검증.

### FE (dev-frontend) — Main 승인으로 이번 이니셔티브에 추가 투입됨
- 아래 5개 파일은 raw Tailwind 색상 클래스 하드코딩으로 토큰 개편만으로 자동 반영되지 않아 개별 수정 필요(REQ-UIX-013):
  - `source/frontend/src/features/auth/LoginPage.tsx`
  - `source/frontend/src/features/admin/UserCreatePage.tsx`
  - `source/frontend/src/features/admin/UserDetailPage.tsx`
  - `source/frontend/src/features/service-request/PortalPage.tsx`
  - `source/frontend/src/features/incident/IncidentDetailPage.tsx`
  - raw 색상 클래스(예: `text-blue-600`, `bg-red-50` 등)를 2.1~2.2절 시맨틱 토큰(CSS 변수) 또는 dev-ui가 확정한 유틸리티 클래스로 교체. 레이아웃/구조/로직 변경 없이 색상 참조만 교체.
  - **순서**: dev-ui가 토큰(2.1~2.2절 CSS 변수, Lozenge 배지 스타일) 확정 후 dev-frontend 착수 권장(재작업 방지). 정확한 클래스/토큰명은 dev-frontend가 dev-ui와 직접 SendMessage로 조율한다.

## 4. 진행 순서 · 의존성
1. 토큰(2.1~2.8절) 정의 → 공통 프리미티브(`components/ui`) → 공통 패턴(`components/common`) → 레이아웃 셸(`components/layout`) → 테마 토글(SCR-COM-010) 순으로 진행(하위 컴포넌트가 토큰에 의존하므로 토큰 우선).
2. 계약 단일 기준 `screen/common.md` v0.2. 모호점(예: WCAG 대비 실패 값, elevation 혼용 발견 등)은 dev-lead에게 질문 → 설계 이슈는 designer에게 에스컬레이션.

## 5. 완료(테스트 통과) 기준
- 전 도메인(7개) 화면에서 토큰·컴포넌트 개편이 시각적으로 반영되고 기존 기능(상태 전이, RBAC, 폼 제출 등)에 회귀 없음.
- 다크모드 토글 정상 동작(즉시 전환, 새로고침/재방문 시 유지).
- Button/Dialog/Badge 등 공통 컴포넌트 API(props) 변경 없음(호출부 수정 불필요).
- 배지가 Lozenge(4px radius+테두리)로 렌더링되고 텍스트 라벨 병행.
- 포커스 링 2px+색상 토큰 동시 렌더링, 신규 색상 조합 WCAG AA 대비 충족.
- 모션이 transform/opacity만 사용(사이드바 폭 전환 예외), `prefers-reduced-motion` 시 즉시 전환.
- tester 통합/회귀 테스트(주로 FE 스모크·시각 회귀 위주, 신규 API 없으므로 BE 통합테스트 대상 아님) 실패 0 → `feat(ui-revamp): ...` 또는 `style(ui-revamp): ...` 커밋/푸시.

## 6. 파일 소유
- `source/frontend/src/index.css`, `components/ui/*`, `components/common/*`, `components/layout/*` — dev-ui.
- 5개 개별 화면(feature 레이어: auth/LoginPage.tsx, admin/UserCreatePage.tsx, admin/UserDetailPage.tsx, service-request/PortalPage.tsx, incident/IncidentDetailPage.tsx) — dev-frontend.

## 7. 특이사항
- 새 비즈니스 로직·API·DB 변경 없음 — BE/DB 에이전트 소집 불필요.
- Button 5종 세분화(Icon/Link/Split Button), Atlassian Sans/Mono·자체 아이콘 자산 이식, 네비게이션 정보구조 변경은 범위 제외(analyzer 확정).
- Main 승인(2026-07-10)으로 dev-frontend가 이번 이니셔티브에 추가 투입됨. dev-ui 토큰 확정 후 dev-frontend 착수 권장, 정확한 토큰/클래스명은 두 에이전트가 직접 조율.
