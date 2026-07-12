# 유지보수 이력 — auth

> 유지보수 일시: 20260711-125730 · 도메인: auth

## 1. 요구사항

관리자는 역할과 메뉴를 동적으로 매핑할 수 있어야 한다.

## 2. 해결 방법

기존 `screen` 테이블을 메뉴 마스터로 확장했다.
`icon_name`·`group_code`·`group_label`·`sort_order`·`nav_visible` 컬럼을 추가했다.
역할-메뉴 매핑은 신규 테이블을 만들지 않고 기존 `screen_role` 테이블을 그대로 재사용했다.
신규 관리자 화면(SCR-ADMIN-006: 메뉴 관리)을 추가했다.
사이드바(SCR-COM-003)는 정적 `navConfig.tsx` 대신 신규 API(API-AUTH-022: 내 메뉴 조회) 기반 동적 구성으로 전환했다.
DB에는 `source/db/sql/24_auth_menu_columns.sql`로 screen 컬럼 5종을 추가하고, `path`에 UNIQUE 제약을 걸었다(designer 확정).
기존 `navConfig.tsx` 기준 전 화면 42개를 백필했고, SCR-ADMIN-006과 SCR-COM-010~013을 신규 시드했다.
기존 경로 불일치 7건(SCR-KM-004/ITAM-004/IOM-001~005)을 함께 보정했다.
BE는 `com.itsm.auth`에 Screen/ScreenRole 엔티티를 추가하고, ScreenAdminService(CRUD+역할매핑, ROLE_CHANGE 감사로그)와 MyMenuService(무매핑=전체공개 RBAC 판정)를 구현했다.
AdminScreenController(API-AUTH-016~021)와 MenuController(API-AUTH-022)를 신규 추가했고, ErrorCode 4종을 추가했다.
FE는 `routes/navConfig.tsx`를 삭제하고, AppLayout.tsx가 `/menus/mine` 기반으로 사이드바를 동적 구성하도록 변경했다(아이콘 문자열→lucide 컴포넌트 변환 유틸 신규).
`features/admin/MenuManagementPage.tsx`(SCR-ADMIN-006, CRUD+역할매핑 슬라이드 패널)를 신규 구현했다.
개발 중 대시보드("/")에 screen 코드가 없다는 점을 발견해, designer 확정으로 SCR-COM-013을 신규 부여했다.
SCR-COM-010/011/012도 DB에 미등록 상태였음을 발견해 이번에 함께 시드했다(nav_visible=false).
BE 페이지네이션에서 sort_order 동률이 다수일 때 정렬이 불안정한 버그를 발견해 id 2차 정렬로 수정했다.

## 3. 변경 파일

- `source/db/sql/24_auth_menu_columns.sql`
- `com.itsm.auth` 하위 Screen/ScreenRole 엔티티, ScreenAdminService, MyMenuService
- `com.itsm.auth` 하위 AdminScreenController, MenuController
- `source/frontend/src/routes/navConfig.tsx` (삭제)
- `source/frontend/src/routes/AppLayout.tsx`
- `source/frontend/src/features/admin/MenuManagementPage.tsx`

## 4. 테스트 결과

통합 테스트 33건과 회귀 테스트 15건 전부 PASS했다.
커밋 `4a54679`, `9e2c79e`(추가 검증)로 반영했다.
