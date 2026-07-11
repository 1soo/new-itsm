-- =====================================================================
-- ITSM — 공용 승인 프로세스 엔진 도입(유지보수 요청 2026-07-11)
-- 단일 원천: docs/02_plan/database/common.md 4절(approval_process~approval_decision)
-- 기존 단일 approval 테이블(03_common_schema.sql)을 전 도메인 공용 커스텀
-- 다차 승인 엔진으로 완전 대체. 03/04/10 원본 파일은 수정하지 않고
-- 이 파일에서 ALTER/DROP으로 정리한다(24번 선례와 동일 패턴).
-- 마이그레이션 데이터 보존 불필요(local fresh 재기동 전제, dev-lead 확정).
-- 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
--   approval_decision은 append-only 성격이라 updated_*/is_deleted 미사용
--   (approval_process.md 4절, audit_log/refresh_token과 동일 규칙).
-- =====================================================================

-- ── approval_process : 승인 프로세스 정의(규칙 헤더, 전 도메인 공용) ─
CREATE TABLE approval_process (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    domain              VARCHAR(30)  NOT NULL,   -- 대상 도메인 코드(SERVICE_REQUEST/CHANGE/KNOWLEDGE/INCIDENT/PROBLEM 등)
    request_subtype_key VARCHAR(50),             -- 요청 유형 스코프 값(하위유형 개념 없는 도메인은 NULL)
    priority_tier       SMALLINT     NOT NULL,   -- 1=도메인 기본, 2=요청유형 전용, 3=승인요청자 역할 전용
    name                VARCHAR(150) NOT NULL,
    description         VARCHAR(500),
    created_by          VARCHAR(100) NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by          VARCHAR(100),
    updated_at          TIMESTAMPTZ,
    is_deleted          BOOLEAN      NOT NULL DEFAULT false
);

-- ── approval_process_requester_role : 규칙의 승인요청자 역할 스코프(ANY 매칭) ─
CREATE TABLE approval_process_requester_role (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    approval_process_id  BIGINT       NOT NULL REFERENCES approval_process (id),
    role_id              BIGINT       NOT NULL REFERENCES role (id),
    created_by           VARCHAR(100) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,
    is_deleted           BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_approval_process_requester_role UNIQUE (approval_process_id, role_id)
);

-- ── approval_process_step : 규칙의 승인자 차수(n차, 최대 10차) ────────
CREATE TABLE approval_process_step (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    approval_process_id  BIGINT       NOT NULL REFERENCES approval_process (id),
    step_no              SMALLINT     NOT NULL,
    decision_mode        VARCHAR(5)   NOT NULL DEFAULT 'OR',   -- AND/OR(역할 2개 이상일 때만 의미)
    created_by           VARCHAR(100) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,
    is_deleted           BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_approval_process_step UNIQUE (approval_process_id, step_no),
    CONSTRAINT ck_approval_process_step_no CHECK (step_no BETWEEN 1 AND 10),
    CONSTRAINT ck_approval_process_step_decision_mode CHECK (decision_mode IN ('AND', 'OR'))
);

-- ── approval_process_step_role : 차수별 승인 역할(1개 이상) ───────────
CREATE TABLE approval_process_step_role (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    step_id     BIGINT       NOT NULL REFERENCES approval_process_step (id),
    role_id     BIGINT       NOT NULL REFERENCES role (id),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_approval_process_step_role UNIQUE (step_id, role_id)
);

-- ── approval_request : 승인 인스턴스 헤더(기존 approval 테이블 대체) ──
--   전 도메인 공용 다형 참조(ticket_type + ticket_id).
CREATE TABLE approval_request (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_type          VARCHAR(20)  NOT NULL,
    ticket_id            BIGINT       NOT NULL,
    approval_process_id  BIGINT       NOT NULL REFERENCES approval_process (id),
    status               VARCHAR(15)  NOT NULL DEFAULT 'IN_PROGRESS',
    current_step_no      SMALLINT,               -- 전체 승인 완료 시 NULL
    created_by           VARCHAR(100) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,
    is_deleted           BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_approval_request_status CHECK (status IN ('IN_PROGRESS', 'APPROVED', 'REJECTED'))
);

-- ── approval_request_step : 인스턴스 차수 스냅샷 ──────────────────────
CREATE TABLE approval_request_step (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    approval_request_id  BIGINT       NOT NULL REFERENCES approval_request (id),
    step_no              SMALLINT     NOT NULL,
    decision_mode        VARCHAR(5)   NOT NULL,
    status               VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    created_by           VARCHAR(100) NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by           VARCHAR(100),
    updated_at           TIMESTAMPTZ,
    is_deleted           BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_approval_request_step UNIQUE (approval_request_id, step_no),
    CONSTRAINT ck_approval_request_step_decision_mode CHECK (decision_mode IN ('AND', 'OR')),
    CONSTRAINT ck_approval_request_step_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SKIPPED'))
);

-- ── approval_request_step_role : 인스턴스 차수별 역할 스냅샷(AND 분모) ─
CREATE TABLE approval_request_step_role (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    step_id     BIGINT       NOT NULL REFERENCES approval_request_step (id),
    role_id     BIGINT       NOT NULL REFERENCES role (id),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_approval_request_step_role UNIQUE (step_id, role_id)
);

-- ── approval_decision : 역할별 승인/반려 결정 기록(append-only) ──────
CREATE TABLE approval_decision (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    step_id        BIGINT       NOT NULL REFERENCES approval_request_step (id),
    role_id        BIGINT       NOT NULL REFERENCES role (id),
    decided_by_id  BIGINT       NOT NULL REFERENCES app_user (id),
    decision       VARCHAR(10)  NOT NULL,
    reason         VARCHAR(500),
    decided_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(100) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_approval_decision UNIQUE (step_id, role_id),
    CONSTRAINT ck_approval_decision_decision CHECK (decision IN ('APPROVE', 'REJECT'))
);

-- ── 부분 유니크 인덱스(tier=1/2 중복 방지, common.md 4절 approval_process 상세) ─
--   tier=3(승인요청자 역할 전용)은 역할 조합 교집합 판정이 필요해 애플리케이션이 검증.
CREATE UNIQUE INDEX uq_approval_process_domain_tier1
    ON approval_process (domain) WHERE priority_tier = 1;
CREATE UNIQUE INDEX uq_approval_process_domain_subtype_tier2
    ON approval_process (domain, request_subtype_key) WHERE priority_tier = 2;

-- ── 조회 인덱스 ───────────────────────────────────────────────────────
CREATE INDEX idx_approval_process_requester_role_process ON approval_process_requester_role (approval_process_id);
CREATE INDEX idx_approval_process_step_process            ON approval_process_step (approval_process_id);
CREATE INDEX idx_approval_process_step_role_step           ON approval_process_step_role (step_id);
CREATE INDEX idx_approval_request_ticket                   ON approval_request (ticket_type, ticket_id);
CREATE INDEX idx_approval_request_step_request             ON approval_request_step (approval_request_id);
CREATE INDEX idx_approval_request_step_role_step           ON approval_request_step_role (step_id);
CREATE INDEX idx_approval_decision_step                     ON approval_decision (step_id);

-- ── service_catalog_item : 승인 설정 컬럼 제거(SCR-ADMIN-008로 이관) ──
--   원본 정의: 04_srm_schema.sql 라인 27~28(approval_required/approver_role).
--   05_srm_seed.sql은 이 컬럼으로 INSERT가 이미 끝난 뒤(26번은 05번보다 나중 실행)라 수정 불필요.
ALTER TABLE service_catalog_item
    DROP COLUMN approval_required,
    DROP COLUMN approver_role;

-- ── change_request : 승인 경로 컬럼 제거(커스텀 승인 프로세스로 대체) ─
--   원본 정의: 10_change_schema.sql 라인 32,49(approval_route, CHECK 포함).
--   컬럼 삭제 시 ck_change_request_approval_route CHECK 제약도 함께 자동 제거된다.
ALTER TABLE change_request
    DROP COLUMN approval_route;

-- ── approval : 기존 단일 승인 테이블 제거 ─────────────────────────────
--   원본 정의: 03_common_schema.sql 라인 10~29, 인덱스 라인 64(idx_approval_ticket).
--   테이블 삭제 시 CHECK 제약·인덱스도 함께 자동 제거된다.
DROP TABLE approval;
