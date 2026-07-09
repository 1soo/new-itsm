# 테이블 정의서 — 인증/계정/권한 (Auth & RBAC)

> 도메인: auth · 버전: 0.1 · 작성일: 2026-07-09

계정·역할·RBAC·화면 매핑·세션(Refresh Token)·감사 로그를 정의한다. RBAC/화면 매핑 테이블(`screen`, `user_role`, `screen_role`)의 **단일 원천(single source of truth)**이며, 타 도메인 정의서는 이를 참조한다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| user ↔ role (다대다) | 3NF | 한 사용자가 복수 역할을 가지므로 `user_role` 매핑 테이블로 분리(반복 컬럼 제거). |
| role ↔ screen (다대다) | 3NF | 역할별 접근 화면이 다대다이므로 `screen_role`로 분리. |
| audit_log | 1NF·정규화 최소화 | 로그는 조회·추적 목적의 append-only 성격이라 조인 최소화를 위해 주체/대상 식별 문자열을 함께 비정규 보관(추적성 우선). |

## 2. 공통 컬럼 규칙

모든 테이블에 포함한다. (snake_case, PostgreSQL 기준 `BIGINT GENERATED ALWAYS AS IDENTITY` PK)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, IDENTITY | sequence PK |
| created_by | VARCHAR(100) | NOT NULL | 등록자 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 등록일 |
| updated_by | VARCHAR(100) | NULL | 수정자 |
| updated_at | TIMESTAMPTZ | NULL | 수정일 |
| is_deleted | BOOLEAN | NOT NULL, DEFAULT false | soft delete 여부 |

> 이하 각 테이블 상세에서는 공통 컬럼을 생략하고 도메인 컬럼만 기술한다. (append-only 성격의 `audit_log`, `refresh_token`은 `updated_*`/`is_deleted` 미사용)

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| app_user | 사용자 계정 | REQ-AUTH-001/002/007 |
| role | 역할 정의 | REQ-AUTH-006 |
| user_role | 사용자-역할 매핑 | REQ-AUTH-005/006 |
| screen | 화면 정보 | REQ-AUTH-005 |
| screen_role | 역할-화면 매핑 | REQ-AUTH-005 |
| refresh_token | Refresh Token 세션(JTI) | REQ-AUTH-002/003/004 |
| audit_log | 감사 로그 | REQ-AUTH-008 |

## 4. 테이블 상세

### app_user

사용자 계정. `user`는 PostgreSQL 예약어라 `app_user`로 명명. 비밀번호는 단방향 해시만 저장.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 로그인 ID |
| password_hash | VARCHAR(255) | NOT NULL | 단방향 해시(BCrypt) |
| name | VARCHAR(100) | NOT NULL | 이름 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' | ACTIVE / INACTIVE |
| access_token_jti | UUID | NULL | 현재 세션 Access Token JTI(로그아웃 시 NULL) — [security/authentication.md](../security/authentication.md) |
| ...공통 컬럼... | | | |

### role

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| role_code | VARCHAR(50) | UNIQUE, NOT NULL | 역할 코드(SYSTEM_ADMIN 등) |
| role_name | VARCHAR(100) | NOT NULL | 표시명 |
| description | VARCHAR(255) | NULL | 설명 |
| ...공통 컬럼... | | | |

### user_role (사용자-역할 매핑)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| user_id | BIGINT | FK → app_user.id, NOT NULL | 사용자 |
| role_id | BIGINT | FK → role.id, NOT NULL | 역할 |
| | | UNIQUE(user_id, role_id) | 중복 부여 방지 |
| ...공통 컬럼... | | | |

### refresh_token (세션·JTI)

Refresh Token 세션 관리. 로그아웃/재발급 무효화 판정에 사용. [security/authentication.md](../security/authentication.md) 참조.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| jti | UUID | UNIQUE, NOT NULL | 토큰 고유 식별자(claim jti) |
| user_id | BIGINT | FK → app_user.id, NOT NULL | 소유자 |
| expires_at | TIMESTAMPTZ | NOT NULL | 만료 시각 |
| revoked | BOOLEAN | NOT NULL, DEFAULT false | 무효화 여부(로그아웃 시 true) |
| created_by | VARCHAR(100) | NOT NULL | |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | |

### audit_log

인증·인가·계정/역할 변경 이벤트 append-only 기록.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| event_type | VARCHAR(30) | NOT NULL | LOGIN/LOGOUT/REFRESH/USER_CHANGE/ROLE_CHANGE |
| actor_id | BIGINT | FK → app_user.id, NULL | 행위 주체(로그인 실패 시 NULL 가능) |
| actor_email | VARCHAR(255) | NULL | 주체 식별(추적용 비정규 보관) |
| target | VARCHAR(255) | NULL | 대상(계정/역할 식별) |
| result | VARCHAR(10) | NOT NULL | SUCCESS / FAILURE |
| occurred_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 발생 시각 |
| created_by | VARCHAR(100) | NOT NULL | |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | |

## 5. RBAC · 화면 관리 테이블

정석 RBAC 2단 매핑. `user_role`(위 4절) + `screen_role`.

### screen

화면 정보. `screen_code`는 화면 설계서의 SCR-* 코드와 1:1 매핑.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| screen_code | VARCHAR(50) | UNIQUE, NOT NULL | 화면 식별 코드(예: SCR-INC-001) |
| screen_name | VARCHAR(100) | NOT NULL | 화면명 |
| path | VARCHAR(255) | NOT NULL | 라우팅 경로 |
| domain | VARCHAR(30) | NOT NULL | 소속 도메인(auth/incident 등) |
| ...공통 컬럼... | | | |

### screen_role (역할-화면 매핑)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| screen_id | BIGINT | FK → screen.id, NOT NULL | 화면 |
| role_id | BIGINT | FK → role.id, NOT NULL | 역할 |
| | | UNIQUE(screen_id, role_id) | 중복 방지 |
| ...공통 컬럼... | | | |

> 역할 코드·화면별 접근 매핑의 구체 값은 [security/authorization/](../security/authorization/)의 역할 정의서를 seed 근거로 사용한다.

## 6. 관계 · 제약조건 요약

- user_role.user_id → app_user.id (FK), user_role.role_id → role.id (FK), UNIQUE(user_id, role_id)
- screen_role.screen_id → screen.id (FK), screen_role.role_id → role.id (FK), UNIQUE(screen_id, role_id)
- refresh_token.user_id → app_user.id (FK), UNIQUE(jti)
- audit_log.actor_id → app_user.id (FK, nullable)
- app_user.email UNIQUE, role.role_code UNIQUE, screen.screen_code UNIQUE
