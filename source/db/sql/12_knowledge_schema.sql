-- =====================================================================
-- ITSM — knowledge 도메인 스키마
-- 단일 원천: docs/02_plan/database/knowledge.md
-- 지식 기사·카테고리/라벨·유용성 피드백·검토 이력·검색 로그(무결과 지표).
-- KCS 티켓 연계는 common.ticket_link(target_type='KNOWLEDGE') 재사용(신규 아님).
-- 공통 컬럼 규칙 준수(id, created_by/at, updated_by/at, is_deleted).
--   (search_log 는 append-only 성격이라 updated_*/is_deleted 미사용)
-- =====================================================================

-- ── knowledge_category : 기사 카테고리 ────────────────────────────
CREATE TABLE knowledge_category (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── knowledge_label : 라벨 ─────────────────────────────────────────
CREATE TABLE knowledge_label (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── knowledge_article : 지식 기사 ─────────────────────────────────
CREATE TABLE knowledge_article (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title             VARCHAR(300) NOT NULL,
    body              TEXT         NOT NULL,
    status            VARCHAR(15)  NOT NULL DEFAULT 'DRAFT',
    category_id       BIGINT       REFERENCES knowledge_category (id),
    author_id         BIGINT       NOT NULL REFERENCES app_user (id),
    helpful_count     INT          NOT NULL DEFAULT 0,
    not_helpful_count INT          NOT NULL DEFAULT 0,
    view_count        INT          NOT NULL DEFAULT 0,
    published_at      TIMESTAMPTZ,
    created_by        VARCHAR(100) NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by        VARCHAR(100),
    updated_at        TIMESTAMPTZ,
    is_deleted        BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_knowledge_article_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'PUBLISHED'))
);

-- ── article_label : 기사-라벨 매핑 ─────────────────────────────────
CREATE TABLE article_label (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    article_id  BIGINT       NOT NULL REFERENCES knowledge_article (id),
    label_id    BIGINT       NOT NULL REFERENCES knowledge_label (id),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT uq_article_label UNIQUE (article_id, label_id)
);

-- ── knowledge_feedback : 유용성 평가 ───────────────────────────────
CREATE TABLE knowledge_feedback (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    article_id  BIGINT       NOT NULL REFERENCES knowledge_article (id),
    user_id     BIGINT       NOT NULL REFERENCES app_user (id),
    helpful     BOOLEAN      NOT NULL,
    comment     VARCHAR(500),
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── knowledge_review : 검토·게시 이력 ──────────────────────────────
CREATE TABLE knowledge_review (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    article_id     BIGINT       NOT NULL REFERENCES knowledge_article (id),
    gatekeeper_id  BIGINT       NOT NULL REFERENCES app_user (id),
    decision       VARCHAR(10)  NOT NULL,
    reason         VARCHAR(500),
    created_by     VARCHAR(100) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by     VARCHAR(100),
    updated_at     TIMESTAMPTZ,
    is_deleted     BOOLEAN      NOT NULL DEFAULT false,
    CONSTRAINT ck_knowledge_review_decision CHECK (decision IN ('APPROVE', 'REJECT'))
);

-- ── search_log : 검색 로그(무결과 지표), append-only ───────────────
CREATE TABLE search_log (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    keyword      VARCHAR(255) NOT NULL,
    result_count INT          NOT NULL,
    user_id      BIGINT       REFERENCES app_user (id),
    searched_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(100) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── 조회 인덱스 ───────────────────────────────────────────────────
CREATE INDEX idx_knowledge_article_status    ON knowledge_article (status);
CREATE INDEX idx_knowledge_article_category  ON knowledge_article (category_id);
CREATE INDEX idx_knowledge_article_author    ON knowledge_article (author_id);
CREATE INDEX idx_article_label_article       ON article_label (article_id);
CREATE INDEX idx_article_label_label         ON article_label (label_id);
CREATE INDEX idx_knowledge_feedback_article  ON knowledge_feedback (article_id);
CREATE INDEX idx_knowledge_review_article    ON knowledge_review (article_id);
CREATE INDEX idx_search_log_keyword          ON search_log (keyword);
CREATE INDEX idx_search_log_result_count     ON search_log (result_count);
