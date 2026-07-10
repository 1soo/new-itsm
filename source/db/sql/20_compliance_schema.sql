-- =====================================================================
-- ITSM — compliance(컴플라이언스 관리, COMP) 도메인 스키마
-- 단일 원천: docs/02_plan/database/compliance.md
-- 변경 요청 연계는 common.ticket_link(source_type='COMPLIANCE_REQUIREMENT',
--   target_type='CHANGE' 기존 값 재사용), 감사 로그는 auth.audit_log 재사용(신규 아님).
-- compliance_status(준수 상태)는 corrective_action의 미해결(DETECTED/IN_PROGRESS)
--   건 존재 여부로 조회 시점에 산정하는 비저장 계산값(컬럼 없음).
-- 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
-- =====================================================================

-- ── common 증분 : ticket_link CHECK에 'COMPLIANCE_REQUIREMENT' 추가(source_type만) ──
--   'COMPLIANCE_REQUIREMENT'(22자)가 기존 source_type VARCHAR(20)을 초과해 컬럼 폭도 함께 확장.
--   target_type은 기존 'CHANGE' 값을 그대로 사용하므로 target_type 컬럼/CHECK는 변경 없음.
ALTER TABLE ticket_link ALTER COLUMN source_type TYPE VARCHAR(25);
ALTER TABLE ticket_link DROP CONSTRAINT ck_ticket_link_source_type;
ALTER TABLE ticket_link ADD CONSTRAINT ck_ticket_link_source_type CHECK (source_type IN
    ('SERVICE_REQUEST', 'INCIDENT', 'PROBLEM', 'CHANGE', 'ASSET', 'CI', 'KNOWLEDGE', 'VULNERABILITY', 'COMPLIANCE_REQUIREMENT'));

-- ── auth 증분 : audit_log.event_type 폭 확장 ───────────────────────
--   신규 이벤트타입 'COMPLIANCE_ACTION_STATUS_CHANGE'(31자)가 기존 VARCHAR(30)을 초과해 확장.
ALTER TABLE audit_log ALTER COLUMN event_type TYPE VARCHAR(35);

-- ── compliance_requirement : 컴플라이언스 요구사항 ─────────────────
CREATE TABLE compliance_requirement (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    requirement_key VARCHAR(20)  NOT NULL UNIQUE,   -- COMP-YYYY-####
    name            VARCHAR(200) NOT NULL,
    basis           VARCHAR(500) NOT NULL,
    scope           VARCHAR(500),
    owner_id        BIGINT       REFERENCES app_user (id),   -- NULL이면 책임자 미지정
    created_by      VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by      VARCHAR(100),
    updated_at      TIMESTAMPTZ,
    is_deleted      BOOLEAN      NOT NULL DEFAULT false
);

-- ── corrective_action : 시정조치 항목 ──────────────────────────────
CREATE TABLE corrective_action (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    requirement_id BIGINT       NOT NULL REFERENCES compliance_requirement (id),
    description    VARCHAR(500) NOT NULL,
    status         VARCHAR(15)  NOT NULL DEFAULT 'DETECTED',
    created_by     VARCHAR(100) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by     VARCHAR(100),
    updated_at     TIMESTAMPTZ,
    is_deleted     BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_corrective_action_status CHECK (status IN ('DETECTED', 'IN_PROGRESS', 'RESOLVED'))
);

-- ── 조회 인덱스 ───────────────────────────────────────────────────
CREATE INDEX idx_corrective_action_requirement_status ON corrective_action (requirement_id, status);   -- 미해결 건수 집계용
