-- =====================================================================
-- ITSM — change 도메인 스키마
-- 단일 원천: docs/02_plan/database/change.md
-- 표준 변경 사전승인 템플릿(change_template)·변경 요청(change_request)·영향 시스템.
-- 승인은 common.approval(ticket_type='CHANGE'),
-- 인시던트/문제 연계는 common.ticket_link(source_type='CHANGE'),
-- 코멘트/타임라인은 common.comment / common.timeline_event 재사용(신규 아님).
-- 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
-- =====================================================================

-- ── change_template : 표준 변경 사전승인 템플릿 ───────────────────
CREATE TABLE change_template (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── change_request : 변경 요청(RFC) ───────────────────────────────
CREATE TABLE change_request (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_key           VARCHAR(20)  NOT NULL UNIQUE,   -- CHG-YYYY-####
    summary              VARCHAR(300) NOT NULL,
    description          TEXT,
    type                 VARCHAR(15)  NOT NULL,          -- STANDARD/NORMAL/EMERGENCY
    risk                 VARCHAR(10),                    -- HIGH/MEDIUM/LOW(미평가 NULL)
    status               VARCHAR(20)  NOT NULL DEFAULT 'REQUESTED',
    approval_route       VARCHAR(15),                    -- AUTO/PEER_REVIEW/CAB
    implementation_plan  TEXT,
    rollback_plan        TEXT,
    scheduled_at         TIMESTAMPTZ,
    template_id          BIGINT       REFERENCES change_template (id),
    outcome              VARCHAR(10),                    -- SUCCESS/FAILURE
    rolled_back          BOOLEAN,
    result_note          VARCHAR(500),
    created_by           VARCHAR(100) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,
    is_deleted           BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_change_request_type CHECK (type IN ('STANDARD', 'NORMAL', 'EMERGENCY')),
    CONSTRAINT ck_change_request_risk CHECK (risk IN ('HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT ck_change_request_status CHECK (status IN
        ('REQUESTED', 'REVIEW', 'PLANNING', 'APPROVAL', 'IMPLEMENTATION', 'CLOSED')),
    CONSTRAINT ck_change_request_approval_route CHECK (approval_route IN ('AUTO', 'PEER_REVIEW', 'CAB')),
    CONSTRAINT ck_change_request_outcome CHECK (outcome IN ('SUCCESS', 'FAILURE'))
);

-- ── change_affected_system : 변경 영향 시스템 ─────────────────────
CREATE TABLE change_affected_system (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    change_id   BIGINT       NOT NULL REFERENCES change_request (id),
    system_name VARCHAR(150) NOT NULL,
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── 조회 인덱스 ───────────────────────────────────────────────────
CREATE INDEX idx_change_request_status       ON change_request (status);
CREATE INDEX idx_change_request_scheduled_at ON change_request (scheduled_at);
CREATE INDEX idx_change_request_template     ON change_request (template_id);
CREATE INDEX idx_change_affected_system_chg  ON change_affected_system (change_id);
