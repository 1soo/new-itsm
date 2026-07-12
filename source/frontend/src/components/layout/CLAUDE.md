# CLAUDE.md

애플리케이션 레이아웃 셸 컴포넌트(common.md SCR-COM-001~004). 헤더·사이드바·푸터를 합친 프레젠테이션 전용 셸로, 인증 가드·RBAC 메뉴 필터·라우팅·동작은 FE(`routes/AppLayout.tsx`)가 props로 주입한다.

## 파일
- `app-shell.tsx` — 앱 셸. 상단 헤더 / 좌측 사이드바(접기 가능) / 메인 콘텐츠 / 하단 푸터를 조립하고 사이드바 접힘 상태를 관리한다.
- `header.tsx` — 글로벌 헤더. 로고·타이틀, 통합 검색(입력 중 Popover 미리보기 드롭다운, `searchResults`/`onSearchInputChange`/`onSelectSearchResult`로 FE가 결과·디바운스 검색·선택 이동 주입), 사용자 가이드 아이콘("?", `onOpenGuide` 클릭 시 FE가 `/guide`로 라우팅 — 모달 아님), 언어 선택(`LanguageToggle`, "?"와 테마 토글 사이, 2026-07-12 다국어 지원), 테마 토글, 알림 벨(클릭 시 Popover(폭 320px), `notifications`/`onSelectNotification`으로 FE가 항목·상세 이동 주입(표시 상한 없음, `max-h-80 overflow-auto` 스크롤로 전체 노출), `onNotificationsOpenChange`로 팝오버 열림/닫힘을 FE에 통지(열릴 때 FE가 timeLabel을 현재 시각 기준 재계산), `HeaderNotificationItem` 타입 제공, 항목은 2줄 레이아웃 — 1행 `StatusBadge`(tone="info") 도메인 라벨 + 우측 `timeLabel`+개별 X 버튼(`onDismissNotification`, `stopPropagation`으로 라인 클릭과 분리), 2행 제목(truncate) + "상세 보기" 링크 버튼, 라인 클릭도 동일 이동. 팝오버 상단에는 알림이 1건 이상일 때 "모두 지우기" 버튼(`onDismissAllNotifications`) 노출), 사용자 드롭다운(프로필·비밀번호 변경·로그아웃). 동작은 콜백 주입. 헤더 자체 하드코딩 문자열은 `useTranslation("common")`의 `t()`로 `common.json`의 `header.*` 키를 참조(2026-07-12 다국어 지원).
- `theme-toggle.tsx` — 테마 토글(SCR-COM-010, 자체 상태 관리). 라이트/다크 전환 시 `document.documentElement`의 `data-theme` 속성과 `localStorage`를 갱신하며, 최초 마운트 시 저장값을 로드한다(없거나 유효하지 않으면 라이트 기본). `aria-label`은 `common:header.themeToggleAria` 키(2026-07-12 다국어 지원).
- `language-toggle.tsx` — 언어 선택(SCR-COM-015, 자체 상태 관리, 2026-07-12 다국어 지원). 지구본 아이콘 클릭 시 Popover(한국어/English, 현재 선택 체크 아이콘)를 열고, 선택 시 `i18next.changeLanguage()` + `itsm-language` localStorage 키(`@/i18n/language.ts`)로 저장. 최초 마운트 시 저장값을 로드한다(없거나 유효하지 않으면 한국어 기본, `theme-toggle.tsx`와 동일 패턴).
- `sidebar.tsx` — 사이드바 내비게이션. 그룹/항목 렌더, 활성 강조·접힘 표시. RBAC 필터링된 `groups`를 FE가 전달. `NavItem`/`NavGroup` 타입 제공.
- `footer.tsx` — 푸터. 좌측 저작권, 우측 버전 정적 표시.
