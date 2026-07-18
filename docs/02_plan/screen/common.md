# 화면 설계서 — 공통 (Common)

> 도메인: common · 버전: 0.17

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |
| 2026-07-10 | UI/UX 개편(ADS 기반 디자인 토큰·Lozenge 배지 전환, REQ-UIX-001~013), 개별 화면 raw 색상 하드코딩 재검증 결과 미발견 정정 |
| 2026-07-11 | 승인 프로세스 커스텀 기능 반영(전 도메인 공용 승인 대기함 SCR-COM-014 신규, SCR-SRM-006/SCR-CHG-004/SCR-KM-004 대체), 사이드바 메뉴 Role-Menu 동적 매핑 전환(SCR-ADMIN-006 연동), 대시보드 화면 코드 신규 부여(SCR-COM-013), 헤더 알림 5초 polling 전환 및 확인처리(모두 지우기·개별 X) 신규, 알림 드롭다운 2줄 레이아웃, 사용자 가이드 모달→전용 화면 전환(SCR-COM-012) |
| 2026-07-12 | 다국어(i18n) 지원 신규(헤더 언어 선택 SCR-COM-015, 전 도메인 텍스트 번역 아키텍처 6절), 토스트·확인 다이얼로그 SweetAlert2 전환 |
| 2026-07-13 | 페이지 배경 회색→흰색 전환, i18n 커버리지 결함 수정(사이드바 메뉴 라벨, 승인 프로세스 카드 스택) |
| 2026-07-14 | 사이드바 폭/폰트 축소(펼침 240→190px/접힘 64→48px, 라벨 14→12px/그룹헤더 12→10px, SCR-COM-003), 공통 목록 표(SCR-COM-007) `Column<T>`에 `width` 도입해 컬럼 폭 고정 아키텍처 정의(구체 px 값은 도메인별 screen 문서), 페이지당 아이템 수 재산정 기준(7절 신규) |
| 2026-07-16 | SCR-COM-008 상태 전이 버튼 라벨을 도착 상태명 대신 동작 동사형으로 전환하는 공통 아키텍처 정의(구체 라벨 매핑은 SRM/INCIDENT/PROBLEM/CHANGE/VULNERABILITY/ASSET/ESM 도메인별 screen 문서), 타임라인에 행위 수행 주체자(actor) 표시 추가(SRM/ESM/INCIDENT 3개 도메인), SCR-COM-014 승인 대기함에 "상세보기" 버튼 신규 |
| 2026-07-17 | 서비스 카탈로그 커스텀 폼 빌더(form.io 스타일) — `@formio/react` 기반 동적 폼 빌더/렌더러 공통 아키텍처 정의(8절 신규, SRM/ESM 공용). 팔레트 구성·Bootstrap↔ADS 스타일 통합 방안·서버 저장 흐름 포함(구체 화면 배치는 [service-request.md](service-request.md) SCR-SRM-007/002, [esm.md](esm.md) SCR-ESM-006/002) |

## 1. 개요

ITSM 플랫폼 전 도메인이 공유하는 공통 레이아웃(헤더·사이드바·푸터), 인증 게이트, 공통 UI 패턴(티켓 목록/상세, 코멘트/타임라인, 상태·우선순위 배지, 토스트/알림)을 정의한다. 모든 도메인 화면은 본 문서의 레이아웃과 디자인 토큰을 기준으로 구성한다.

v0.2에서 Atlassian Design System(ADS·Jira 스타일)에 가깝게 디자인 토큰 체계(색상 라이트/다크·타이포·스페이싱/Radius·Elevation·모션·아이콘·접근성)를 재정의했다(`docs/01_analyze/feature/ui-revamp.md` FEAT-UIX-001~012). 레이아웃 구조·컴포넌트 API·비즈니스 로직은 변경하지 않으며 비주얼 토큰만 갱신한다.

## 2. 디자인 토큰

### 2.1 색상 토큰 (라이트 · 다크)

라이트 테마의 Base는 기존과 동일하게 신뢰감을 주는 프로페셔널 블루(#2563EB)를 유지한다. 다크 테마는 **동일 톤을 단순 반전한 값이 아니라**, 어두운 배경 위에서 가독성을 확보하도록 재매핑한 값(더 밝은 파랑 계열 + 반전된 전경색)을 사용한다(REQ-UIX-001, REQ-UIX-012).

| 역할 | 라이트 | 다크 | 적용 CSS 변수 |
|------|--------|------|----------------|
| Brand 배경(강조) | #2563EB | #60A5FA | `--primary` |
| Brand Hover/Active | #1E40AF | #93C5FD | `--primary-hover` |
| Brand 위 전경색 | #F9FAFB | #0B1220 | `--primary-foreground` |
| 페이지 배경(Default 서피스) | #FFFFFF | #161A1F | `--background` |
| Surface Raised(카드) | #FFFFFF | #1E242B | `--card` |
| Surface Overlay(모달·팝오버) | #FFFFFF | #242B33 | `--popover` |
| 본문 텍스트 | #1F2937 | #E5E7EB | `--foreground` |
| 보조 텍스트(라벨·메타, 긴 본문 금지) | #6B7280 | #9CA3AF | `--muted-foreground` |
| 구분선·입력 테두리 | #E5E7EB | rgba(249,250,251,0.12) | `--border` |
| 포커스 링 색상 | #60A5FA | #93C5FD | `--ring` |
| 사이드바 배경 | #1F2937 | #10141A | `--sidebar` |
| 사이드바 활성 항목 | #2563EB | #60A5FA | `--sidebar-primary` |

> Brand는 화면당 하나의 주요 액션에만 예약해서 사용하고(예: 생성/저장 Primary 버튼), 남용하지 않는다. 원시 hex를 컴포넌트에 하드코딩하지 않고 위 시맨틱 토큰(CSS 변수)만 참조해야 한다(REQ-UIX-001 두 번째 EARS).

> **회색→흰색 전환**: 라이트 테마 페이지 배경(`--background`)을 옅은 회색(#F9FAFB)에서 순백(#FFFFFF)으로 변경한다. 헤더·사이드바 콘텐츠 영역(SCR-COM-001)이 대상이며, 사이드바(`--sidebar`, #1F2937 다크 그레이 유지)와 다크 테마 값은 변경하지 않는다. `--background`와 `--card`가 라이트 테마에서 동일한 #FFFFFF가 되지만, 카드는 Elevation(2.5절) 그림자와 `--border`로 구분되므로 시각적 계층은 유지된다.

### 2.2 상태 시맨틱 색상 · Lozenge 배지 (REQ-UIX-007)

상태(Status)·우선순위(Priority) 배지는 pill(999px, 단색 배경)에서 **Lozenge 규격(4px radius, 테두리 유지)** 으로 전환한다. 기본 스타일은 Jira 기본 배지와 동일하게 **subtle**(옅은 배경 + 색상 테두리·텍스트)을 사용하고, SEV1/P1처럼 강한 강조가 꼭 필요한 경우에 한해 **bold**(불투명 배경 + 대비 텍스트, 기존 방식)를 허용한다.

| 의미 | 라이트 subtle 배경 | 라이트 텍스트/테두리 | 다크 subtle 배경 | 다크 텍스트/테두리 | 사용처 |
|------|--------------------|------------------------|--------------------|------------------------|--------|
| Success | #DCFCE7 | #15803D | rgba(74,222,128,0.16) | #4ADE80 | 완료·해결·게시·SLA 준수 |
| Warning | #FEF3C7 | #B45309 | rgba(251,191,36,0.16) | #FBBF24 | 대기·검토·SLA 임박·만료 임박 |
| Danger | #FEE2E2 | #B91C1C | rgba(248,113,113,0.16) | #F87171 | 실패·SLA 위반·SEV1·반려 |
| Info | #DBEAFE | #1D4ED8 | rgba(96,165,250,0.16) | #60A5FA | 진행중·정보 안내 |
| Muted | #F3F4F6 | #6B7280 | rgba(156,163,175,0.16) | #9CA3AF | 종료·비활성·초안 |

- 배지는 색상만으로 상태를 구분하지 않고 항상 텍스트 라벨을 함께 표시한다(REQ-UIX-007 두 번째 EARS).
- `StatusBadge`/`PriorityBadge`의 `tone`/`priority` prop 인터페이스는 변경하지 않고 스타일만 상속한다(FEAT-UIX-007).

### 2.3 타이포그래피 스케일 (REQ-UIX-003)

서체는 Pretendard를 유지한다. Pretendard는 가변 폰트축(Atlassian Sans 전용 653)을 지원하지 않으므로 Bold는 표준 700으로 대체한다. 크기·줄높이·굵기만 ADS 스케일에 정렬한다.

| 구분 | 토큰 | 크기 | 줄높이 | 굵기 | 용도 |
|------|------|------|--------|------|------|
| Heading XXL | `font.heading.xxlarge` | 32px | 36px | 700 | (미사용 — 브랜드/마케팅 전용) |
| Heading L | `font.heading.large` | 24px | 28px | 700 | 페이지 타이틀 |
| Heading M | `font.heading.medium` | 20px | 24px | 700 | 모달 등 큰 컴포넌트 헤더 |
| Heading S | `font.heading.small` | 16px | 20px | 700 | 카드/섹션 헤더 |
| Heading XS | `font.heading.xsmall` | 14px | 20px | 700 | 좁은 공간 헤더 |
| Body L | `font.body.large` | 16px | 24px | 400 | 긴 설명/본문 |
| Body M(기본) | `font.body` | 14px | 20px | 400 | 본문·버튼/컴포넌트 라벨 |
| Body S | `font.body.small` | 12px | 16px | 400 | 보조 콘텐츠(본문 최소 크기, 12px 미만 금지) |
| Metric L | `font.metric.large` | 28px | 32px | 700 | 대시보드 KPI 숫자 |
| Metric M | `font.metric.medium` | 24px | 28px | 700 | 카드 내 KPI 숫자 |
| Metric S | `font.metric.small` | 16px | 20px | 700 | 인라인 수치 강조 |

적용 예: 페이지 타이틀→`heading.large`, 모달 헤더→`heading.medium`, 카드/섹션 헤더→`heading.small`, KPI 카드 숫자→`metric.medium`, 일반 본문/라벨→`body`/`body.small`.

### 2.4 스페이싱 · Radius (REQ-UIX-004)

8px(`space.100`) 기준 스페이싱 스케일을 사용한다.

| 토큰 | px | 용도 |
|------|----|------|
| `space.050` | 4px | 아이콘-텍스트 간격, 밀착 요소 |
| `space.100` | 8px | 컴포넌트 기본 패딩(기준) |
| `space.150` | 12px | 여유로운 컴포넌트 패딩 |
| `space.200` | 16px | 카드 내부 요소 간격 |
| `space.300` | 24px | 카드/섹션 간 간격 |
| `space.400` | 32px | 페이지 레벨 레이아웃 여백 |

컴포넌트 유형별 Radius(기존 단일 `--radius` 구조를 아래 차등 구조로 대체):

| 토큰 | 값 | 적용 컴포넌트 |
|------|----|----------------|
| `radius.xsmall` | 2px | 체크박스, 배지 내부 아이콘 |
| `radius.small` | 4px | Lozenge(상태/우선순위 배지), 태그, 타임스탬프 |
| `radius.medium` | 6px | 버튼, 인풋, 셀렉트, 텍스트영역 |
| `radius.large` | 8px | 카드, 드롭다운 메뉴 |
| `radius.xlarge` | 12px | 모달 |
| `radius.full` | 999px | 아바타(원형 전용, Lozenge에는 사용하지 않음) |

### 2.5 Elevation (REQ-UIX-005)

| 레벨 | 서피스 변수 | 라이트 그림자 | 다크 그림자 | 적용 |
|------|-------------|----------------|--------------|------|
| Default | `--background` | 없음 | 없음 | 페이지 배경 |
| Raised | `--card` | `0 1px 2px rgba(31,41,55,0.08)` | `0 1px 2px rgba(0,0,0,0.4)` | 카드 |
| Overlay | `--popover` | `0 4px 12px rgba(31,41,55,0.16)` | `0 4px 16px rgba(0,0,0,0.6)` | 모달, 드롭다운, 팝오버, 툴팁 |

같은 컴포넌트 안에서 서로 다른 elevation 레벨의 서피스·그림자 토큰을 혼용하지 않는다(REQ-UIX-005 Unwanted 조건).

### 2.6 모션 (REQ-UIX-006)

| 컴포넌트 | 진입 | 퇴장 |
|----------|------|------|
| 모달 | 250ms ease-in-out bold, scale 95%→100% + fade | 200ms ease-in practical, scale 100%→95% + fade |
| 드롭다운/팝오버 | 150ms ease-out practical, fade | 100ms ease-in practical, fade |
| 토스트 | 200ms ease-out bold, fade + slide | 150ms ease-in practical, fade |
| Hover(공통) | 50ms ease-out practical | - |

- `transform`/`opacity` 속성만 애니메이션하고 `width`/`height` 등 레이아웃 속성은 애니메이션하지 않는다(REQ-UIX-006 두 번째 EARS).
- 사이드바 접기/펼치기(폭 190px↔48px)는 레이아웃 속성이라 완전한 transform 대체가 불가하므로, 폭 전환은 유지하되 `duration.medium`(200ms)+ease-in-out bold로 제한하고 메뉴 라벨 텍스트는 opacity 페이드로 보조한다. 이 예외는 **사이드바 컨테이너 자체의 폭 전환에 한정**되며, 메뉴 라벨 등 하위 요소에 별도의 `width`/`max-width` 전환을 추가로 선언해서는 안 된다. 라벨 축소가 필요하면 아이콘은 `shrink-0` 고정폭, 라벨은 `flex-1`+`min-width:0`(overflow 처리)로 구성해 컨테이너 폭 전환에 자연히 종속되게 하고, 라벨에는 opacity 전환만 적용한다.
- `prefers-reduced-motion: reduce` 활성화 시 모든 전환 duration을 0으로 처리한다(REQ-UIX-006 State-driven 조건).

### 2.7 아이콘 (REQ-UIX-010)

lucide-react를 유지하며 기본 크기를 16px로 통일한다(좁은 공간·보조 액션에 한해 12px 허용). 아이콘은 텍스트 라벨을 보강하는 용도로 사용하고, 텍스트 라벨 없이 아이콘 단독으로 쓰이는 요소(헤더 알림 벨, 사이드바 접기 토글, 테마 토글 등)에는 반드시 `aria-label`을 제공한다(REQ-UIX-010 Unwanted 조건).

### 2.8 접근성 — 포커스 링 · 색상 대비 (REQ-UIX-011, REQ-UIX-012)

- 포커스 링: 두께 2px(`border.width.focused`) + 색상 토큰(`--ring`)을 항상 함께 렌더링한다. 링 radius = 요소 radius + 2px, 오프셋 2px.
- 색상 대비: 24px 미만 텍스트 4.5:1, 24px 이상 텍스트/필수 UI 그래픽 3:1(WCAG 2.2 AA). 2.1~2.2절의 라이트/다크 조합은 이 기준을 만족하도록 선정했으며, 개발 단계에서 실제 렌더링 값 기준으로 재검증한다(FEAT-UIX-012 처리 흐름).
- 색상만으로 의미를 전달하지 않는다 — 배지·상태 표시는 항상 텍스트 라벨을 병행한다.

## 3. 화면 목록

| 화면 ID | 화면명 | 관련 요구사항/기능 | 설명 |
|---------|--------|--------------------|------|
| SCR-COM-001 | 앱 셸(App Shell) 레이아웃 | 전 도메인 | 헤더 + 사이드바 + 콘텐츠 + 푸터 골격 |
| SCR-COM-002 | 글로벌 헤더 | REQ-AUTH-004/005, REQ-COM-001 | 로고·통합검색·알림(드롭다운)·사용자 메뉴 |
| SCR-COM-003 | 사이드바 내비게이션 | REQ-AUTH-005 | 역할별 메뉴 노출(RBAC) |
| SCR-COM-004 | 푸터 | 전 도메인 | 버전·저작권 |
| SCR-COM-005 | 인증 가드 / 401 리다이렉트 | REQ-AUTH-005 | 미인증 시 로그인으로 이동 |
| SCR-COM-006 | 403 접근 거부 | REQ-AUTH-005 | 권한 부족 안내 |
| SCR-COM-007 | 공통 티켓 목록/필터 패턴 | 전 티켓 도메인 | 목록·검색·필터·페이지네이션 공통 UX |
| SCR-COM-008 | 공통 티켓 상세 패턴 | 전 티켓 도메인 | 상세·코멘트·타임라인·연결 항목 공통 UX |
| SCR-COM-009 | 토스트·확인 다이얼로그 | 전 도메인 | 성공/오류 피드백·파괴적 동작 확인 |
| SCR-COM-010 | 테마 토글(라이트/다크) | REQ-UIX-002 | 헤더의 테마 전환 컨트롤, 선택 유지 |
| SCR-COM-011 | 통합 검색 결과 | REQ-KM-004 | 지식+티켓 교차 도메인 검색 결과 전체 목록(페이지네이션) |
| SCR-COM-012 | 사용자 가이드 | REQ-COM-002 | 헤더 "?" 아이콘 클릭 시 이동하는 전용 화면. 개요·도메인 및 원칙·역할별 수행 내용과 방법 |
| SCR-COM-013 | 대시보드 | 전 도메인(Role-Menu 동적 매핑 개발 중 식별) | 로그인 후 기본 홈. 전 역할 공통 진입 화면 |
| SCR-COM-014 | 승인 대기함(전 도메인 공용) | 유지보수 요청(승인 프로세스 커스텀) | 승인 프로세스 커스텀 기능 도입에 따라 SCR-SRM-006·SCR-CHG-004·SCR-KM-004를 대체하는 공용 승인 대기·결정 화면 |
| SCR-COM-015 | 언어 선택(i18n) | 유지보수 요청(다국어 지원) | 헤더의 한국어/영어 전환 컨트롤, 선택 유지(6절 i18n 아키텍처 참조) |

## 4. 화면 상세

### SCR-COM-001 · 앱 셸(App Shell) 레이아웃

- **목적**: 로그인 후 모든 화면이 공유하는 기본 골격 제공.
- **레이아웃**: 상단 고정 헤더(높이 56px) / 좌측 사이드바(펼침 190px/접힘 48px, 접기 가능) / 우측 메인 콘텐츠(가변) / 하단 푸터.
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 헤더 영역 | 컨테이너 | SCR-COM-002 삽입 | `--background`(Default elevation) |
  | 사이드바 영역 | 컨테이너 | SCR-COM-003 삽입 | `--sidebar`(라이트/다크 테마 대응) |
  | 콘텐츠 영역 | 컨테이너 | 라우팅된 도메인 화면 렌더 | `--background`(Default elevation) |
- **상태 · 인터랙션**: 사이드바 토글 버튼으로 접기/펼치기(모션은 2.6절 참조). 미인증 진입 시 SCR-COM-005로 리다이렉트. `data-theme` 속성으로 라이트/다크 렌더링(SCR-COM-010).
- **연관 API**: 없음(레이아웃)

### SCR-COM-002 · 글로벌 헤더

- **목적**: 통합 검색(지식+티켓 교차 도메인)·사용자 가이드·언어 전환·알림·테마 전환·계정 접근 진입점.
- **레이아웃**: 좌측 로고·현재 도메인 타이틀 / 중앙 통합 검색바(입력 시 드롭다운 미리보기) / 우측 "?" 사용자 가이드 아이콘·지구본(언어 선택) 아이콘·테마 토글·알림 벨·사용자 아바타 메뉴(이 순서로 배치 — "설정성" 토글인 언어·테마를 인접 배치하고 알림·계정 액션과 구분).
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 통합 검색바 | 입력 | 지식/서비스요청/인시던트/문제/변경 키워드 검색 진입(역할별 접근 가능 도메인만 노출) | `--border`/`--ring`(포커스) |
  | 검색 미리보기 드롭다운 | 오버레이 리스트 | 도메인 아이콘·제목·상태 배지 상위 5~8건 미리보기 | Overlay elevation(2.5절) |
  | 사용자 가이드 아이콘 | 아이콘 버튼("?") | 클릭 시 사용자 가이드 전용 화면(SCR-COM-012)으로 이동, 지구본(언어) 아이콘 왼쪽 배치 | `--foreground`, `aria-label`="사용자 가이드" |
  | 언어 선택 아이콘 | 아이콘 버튼(지구본) | 클릭 시 아이콘 바로 아래 언어 선택 미니 팝업(SCR-COM-015), 사용자 가이드 아이콘과 테마 토글 사이 배치 | `--foreground`, `aria-label`="언어 선택" |
  | 테마 토글 | 아이콘 버튼 | 라이트/다크 전환(SCR-COM-010) | `--foreground`, `aria-label` 필수 |
  | 알림 벨 | 버튼+뱃지 | 만료·승인 대기 등 알림 카운트(확인처리(dismiss)된 알림은 제외한 전체 건수 합계), 클릭 시 알림 드롭다운 오픈 | `--warning`(뱃지) |
  | 알림 드롭다운 | 오버레이 리스트 | 역할별 승인 대기(전 도메인 공용, 승인 프로세스 커스텀 기능으로 서비스요청·변경 한정에서 확장)·자산 만료 임박 알림 중 확인처리되지 않은 항목(5초 polling으로 누적, 표시 상한 없음). 각 라인 2줄(1행: 도메인 라벨(Lozenge)+우측 시간/만료 표시+확인처리(X) 버튼, 2행: 제목 truncate)+"상세 보기" 버튼(REQ-COM-001/FEAT-COM-001), 헤더 우측 상단 "모두 지우기" 버튼 | Overlay elevation(2.5절) |
  | 모두 지우기 버튼 | 텍스트 버튼 | 알림 드롭다운 헤더 우측 최상단에 배치. 클릭 시 현재 팝오버에 표시 중인 알림 전체(누적된 만큼, 상한 없음)를 일괄 확인처리 | `--muted-foreground`, `aria-label`="모든 알림 확인처리" |
  | 개별 확인처리(X) 버튼 | 아이콘 버튼 | 각 알림 라인 1행의 시간/만료 표시 우측에 배치. 클릭 시 해당 알림 1건만 확인처리(라인 클릭·"상세 보기"와 별개 동작, 이벤트 전파 차단) | `--muted-foreground`, `aria-label`="알림 확인처리" |
  | 사용자 메뉴 | 드롭다운 | 내 프로필·비밀번호 변경·로그아웃 | `--primary` |
- **알림 드롭다운 항목 매핑(REQ-COM-001/FEAT-COM-001, 신규 API 없이 기존 대기함 API 응답에 제목·시간 필드만 추가)**:
  | 알림 유형 | 데이터 출처 | 도메인 라벨(1행 좌) | 1행 우측 표시 | 2행 제목(40자 초과 시 말줄임) | 상세 보기 이동 경로 |
  |-----------|-------------|----------------------|----------------|-------------------------------|----------------------|
  | 승인 대기(전 도메인 공용) | API-COM-003 `GET /api/v1/approvals?scope=mine` (`approvalRequestId`/`ticketType`/`ticketId`/`ticketKey`/`ticketSummary`/`requester`/`requestedAt`) — 승인 프로세스 커스텀 기능으로 API-SRM-012/API-CHG-007 대체 | `ticketType`별 라벨(서비스요청 승인/변경 승인/지식 승인/인시던트 승인/문제 승인 등) | `requestedAt` 기준 상대 시간 | `ticketSummary`(도메인별 summary/title/name을 그대로 노출) | `ticketType`별 상세 경로(`/service-requests/{ticketId}`, `/changes/{ticketId}` 등) |
  | 자산 만료 임박 | API-ITAM-001 `GET /api/v1/assets?expiringWithinDays=30&size=8` (`id`/`assetKey`/`name`/`expiryDate`) | 자산 만료 | `{expiryDate} 만료`(미래 날짜라 상대 시간 대신 만료일 그대로 표시, 목록 화면과 동일 포맷) | `name`(자산명) | `/assets/{id}` |
- **상대 시간 표시 규칙(1행 우측, 팝오버를 연 시점 기준 1회 계산, 실시간 갱신 불필요)**:
  | 경과 시간 | 표시 |
  |-----------|------|
  | 60초 미만 | 방금 전 |
  | 60분 미만 | N분 전 |
  | 24시간 미만 | N시간 전 |
  | 7일 미만 | N일 전 |
  | 7일 이상 | 절대 날짜(예: 2026-08-10, 목록 화면과 동일 날짜 포맷) |
- **상태 · 인터랙션**:
  - 검색어 입력(디바운스) 시 미리보기 드롭다운 갱신, 결과 항목 클릭 시 해당 상세 화면으로 이동. Enter 또는 "전체 결과 보기" 클릭 시 SCR-COM-011로 이동. 결과 0건이면 "검색 결과가 없습니다" 안내.
  - 알림 벨 클릭 시 벨 버튼 하단 우측 정렬(`align="end"`)로 알림 드롭다운을 오픈(검색 미리보기와 동일한 Popover 패턴 재사용, 외부 클릭/Esc로 닫힘). 폭 320px 고정(검색 드롭다운과 달리 트리거 폭에 종속되지 않음), 목록 높이 320px 초과 시 세로 스크롤.
  - 드롭다운 목록은 최초 진입 시 승인 대기(API-COM-003 응답 순서, 전 도메인 공용) → 자산 만료 임박 순으로 이어붙여 노출하며, 이후 5초 polling으로 누적되는 신규 항목도 이 순서를 유지한 채 뒤에 추가된다(**표시 상한 없음** — 기존 "상위 8건" cap은 폐지되었으며, 목록 높이 320px 초과 시 세로 스크롤로 전체 항목에 접근). 알림 벨 뱃지 카운트는 표시 목록의 누적 여부와 무관하게 역할별 전체 대기 건수 합계(승인 대기 전체 + 자산 만료 임박 전체 `totalElements`)를 그대로 표시하며, 알림이 0건이면 뱃지를 숨긴다.
  - **알림 5초 polling**: 인증된 세션 동안 알림 후보(승인 대기(전 도메인 공용, API-COM-003)·자산 만료)와 확인처리 이력(API-COM-002)을 5초 간격으로 재조회한다(BE/DB 변경 없음, 승인 대기 출처만 API-SRM-012/API-CHG-007에서 API-COM-003으로 전환). 브라우저 탭이 백그라운드(비활성) 상태이면 polling을 정지하고, Page Visibility API로 포그라운드 복귀를 감지해 재개한다. 알림 드롭다운이 열려 있는 동안에도 polling은 계속 진행된다. 조회 결과는 현재 표시 중인 목록과 **merge**한다 — 신규 조회 결과 중 클라이언트에 아직 없는 항목(알림 유형+원본 ID 키 기준)만 판별해 기존 목록 뒤에 이어 붙이고, 기존 표시 순서는 그대로 유지한다(도메인 우선순위 재정렬 없음). 이미 표시된 항목은 서버 응답에서 더 이상 조회되지 않게 되더라도(예: 승인 대기가 다른 경로로 처리됨) 목록에서 제거하지 않으며, 제거는 오직 사용자의 개별 X 또는 "모두 지우기" 확인처리(dismiss)에만 의존한다. 알림 벨 뱃지 카운트는 이 목록 merge/누적과 무관하게 매 polling 주기마다 확인처리 이력을 제외한 서버 기준 실시간 전체 대기 건수 합계로 다시 계산한다. 연속 조회 실패 시에도 별도 backoff나 polling 중단 없이 5초 고정 간격 재시도를 유지한다.
  - 각 알림 라인의 "상세 보기" 버튼(또는 라인 클릭)을 클릭하면 위 매핑 표의 이동 경로로 이동하고 드롭다운을 닫는다.
  - 알림이 0건이면 드롭다운 내부에 "새로운 알림이 없습니다" 안내를 표시한다.
  - **알림 확인처리**: "모두 지우기" 클릭 시 현재 드롭다운에 표시 중인 알림 전체(누적된 만큼, 상한 없음)를 확인처리 API(신규, `api_spec/common.md`)로 일괄 전송하고, 성공 시 목록을 즉시 비워 "새로운 알림이 없습니다"로 전환하며 뱃지 카운트에서도 확인처리된 건수만큼 차감한다. 개별 X 버튼 클릭 시 해당 알림 1건만 동일 API로 확인처리하고 목록에서 즉시 제거·뱃지 카운트 -1 한다(라인 클릭·"상세 보기" 이동과는 별개 동작이라 클릭 이벤트 전파를 막는다). 확인처리는 표시 여부에만 영향을 주며 원본 업무 데이터(승인 대기·자산 만료 상태)는 변경하지 않으므로 파괴적 동작이 아니다 — SCR-COM-009 확인 다이얼로그 없이 즉시 처리한다. 확인처리 이력은 사용자별로 영구 저장되어, 같은 알림(동일 승인 건·동일 자산)은 이후 재조회에서도 다시 나타나지 않는다.
  - 사용자 메뉴 > 로그아웃 클릭 시 확인 후 토큰 무효화. 테마 토글 클릭 시 SCR-COM-010 동작 수행. 언어 선택 아이콘 클릭 시 SCR-COM-015 동작 수행.
- **연관 API**: `GET /api/v1/search?keyword=&size=`(미리보기), `POST /api/v1/auth/logout`, API-COM-003 `GET /api/v1/approvals?scope=mine`(전 도메인 공용 승인 대기, `api_spec/common.md`), API-ITAM-001 `GET /api/v1/assets?expiringWithinDays=&size=`, 알림 확인처리 API(API-COM-001, `api_spec/common.md`, "모두 지우기"·개별 X 공용), 확인처리 이력 조회 API(API-COM-002, `api_spec/common.md`, 알림 후보 필터링용, 5초 polling마다 재조회)

### SCR-COM-003 · 사이드바 내비게이션

- **목적**: 도메인 간 이동. 사용자 역할에 따라 메뉴 항목을 동적 노출(RBAC).
- **레이아웃**: 세로 메뉴 리스트(대시보드·서비스요청·인시던트·문제·변경·지식·자산·관리자 등). 그룹 구분선. 폭은 펼침 190px/접힘 48px(콘텐츠 영역 확보 목적).
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 그룹 헤더 라벨 | 텍스트 | 그룹명(uppercase), 10px | `--sidebar-foreground/60` |
  | 메뉴 항목 | 링크 | 라벨 12px, 활성 항목 강조 | `--sidebar-primary`(활성)/`--sidebar-foreground`(텍스트) |
  | 관리자 그룹 | 링크 그룹 | System Admin 역할에게만 노출 | `--sidebar-primary` |
- **상태 · 인터랙션**: 메뉴 항목·그룹·순서는 SCR-ADMIN-006에서 관리자가 등록한 메뉴 마스터 데이터를 조회해 구성하며(하드코딩 아님), 로그인 사용자가 보유한 역할에 매핑된 메뉴만 렌더링(RBAC). 최종 사용자(End User)는 매핑된 포털 성격 메뉴(서비스요청·지식)만 노출. 사이드바 배경은 라이트/다크 테마별 값(2.1절 `--sidebar`)으로 렌더링되며 고정 색이 아니다. 폭·폰트 축소는 2.6절 모션 예외와 동일한 컨테이너 폭 전환 구조를 유지한 채 값만 조정하며, 라벨 축소로 인한 truncate 처리는 기존 `min-w-0`+`truncate` 구조를 그대로 사용한다.
- **연관 API**: 내 메뉴 조회 API(신규, `api_spec/auth.md`, 로그인 사용자의 역할 기반 메뉴 목록)

### SCR-COM-004 · 푸터

- **목적**: 시스템 버전·저작권 표시.
- **레이아웃**: 콘텐츠 하단 1행. 좌측 저작권, 우측 버전.
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 저작권/버전 텍스트 | 텍스트 | 정적 표시 | `--muted-foreground` |
- **상태 · 인터랙션**: 정적.
- **연관 API**: 없음

### SCR-COM-005 · 인증 가드 / 401 리다이렉트

- **목적**: 보호 라우트 진입 시 유효 Access Token 확인, 미인증/만료 시 처리.
- **레이아웃**: 화면 없음(라우트 가드). 토큰 만료 시 Refresh 시도 → 실패 시 로그인 화면으로 이동.
- **구성 요소**: 없음(로직).
- **상태 · 인터랙션**: 401 응답 수신 시 Refresh 재시도 1회 → 실패 시 세션 종료·로그인 이동 및 토스트 "세션이 만료되어 다시 로그인해주세요".
- **연관 API**: `POST /api/v1/auth/refresh`

### SCR-COM-006 · 403 접근 거부

- **목적**: 권한 없는 리소스 접근 시 안내.
- **레이아웃**: 중앙 정렬 안내 카드(아이콘·메시지·"이전으로" 버튼).
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 안내 메시지 | 텍스트 | "이 페이지에 접근할 권한이 없습니다" | `--danger`(아이콘) |
  | 이전으로 버튼 | 버튼 | 직전 화면 복귀 | `--primary` |
- **상태 · 인터랙션**: API 403 또는 라우트 권한 미달 시 표시.
- **연관 API**: 없음

### SCR-COM-007 · 공통 티켓 목록/필터 패턴

- **목적**: 요청·인시던트·문제·변경·자산 등 목록 화면이 공유하는 목록 UX 정의.
- **레이아웃**: 상단 필터바(상태·우선순위·담당자·기간·키워드) / 표 형태 목록 / 하단 페이지네이션. 우측 상단 "신규 생성" 버튼.
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 필터바 | 입력/셀렉트 | 상태·우선순위·담당자·기간·키워드 | `--border`/`--ring`(포커스) |
  | 목록 표 | 표 | 식별키·제목·상태·우선순위·담당자·갱신일 | `--foreground`/`--card` |
  | 상태 배지 | Lozenge(2.2절) | 상태별 시맨틱 색상, 4px radius+테두리 | 상태 시맨틱(subtle 기본) |
  | 우선순위 배지 | Lozenge(2.2절) | P1~P4 등급, 4px radius+테두리 | Danger~Muted(subtle 기본, P1은 bold 허용) |
  | 신규 생성 버튼 | 버튼 | 생성 화면 이동 | `--primary` |
  | 페이지네이션 | 컨트롤 | 페이지 이동 | `--primary` |
- **상태 · 인터랙션**: 로딩 시 스켈레톤, 결과 0건 시 빈 상태 안내. 행 클릭 시 상세로 이동. 권한 없는 "신규 생성"은 숨김.
- **컬럼 폭 고정**: 페이지 이동 시 컬럼 폭이 데이터 길이에 따라 흔들리는 문제를 해결하기 위해 목록 표 컬럼에 고정 폭을 도입한다.
  - `components/common/data-table.tsx`의 `Column<T>`에 선택 필드 `width?: number`(px)를 추가한다. `DataTable`은 `<colgroup>`으로 각 컬럼에 `<col style={{ width, minWidth: width, maxWidth: width }} />`(width 지정 시) 또는 `<col />`(미지정 시)을 렌더링하고, 내부 `<Table>`에 `table-fixed`(table-layout:fixed)를 적용한다. 이 변경은 `DataTable`을 경유하는 목록에만 적용되며, `components/ui/table.tsx` 프리미티브 자체(예: `LoginPage.tsx`의 테스트 계정 표처럼 `DataTable` 없이 직접 조합하는 표)는 기존 auto layout을 그대로 유지해 영향받지 않는다.
  - **폭 지정 원칙**: 화면별 컬럼 중 상태/유형/심각도/우선순위/날짜/담당자 등 값의 글자 수가 일정한 범주형·정형 컬럼은 `width`를 지정하고, 제목/요약/이름처럼 길이가 가변적인 대표 텍스트 컬럼 1개는 `width`를 지정하지 않아(auto) 나머지 여백을 모두 흡수하도록 한다(`table-fixed`에서 미지정 컬럼이 잔여 폭을 가져감). 폭 값은 헤더 라벨 글자 수(12px 기준)와 셀 콘텐츠 유형(뱃지/날짜/텍스트/숫자)을 근거로 화면별로 산정하며, 구체적인 값은 각 도메인 `screen/{domain}.md`의 해당 화면 항목에 표로 명시한다.
  - **행 높이 균일화(7절 페이지당 아이템 수 산정의 전제)**: 페이지당 아이템 수를 "스크롤 미노출" 기준으로 정확히 산정하려면 모든 행이 동일한 높이(40px, `TableCell`/`TableHead` 기본 높이)를 유지해야 한다. 이를 위해 자유 텍스트를 렌더링하는 모든 셀(고정 폭·auto 폭 무관 — 이메일·경로·제목·주체/대상 등)에는 단일 줄 말줄임(`className="truncate"` 또는 기존 관례인 `line-clamp-1`)을 적용해 줄바꿈으로 인한 행 높이 증가를 방지한다. 뱃지·날짜·숫자 등은 원래 한 줄 콘텐츠라 별도 처리가 필요 없다.
  - **공통 폭 기준값(도메인 표에서 참조하는 표준 카테고리)**:
    | 컬럼 유형 | 폭(px) | 근거 |
    |-----------|--------|------|
    | 짧은 식별키/코드(식별키·접수번호·케이스ID 등) | 130 | 영숫자 코드(예: `SR-2026-00123`) 길이 고정 |
    | 단일 상태성 뱃지(상태·유형·심각도·우선순위·위험도·SLA·부서 등) | 110 | Lozenge 배지 2~5자 라벨 |
    | 좁은 뱃지(2자 헤더 등, 예: PM) | 90 | 헤더 라벨이 매우 짧음 |
    | 숫자/스코어 뱃지(리스크 스코어 등) | 130 | 헤더 라벨(6~7자)+숫자 또는 "미산정" 폴백 |
    | 사람/소유자(담당자·소유자·책임자·주체·대상자) | 120 | 이름 또는 "미배정/미지정" 폴백 |
    | 날짜(YYYY-MM-DD) | 110 | 고정 10자 포맷 |
    | 일시(YYYY-MM-DD HH:mm) | 160 | 고정 16자 포맷 |
    | 숫자(우측 정렬, 예: 순서) | 80 | 1~3자리 숫자 |
    | 액션 버튼 1개(우측 정렬) | 140 | 버튼 1개 폭 |
    | 액션 버튼 2개(우측 정렬) | 180 | 버튼 2개+간격 |
    | 액션 버튼 3개(우측 정렬) | 260 | 버튼 3개+간격 |
    | 대표 텍스트(제목·요약·이름 등, 1개만) | 미지정(auto) | 잔여 폭 흡수, `line-clamp-1`로 말줄임 유지 |
- **연관 API**: 각 도메인 목록 조회 API(`GET .../{domain}?filters`)

### SCR-COM-008 · 공통 티켓 상세 패턴

- **목적**: 티켓 상세 화면이 공유하는 레이아웃(본문·사이드 메타·코멘트·타임라인·연결 항목).
- **레이아웃**: 상단 제목·식별키·상태/우선순위 배지·액션 버튼 / 좌측 본문·코멘트·타임라인 / 우측 메타 패널(담당자·SLA·연결 항목·자산/CI).
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 상태 전이 버튼 | 버튼 | 허용된 다음 상태만 노출. 라벨은 도착 상태명이 아닌 **동작 동사형**(예: "이행중"이 아니라 "이행 시작") | `--primary` |
  | 코멘트 입력·목록 | 입력/리스트 | 시간순 코멘트 | `--foreground`/`--card` |
  | 타임라인 | 리스트 | 상태 변경·배정·업데이트 이력. 각 항목에 행위 수행 주체자(actor) 표시(SRM/ESM/INCIDENT 3개 도메인만 — 이 3개 도메인만 본 공통 `Timeline` 컴포넌트를 사용) | `--info` |
  | 연결 항목 패널 | 리스트 | 인시던트↔문제↔변경, 자산/CI, 지식 링크 | `--ring` |
- **상태 · 인터랙션**: 허용되지 않은 상태 전이 버튼은 비노출. 권한 부족 액션은 비활성/숨김(403 방지). 저장 시 토스트 피드백.
- **상태 전이 버튼 라벨 아키텍처**: 버튼 라벨은 각 도메인 `status.ts`의 `transitionLabel(t, target)`(도착 상태별 동작 동사형 매핑, i18n 키 `{ns}:transition.{target}`, SRM/INCIDENT/PROBLEM/CHANGE/VULNERABILITY/ASSET/ESM 7개 도메인에 존재)이 결정한다. **`statusLabel(t, target)`은 배지 표시와 전이 완료 토스트 문구("상태가 '{{status}}'로 변경되었습니다")에 사용**한다(전이의 "결과 상태"를 알리는 문구라 도착 상태명이 맞다 — 버튼(동작 예고)과 토스트(결과 안내)의 문구 의도가 다르므로 별개 함수로 관리한다). 구체적인 도메인별 라벨 값은 각 도메인 `screen/{domain}.md`의 해당 화면 항목에 표로 명시한다.
- **타임라인 actor 표시 아키텍처**: `TimelineEvent`는 `BaseEntity` 상속으로 `createdBy`(인증 사용자 email, JPA Auditing 자동기록)를 저장한다. SRM/ESM/INCIDENT 3개 도메인의 상세 응답 `TimelineEntry` DTO(SRM/ESM `RequestDetailResponse.TimelineEntry`, INCIDENT `IncidentDetailResponse.TimelineEntry`)는 `actor: string`(표시용 이름) 필드를 갖고, 각 Service의 상세 조회 메서드가 `appUserRepository.findByEmail(event.getCreatedBy())`로 이름을 조회해(다른 도메인의 `requesterName` 조회와 동일 패턴, 조회 실패 시 email 그대로 폴백) 채운다. FE는 `Timeline`(`components/common/timeline.tsx`)의 `actor` prop에 이 값을 주입한다(컴포넌트 자체는 이미 `actor` prop을 지원). 타임라인 메시지는 이 3개 도메인의 Service가 `target.name()`(enum 코드) 대신 각 상태 enum의 한글 라벨(FE `status.ts`의 `STATUS_LABEL`과 동일한 값, 백엔드 enum의 `label()` 메서드)을 사용해 구성한다(예: `"상태가 '이행 중'으로 변경되었습니다"`. 다른 타임라인 메시지 유형(SUBMIT/ASSIGN 등)은 기존처럼 한글 하드코딩을 유지, i18n 전환은 이번 범위 밖).
- **연관 API**: 각 도메인 상세·코멘트·전이·링크 API

### SCR-COM-009 · 토스트·확인 다이얼로그

- **목적**: 성공/오류 피드백과 파괴적/비가역 동작(폐기·삭제·반려·로그아웃) 확인.
- **레이아웃**: 토스트는 우상단 스택, Overlay elevation(2.5절) 적용. 확인 다이얼로그는 중앙 모달(제목·본문·취소/확인), Overlay elevation 적용.
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 성공 토스트 | 토스트 | 처리 성공 안내 | `--success` |
  | 오류 토스트 | 토스트 | 4xx/5xx 오류 메시지 | `--danger` |
  | 확인 다이얼로그 | 모달 | 파괴적 동작 확인 | `--danger`(확인 버튼) |
- **상태 · 인터랙션**: 오류 응답의 표준 메시지를 토스트로 노출. 확인 다이얼로그는 확인 시에만 API 호출. 진입/퇴장 모션은 2.6절(토스트·모달) 토큰을 따른다.
- **구현 참고(SweetAlert2 도입)**:
  - 토스트(`components/common/toast.ts`)와 확인 다이얼로그(`components/common/confirm-dialog.tsx`)의 내부 구현을 SweetAlert2로 교체한다. **범용 모달(`components/common/modal.tsx`, 폼/상세 등 비파괴 콘텐츠)은 대상에서 제외**하고 기존 Radix Dialog 그대로 유지한다(확정된 결정 1).
  - 두 컴포넌트의 **외부 API(props/함수 시그니처)는 그대로 유지**한다 — `toast.success/error/info(message, description?)`, `<ConfirmDialog open onOpenChange title description confirmLabel cancelLabel destructive loading onConfirm />`를 바꾸지 않아 83개 이상의 기존 호출부를 수정하지 않는다. `ConfirmDialog`는 선언형 컴포넌트 API를 유지한 채, 내부에서 `open` prop 변화를 감지해 SweetAlert2를 명령형으로 호출(`Swal.fire(...).then(result => result.isConfirmed ? onConfirm() : onOpenChange(false))`)하는 래퍼 패턴으로 구현한다.
  - 시각적 일관성: SweetAlert2 `customClass`(`popup`/`title`/`htmlContainer`/`confirmButton`/`cancelButton` 등)에 프로젝트 전용 CSS 클래스를 지정하고, 해당 클래스는 기존 시맨틱 토큰(`--card`/`--popover`/`--foreground`/`--primary`/`--destructive`/`--border`, 2.4절 Radius `radius.large`(토스트)·`radius.xlarge`(확인 다이얼로그), 2.5절 Overlay elevation)만 참조한다. `buttonsStyling: false`로 SweetAlert2 기본 버튼 스타일을 끄고 기존 `Button` 컴포넌트(variant=primary/destructive/ghost)와 동일한 시각 규격을 적용한다.
  - 다크모드: 팝업은 `document.body` 하위(=`document.documentElement`의 자손)에 렌더링되므로, `theme-toggle.tsx`가 `documentElement`에 설정하는 `data-theme` 속성 기반 CSS 변수(`:root`/`[data-theme="dark"]`)가 별도 동기화 로직 없이 그대로 상속된다. 이 프로젝트는 OS 설정이 아니라 사용자가 직접 전환하는 명시적 라이트/다크 토글(SCR-COM-010)을 이미 갖고 있으므로, SweetAlert2 내장 `theme` 옵션(OS 설정 기반 `auto` 등)은 사용하지 않고 토큰 기반 커스텀 CSS로 다크모드를 반영한다.
  - 토스트는 `Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, ... })` 패턴으로 구현해 기존 우상단 스택 레이아웃을 유지한다.
  - 모션(2.6절)은 SweetAlert2 기본 애니메이션이 아니라 프로젝트 지정 duration/easing에 맞춰 `showClass`/`hideClass`(커스텀 CSS 애니메이션)로 재정의한다.
  - 신규 의존성: `sweetalert2`(`package.json`).
- **연관 API**: 없음(공통 UX)

### SCR-COM-010 · 테마 토글(라이트/다크)

- **목적**: 사용자가 라이트/다크 테마를 전환하고 선택을 유지한다(REQ-UIX-002).
- **레이아웃**: 헤더 우측(알림 벨 좌측)에 배치되는 아이콘 버튼. 라이트 모드에서는 달 아이콘, 다크 모드에서는 해 아이콘 표시.
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 테마 토글 버튼 | 아이콘 버튼 | 클릭 시 즉시 테마 전환 | `--foreground`, `aria-label`="테마 전환" |
- **상태 · 인터랙션**:
  - 최초 진입 시 저장된 선호 테마를 로드해 `data-theme` 속성에 적용, 저장된 값이 없거나 유효하지 않으면 라이트를 기본값으로 적용.
  - 클릭 시 반대 테마로 즉시 전환하고 선택값을 저장, 재방문 시에도 유지.
- **연관 API**: 없음(클라이언트 로컬 저장)

### SCR-COM-011 · 통합 검색 결과

- **목적**: 헤더 검색에서 Enter 또는 "전체 결과 보기" 선택 시, 지식+티켓(서비스요청·인시던트·문제·변경) 교차 도메인 검색 결과를 페이지네이션으로 조회.
- **레이아웃**: 상단 검색어 표시·재검색 입력 / 결과 목록(도메인 배지·제목·상태 배지·발췌·갱신일) / 하단 페이지네이션.
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 도메인 배지 | Lozenge(2.2절) | KNOWLEDGE/SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE 구분 | Info(subtle) |
  | 결과 행 | 리스트 아이템 | 제목·상태 배지·발췌(snippet)·갱신일 | `--foreground`/`--card` |
  | 상태 배지 | Lozenge(2.2절) | 도메인별 상태 원문 값 | 상태 시맨틱(subtle) |
  | 페이지네이션 | 컨트롤 | 병합·정렬된 전체 결과 기준 페이지 이동 | `--primary` |
- **상태 · 인터랙션**: `updatedAt` 내림차순 단일 정렬(관련도 스코어링 없음, MVP). 결과 0건 시 빈 상태 안내. 행 클릭 시 `url` 필드 기준 해당 도메인 상세 화면으로 이동. 역할별 접근 불가 도메인은 결과에서 자동 제외(SCR-COM-002 참고).
- **컬럼 폭(SCR-COM-007 아키텍처 적용)**:
  | 컬럼 | 폭(px) | 근거 |
  |------|--------|------|
  | 도메인 | 130 | "서비스 요청" 등 최장 도메인 라벨 기준 |
  | 제목 | 미지정(auto) | 제목+식별키+발췌 3줄 콘텐츠, 잔여 폭 흡수 |
  | 상태 | 130 | 도메인별 상태 라벨 길이 편차 고려 |
  | 갱신일 | 160 | `formatDateTime` 일시 포맷(YYYY-MM-DD HH:mm) |
- **페이지당 아이템 수**: 13건(7절 패턴 A).
- **연관 API**: `GET /api/v1/search?keyword=&page=&size=`

### SCR-COM-012 · 사용자 가이드

- **목적**: 인증된 사용자가 플랫폼 개요·업무 도메인별 목적과 원칙·역할별 수행 내용과 방법을 전용 화면에서 확인할 수 있게 한다(REQ-COM-002/FEAT-COM-002, 모달이 아닌 전용 화면으로 제공).
- **레이아웃**: Atlassian Confluence 문서 페이지 스타일(ADS 토큰 체계와 자연스럽게 어울리는 긴 글 문서 레이아웃)로 구성한다. 상단 문서 헤더 영역(문서 제목 "사용자 가이드", `heading.large`)을 명확히 구분하고, 그 아래 좌측에 sticky 섹션 목차(TOC) 패널(개요·도메인 및 원칙·역할별 수행 내용과 방법 3개 링크, 스크롤에 따라 현재 섹션 강조), 우측 넓은 영역에 Markdown 본문(최대 폭 제한으로 읽기 좋은 line-length 유지, 헤딩 계층 `heading.medium`(H2, 대분류)/`heading.small`(H3, 하위 항목)로 명확히 구분). 사이드바·헤더는 유지된 채(SCR-COM-001 콘텐츠 영역에 렌더) 일반 도메인 화면과 동일한 앱 셸 안에서 열린다(모달 아님).
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 문서 헤더 | 텍스트 | 문서 제목 "사용자 가이드"(`heading.large`), TOC와 시각적으로 구분되는 최상위 진입 영역 | `--foreground` |
  | 섹션 목차 패널(TOC) | 사이드 네비(sticky) | 개요/도메인 및 원칙/역할별 수행 내용과 방법 3개 링크, 현재 스크롤 위치의 섹션을 강조 | `--sidebar-primary`(활성)/`--foreground`(텍스트) |
  | 개요 콘텐츠(1절, H2) | Markdown 렌더링(읽기 폭 제한) | `user-guide-content.md` 1절 전체를 문단 그대로 Markdown 렌더링(굵게·구분선 포함), 아코디언 없이 순차 표시, 최대 폭 제한으로 가독성 확보 | `--foreground` |
  | 도메인 섹션(2절, H2 아래 H3 11개) | 아코디언 리스트 | 11개 업무 도메인(auth·service-request·incident·problem·change·knowledge·asset·esm·vulnerability·compliance·infra-monitoring) 각각 도메인명(H3)+목적+핵심 원칙(항목 내부 텍스트는 Markdown 인라인 서식 그대로 렌더링), 역할 무관 전체 노출 | `--foreground`/`--card` |
  | 역할 섹션(3절, H2 아래 H3 16개) | 아코디언 리스트 | 정의된 16개 역할 각각 역할명(H3)+페르소나+주요 수행 내용과 방법(항목 내부 텍스트는 Markdown 인라인 서식 그대로 렌더링) | `--foreground`/`--card` |
  | 내 역할 배지 | Lozenge | 로그인 사용자가 보유한 역할에 표시 | Info(subtle) |
- **상태 · 인터랙션**:
  - 헤더 "?" 아이콘 클릭 시 라우팅으로 본 화면(`/guide`)으로 이동(신규 탭/모달이 아니라 일반 페이지 전환, 뒤로가기로 이전 화면 복귀).
  - 진입 시 최상단(개요)부터 표시하며, 좌측 목차 링크 클릭 시 해당 섹션으로 스크롤 이동. TOC는 3개 대분류 링크(개요/도메인 및 원칙/역할별 수행 내용과 방법)만 제공하며, 11개 도메인·16개 역할 각각에 대한 하위 TOC 링크는 두지 않는다(아코디언 목록 자체가 하위 탐색 역할을 한다).
  - "개요" 섹션은 아코디언 없이 문서 원문을 그대로 순차 렌더링한다(1절 전체를 위에서 아래로).
  - "도메인 및 원칙" 섹션은 아코디언 리스트로 구성한다(1개 Markdown 블록으로 순차 렌더링하지 않음). 11개 도메인 아코디언은 로그인 사용자의 역할과 무관하게 항상 전체 노출(시스템 전체 소개 목적, 필터링 없음)하며 원문 순서(2.1~2.11)를 유지한다. 기본은 모두 접힌 상태이며 클릭 시 개별 펼침.
  - "역할별 수행 내용과 방법" 섹션도 아코디언 리스트로 구성한다. 로그인 사용자가 보유한 역할(1개 이상)은 목록 최상단에 "내 역할" 배지와 함께 기본 펼쳐진 상태로 고정 노출하고, 나머지 역할은 원문 순서(3.1~3.16)를 유지한 채 그 아래 접힌 아코디언으로 나열한다(클릭 시 펼침). 16개 역할 전체가 항상 탐색 가능해야 한다(사용자가 역할을 보유하지 않은 경우에도 전체가 접힌 상태로 노출). 아코디언으로 구성하는 이유는 "내 역할 고정 노출"을 위해 도메인/역할 섹션의 표시 순서를 사용자별로 다르게 조정해야 하기 때문이며(전체를 고정 순서 원문 그대로 순차 렌더링하면 이 요구를 만족할 수 없음), 이는 Confluence의 "펼치기(Expand)" 매크로와 동일한 패턴이라 문서 스타일과도 배치되지 않는다.
  - 콘텐츠는 정적 데이터로 제공되며 화면 진입 시 서버 조회 없이 즉시 렌더링한다. 콘텐츠 로드 실패 시 오류 안내를 표시한다(FEAT-COM-002 예외 처리).
- **구현 참고**:
  - 콘텐츠가 Markdown 형식으로 작성되어 있어 렌더링을 위해 Markdown 파서/렌더러가 필요하다(현재 frontend에 미도입 — FE 구현 시 라이브러리 도입 필요).
  - `docs/01_analyze/feature/user-guide-content.md`는 `source/frontend` 빌드에 포함되지 않는 위치이므로, 화면 구현 시 이 문서의 내용을 그대로(가공 없이) frontend 소스(`src/` 하위)로 옮겨와 사용한다. 이후 콘텐츠 문구가 바뀌면 analyzer가 원본 문서를 갱신하고 FE가 동일하게 재동기화한다.
  - **다국어**: 본문도 번역 대상에 포함되며(범위 확정, 6.1절), 문장 단위 키가 아닌 **문서 단위 병행 파일**로 관리한다. 한국어 원문과 절·아코디언 구조가 동일한 영어본 `docs/01_analyze/feature/user-guide-content.en.md`를 신규 작성하고, 두 문서 모두 가공 없이 frontend로 이관한다. 현재 선택된 언어(SCR-COM-015)에 따라 `UserGuideOverview`/`UserGuideDomainSection`/`UserGuideRoleSection`가 대응하는 언어의 콘텐츠 세트를 렌더링한다(6.3절 참조).
- **연관 API**: 없음(정적 콘텐츠)

### SCR-COM-013 · 대시보드

- **목적**: 로그인 성공 시 이동하는 기본 홈. 전 역할 공통 진입 화면(Role-Menu 동적 매핑 개발 중 식별 — `screen`/`screen_role` 기반 메뉴 매핑(API-AUTH-022)이 동작하려면 사이드바 최상단 항목(경로 `/`)도 정식 화면 코드가 있어야 함을 반영해 신규 부여).
- **레이아웃**: 콘텐츠 영역에 환영 안내(placeholder 수준, 향후 위젯 확장 여지).
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 환영 메시지 | 텍스트 | 로그인 사용자 이름 등 인사말 | `--foreground` |
- **상태 · 인터랙션**: 사이드바 최상단(그룹 라벨 없음) 단독 항목으로 노출, 전 인증 사용자 공통(역할 매핑 없음 = 전체 공개, `docs/02_plan/database/auth.md` 5절 참조). 로그인 성공 시 기본 이동 대상.
- **연관 API**: 없음(정적 placeholder)

### SCR-COM-014 · 승인 대기함(전 도메인 공용)

- **목적**: 로그인 사용자가 보유한 역할이 필요한 승인 건을 도메인 구분 없이 한 곳에서 확인하고 승인/반려한다. 승인 프로세스 커스텀 기능으로 "누가 승인하는가"가 티켓마다 동적으로 결정되므로(고정 역할 기반 전용 화면 대신) 전 도메인 공용 화면으로 설계했다. 기존 SCR-SRM-006(승인 대기함)·SCR-CHG-004(CAB 승인 대기함)·SCR-KM-004(검토·게시 승인함)는 이 화면으로 완전히 대체되어 제거됐다.
- **레이아웃**: [SCR-COM-007] 목록 패턴. 상단 도메인 필터(셀렉트, 미지정 시 전체) / 목록 표(티켓 유형 배지·티켓 요약·현재 차수·요청자·요청일) / 행 클릭 시 우측(또는 하단) 상세 패널(차수별 진행 상태 — 역할별 결정 현황·AND/OR 표시·승인/반려 액션).
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 도메인 필터 | 셀렉트 | SERVICE_REQUEST/CHANGE/KNOWLEDGE/INCIDENT/PROBLEM 등 | Border/Base |
  | 승인 대기 목록 표 | 표 | 티켓 유형 배지·티켓 요약·현재 차수(N차)·요청자·요청일 | Warning(대기 배지) |
  | 차수 진행 패널 | 단계 인디케이터 | 전체 차수를 순서대로 표시, 완료된 차수는 Success, 대기 차수는 Warning, 이후 차수는 Muted | Success/Warning/Muted |
  | 역할별 결정 현황 | 리스트 | 현재 차수에 필요한 각 역할과 결정 상태(대기/승인/반려), AND 차수는 각 역할 슬롯을 모두 나열, OR 차수는 "역할 중 하나"로 표시 | Info |
  | 승인/반려 버튼 | 버튼 | 결정 처리, 반려 시 사유 입력 필수 | Success/Danger |
  | 상세보기 버튼 | 버튼 | 목록 표 각 행 우측(승인/반려 처리용 "상세" 버튼과 별개), 클릭 시 새 탭이 아니라 해당 티켓의 실제 상세 화면으로 이동(승인 처리 모달은 열지 않음) | Border/Base |
- **상태 · 인터랙션**: 목록은 로그인 사용자가 현재 대기 차수에 필요한 역할을 보유하고 아직 해당 역할 슬롯을 결정하지 않은 건만 노출(역할 기반 공유 대기함). 이미 다른 사람이 처리해 종료된 건에 결정 시도 시 409 오류 토스트. 반려 사유 누락 시 400 인라인 오류. 결과 0건 시 빈 상태 안내("현재 대기 중인 승인이 없습니다"). 결정 성공 시 토스트 후 목록에서 제거. **상세보기 버튼**: 클릭 시 `ticketDetailPath(ticketType, ticketId)`(`features/common/status.ts`, 헤더 알림 드롭다운(SCR-COM-002)과 동일 헬퍼 재사용)로 계산한 경로로 라우팅 이동한다. 목록 행의 `ticketType`/`ticketId`(API-COM-003 응답에 이미 포함)를 그대로 사용하므로 신규 API가 필요 없다. 이동 대상 화면은 승인 대상자(APPROVER) 접근 권한이 이미 각 도메인 라우트 가드(`ROLE_APPROVER`, `api_spec/common.md` 0-1절)로 허용돼 있어 별도 권한 설계 변경이 필요 없다.
- **연관 API**: `GET /api/v1/approvals?scope=mine&domain=`(API-COM-003), `GET /api/v1/approvals/{approvalRequestId}`(API-COM-004), `POST /api/v1/approvals/{approvalRequestId}/decisions`(API-COM-005). 상세보기는 신규 API 없이 기존 응답 필드(`ticketType`/`ticketId`)만 사용.

### SCR-COM-015 · 언어 선택(i18n)

- **목적**: 사용자가 화면 표시 언어(한국어/영어)를 전환하고 선택을 유지한다. 6절 i18n 아키텍처의 진입점.
- **레이아웃**: 헤더 우측, "?" 사용자 가이드 아이콘과 테마 토글 사이에 배치되는 아이콘 버튼(지구본, lucide `Globe`). 클릭 시 버튼 바로 아래 우측 정렬(`align="end"`)로 Popover 미니 팝업을 오픈(알림 벨과 동일한 Popover 패턴 재사용, 외부 클릭/Esc로 닫힘)해 "한국어"/"English" 2개 항목을 노출하고, 현재 선택된 언어 항목에 체크 아이콘을 표시한다.
- **구성 요소**:
  | 요소 | 유형 | 설명 | 색상 |
  |------|------|------|------|
  | 언어 선택 버튼 | 아이콘 버튼(Globe) | 클릭 시 팝업 오픈 | `--foreground`, `aria-label`="언어 선택" |
  | 언어 팝업 | 오버레이 리스트(Popover) | 한국어/English 2개 항목 | Overlay elevation(2.5절) |
  | 언어 항목 | 리스트 아이템(버튼) | 클릭 시 즉시 전환 + 팝업 닫힘. 현재 선택 항목은 체크 아이콘으로 표시 | `--foreground`(기본 텍스트)/`--primary`(선택 체크 아이콘) |
- **상태 · 인터랙션**:
  - 최초 진입 시 저장된 언어 선호도를 `localStorage`(`itsm-language` 키)에서 로드해 즉시 적용, 저장된 값이 없거나 유효하지 않으면 한국어를 기본값으로 적용(`theme-toggle.tsx`와 동일한 자체 상태 관리 패턴 — 브라우저 언어 자동 감지 없음).
  - 언어 항목 클릭 시 해당 언어로 즉시 전환(`i18next.changeLanguage`)하고 선택값을 저장, 팝업을 닫는다. 재방문 시에도 유지.
  - 전환은 새로고침 없이 현재 화면의 모든 번역 텍스트에 즉시 반영된다(react-i18next 훅 기반 리렌더링, 6절 참조).
  - 날짜/숫자 포맷(각 도메인 `format.ts`)은 언어 전환과 무관하게 기존 `ko-KR` 고정 포맷을 유지한다(확정된 결정 2 — 이번 범위에서 현지화하지 않음).
- **연관 API**: 없음(클라이언트 로컬 저장)

## 5. 개별 화면 조정 필요 목록 (FEAT-UIX-013)

2절 디자인 토큰과 공통 컴포넌트(`components/ui/*`, `components/common/*`, `components/layout/*`) 개편은 이를 조합해 구성된 화면에 자동 반영된다.

> **정정**: 분석 단계에서 5개 화면(`LoginPage.tsx`, `UserCreatePage.tsx`, `UserDetailPage.tsx`, `PortalPage.tsx`, `IncidentDetailPage.tsx`)에 raw Tailwind 색상 클래스가 하드코딩되어 있다고 식별했으나(REQ-UIX-013/FEAT-UIX-013), 개발 착수 후 dev-lead·dev-frontend·designer·analyzer가 각각 `source/frontend/src` 전체를 재검증한 결과 raw 색상/hex 하드코딩이 발견되지 않았다(최초 분석 grep의 오탐으로 판단). 해당 5개 화면을 포함한 8개 전 도메인 모두 토큰·공통 컴포넌트 개편만으로 **자동 반영**되며, 개별 화면(feature 레이어) 수정이나 FE 담당자 추가 소집은 불필요하다. 근거는 `docs/01_analyze/prd/ui-revamp.md` REQ-UIX-013, `docs/01_analyze/feature/ui-revamp.md` FEAT-UIX-013/4절 정정 내용을 참고한다.

## 6. 다국어(i18n) 아키텍처

### 6.1 개요

전 도메인(11개) 화면과 공통 레이아웃·알림 메시지에 한국어/영어 전환을 도입한다. 진입점은 헤더의 지구본 아이콘(SCR-COM-002/SCR-COM-015)이다. 이번 작업은 **화면 레이아웃·컴포넌트 구성을 변경하지 않고 표시 텍스트만 번역 키로 치환**하므로, 각 도메인 화면 설계서(`screen/{domain}.md`)의 레이아웃·구성 요소·상태·인터랙션·연관 API는 그대로 유지하며 갱신하지 않는다 — 이 6절이 전 도메인 공통 i18n 설계를 전담한다.

> **범위 확정(Main)**: 사용자 가이드(SCR-COM-012) 본문(11개 도메인 설명 + 16개 역할 페르소나/수행 내용)도 번역 대상에 **포함**한다. 원문과 동일 분량의 영어 콘텐츠를 신규 작성해야 하고, 이후 원본이 갱신될 때마다 영어판도 함께 동기화해야 하는 지속적 유지보수 부담을 사용자가 인지하고 수용했다(6.3절 예외 구조 참조).

### 6.2 라이브러리 선정

| 항목 | 선택 | 근거 |
|------|------|------|
| 코어 | i18next | React 생태계 표준, 네임스페이스·보간(interpolation)·복수형(pluralization) 기본 지원 |
| React 바인딩 | react-i18next | `useTranslation` 훅으로 함수형 컴포넌트에서 바로 사용, 기존 함수형 컴포넌트+Redux Toolkit 구조와 마찰 없음 |
| 브라우저 언어 자동 감지(`i18next-browser-languagedetector`) | 미도입 | 확정된 결정 3("localStorage 저장, 기본값 한국어, `theme-toggle.tsx`와 동일한 자체 상태 관리 패턴")과 배치되므로, 자동 감지 대신 `theme-toggle.tsx`와 동일하게 직접 `localStorage` 읽기/쓰기로 구현한다(SCR-COM-015 참조) |

신규 의존성(설치는 개발 단계에서 수행): `i18next`, `react-i18next`, `sweetalert2` — `source/frontend/package.json`.

### 6.3 리소스 구조

`source/frontend/src/i18n/` 신규 디렉토리(예시 구조):

```
src/i18n/
├── index.ts             # i18next.init() 호출, i18next 인스턴스 export
├── language.ts           # localStorage 읽기/쓰기(itsm-language 키, 기본 ko) — theme-toggle.tsx와 동일 패턴
└── locales/
    ├── ko/
    │   ├── common.json
    │   ├── auth.json
    │   ├── service-request.json
    │   ├── incident.json
    │   ├── problem.json
    │   ├── change.json
    │   ├── knowledge.json
    │   ├── asset.json
    │   ├── esm.json
    │   ├── vulnerability.json
    │   ├── compliance.json
    │   └── infra-monitoring.json
    └── en/
        └── (ko/와 동일한 12개 파일, 키 구조 동일)
```

- **네임스페이스** = `common` + 11개 업무 도메인 slug(`tech.md` 5절과 동일). `common` 네임스페이스는 특정 업무 도메인에 속하지 않는 레이아웃(헤더·사이드바·푸터)·토스트/확인 다이얼로그 공통 문구·알림 조립 라벨·통합 검색·사용자 가이드 chrome·대시보드·승인 대기함(SCR-COM-001~015, SCR-ERR-404)을 포괄한다. `admin.md`(SCR-ADMIN-*)는 REQ-AUTH-*에 연결되므로 `auth` 네임스페이스에 포함한다.
- `defaultNS: "common"`. 각 도메인 화면은 `useTranslation(["{domain-ns}", "common"])`로 필요한 네임스페이스만 로드한다(12개 JSON 전체를 한 번에 로드하지 않아 번들 크기를 최소화).
- **키 컨벤션**: `{section}.{itemKey}` 계층 구조(예: `list.title`, `list.createButton`, `status.PLANNING`, `type.HARDWARE`). 정확한 키 목록은 각 도메인 화면(`*Page.tsx`)·`status.ts`의 기존 하드코딩 한국어 문자열을 1:1로 옮기는 구현 단계 작업이며, 이 설계 문서는 네임스페이스 분리 기준과 키 계층 규칙만 정의한다.
- **`status.ts` 라벨 매핑 전환 패턴**: 기존 `Record<Code, string>` 딕셔너리 기반 함수(예: `statusLabel(s: AssetStatus)`)는 `t: TFunction`을 첫 인자로 받도록 전환하고, `t(`status.${s}`, { ns: "{domain}", defaultValue: 기존 하드코딩 값 })`으로 조회한다. 호출부는 컴포넌트 내부의 `useTranslation`에서 얻은 `t`를 그대로 전달한다. `features/search/status.ts`처럼 다른 도메인 `status.ts`를 재사용하는 함수는 각 도메인 함수에 동일한 `t`(다중 네임스페이스로 획득)를 전달한다.
- **예외 — 사용자 가이드(SCR-COM-012) 본문**: 장문 서술형 Markdown(11개 도메인 설명 + 16개 역할 페르소나/수행 내용)이라 문장 단위 JSON 키 분해에 적합하지 않으므로, 다른 화면과 달리 **문서 단위 병행 파일**로 관리한다. `docs/01_analyze/feature/user-guide-content.md`(한국어 원문)와 동일한 절·아코디언 구조를 갖는 영어본 `docs/01_analyze/feature/user-guide-content.en.md`를 신규 작성하고, 기존과 동일하게 두 문서 모두 가공 없이 `source/frontend/src/components/common/user-guide-content.tsx`로 그대로 이관한다(`UserGuideOverview`/`UserGuideDomainSection`/`UserGuideRoleSection`가 현재 언어에 맞는 콘텐츠 세트를 선택). 이후 원본 콘텐츠가 갱신되면 영어본도 함께 동기화한다(분석 단계 콘텐츠 변경 시 원문 갱신 담당자가 영어본도 동일하게 갱신). 영어본 최초 작성은 신규 요구사항 정의가 아닌 번역 작업이므로 analyzer 재소집 없이 개발 단계(dev-lead가 배정하는 담당자)에서 수행한다.

### 6.4 알림 메시지 번역 처리 (`common` 네임스페이스)

`routes/AppLayout.tsx`/`components/layout/header.tsx`가 조립하는 알림 관련 하드코딩 문자열은 `common` 네임스페이스 키로 전환한다(BE/DB 변경 없음, FE 조립 로직만 전환):

| 현재 하드코딩 | 위치 | 전환 |
|------|------|------|
| `ticketTypeApprovalLabel()` 반환값(서비스요청 승인/변경 승인 등) | `features/common/status.ts` | `common:notification.domainLabel.{TICKET_TYPE}` |
| `"자산 만료"` | `routes/AppLayout.tsx` | `common:notification.domainLabel.assetExpiry` |
| `"방금 전"`/`"N분 전"`/`"N시간 전"`/`"N일 전"` | `routes/AppLayout.tsx` `formatRelativeTime` | `common:notification.relativeTime.{unit}`(count 보간) |
| `"새로운 알림이 없습니다"`/`"모두 지우기"`/`"상세 보기"`/`"검색 결과가 없습니다"` 등 | `header.tsx` | `common:header.*` |
| 아이콘 전용 버튼 `aria-label`(사이드바 토글·사용자 가이드·언어 선택·테마 전환·알림 등) | `header.tsx`/`theme-toggle.tsx`/신규 언어 선택 버튼 | `common:header.*Aria` |
| 티켓 요약(`ticketSummary`)·자산명(`name`) | 서버 응답 원문 | 번역 대상 아님(사용자 입력 데이터) — 확정된 결정 2 "라벨·메시지 텍스트만 번역"에 따라 원문 그대로 유지 |

### 6.5 언어 선택 UI

SCR-COM-015(4절) 참조.

### 6.6 SweetAlert2 도입

SCR-COM-009(4절) "구현 참고" 참조. 요약: `ConfirmDialog`+`toast`만 SweetAlert2로 교체(외부 API 불변), `Modal`은 대상 제외, 커스텀 CSS는 기존 시맨틱 토큰만 참조.

### 6.7 도메인별 번역 대상 인벤토리 (dev-lead 도메인별 개발 계획 분배 기준)

| 도메인 | 네임스페이스 | 대상 화면 ID | status.ts |
|------|------|------|------|
| 인증/계정/권한 | `auth` | SCR-AUTH-001~003, SCR-ADMIN-001~008 | 없음(라벨 매핑 없음) |
| 서비스 요청 | `service-request` | SCR-SRM-001~005, 007~008 | `features/service-request/status.ts` |
| 인시던트 | `incident` | SCR-INC-001~005 | `features/incident/status.ts` |
| 문제 | `problem` | SCR-PRB-001~004 | `features/problem/status.ts` |
| 변경 | `change` | SCR-CHG-001~003, 005~006 | `features/change/status.ts` |
| 지식 | `knowledge` | SCR-KM-001~003, 005 | `features/knowledge/status.ts` |
| IT 자산/CMDB | `asset` | SCR-ITAM-001~005 | `features/asset/status.ts` |
| ESM | `esm` | SCR-ESM-001~011 | `features/esm/status.ts` |
| 취약점 | `vulnerability` | SCR-VULN-001~004 | `features/vulnerability/status.ts` |
| 컴플라이언스 | `compliance` | SCR-COMP-001~004 | `features/compliance/status.ts` |
| 인프라 모니터링 | `infra-monitoring` | SCR-IOM-001~005 | `features/infra-monitoring/status.ts` |
| 공통(레이아웃·알림·검색·가이드·대시보드·승인 대기함·토스트/확인 다이얼로그) | `common` | SCR-COM-001~015, SCR-ERR-404 | `features/search/status.ts`(도메인 배지, 각 도메인 `status.ts` 재사용) |

> SCR-COM-012(사용자 가이드) 본문은 위 JSON 키 방식이 아니라 6.3절 예외 구조(`user-guide-content.en.md` 신규 작성)를 따른다. `common` 네임스페이스 작업 범위에는 이 영어본 콘텐츠 작성도 포함된다.

각 도메인 담당 개발자는 위 화면 ID에 해당하는 `*Page.tsx`와 `status.ts`(있는 경우)의 하드코딩 한국어 문자열을 6.3절 키 컨벤션에 따라 번역 키로 치환하고, `locales/ko/{ns}.json`·`locales/en/{ns}.json` 양쪽에 동일한 키 구조로 값을 채운다.

### 6.8 i18n 커버리지 결함 수정

다국어 지원 1차 적용 이후 아래 2건이 번역 대상에서 누락된 것을 확인해 보완한다.

- **사이드바 메뉴 라벨(SCR-COM-003)**: `AppLayout.tsx`가 `GET /api/v1/menus/mine`(API-AUTH-022) 응답의 `groupLabel`/`screenName`을 그대로 렌더링하는데, 이 값은 6.3절의 정적 JSON 번역 키 방식이 아니라 관리자가 SCR-ADMIN-006(메뉴 관리)에서 직접 입력하는 **DB 원문**이라 애초에 번역 키 치환 대상이 아니었다(6.4절은 FE가 조립하는 알림 문구만 다룸). 해결 방법은 6.3절 키 방식이 아니라 **이중언어 DB 컬럼**(`screen.screen_name_en`/`group_label_en`, `database/auth.md` 5절)이며, `AppLayout.tsx`가 `i18n.language`에 따라 `screenName`/`screenNameEn`(`groupLabel`/`groupLabelEn`)을 선택해 렌더링하도록 전환한다.
- **승인 프로세스 카드 스택(SCR-ADMIN-008)**: `approval-step-progress.tsx`/`approval-panel.tsx`는 1차 적용 시 `auth` 네임스페이스 키로 전환됐으나, 같은 화면이 사용하는 `approval-process-flow.tsx`(카드 스택·역할 선택 패널·드래그 앤 드롭 UI 문구)가 누락되어 여전히 하드코딩 한국어다. 신규 설계 변경 없이 **동일한 `auth` 네임스페이스**(6.7절 표, SCR-ADMIN-001~008)로 6.3절 키 컨벤션을 그대로 적용해 누락분을 보완한다.

## 7. 페이지당 아이템 수 재산정

### 7.1 기준과 가정

목표는 **1920×1080 해상도, 100% 배율 기준으로 목록 화면에 스크롤이 나타나지 않는 페이지당 아이템 수**를 화면별로 재산정하는 것이다. 브라우저 실제 콘텐츠 영역 높이는 OS 작업표시줄·브라우저 chrome(탭/주소창)만큼 1080px보다 작으며, 이 값은 OS/브라우저 설정에 따라 달라진다.

> **가정(maintainer 확인 완료)**: Chrome 최대화 창 + 북마크바 숨김 기준 통상값인 **937px**을 브라우저 콘텐츠 영역 높이로 가정한다. 실측 환경이 다르면(예: 북마크바 표시, 다른 브라우저, 배율 차이) 아래 산식의 `937` 값만 교체해 재계산하면 되며, 나머지 구조는 동일하다. 개발 단계에서 실제 브라우저로 최종 확인(스크롤 발생 여부)해 필요시 ±1건 보정한다.

### 7.2 공통 산식과 컴포넌트 높이(코드 기준 실측)

```
가용 높이 = 937(뷰포트) - 56(헤더 SCR-COM-002) - 40(푸터 SCR-COM-004) - 48(메인 영역 p-6 상하 패딩)
        = 793px
```

레이아웃 패턴별로 793px 안에서 "고정 UI"가 차지하는 높이를 뺀 나머지를 40px(표 1행 높이)로 나눠 아이템 수를 구한다. 표 1행 높이 40px은 `TableHead`(`h-10`)·`TableCell`(`py-2.5`+`text-sm` 20px 줄높이)의 실제 클래스 기준이며, **7.3절의 행 높이 균일화(SCR-COM-007 참조) 전제가 지켜져야** 이 값이 유지된다.

| UI 요소 | 높이(px) | 근거 |
|---------|----------|------|
| 헤더 | 56 | SCR-COM-001 |
| 푸터 | 40 | `footer.tsx` `h-10` |
| 메인 영역 패딩(상+하) | 48 | `app-shell.tsx` `main` `p-6`(24px×2) |
| 제목만(설명 없음) | 28 | `text-heading-large font-bold` 1줄 |
| 제목+설명 | 52 | 제목 28 + `space-y-1`(4) + 설명(`text-sm`, 20) |
| 필터 바(Label+Input/Select 1행, `TicketListLayout`) | 80 | 카드 `p-3`(24) + Label(14)+`space-y-1`(4)+Input/Select `h-9`(36) |
| 필터 바(Input만, Label 없음, 예: 검색창) | 60 | 카드 `p-3`(24) + Input `h-9`(36) |
| 필터 바(plain `grid` 폼, admin 화면) | 80 | `p-4`(32, 상하) + Label(14)+gap(4)+Input/Select `h-9`(36) ≈ 동일 카테고리로 반올림 |
| 표 헤더 행 | 40 | `TableHead` `h-10` |
| 표 데이터 행(1건) | 40 | `TableCell` `py-2.5`+`text-sm` |
| 페이지네이션 | 36 | `Button` `size="icon"`(`size-9`) |
| 블록 간 gap(`TicketListLayout`/`space-y-4` 등) | 16 | Tailwind `gap-4`/`space-y-4` |

### 7.3 레이아웃 패턴 분류와 계산 결과

| 패턴 | 해당 화면 | 산식(793px 기준) | 아이템 수(기존→변경) |
|------|-----------|-------------------|------------------------|
| A. `TicketListLayout` 제목+설명+필터+표+페이지네이션 | RequestListPage(SRM), MyEsmRequestsPage(ESM), HrCaseListPage(ESM), MyChecklistTasksPage(ESM), EsmRequestQueuePage(ESM), SearchResultsPage(SEARCH) | 793 = 32(gap×2)+52(제목+설명)+80(필터)+40(표헤더)+16(gap)+36(페이지네이션)+40N | 10→**13** |
| B. `TicketListLayout` 제목(설명 없음)+필터+표+페이지네이션 | IncidentListPage, ProblemListPage, ChangeListPage, VulnerabilityListPage, ComplianceListPage, KnowledgeListPage, AssetListPage | 793 = 32+28(제목)+80(필터)+40+16+36+40N | 10→**14** |
| C. plain `div`(제목, 설명 없음)+`grid` 필터 폼+표+페이지네이션 | AuditLogPage, UserListPage | `space-y-4`로 4블록(제목·필터·표·페이지네이션)이 이어져 gap 3개(48)가 발생 — 793 = 48(gap×3)+28(제목)+80(필터)+40(표헤더)+36(페이지네이션)+40N, 총 고정분 232로 B와 동일 | 10→**14** |
| D. 필터 바 없음(사이드/보조 UI가 필터 역할) | RequestQueuePage(SRM 큐, 좌측 큐 목록이 필터) — 바깥 `space-y-4`(제목→그리드, gap 1개)+그리드 안 우측 컬럼 `gap-4`(표→페이지네이션, gap 1개) = gap 2개(32), 제목만(설명 없음), 필터 블록 없음; MenuManagementPage(관리자, 검색 폼 자체가 없음) — `space-y-4` 3블록(제목→표→페이지네이션) = gap 2개(32), 동일 총 고정분 | 793 = 32(gap×2)+28(제목)+40(표헤더)+36(페이지네이션)+40N | 10→**16**(RequestQueuePage), 20→**16**(MenuManagementPage) |

### 7.4 화면별 최종 `PAGE_SIZE` 값

| 파일 | 패턴 | 기존 | 변경 |
|------|------|------|------|
| `features/service-request/RequestListPage.tsx` | A | 10 | 13 |
| `features/service-request/RequestQueuePage.tsx` | D | 10 | 16 |
| `features/incident/IncidentListPage.tsx` | B | 10 | 14 |
| `features/problem/ProblemListPage.tsx` | B | 10 | 14 |
| `features/problem/KnownErrorSearchPage.tsx` | 별도(7.5절, 카드 리스트) | 10 | 10(변경 없음) |
| `features/change/ChangeListPage.tsx` | B | 10 | 14 |
| `features/knowledge/KnowledgeListPage.tsx` | B | 10 | 14 |
| `features/asset/AssetListPage.tsx` | B | 10 | 14 |
| `features/esm/EsmRequestQueuePage.tsx` | A | 10 | 13 |
| `features/esm/MyEsmRequestsPage.tsx` | A | 10 | 13 |
| `features/esm/HrCaseListPage.tsx` | A | 10 | 13 |
| `features/esm/MyChecklistTasksPage.tsx` | A | 10 | 13 |
| `features/vulnerability/VulnerabilityListPage.tsx` | B | 10 | 14 |
| `features/compliance/ComplianceListPage.tsx` | B | 10 | 14 |
| `features/search/SearchResultsPage.tsx` | A | 20 | 13 |
| `features/admin/AuditLogPage.tsx` | C | 10 | 14 |
| `features/admin/UserListPage.tsx` | C | 10 | 14 |
| `features/admin/MenuManagementPage.tsx` | D | 20 | 16 |

### 7.5 카드 리스트 예외 — KnownErrorSearchPage(SCR-PRB-004)

이 화면은 표가 아니라 `Card` 반복 리스트이며, 각 카드가 근본원인·워크어라운드 전문(`whitespace-pre-wrap`, 줄 수 제한 없음)을 그대로 렌더링해 카드 높이가 콘텐츠 길이에 따라 달라진다. 표 기반 화면처럼 "40px 균일 행"을 전제할 수 없어 콘텐츠가 긴 항목은 카드 1개조차 뷰포트를 넘길 수 있고, 이 때문에 산정한 아이템 수로도 무스크롤을 보장할 수 없다.

**확정 결정(Main)**: 근본원인/워크어라운드 미리보기에 줄 수 제한(`line-clamp`)을 추가하는 등 카드 콘텐츠 축약 기능은 이번 유지보수 요청 범위(원 요구사항 3건: 사이드바·컬럼 폭·페이지당 아이템 수)를 벗어나므로 넣지 않는다. 이 화면은 **기존 `PAGE_SIZE=10`을 그대로 유지**하고, 카드 높이 가변으로 무스크롤을 보장할 수 없다는 사유만 본 절에 기록한다(코드 변경 없음).

## 8. 동적 폼 빌더·렌더러 아키텍처 (form.io 도입)

서비스 카탈로그의 입력항목 설계 화면을 form.io 스타일의 완전한 드래그앤드롭 자유배치 폼 빌더로 전환한다(SRM/ESM 공용, 레이아웃 컴포넌트 포함 — [database/service-request.md](../database/service-request.md) `form_schema` JSONB 전환 참조). Form.io 자체 서버는 사용하지 않고 `@formio/js`+`@formio/react` 라이브러리(Builder+Renderer)만 임베드하며, 저장은 기존 Spring Boot API가 담당한다(`docs/source/form_io/overview.md`·`integration-guide-for-itsm.md` 근거).

### 8.1 공용 컴포넌트

기존 공용 컴포넌트(`components/common/field-builder.tsx`·`dynamic-form.tsx`·`form-schema.ts`)를 아래로 대체한다. SRM(SCR-SRM-007/002)·ESM(SCR-ESM-006/002) 모두 이 공용 컴포넌트를 그대로 재사용한다.

| 컴포넌트(대체 대상) | 역할 |
|---|---|
| `dynamic-form-builder.tsx`(← `field-builder.tsx`) | `@formio/react`의 `FormBuilder`를 감싼 관리자용 폼 설계 컴포넌트. `initialForm`(기존 저장된 스키마)으로 편집 모드 진입, `onChange`로 최신 Form JSON을 상위 상태에 축적, 명시적 "저장" 버튼 클릭 시에만 카탈로그 CRUD API로 전달(자동저장 없음 — `onChange`가 편집 중에도 잦게 호출되므로) |
| `dynamic-form-renderer.tsx`(← `dynamic-form.tsx`) | `@formio/react`의 `Form`을 감싼 요청자용 렌더 컴포넌트. `src`에 조회한 `formSchema`를 그대로 주입, `onSubmit`으로 제출 데이터(`submission.data`)를 상위로 전달(클라이언트 검증은 Form.io 내장 기능 사용, 서버 재검증은 [api_spec/common.md](../api_spec/common.md) 0-2절) |
| `form-schema.ts` | 기존 `FormFieldSchema`/`FormFieldType`/`validateForm`/`hasOptions` 계약을 폐기하고, Form.io Form JSON 타입 별칭과 8.2절 팔레트 옵션 상수만 남긴다 |

> **Known limitation**: `FormBuilder`가 필드 라벨에서 key(Property Name)를 자동생성할 때 영숫자만 허용해, 한글 라벨 입력 시 저장이 차단된다(우회: 컴포넌트 편집 모달의 API 탭에서 영문 key 직접 입력). Form.io 자체 동작이며 별도 자동변환 로직 추가는 이번 범위 밖 — 관리자는 새 필드 추가 시 API 탭에서 영문 key를 직접 지정한다.

### 8.2 팔레트 구성

관리자에게 ITSM 카탈로그 설계에 필요한 컴포넌트만 노출한다(`FormBuilder`의 `options.builder`).

| 그룹 | 노출 | 포함 컴포넌트 |
|---|---|---|
| Basic | 노출 | textfield, textarea, number, checkbox, selectboxes, select, radio |
| Advanced | 노출 | email, phoneNumber, datetime(기존 `date` 유형 대체, `enableTime:false`), file(`storage:'base64'` 기본값 강제 — [api_spec/common.md](../api_spec/common.md) 0-2절) |
| Layout | 노출 | columns, panel, tabs, fieldset(요구사항의 "레이아웃 포함 자유 배치" 충족) |
| Data(datagrid/editgrid/datamap/tree) | 숨김 | 반복 그리드 등은 이번 요구사항에 없어 과잉기능 방지 차원에서 제외 |
| Premium/Resource | 숨김 | 서명·설문·reCAPTCHA·외부 리소스 연동 등 요구사항 범위 밖 |

조건부 표시(Conditional)·계산 필드(Calculated Value) 편집 UI는 Form.io 컴포넌트 편집 모달에 기본 내장되어 완전히 숨기기 어려우나, 서버는 이를 해석·집행하지 않는다([api_spec/common.md](../api_spec/common.md) 0-2절 — 이번 유지보수 범위 밖).

### 8.3 Bootstrap ↔ ADS(우리 디자인 시스템) 통합 방안

Form.io Builder/Renderer는 기본적으로 Bootstrap 전제 스타일이다(`docs/source/form_io/form-builder.md` 주의사항). 전면 Bootstrap CSS(`bootstrap.min.css`) 도입은 전역 리셋·타이포그래피가 ADS 토큰과 충돌하므로, 아래 방식으로 스코핑한다.

- `@formio/js`가 배포하는 폼 전용 CSS(예: `formio.form.min.css` 계열, 전역 리셋이 없는 컴포넌트 스타일 시트)만 임포트한다 — Bootstrap 전체 리셋 CSS는 임포트하지 않는다.
- `dynamic-form-builder.tsx`/`dynamic-form-renderer.tsx`는 렌더 최상위를 전용 래퍼(`.formio-scope`)로 감싸, 그 안에서만 Form.io CSS가 적용되도록 컨테이너를 분리한다.
- `.formio-scope` 하위에 오버라이드 스타일시트(`index.css`에 추가)를 두어 버튼 강조색(`.btn-primary` → `color.background.brand.bold`), 입력 테두리·라운드(`border.radius`), 폰트(Atlassian Sans/시스템 폰트 스택)를 ADS 토큰 값으로 재정의한다. 컴포넌트 구조 자체(`.formio-component-*` 클래스 등)는 건드리지 않고 색상·타이포·spacing 토큰만 덮어써 시각적 일관성을 맞춘다.
- 아이콘은 Bootstrap Icons 대신 프로젝트가 이미 쓰는 `lucide-react`로 대체 가능한지 우선 검토하되, 팔레트 아이콘은 Form.io 내장 아이콘 폰트에 의존하므로 이번 범위에서는 Bootstrap Icons CSS만 최소로 유지한다(전체 Bootstrap CSS는 미도입).

### 8.4 저장 흐름

`FormBuilder`는 URL 기반 자동 로드·저장을 하지 않으므로([form-builder.md](../../source/form_io/form-builder.md) 3절), 스키마 로드·저장은 기존 apiClient로 명시 처리한다 — 카탈로그 상세 조회(API-SRM-002/API-ESM-002)로 받은 `formSchema`를 `initialForm`에 주입해 편집 모드로 열고, `onChange`로 축적한 최신 스키마를 "저장" 버튼 클릭 시 카탈로그 생성/수정 API(API-SRM-003/004, API-ESM-003/004)로 PUT/PATCH한다.
