-- =====================================================================
-- ITSM — infra-monitoring(IT 인프라 모니터링 & 용량관리, IOM) 도메인 스키마
-- 단일 원천: docs/02_plan/database/infra-monitoring.md
-- 대상 자산은 asset.md의 asset 참조(신규 FK, ticket_link 재사용 아님 — 이 도메인은 티켓형이 아님).
-- 가동률(uptime)·용량 활용률(utilization)은 조회 시점 계산값(캐시하지 않음).
-- 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
-- =====================================================================

-- ── infra_metric : 인프라 지표 레코드(시계열, append) ──────────────
CREATE TABLE infra_metric (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id     BIGINT         NOT NULL REFERENCES asset (id),
    metric_type  VARCHAR(20)    NOT NULL,          -- UPTIME/CPU/MEMORY/RESPONSE_TIME
    value        NUMERIC(10,2)  NOT NULL,
    measured_at  TIMESTAMPTZ    NOT NULL,
    created_by   VARCHAR(100)   NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_by   VARCHAR(100),
    updated_at   TIMESTAMPTZ,
    is_deleted   BOOLEAN        NOT NULL DEFAULT false,
    CONSTRAINT ck_infra_metric_type CHECK (metric_type IN ('UPTIME', 'CPU', 'MEMORY', 'RESPONSE_TIME'))
);

-- ── infra_metric_threshold : 지표 항목별(전역) 임계치 ──────────────
CREATE TABLE infra_metric_threshold (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    metric_type  VARCHAR(20)    NOT NULL UNIQUE,
    upper_limit  NUMERIC(10,2),
    lower_limit  NUMERIC(10,2),
    created_by   VARCHAR(100)   NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_by   VARCHAR(100),
    updated_at   TIMESTAMPTZ,
    is_deleted   BOOLEAN        NOT NULL DEFAULT false,
    CONSTRAINT ck_infra_metric_threshold_type CHECK (metric_type IN ('UPTIME', 'CPU', 'MEMORY', 'RESPONSE_TIME'))
);

-- ── infra_metric_alert : 임계치 초과 알림(자산 asset(expiry_alert)와 동일 패턴) ──
CREATE TABLE infra_metric_alert (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    metric_id       BIGINT         NOT NULL REFERENCES infra_metric (id),
    asset_id        BIGINT         NOT NULL REFERENCES asset (id),          -- 조회 성능용 비정규화
    metric_type     VARCHAR(20)    NOT NULL,                                -- 비정규화
    breached_value  NUMERIC(10,2)  NOT NULL,
    threshold_type  VARCHAR(10)    NOT NULL,          -- UPPER/LOWER
    acknowledged    BOOLEAN        NOT NULL DEFAULT false,
    created_by      VARCHAR(100)   NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_by      VARCHAR(100),
    updated_at      TIMESTAMPTZ,
    is_deleted      BOOLEAN        NOT NULL DEFAULT false,
    CONSTRAINT ck_infra_metric_alert_type CHECK (metric_type IN ('UPTIME', 'CPU', 'MEMORY', 'RESPONSE_TIME')),
    CONSTRAINT ck_infra_metric_alert_threshold_type CHECK (threshold_type IN ('UPPER', 'LOWER'))
);

-- ── uptime_target : 자산별 가동률 목표(SLA, 1:1) ───────────────────
CREATE TABLE uptime_target (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id           BIGINT        NOT NULL UNIQUE REFERENCES asset (id),
    target_percentage  NUMERIC(5,2)  NOT NULL,
    created_by         VARCHAR(100)  NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_by         VARCHAR(100),
    updated_at         TIMESTAMPTZ,
    is_deleted         BOOLEAN       NOT NULL DEFAULT false
);

-- ── capacity_plan : 팀/서비스별 용량 계획 ──────────────────────────
CREATE TABLE capacity_plan (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    team_or_service  VARCHAR(150)   NOT NULL,
    capacity         NUMERIC(12,2)  NOT NULL,
    demand           NUMERIC(12,2)  NOT NULL,
    created_by       VARCHAR(100)   NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_by       VARCHAR(100),
    updated_at       TIMESTAMPTZ,
    is_deleted       BOOLEAN        NOT NULL DEFAULT false
);

-- ── 조회 인덱스 ───────────────────────────────────────────────────
CREATE INDEX idx_infra_metric_asset_type_time ON infra_metric (asset_id, metric_type, measured_at);   -- 시계열·가동률 평균 계산용
CREATE INDEX idx_infra_metric_alert_asset     ON infra_metric_alert (asset_id);
