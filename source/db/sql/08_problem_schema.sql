-- =====================================================================
-- ITSM — problem 도메인 스키마
-- 단일 원천: docs/02_plan/database/problem.md
-- 문제 티켓·5 Whys·알려진 오류(KEDB)·후속 조치.
-- 인시던트/변경 연계는 common.ticket_link(source_type='PROBLEM'),
-- 코멘트/타임라인은 common.comment / common.timeline_event 재사용(신규 아님).
-- priority 는 impact×urgency 매트릭스 산정값(둘 중 하나라도 없으면 NULL=미산정),
--   값 체계는 incident 도메인과 동일(P1~P4).
-- 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
-- =====================================================================

-- ── problem : 문제 티켓 ───────────────────────────────────────────
CREATE TABLE problem (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_key           VARCHAR(20)  NOT NULL UNIQUE,   -- PRB-YYYY-####
    summary              VARCHAR(300) NOT NULL,
    description          TEXT,
    origin               VARCHAR(10),                    -- REACTIVE/PROACTIVE
    investigation_reason VARCHAR(500),
    impact               VARCHAR(10),                    -- HIGH/MEDIUM/LOW
    urgency              VARCHAR(10),                    -- HIGH/MEDIUM/LOW
    priority             VARCHAR(10),                    -- P1~P4 (NULL=미산정)
    status               VARCHAR(20)  NOT NULL DEFAULT 'DETECTION',
    root_cause           VARCHAR(1000),
    root_cause_category  VARCHAR(100),
    workaround           TEXT,
    component            VARCHAR(150),
    created_by           VARCHAR(100) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,
    is_deleted           BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_problem_origin   CHECK (origin  IN ('REACTIVE', 'PROACTIVE')),
    CONSTRAINT ck_problem_impact   CHECK (impact  IN ('HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT ck_problem_urgency  CHECK (urgency IN ('HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT ck_problem_priority CHECK (priority IN ('P1', 'P2', 'P3', 'P4')),
    CONSTRAINT ck_problem_status   CHECK (status IN
        ('DETECTION', 'CLASSIFICATION', 'INVESTIGATION',
         'KNOWN_ERROR', 'WORKAROUND', 'RESOLVED_CLOSED'))
);

-- ── problem_five_why : RCA 5 Whys 단계 ────────────────────────────
CREATE TABLE problem_five_why (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    problem_id  BIGINT       NOT NULL REFERENCES problem (id),
    step_no     SMALLINT     NOT NULL,
    content     VARCHAR(500) NOT NULL,
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_problem_five_why UNIQUE (problem_id, step_no)
);

-- ── known_error : 알려진 오류(KEDB) ───────────────────────────────
CREATE TABLE known_error (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    problem_id  BIGINT       NOT NULL REFERENCES problem (id),
    title       VARCHAR(300) NOT NULL,   -- 검색 대상
    root_cause  VARCHAR(1000),
    workaround  TEXT,
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── problem_action : 후속(시정) 조치 ──────────────────────────────
CREATE TABLE problem_action (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    problem_id  BIGINT       NOT NULL REFERENCES problem (id),
    description VARCHAR(500) NOT NULL,
    owner       VARCHAR(100),
    due_date    DATE,
    status      VARCHAR(15)  NOT NULL DEFAULT 'IN_PROGRESS',
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_problem_action_status CHECK (status IN ('IN_PROGRESS', 'DONE'))
);

-- ── 조회 인덱스 ───────────────────────────────────────────────────
CREATE INDEX idx_problem_status        ON problem (status);
CREATE INDEX idx_problem_five_why_prb  ON problem_five_why (problem_id);
CREATE INDEX idx_known_error_prb       ON known_error (problem_id);
CREATE INDEX idx_known_error_title     ON known_error (title);   -- KEDB 키워드 조회
CREATE INDEX idx_problem_action_prb    ON problem_action (problem_id);
