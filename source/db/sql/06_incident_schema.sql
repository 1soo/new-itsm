-- =====================================================================
-- ITSM — incident 도메인 스키마 + common.ticket_link 도입
-- 단일 원천: docs/02_plan/database/incident.md, common.md
-- 타임라인은 common.timeline_event, 문제/자산 연계는 common.ticket_link 사용.
-- 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
-- =====================================================================

-- ── ticket_link : 다형 링크 (common, incident 단계에서 도입) ──────
--   *_type: SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE/ASSET/CI/KNOWLEDGE.
--   다형 참조라 target/source id 에는 DB FK 없음 → 대상 존재 검증은 앱 레벨(400).
CREATE TABLE ticket_link (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_type VARCHAR(20)  NOT NULL,
    source_id   BIGINT       NOT NULL,
    target_type VARCHAR(20)  NOT NULL,
    target_id   BIGINT       NOT NULL,
    link_type   VARCHAR(30),             -- RELATED/CAUSED_BY 등
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_ticket_link UNIQUE (source_type, source_id, target_type, target_id),
    CONSTRAINT ck_ticket_link_source_type CHECK (source_type IN
        ('SERVICE_REQUEST', 'INCIDENT', 'PROBLEM', 'CHANGE', 'ASSET', 'CI', 'KNOWLEDGE')),
    CONSTRAINT ck_ticket_link_target_type CHECK (target_type IN
        ('SERVICE_REQUEST', 'INCIDENT', 'PROBLEM', 'CHANGE', 'ASSET', 'CI', 'KNOWLEDGE'))
);

-- ── incident : 인시던트 티켓 ──────────────────────────────────────
CREATE TABLE incident (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_key       VARCHAR(20)  NOT NULL UNIQUE,   -- INC-YYYY-####
    summary          VARCHAR(300) NOT NULL,
    description      TEXT,
    severity         VARCHAR(10)  NOT NULL,
    priority         VARCHAR(10),
    status           VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    affected_service VARCHAR(150),
    affected_product VARCHAR(150),
    impact_start_at  TIMESTAMPTZ,
    detected_at      TIMESTAMPTZ,
    impact_end_at    TIMESTAMPTZ,
    resolved_at      TIMESTAMPTZ,
    mttd_minutes     INT,
    mtta_minutes     INT,
    mttr_minutes     INT,
    created_by       VARCHAR(100) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by       VARCHAR(100),
    updated_at       TIMESTAMPTZ,
    is_deleted       BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_incident_severity CHECK (severity IN ('SEV1', 'SEV2', 'SEV3')),
    CONSTRAINT ck_incident_priority CHECK (priority IN ('P1', 'P2', 'P3', 'P4')),
    CONSTRAINT ck_incident_status   CHECK (status IN ('NEW', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))
);

-- ── incident_responder : 대응 역할 배정 ───────────────────────────
CREATE TABLE incident_responder (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    incident_id   BIGINT       NOT NULL REFERENCES incident (id),
    user_id       BIGINT       NOT NULL REFERENCES app_user (id),
    response_role VARCHAR(20)  NOT NULL,
    created_by    VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by    VARCHAR(100),
    updated_at    TIMESTAMPTZ,
    is_deleted    BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_incident_responder UNIQUE (incident_id, user_id, response_role),
    CONSTRAINT ck_incident_responder_role CHECK (response_role IN ('TECH_LEAD', 'COMMS', 'SCRIBE'))
);

-- ── incident_severity_history : 심각도·우선순위 변경 이력 ─────────
CREATE TABLE incident_severity_history (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    incident_id  BIGINT       NOT NULL REFERENCES incident (id),
    old_severity VARCHAR(10),
    new_severity VARCHAR(10),
    old_priority VARCHAR(10),
    new_priority VARCHAR(10),
    created_by   VARCHAR(100) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by   VARCHAR(100),
    updated_at   TIMESTAMPTZ,
    is_deleted   BOOLEAN      NOT NULL DEFAULT false
);

-- ── postmortem : 포스트모템 (인시던트 1:1) ────────────────────────
CREATE TABLE postmortem (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    incident_id      BIGINT       NOT NULL UNIQUE REFERENCES incident (id),
    summary          TEXT,
    timeline_summary TEXT,
    root_cause       VARCHAR(500) NOT NULL,
    created_by       VARCHAR(100) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by       VARCHAR(100),
    updated_at       TIMESTAMPTZ,
    is_deleted       BOOLEAN      NOT NULL DEFAULT false
);

-- ── postmortem_five_why : 5 Whys 단계 ─────────────────────────────
CREATE TABLE postmortem_five_why (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    postmortem_id BIGINT       NOT NULL REFERENCES postmortem (id),
    step_no       SMALLINT     NOT NULL,
    content       VARCHAR(500) NOT NULL,
    created_by    VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by    VARCHAR(100),
    updated_at    TIMESTAMPTZ,
    is_deleted    BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_postmortem_five_why UNIQUE (postmortem_id, step_no)
);

-- ── postmortem_action_item : 조치항목 ─────────────────────────────
CREATE TABLE postmortem_action_item (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    postmortem_id BIGINT       NOT NULL REFERENCES postmortem (id),
    description   VARCHAR(500) NOT NULL,
    owner         VARCHAR(100),
    due_date      DATE,
    status        VARCHAR(10)  NOT NULL DEFAULT 'OPEN',
    created_by    VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by    VARCHAR(100),
    updated_at    TIMESTAMPTZ,
    is_deleted    BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_postmortem_action_status CHECK (status IN ('OPEN', 'DONE'))
);

-- ── 조회 인덱스 ───────────────────────────────────────────────────
CREATE INDEX idx_ticket_link_target      ON ticket_link (target_type, target_id);
CREATE INDEX idx_incident_status         ON incident (status);
CREATE INDEX idx_incident_responder_inc  ON incident_responder (incident_id);
CREATE INDEX idx_incident_sev_hist_inc   ON incident_severity_history (incident_id);
CREATE INDEX idx_pm_five_why_pm          ON postmortem_five_why (postmortem_id);
CREATE INDEX idx_pm_action_item_pm       ON postmortem_action_item (postmortem_id);
