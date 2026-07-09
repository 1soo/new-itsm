-- =====================================================================
-- ITSM — auth 도메인 스키마 (PostgreSQL)
-- 단일 원천: docs/02_plan/database/auth.md
-- 공통 컬럼 규칙: id(IDENTITY PK), created_by/at, updated_by/at, is_deleted
--   (append-only 성격의 refresh_token, audit_log 는 updated_*/is_deleted 미사용)
-- =====================================================================

-- ── role : 역할 정의 ──────────────────────────────────────────────
CREATE TABLE role (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_code   VARCHAR(50)  NOT NULL UNIQUE,
    role_name   VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── app_user : 사용자 계정 (user 는 예약어) ──────────────────────
CREATE TABLE app_user (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,               -- 단방향 해시(BCrypt)
    name             VARCHAR(100) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    access_token_jti UUID,                                -- 현재 세션 Access Token JTI(로그아웃 시 NULL)
    created_by       VARCHAR(100) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by       VARCHAR(100),
    updated_at       TIMESTAMPTZ,
    is_deleted       BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_app_user_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- ── user_role : 사용자-역할 매핑 (다대다) ─────────────────────────
CREATE TABLE user_role (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES app_user (id),
    role_id    BIGINT       NOT NULL REFERENCES role (id),
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ,
    is_deleted BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
);

-- ── screen : 화면 정보 (screen_code ↔ 화면 설계서 SCR-* 1:1) ──────
CREATE TABLE screen (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    screen_code VARCHAR(50)  NOT NULL UNIQUE,
    screen_name VARCHAR(100) NOT NULL,
    path        VARCHAR(255) NOT NULL,
    domain      VARCHAR(30)  NOT NULL,
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── screen_role : 역할-화면 매핑 (다대다, RBAC) ───────────────────
CREATE TABLE screen_role (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    screen_id  BIGINT       NOT NULL REFERENCES screen (id),
    role_id    BIGINT       NOT NULL REFERENCES role (id),
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by VARCHAR(100),
    updated_at TIMESTAMPTZ,
    is_deleted BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_screen_role UNIQUE (screen_id, role_id)
);

-- ── refresh_token : Refresh Token 세션(JTI), append-only ──────────
CREATE TABLE refresh_token (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jti        UUID         NOT NULL UNIQUE,
    user_id    BIGINT       NOT NULL REFERENCES app_user (id),
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT false,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── audit_log : 인증·인가·계정/역할 변경 이벤트, append-only ──────
CREATE TABLE audit_log (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_type  VARCHAR(30)  NOT NULL,   -- LOGIN/LOGOUT/REFRESH/USER_CHANGE/ROLE_CHANGE
    actor_id    BIGINT       REFERENCES app_user (id),
    actor_email VARCHAR(255),
    target      VARCHAR(255),
    result      VARCHAR(10)  NOT NULL,   -- SUCCESS / FAILURE
    occurred_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_audit_log_result CHECK (result IN ('SUCCESS', 'FAILURE'))
);

-- ── 조회 인덱스 ───────────────────────────────────────────────────
CREATE INDEX idx_user_role_user   ON user_role (user_id);
CREATE INDEX idx_screen_role_role ON screen_role (role_id);
CREATE INDEX idx_refresh_token_user ON refresh_token (user_id);
CREATE INDEX idx_audit_log_actor   ON audit_log (actor_id);
CREATE INDEX idx_audit_log_event   ON audit_log (event_type, occurred_at);
