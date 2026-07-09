-- =====================================================================
-- ITSM — common 교차 테이블 (다형 참조)
-- 단일 원천: docs/02_plan/database/common.md
-- 도입 시점: service-request 단계 (approval[SERVICE_REQUEST], comment, timeline_event).
--   ticket_link 는 incident 단계에서 도입(보류).
-- 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
-- 다형 참조(ticket_type, ticket_id)라 DB FK 대신 애플리케이션에서 대상 검증.
-- =====================================================================

-- ── approval : 승인 (서비스요청·변경 공용) ────────────────────────
--   역할 기반 승인: approver_role 보유 사용자가 공유 대기함에서 처리,
--   먼저 처리한 사용자를 decided_by_id 에 기록. 인가는 role claim 의
--   approver_role 포함 여부로 판정. approver_role 은 role.role_code 논리 참조.
CREATE TABLE approval (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_type     VARCHAR(20)  NOT NULL,   -- SERVICE_REQUEST / CHANGE
    ticket_id       BIGINT       NOT NULL,
    approver_role   VARCHAR(50)  NOT NULL,   -- 승인 담당 역할(role.role_code)
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    decided_by_id   BIGINT       REFERENCES app_user (id),   -- 실제 결정한 사용자(NULL=미결정)
    decision_reason VARCHAR(500),
    decided_at      TIMESTAMPTZ,
    created_by      VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by      VARCHAR(100),
    updated_at      TIMESTAMPTZ,
    is_deleted      BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_approval_ticket_type CHECK (ticket_type IN ('SERVICE_REQUEST', 'CHANGE')),
    CONSTRAINT ck_approval_status      CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- ── comment : 티켓 공용 코멘트 ────────────────────────────────────
CREATE TABLE comment (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_type VARCHAR(20)  NOT NULL,
    ticket_id   BIGINT       NOT NULL,
    author_id   BIGINT       NOT NULL REFERENCES app_user (id),
    body        TEXT         NOT NULL,
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── timeline_event : 티켓 공용 타임라인 이벤트 ────────────────────
CREATE TABLE timeline_event (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_type VARCHAR(20)  NOT NULL,
    ticket_id   BIGINT       NOT NULL,
    event_type  VARCHAR(30)  NOT NULL,   -- STATUS_CHANGE/ASSIGN/UPDATE/ESCALATE 등
    message     TEXT,
    visibility  VARCHAR(10)  NOT NULL DEFAULT 'INTERNAL',
    occurred_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_timeline_visibility CHECK (visibility IN ('INTERNAL', 'EXTERNAL'))
);

-- ── (ticket_type, ticket_id) 조회 인덱스 (common.md 6절 권장) ─────
CREATE INDEX idx_approval_ticket        ON approval (ticket_type, ticket_id);
CREATE INDEX idx_comment_ticket         ON comment (ticket_type, ticket_id);
CREATE INDEX idx_timeline_event_ticket  ON timeline_event (ticket_type, ticket_id);
