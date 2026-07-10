-- =====================================================================
-- ITSM — esm(엔터프라이즈 서비스 관리) 도메인 스키마
-- 단일 원천: docs/02_plan/database/esm.md
-- auth 도메인 증분: app_user.department 컬럼 추가(순수 컬럼 추가, 기존 마이그레이션 영향 없음).
-- 코멘트/타임라인은 common.comment/timeline_event(ticket_type='ESM_REQUEST'/'HR_CASE') 재사용.
-- 자산 회수 연계는 asset.id 참조(FK, nullable). 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
-- =====================================================================

-- ── auth 증분 : app_user.department (부서 요청/체크리스트 담당 부서 판정) ──
ALTER TABLE app_user ADD COLUMN department VARCHAR(20);
ALTER TABLE app_user ADD CONSTRAINT ck_app_user_department
    CHECK (department IS NULL OR department IN ('HR', 'LEGAL', 'FACILITIES', 'FINANCE', 'IT'));

-- ── esm_catalog_item : 부서별 요청 유형(카탈로그 항목) ─────────────
CREATE TABLE esm_catalog_item (
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                     VARCHAR(150) NOT NULL,
    description              VARCHAR(500),
    department               VARCHAR(20)  NOT NULL,
    checklist_template_type  VARCHAR(15)  NOT NULL DEFAULT 'NONE',
    created_by               VARCHAR(100) NOT NULL,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by               VARCHAR(100),
    updated_at               TIMESTAMPTZ,
    is_deleted               BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_esm_catalog_item_department CHECK (department IN ('HR', 'LEGAL', 'FACILITIES', 'FINANCE')),
    CONSTRAINT ck_esm_catalog_item_checklist_type CHECK (checklist_template_type IN ('NONE', 'ONBOARDING', 'OFFBOARDING'))
);

-- ── esm_catalog_form_field : 요청 유형 동적 양식 필드 ──────────────
CREATE TABLE esm_catalog_form_field (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    catalog_item_id BIGINT       NOT NULL REFERENCES esm_catalog_item (id),
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
    CONSTRAINT uq_esm_catalog_form_field UNIQUE (catalog_item_id, field_key),
    CONSTRAINT ck_esm_catalog_form_field_type
        CHECK (field_type IN ('text', 'select', 'number', 'date', 'file'))
);

-- ── esm_checklist_template_task : 카탈로그 항목의 체크리스트 하위 작업 템플릿 ──
-- checklist_template_type != 'NONE' 인 카탈로그 항목에만 존재. 요청 제출 시 복제되어 esm_checklist_task 생성.
CREATE TABLE esm_checklist_template_task (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    catalog_item_id BIGINT       NOT NULL REFERENCES esm_catalog_item (id),
    department      VARCHAR(20)  NOT NULL,
    task_description VARCHAR(300) NOT NULL,
    sort_order      INT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by      VARCHAR(100),
    updated_at      TIMESTAMPTZ,
    is_deleted      BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_esm_checklist_template_task_dept
        CHECK (department IN ('HR', 'LEGAL', 'FACILITIES', 'FINANCE', 'IT'))
);

-- ── esm_checklist : 온보딩/오프보딩 체크리스트 ─────────────────────
CREATE TABLE esm_checklist (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type              VARCHAR(15)  NOT NULL,
    target_user_name  VARCHAR(100) NOT NULL,
    status            VARCHAR(15)  NOT NULL DEFAULT 'IN_PROGRESS',
    created_by        VARCHAR(100) NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by        VARCHAR(100),
    updated_at        TIMESTAMPTZ,
    is_deleted        BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_esm_checklist_type CHECK (type IN ('ONBOARDING', 'OFFBOARDING')),
    CONSTRAINT ck_esm_checklist_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED'))
);

-- ── esm_checklist_task : 체크리스트 하위 작업(실행 인스턴스) ───────
CREATE TABLE esm_checklist_task (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    checklist_id      BIGINT       NOT NULL REFERENCES esm_checklist (id),
    department        VARCHAR(20)  NOT NULL,
    description       VARCHAR(300) NOT NULL,
    status            VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    related_asset_id  BIGINT       REFERENCES asset (id),   -- 오프보딩 자산 회수 작업의 대상 자산
    created_by        VARCHAR(100) NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by        VARCHAR(100),
    updated_at        TIMESTAMPTZ,
    is_deleted        BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_esm_checklist_task_dept CHECK (department IN ('HR', 'LEGAL', 'FACILITIES', 'FINANCE', 'IT')),
    CONSTRAINT ck_esm_checklist_task_status CHECK (status IN ('PENDING', 'DONE'))
);

-- ── esm_request : 부서 요청 티켓 ────────────────────────────────────
CREATE TABLE esm_request (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_key       VARCHAR(20)  NOT NULL UNIQUE,   -- ESM-YYYY-####
    catalog_item_id  BIGINT       NOT NULL REFERENCES esm_catalog_item (id),
    requester_id     BIGINT       NOT NULL REFERENCES app_user (id),
    assignee_id      BIGINT       REFERENCES app_user (id),
    department       VARCHAR(20)  NOT NULL,   -- 카탈로그 항목에서 복제(필터 성능용)
    target_user_name VARCHAR(100),
    checklist_id     BIGINT       UNIQUE REFERENCES esm_checklist (id),
    status           VARCHAR(15)  NOT NULL DEFAULT 'SUBMITTED',
    created_by       VARCHAR(100) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by       VARCHAR(100),
    updated_at       TIMESTAMPTZ,
    is_deleted       BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_esm_request_department CHECK (department IN ('HR', 'LEGAL', 'FACILITIES', 'FINANCE')),
    CONSTRAINT ck_esm_request_status CHECK (status IN ('SUBMITTED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'))
);

-- ── esm_request_form_value : 요청 양식 입력 값(EAV) ────────────────
CREATE TABLE esm_request_form_value (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    esm_request_id BIGINT       NOT NULL REFERENCES esm_request (id),
    field_key      VARCHAR(50)  NOT NULL,
    field_value    TEXT,
    created_by     VARCHAR(100) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by     VARCHAR(100),
    updated_at     TIMESTAMPTZ,
    is_deleted     BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_esm_request_form_value UNIQUE (esm_request_id, field_key)
);

-- ── esm_hr_case : HR 케이스(민감 정보, HR_CASE_MANAGER 전용 애플리케이션 레벨 강제) ──
CREATE TABLE esm_hr_case (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title              VARCHAR(200) NOT NULL,
    description        TEXT,
    subject_user_name  VARCHAR(100),
    status             VARCHAR(15)  NOT NULL DEFAULT 'INTAKE',
    created_by         VARCHAR(100) NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by         VARCHAR(100),
    updated_at         TIMESTAMPTZ,
    is_deleted         BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_esm_hr_case_status CHECK (status IN ('INTAKE', 'DOCUMENTATION', 'INVESTIGATION', 'RESOLUTION'))
);

-- ── 조회 인덱스 ───────────────────────────────────────────────────
CREATE INDEX idx_esm_catalog_item_department          ON esm_catalog_item (department);
CREATE INDEX idx_esm_catalog_form_field_item           ON esm_catalog_form_field (catalog_item_id);
CREATE INDEX idx_esm_checklist_template_task_item      ON esm_checklist_template_task (catalog_item_id);
CREATE INDEX idx_esm_checklist_task_checklist          ON esm_checklist_task (checklist_id);
CREATE INDEX idx_esm_checklist_task_department         ON esm_checklist_task (department);
CREATE INDEX idx_esm_request_requester                 ON esm_request (requester_id);
CREATE INDEX idx_esm_request_assignee                  ON esm_request (assignee_id);
CREATE INDEX idx_esm_request_department                ON esm_request (department);
CREATE INDEX idx_esm_request_status                    ON esm_request (status);
CREATE INDEX idx_esm_request_form_value_request        ON esm_request_form_value (esm_request_id);
CREATE INDEX idx_esm_hr_case_status                    ON esm_hr_case (status);
CREATE INDEX idx_app_user_department                   ON app_user (department);
