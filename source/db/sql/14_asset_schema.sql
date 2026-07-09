-- =====================================================================
-- ITSM — asset 도메인 스키마 (마지막 도메인, 7/7)
-- 단일 원천: docs/02_plan/database/asset.md
-- 자산·유형별 속성(EAV)·생애주기 이력·만료 알림·구성 항목(CI)·CI 관계(CMDB).
-- 티켓 연계는 common.ticket_link(source_type='ASSET'/'CI') 재사용(신규 아님).
-- 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
--   (asset_lifecycle_history 는 append-only 성격이라 updated_*/is_deleted 미사용)
-- =====================================================================

-- ── asset : IT 자산 ────────────────────────────────────────────────
CREATE TABLE asset (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_key        VARCHAR(20)    NOT NULL UNIQUE,   -- AST-####
    name             VARCHAR(200)   NOT NULL,
    type             VARCHAR(15)    NOT NULL,          -- HARDWARE/SOFTWARE/CLOUD
    status           VARCHAR(15)    NOT NULL DEFAULT 'PLANNING',
    owner            VARCHAR(100),
    location         VARCHAR(150),
    purchase_date    DATE,
    cost             NUMERIC(15,2),
    license_expiry   DATE,
    warranty_expiry  DATE,
    contract_expiry  DATE,
    created_by       VARCHAR(100)   NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_by       VARCHAR(100),
    updated_at       TIMESTAMPTZ,
    is_deleted       BOOLEAN        NOT NULL DEFAULT false,
    CONSTRAINT ck_asset_type   CHECK (type IN ('HARDWARE', 'SOFTWARE', 'CLOUD')),
    CONSTRAINT ck_asset_status CHECK (status IN
        ('PLANNING', 'PROCUREMENT', 'OPERATION', 'MAINTENANCE', 'RETIREMENT'))
);

-- ── asset_attribute : 유형별 속성(EAV) ────────────────────────────
CREATE TABLE asset_attribute (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id    BIGINT       NOT NULL REFERENCES asset (id),
    attr_key    VARCHAR(50)  NOT NULL,
    attr_value  VARCHAR(500),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_asset_attribute UNIQUE (asset_id, attr_key)
);

-- ── asset_lifecycle_history : 생애주기 이력, append-only ──────────
CREATE TABLE asset_lifecycle_history (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id    BIGINT       NOT NULL REFERENCES asset (id),
    stage       VARCHAR(15)  NOT NULL,
    changed_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── expiry_alert : 만료 임박 알림 ──────────────────────────────────
CREATE TABLE expiry_alert (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id     BIGINT       NOT NULL REFERENCES asset (id),
    expiry_type  VARCHAR(15)  NOT NULL,
    due_date     DATE         NOT NULL,
    notified     BOOLEAN      NOT NULL DEFAULT false,
    created_by   VARCHAR(100) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by   VARCHAR(100),
    updated_at   TIMESTAMPTZ,
    is_deleted   BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_expiry_alert_type CHECK (expiry_type IN ('LICENSE', 'WARRANTY', 'CONTRACT'))
);

-- ── configuration_item : 구성 항목(CI) ────────────────────────────
CREATE TABLE configuration_item (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    type        VARCHAR(50),
    asset_id    BIGINT       REFERENCES asset (id),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── ci_relation : CI 간 의존 관계(CMDB, 자기참조) ─────────────────
CREATE TABLE ci_relation (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_ci_id   BIGINT       NOT NULL REFERENCES configuration_item (id),
    target_ci_id   BIGINT       NOT NULL REFERENCES configuration_item (id),
    relation_type  VARCHAR(20)  NOT NULL,
    created_by     VARCHAR(100) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by     VARCHAR(100),
    updated_at     TIMESTAMPTZ,
    is_deleted     BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_ci_relation UNIQUE (source_ci_id, target_ci_id, relation_type),
    CONSTRAINT ck_ci_relation_no_self CHECK (source_ci_id <> target_ci_id),
    CONSTRAINT ck_ci_relation_type CHECK (relation_type IN ('DEPENDS_ON', 'RUNS_ON', 'CONNECTS_TO'))
);

-- ── 조회 인덱스 ───────────────────────────────────────────────────
CREATE INDEX idx_asset_status              ON asset (status);
CREATE INDEX idx_asset_type                ON asset (type);
CREATE INDEX idx_asset_owner               ON asset (owner);
CREATE INDEX idx_asset_attribute_asset     ON asset_attribute (asset_id);
CREATE INDEX idx_asset_lifecycle_asset     ON asset_lifecycle_history (asset_id);
CREATE INDEX idx_expiry_alert_asset        ON expiry_alert (asset_id);
CREATE INDEX idx_expiry_alert_due_date     ON expiry_alert (due_date);
CREATE INDEX idx_configuration_item_asset  ON configuration_item (asset_id);
CREATE INDEX idx_ci_relation_source        ON ci_relation (source_ci_id);
CREATE INDEX idx_ci_relation_target        ON ci_relation (target_ci_id);
