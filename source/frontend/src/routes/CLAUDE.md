# CLAUDE.md

라우팅·인증 가드·앱 레이아웃. 화면 ID(SCR-*)와 경로를 매핑하고, 세션 확인·RBAC 가드로 접근을 제어한다. 셸(`components/layout`)에 RBAC 필터된 메뉴·헤더 동작을 주입하는 계층이다.

## 파일
- `index.tsx` — 라우터 정의(`createBrowserRouter`). SessionBridge → AuthGuard → RequireAdmin/RequireRoles 중첩으로 도메인 화면을 경로에 매핑. `/approvals`(SCR-COM-014, 승인 대기함)는 역할 제한 없이 AuthGuard 하위에 직접 배치(승인 프로세스 커스텀 기능으로 승인자 역할이 티켓마다 동적으로 결정되어 고정 역할 가드가 불가). `/admin/approval-processes`(SCR-ADMIN-007/008)는 RequireAdmin 하위.
- `AppLayout.tsx` — 앱 레이아웃. 마운트 시 `GET /api/v1/menus/mine`(API-AUTH-022, Role-Menu 동적 매핑)을 호출해 사이드바 메뉴를 구성(역할 필터링은 서버 수행)해 AppShell에 주입, active 계산·navigate·로그아웃 확인 처리. 헤더 알림의 승인 대기 항목은 API-COM-003(전 도메인 공용, 승인 프로세스 커스텀 기능)을 전 인증 사용자 대상으로 조회(특정 역할 게이팅 없음).
- `AuthGuard.tsx` — 인증 가드. 진입 시 세션 복구(`bootstrapAuth`), 미인증이면 로그인으로, 인증되면 AppLayout 렌더.
- `SessionBridge.tsx` — apiClient 세션 만료(재발급 실패) 이벤트를 스토어·라우터에 연결(만료 토스트 + 로그인 이동).
- `RequireAdmin.tsx` — SYSTEM_ADMIN 전용 라우트 가드(미달 시 /403).
- `RequireRoles.tsx` — 허용 역할 기반 라우트 가드(미달 시 /403).
- `DashboardPage.tsx` — 로그인 후 기본 홈(환영) placeholder.
- `ForbiddenPage.tsx` — 403 접근 거부(SCR-COM-006). ForbiddenView에 동작 주입.
- `NotFoundPage.tsx` — 404 Not Found(SCR-ERR-404). NotFoundView에 동작 주입.
- `FullscreenLoader.tsx` — 세션 부트스트랩 등 초기 로딩용 전체 화면 로더.
