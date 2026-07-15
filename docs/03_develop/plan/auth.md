# 개발 계획 — auth (인증/계정/권한 & RBAC)

> 도메인: auth (AUTH) · 개발 순서 1/7 (최우선, 나머지 6개 도메인의 기반) · 작성: dev-lead · 2026-07-09

## 1. 목표

인증(JWT)·계정·역할(RBAC)·감사 로그를 구현하고, 이후 모든 도메인이 공유할 **프로젝트 기반(스캐폴드)**을 함께 확립한다. auth가 첫 도메인이므로 DB/BE/FE 프로젝트 초기 구성과 공통 인프라(보안·오류규약·apiClient·앱 셸)를 이 단계에서 세운다.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/auth.md`(SCR-AUTH-001~003), `screen/admin.md`(SCR-ADMIN-001~005), `screen/common.md`(SCR-COM-001~009), `screen/error_404.md`(SCR-ERR-404)
- API: `api_spec/auth.md`(API-AUTH-001~015, Base Path `/api/v1`, 표준 오류 응답)
- DB: `database/auth.md`(단일원천: app_user, role, user_role, screen, screen_role, refresh_token, audit_log + RBAC 매핑)
- 보안: `security/authentication.md`(JWT Access 5분/Refresh 7일, JTI=DB), `security/authorization/*`(11개 역할, 특히 system_admin.md)
- 기술스택: `01_analyze/tech.md`(React CSR + Redux Toolkit / Spring Boot / PostgreSQL / docker-compose local)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- `docker-compose.yml`(PostgreSQL local) + 마이그레이션/SQL 구성(`source/db/sql/`).
- DDL: `app_user, role, user_role, screen, screen_role, refresh_token, audit_log`. 공통컬럼 규칙(`database/auth.md` 2절), snake_case, `BIGINT GENERATED ALWAYS AS IDENTITY` PK, UNIQUE/FK 제약(6절).
- Seed(DML):
  - `role` 11종: SYSTEM_ADMIN, END_USER, SERVICE_DESK_AGENT, APPROVER, INCIDENT_MANAGER, PROBLEM_MANAGER, CHANGE_MANAGER, KNOWLEDGE_CONTRIBUTOR, KNOWLEDGE_GATEKEEPER, ASSET_MANAGER, PROCESS_OWNER.
  - `screen`: auth 도메인에서 확정된 화면 코드(SCR-AUTH-001~003, SCR-ADMIN-001~005, SCR-COM-*, SCR-ERR-404). 타 도메인 화면은 해당 도메인 단계에서 추가(중복 없이).
  - `screen_role`: `security/authorization/system_admin.md` 등 역할 정의서의 접근 화면 기준 매핑(auth 범위 우선).
  - 초기 `app_user` 1건: SYSTEM_ADMIN 계정(email/name + BCrypt password_hash) + `user_role` 매핑.
- common 교차 테이블(ticket_link/comment/timeline_event/approval)은 **auth 단계 제외** — 티켓 도메인 단계에서 필요 시 생성.
- 산출: 스키마 문서/ERD 불필요(설계서가 원천). BCrypt 해시 생성 규칙은 BE와 합의(동일 알고리즘·강도).

### BE (dev-backend) — `source/backend/`
- Spring Boot 프로젝트 초기 구성: DDD 패키지 구조, SOLID, Spring Security, springdoc(OpenAPI/Swagger-UI), PostgreSQL 연동(docker-compose DB), 프로파일(local).
- 공통 인프라: 표준 오류 응답 본문 `{code, message, timestamp}`, 전역 예외 핸들러, 401/403 처리, 공통컬럼 감사(created_by/at 등) 처리.
- JWT: Access(5분, claim `userId`·`jti`·`role[]`)/Refresh(7일, claim `userId`·`jti`). JTI 세션: 로그인 시 `app_user.access_token_jti` 저장 + `refresh_token` insert, 로그아웃 시 NULL + revoked=true, 재발급 시 jti 갱신. 요청마다 jti 일치 검증(불일치→401).
- API-AUTH-001~015 전부 구현. admin API는 SYSTEM_ADMIN 미보유 시 403(메서드/URL 기반 인가). 감사 로그(audit_log) 기록: LOGIN/LOGOUT/REFRESH/USER_CHANGE/ROLE_CHANGE.
- 로그인 오류 표기: 401(불일치)·403(비활성 INACTIVE). 이메일 중복 409, 미존재 404, 검증 400.
- OpenAPI 문서화 규칙은 프로젝트 spring-boot 문서 규약을 따른다.

### UI (dev-ui) — `source/frontend/` 공통 영역
- 공통 팔레트 토큰화(`common.md` 2절/2.1절 시맨틱 색상).
- 공통 컴포넌트: Button, Input(마스킹/토글 포함), Select, MultiSelect(칩), Card, Badge(상태·우선순위 시맨틱), Table, Pagination, Toast, ConfirmDialog, Modal.
- 레이아웃 셸: SCR-COM-001(App Shell), 002(글로벌 헤더), 003(사이드바 - 메뉴 항목 슬롯), 004(푸터), 006(403), SCR-ERR-404. (RBAC 메뉴 필터 로직은 FE가 주입)
- 목록/상세 공통 패턴: SCR-COM-007(목록/필터), 008(상세) 프레젠테이션 컴포넌트.
- dev-frontend와 **소유 경계 합의**: 공통(재사용 UI/레이아웃 뼈대) = UI, 화면 조립/데이터 연동/RBAC 로직 = FE.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- React CSR 스캐폴드(Vite 권장), Redux Toolkit 스토어(auth 슬라이스: user·roles·토큰상태).
- 공통 apiClient: Base `/api/v1`, `Authorization: Bearer` 자동 주입, 401 응답 시 `POST /api/v1/auth/refresh` 1회 재시도 → 실패 시 세션 종료·로그인 이동(SCR-COM-005) + 토스트.
- 라우팅 + 인증 가드(SCR-COM-005) + RBAC 메뉴 노출(SCR-COM-003, 역할은 `/auth/me` claim 기반). 관리자 라우트는 SYSTEM_ADMIN 전용, 미달 시 403(SCR-COM-006).
- 화면 구현: SCR-AUTH-001(로그인, 401/403 통일 메시지), 002(내 프로필), 003(비밀번호 변경), SCR-ADMIN-001(계정 목록)/002(생성)/003(상세·수정·역할·비활성화)/004(역할 관리)/005(감사 로그).
- UI 공통 컴포넌트를 사용해 화면 조립(중복 컴포넌트 생성 금지).

## 4. 진행 순서 · 의존성

병렬 착수하되 통합 지점을 맞춘다.
1. 병렬 시작: DB(스키마·시드), BE(스캐폴드·보안), UI(디자인시스템·셸), FE(스캐폴드·apiClient).
2. 통합 지점: DB 스키마 확정 → BE 영속성 연동 / BE 로그인·me API 계약 확정 → FE 인증 흐름 연동 / UI 컴포넌트 → FE 화면 조립.
3. 상호 계약(응답 형태·필드명)은 `api_spec/auth.md`가 계약의 단일 기준. 이견은 dev-lead에게, 설계 이슈는 designer에게 에스컬레이션.

## 5. 완료(테스트 통과) 기준

- BE: API-AUTH-001~015 정상 + 오류코드(400/401/403/404/409) 케이스, JWT/JTI 세션·강제 로그아웃, RBAC admin 가드, Swagger-UI 노출.
- FE: 로그인→역할 기본 홈, 인증가드/401 refresh 재시도, 관리자 계정·역할·감사로그 플로우(playwright E2E).
- tester가 위 항목 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → 완료 시 `feat(auth): ...` 커밋/푸시.

## 6. 파일 소유 (충돌 방지)

- `source/db/` = DB / `source/backend/` = BE / `source/frontend/` 공통 = UI, 기능 = FE.
- 동일 파일 동시 수정 금지. frontend 공통/기능 경계는 UI·FE가 착수 초기에 폴더 규칙으로 합의.

---

## Role-Menu 동적 매핑 (유지보수 요청, 2026-07-11)

> Main 요청(유지보수). 기존 `screen`/`screen_role`을 메뉴 마스터·역할-메뉴 매핑으로 확장 재사용(신규 테이블 없음). 이번 유지보수는 이 도메인만으로는 UI 신규 소집 없이 FE가 필요한 소규모 컴포넌트(역할 매핑 슬라이드 패널)까지 직접 담당한다(Main 지시).

### 설계 근거

- DB: `docs/02_plan/database/auth.md` 5절(`screen` 컬럼 추가: `icon_name`/`group_code`/`group_label`/`sort_order`/`nav_visible`, `screen_role` 그대로 재사용)
- API: `docs/02_plan/api_spec/auth.md` API-AUTH-016~022
- 화면: `docs/02_plan/screen/admin.md` SCR-ADMIN-006(메뉴 관리, 신규), `docs/02_plan/screen/common.md` SCR-COM-003(사이드바, 동적 구성으로 전환)
- 참고 기존 코드: `source/backend/src/main/java/com/itsm/auth/`(도메인 4계층, `AdminRoleController`/`RoleService` 패턴), `source/frontend/src/routes/navConfig.tsx`(대체 대상, 삭제 예정), `source/frontend/src/routes/AppLayout.tsx`(사이드바 그룹 조립부), `source/frontend/src/features/admin/`(`RoleManagementPage.tsx` 패턴 재사용)

### 담당 범위

#### DB (dev-database) — `source/db/sql/`

- 신규 파일 `24_auth_menu_columns.sql`(다음 순번). `ALTER TABLE screen ADD COLUMN icon_name VARCHAR(50), ADD COLUMN group_code VARCHAR(30), ADD COLUMN group_label VARCHAR(50), ADD COLUMN sort_order INT NOT NULL DEFAULT 0, ADD COLUMN nav_visible BOOLEAN NOT NULL DEFAULT true;`
- 백필(UPDATE): 기존 시드된 모든 `screen` 행에 대해 현재 사이드바 노출 여부·아이콘·그룹·순서를 채운다. **단일 원천은 현재 `source/frontend/src/routes/navConfig.tsx`**이므로, 이 파일의 그룹(`key`/`label`)·항목(`path`/`icon` 컴포넌트명·순서)을 `01_schema.sql`~`23_infra_monitoring_seed.sql`에 흩어진 각 도메인 `screen` INSERT의 `screen_code`/`path`와 매칭해 백필 UPDATE 문을 작성한다(경로 기준 매칭). navConfig에 없는 화면(로그인·상세/서브 화면·403/404·SCR-COM-005/010 등 비메뉴 화면)은 `nav_visible=false`, `icon_name`/`group_code`/`group_label`=NULL, `sort_order`=0으로 둔다. 그룹 라벨은 navConfig의 `label` 그대로(예: "서비스 요청"/"인시던트"), `main` 그룹처럼 label이 없는 항목은 `group_code`/`group_label` 모두 NULL로 둔다.
- 신규 메뉴 1건 추가(SCR-ADMIN-006 자신): `screen_code='SCR-ADMIN-006'`, `screen_name='메뉴 관리'`, `path='/admin/menus'`, `domain='auth'`, `group_code='admin'`, `group_label='관리자'`, `nav_visible=true`, `icon_name`은 기존 관리자 그룹 아이콘(`Users`/`ShieldCheck`/`ScrollText`)과 겹치지 않는 lucide 아이콘명(예: `ListTree`) 선택, `sort_order`는 기존 관리자 그룹 항목(계정 관리/역할 관리/감사 로그) 마지막 순번 다음 값. + `screen_role`에 `SYSTEM_ADMIN` 매핑 1건 추가.
- **(designer 확정, 2026-07-11)** `screen.path`에 `UNIQUE NOT NULL` 제약 추가(`ALTER TABLE screen ADD CONSTRAINT uq_screen_path UNIQUE (path);` 같은 파일에 포함). 백필 대상 기존 `path` 값들이 서로 중복되지 않는지 반드시 사전 확인 후 제약을 건다(중복 발견 시 dev-lead에게 즉시 보고, 임의로 값 변경 금지).
- 이미 존재하는 `SCR-COM-012`(사용자 가이드, `/guide`) 행은 헤더 "?" 진입 전용이라 `nav_visible=false`로 백필(사이드바 미노출 유지, 기존 접근 제어에는 영향 없음).
- 아이콘명 문자열은 FE(`navConfig.tsx`)가 사용하던 lucide-react 컴포넌트명을 그대로 문자열화(예: `<Users />` → `"Users"`). 매핑 과정에서 애매한 항목(그룹 순서·서브메뉴 판단)이 있으면 dev-frontend에게 직접 SendMessage로 확인 후 진행(navConfig.tsx가 원본이므로).
- 완료 후 `source/db/sql/CLAUDE.md`에 파일 추가 사실 반영.

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/auth/`

- 신규 엔티티: `domain/Screen.java`(BaseEntity 상속, screenCode/screenName/path/domain/iconName/groupCode/groupLabel/sortOrder/navVisible), `domain/ScreenRole.java`(BaseEntity 상속, screenId/roleId).
- 신규 리포지토리: `domain/repository/ScreenRepository.java`, `ScreenRoleRepository.java` + `infrastructure/persistence/ScreenJpaRepository.java`, `ScreenRoleJpaRepository.java`(Spring Data JPA). `ScreenRepository`는 목록 조회 시 `groupCode`/`domain` 필터 + `is_deleted=false` + 페이지네이션(`AppUserRepository.search()` 패턴 참고). `ScreenRoleRepository`는 `findByScreenId`/`findByRoleId`/`existsByScreenIdAndRoleId`/`findByScreenIdAndRoleId`.
- 신규 DTO(`application/dto/`): `ScreenResponse`(id/screenCode/screenName/path/domain/iconName/groupCode/groupLabel/sortOrder/navVisible/roles: List\<String\>), `CreateScreenRequest`, `UpdateScreenRequest`, `AssignScreenRoleRequest`(roleId), `ScreenRolesResponse`(screenId/roles), `MyMenuResponse`(groups: List\<MenuGroupResponse\>), `MenuGroupResponse`(groupCode/groupLabel/items), `MenuItemResponse`(screenCode/screenName/path/iconName).
- 신규 서비스 `application/ScreenAdminService.java`: list/create/update(soft delete 포함)/assignRole/revokeRole. `RoleRepository`로 역할 존재 검증(400 `ROLE_NOT_FOUND`). 생성 시 `screenCode` 중복 409. 역할 매핑 부여 시 이미 매핑돼 있으면 409(사용자 역할 부여와 달리 멱등 아님, API-AUTH-020 명시). 모든 변경은 `AuditLogService.record(EventType.ROLE_CHANGE, principal.userId(), principal.email(), screenCode, AuditResult.SUCCESS)`로 기록(`UserAdminService` 패턴 그대로). 삭제는 `screen.is_deleted=true`만 처리하고 `screen_role` 행은 정리하지 않는다(DB 설계 그대로).
- 신규 서비스 `application/MyMenuService.java`(또는 `ScreenAdminService`에 통합 — 재량): `SecurityUtils.currentPrincipal().roles()` 기준으로 `nav_visible=true`·`is_deleted=false`인 화면 중 (a) `screen_role` 매핑이 전혀 없는 화면(전체 공개) 또는 (b) 매핑된 역할 중 하나라도 보유한 화면만 선택, `sort_order` 오름차순 정렬, `group_code`별로 묶어 그룹 표시 순서는 그룹 내 최소 `sort_order` 기준으로 정렬.
- 컨트롤러: `presentation/AdminScreenController.java`(`/api/v1/admin/screens`, API-AUTH-016~021 — `SecurityConfig`의 `/api/v1/admin/**` 매처로 SYSTEM_ADMIN 인가 자동 적용), `presentation/MenuController.java`(`/api/v1/menus/mine`, API-AUTH-022 — 인증만 필요, `/admin/**` 매처 밖이므로 별도 컨트롤러로 분리).
- `common/exception/ErrorCode.java`에 추가: `SCREEN_NOT_FOUND`(404), `SCREEN_CODE_DUPLICATE`(409), `PATH_DUPLICATE`(409, designer 확정 2026-07-11 — `screen.path` UNIQUE 추가, API-AUTH-017/018 둘 다 대상), `SCREEN_ROLE_MAPPING_DUPLICATE`(409).
- 단위/통합 테스트는 기존 `RoleService`/`UserAdminService` 테스트 스타일을 따른다.

#### FE (dev-frontend) — `source/frontend/src/`

- **사이드바 동적화**: `routes/navConfig.tsx` 삭제. `routes/AppLayout.tsx`에서 정적 `navConfig` 순회 대신 마운트 시 `GET /api/v1/menus/mine` 1회 호출해 응답의 `groups`를 `NavGroup[]`으로 변환(그룹 라벨 없으면 `label` 미지정). 아이콘은 문자열(`iconName`)을 실제 lucide 컴포넌트로 변환하는 유틸(`import * as icons from "lucide-react"`, 존재하지 않는 이름이면 기본 아이콘로 폴백) 신규 작성(위치 예: `lib/icon.ts` 또는 `features/admin/icon.ts`, 관리자 메뉴 관리 화면과 공유). `activeKey` 계산 로직은 API 응답의 `path` 기준으로 동일하게 유지.
- 로그인 직후(혹은 라우트 최초 진입 시) 메뉴 API 실패 시 사이드바는 빈 상태로 두고 토스트 없이 조용히 실패(치명적이지 않음) — 기존 알림 로딩 실패 처리(`catch`) 패턴 참고.
- **메뉴 관리 화면(SCR-ADMIN-006)**: 신규 `features/admin/MenuManagementPage.tsx`(+ `features/admin/api.ts`에 메뉴 CRUD·역할매핑 함수 추가, `features/admin/types.ts`에 `Menu`/`CreateMenuRequest`/`UpdateMenuRequest` 타입 추가). `RoleManagementPage.tsx`와 동일한 패턴(`DataTable`+`Modal`+토스트)으로 목록·생성/수정 모달 구성. 우측 슬라이드 패널(역할 매핑, 체크박스 토글마다 즉시 API 호출)은 기존 `components/ui/dialog.tsx`(Radix Dialog) 위에 우측 슬라이드 스타일(`slide-in-from-right`, Overlay elevation)을 적용한 경량 래퍼로 직접 구현(신규 UI 프리미티브가 필요하다고 판단되면 진행 전 dev-lead에게 알릴 것 — 현재는 기존 Dialog 토큰 재사용만으로 충분하다고 판단해 UI 미소집).
- 라우팅: `routes/index.tsx`에 `/admin/menus` 추가(`RequireAdmin` 하위, 다른 admin 라우트와 동일 패턴).

## i18n 다국어 전환 (유지보수 요청, 2026-07-12)

> common 도메인 phase에서 i18n 인프라(`src/i18n/`)·SweetAlert2·언어 선택(SCR-COM-015)이 이미 구축 완료됨(`docs/03_develop/plan/common.md` v3절, 커밋 `bc66f6c`). 이번 auth phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**한다(`docs/02_plan/screen/common.md` 6절). BE/DB 변경 없음. status.ts 없음(6.7절).

### 담당 범위 — dev-fe 단독(UI 미소집 대상)

- 대상 화면: `features/auth/LoginPage.tsx`(SCR-AUTH-001), `features/auth/ChangePasswordPage.tsx`(SCR-AUTH-002 추정), `features/auth/ProfilePage.tsx`(SCR-AUTH-003 추정) — 정확한 화면ID 매핑은 `docs/02_plan/screen/auth.md` 3절 화면 목록 확인. `features/admin/UserListPage.tsx`/`UserCreatePage.tsx`/`UserDetailPage.tsx`/`RoleManagementPage.tsx`/`MenuManagementPage.tsx`/`AuditLogPage.tsx`/`ApprovalProcessListPage.tsx`/`ApprovalProcessFormPage.tsx`(SCR-ADMIN-001~008, `docs/02_plan/screen/admin.md` 3절 확인).
- 각 화면의 하드코딩 한국어 문자열(타이틀·라벨·버튼·placeholder·빈 상태·toast/confirm-dialog 호출 시 넘기는 message 문자열·aria-label 등)을 `useTranslation("auth")` + `t()`로 전환. 6.3절 키 컨벤션(`{section}.{itemKey}`) 그대로 적용.
- `locales/ko/auth.json`·`locales/en/auth.json`(현재 `{}` 스캐폴딩 상태, common phase에서 dev-ui가 생성) — **이번 phase는 dev-fe가 단독 소유**하고 직접 채운다(common.json과 달리 다른 담당자가 동시에 건드리지 않으므로 충돌 없음).
- `toast.success/error(message, description)`/`<ConfirmDialog title description confirmLabel cancelLabel .../>` 호출부는 함수 시그니처 그대로 유지한 채, 넘기는 문자열 인자만 `t()` 호출 결과로 교체(컴포넌트 자체는 common phase에서 이미 SweetAlert2로 교체 완료, 호출부 수정 불필요 원칙 재확인).
- 날짜/숫자 포맷은 기존 `ko-KR` 그대로 유지(포맷 현지화 없음, 확정된 결정 2).

### 완료 기준
- 헤더 지구본 아이콘으로 English 전환 시 로그인/프로필/비밀번호 변경 화면 및 관리자 8개 화면 텍스트가 전부 영어로 전환.
- 기존 로그인/비밀번호 변경/사용자·역할·메뉴·감사로그·승인프로세스 관리 기능 자체는 회귀 없음(텍스트만 치환).
- 신규 디렉토리 생성 없음(기존 `features/admin/` 확장)이라 `CLAUDE.md` 갱신만 파일 목록에 추가.

### 진행 순서

1. DB: 컬럼 추가 + 백필(navConfig.tsx 참조, 필요 시 dev-frontend와 직접 확인) + SCR-ADMIN-006 신규 시드 → 먼저 완료.
2. BE: DB 스키마 확정 후 엔티티/서비스/컨트롤러 구현.
3. FE: BE API 계약 확정 후 사이드바 동적화 + 메뉴 관리 화면. (navConfig.tsx 삭제는 BE `/menus/mine` 정상 동작 확인 후 진행 — 삭제 전까지 정적 navConfig을 유지해 회귀 방지)

### 완료(테스트 통과) 기준

- BE: API-AUTH-016~022 정상 + 오류코드(400/403/404/409) 케이스, `/menus/mine` 무매핑 화면 전체 공개 규칙, 감사 로그(ROLE_CHANGE) 기록.
- FE: SYSTEM_ADMIN이 메뉴 생성·수정·삭제·역할 매핑 부여/회수 가능(즉시 반영), 일반 사용자 로그인 시 보유 역할 매핑 메뉴만 사이드바 노출(무매핑 메뉴는 항상 노출), 메뉴 삭제 시 사이드바에서 즉시 사라짐, 403 접근 통제(System Admin 외 SCR-ADMIN-006 접근 불가).
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → 완료 시 커밋.

## 역할 목록 조회(공개) API (유지보수 요청, 2026-07-15)

> Main 요청(유지보수). SRM 카탈로그 관리 화면(담당자 역할 select)이 소비하는 비관리자 전용 역할 목록 API. UI/FE 신규 소집 없음(FE 작업은 `docs/03_develop/plan/service-request.md` 절에서 함께 처리 — 이 API가 먼저 완료되어야 그쪽 select가 동작).

### 설계 근거

- API: `docs/02_plan/api_spec/auth.md` v0.7 API-AUTH-030
- 참고 기존 코드: `source/backend/src/main/java/com/itsm/auth/application/RoleService.java`, `application/dto/RoleResponse.java`(관리자용, userCount 포함 — 이번엔 별도 경량 DTO 필요), `presentation/AdminRoleController.java`(패턴 참고, 단 신규 엔드포인트는 `/api/v1/admin/**`가 아니므로 별도 컨트롤러), `presentation/MenuController.java`(API-AUTH-022, "관리자 전용 아닌 별도 컨트롤러 분리" 동일 패턴)

### 담당 범위

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/auth/`

- 신규 DTO `application/dto/RoleOptionResponse.java`(id, roleCode, name — userCount 등 관리자 전용 필드 제외).
- `RoleService`에 조회 전용 메서드 추가(기존 목록 조회 재사용 후 DTO만 축소 매핑도 가능 — 재량).
- 신규 컨트롤러 `presentation/RoleController.java`: `GET /api/v1/roles`(인증만 필요, 역할 제한 없음).

### 완료(테스트 통과) 기준

- BE: 인증된 어떤 역할이든 200 응답(userCount 등 관리자 전용 필드 미노출), 미인증 401.
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → 완료 시 커밋.
