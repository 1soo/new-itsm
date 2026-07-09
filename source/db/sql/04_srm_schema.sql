-- =====================================================================
-- ITSM — service-request 도메인 스키마
-- 단일 원천: docs/02_plan/database/service-request.md
-- 승인은 common.approval(ticket_type='SERVICE_REQUEST'), 코멘트는 common.comment 사용.
-- 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
-- =====================================================================

-- ── queue : 처리 큐 ───────────────────────────────────────────────
CREATE TABLE queue (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_default  BOOLEAN      NOT NULL DEFAULT false,
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── service_catalog_item : 요청 유형(카탈로그 항목) ───────────────
CREATE TABLE service_catalog_item (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                 VARCHAR(150) NOT NULL,
    description          VARCHAR(500),
    category             VARCHAR(100),
    approval_required    BOOLEAN      NOT NULL DEFAULT false,
    approver_role        VARCHAR(50),                          -- 승인 담당 역할(role.role_code, 기본 'APPROVER'). approval_required=true 시 승인 라우팅
    queue_id             BIGINT       REFERENCES queue (id),
    sla_response_minutes INT,
    sla_resolve_minutes  INT,
    created_by           VARCHAR(100) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,
    is_deleted           BOOLEAN      NOT NULL DEFAULT false
);

-- ── catalog_form_field : 요청 유형 동적 양식 필드 ─────────────────
CREATE TABLE catalog_form_field (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    catalog_item_id BIGINT       NOT NULL REFERENCES service_catalog_item (id),
    field_key       VARCHAR(50)  NOT NULL,
    label           VARCHAR(150) NOT NULL,
    field_type      VARCHAR(20)  NOT NULL,   -- text/select/number/date/file
    required        BOOLEAN      NOT NULL DEFAULT false,
    options         JSONB,                   -- select 옵션 목록
    sort_order      INT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by      VARCHAR(100),
    updated_at      TIMESTAMPTZ,
    is_deleted      BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_catalog_form_field UNIQUE (catalog_item_id, field_key),
    CONSTRAINT ck_catalog_form_field_type
        CHECK (field_type IN ('text', 'select', 'number', 'date', 'file'))
);

-- ── service_request : 서비스 요청 티켓 ────────────────────────────
CREATE TABLE service_request (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_key       VARCHAR(20)  NOT NULL UNIQUE,   -- SRM-YYYY-####
    catalog_item_id  BIGINT       NOT NULL REFERENCES service_catalog_item (id),
    requester_id     BIGINT       NOT NULL REFERENCES app_user (id),
    assignee_id      BIGINT       REFERENCES app_user (id),
    queue_id         BIGINT       REFERENCES queue (id),
    status           VARCHAR(20)  NOT NULL DEFAULT 'SUBMITTED',
    sla_response_due TIMESTAMPTZ,
    sla_resolve_due  TIMESTAMPTZ,
    sla_status       VARCHAR(10)  NOT NULL DEFAULT 'OK',
    created_by       VARCHAR(100) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by       VARCHAR(100),
    updated_at       TIMESTAMPTZ,
    is_deleted       BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_service_request_status CHECK (status IN (
        'SUBMITTED', 'VALIDATED', 'ROUTED', 'APPROVAL_PENDING',
        'IN_FULFILLMENT', 'FULFILLED', 'CLOSED', 'REJECTED')),
    CONSTRAINT ck_service_request_sla_status CHECK (sla_status IN ('OK', 'WARNING', 'BREACHED'))
);

-- ── service_request_form_value : 요청 양식 입력 값 (EAV) ──────────
CREATE TABLE service_request_form_value (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    service_request_id BIGINT       NOT NULL REFERENCES service_request (id),
    field_key          VARCHAR(50)  NOT NULL,
    field_value        TEXT,
    created_by         VARCHAR(100) NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by         VARCHAR(100),
    updated_at         TIMESTAMPTZ,
    is_deleted         BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_srm_form_value UNIQUE (service_request_id, field_key)
);

-- ── csat : 만족도 평가 (요청 1:1) ─────────────────────────────────
CREATE TABLE csat (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    service_request_id BIGINT       NOT NULL UNIQUE REFERENCES service_request (id),
    score              SMALLINT     NOT NULL,
    comment            VARCHAR(500),
    created_by         VARCHAR(100) NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by         VARCHAR(100),
    updated_at         TIMESTAMPTZ,
    is_deleted         BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_csat_score CHECK (score BETWEEN 1 AND 5)
);

-- ── 조회 인덱스 ───────────────────────────────────────────────────
CREATE INDEX idx_catalog_form_field_item ON catalog_form_field (catalog_item_id);
CREATE INDEX idx_service_request_requester ON service_request (requester_id);
CREATE INDEX idx_service_request_assignee  ON service_request (assignee_id);
CREATE INDEX idx_service_request_queue     ON service_request (queue_id);
CREATE INDEX idx_service_request_status    ON service_request (status);
CREATE INDEX idx_srm_form_value_request    ON service_request_form_value (service_request_id);
