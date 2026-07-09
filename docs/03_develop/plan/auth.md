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
