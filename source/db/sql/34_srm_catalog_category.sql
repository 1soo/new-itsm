-- =====================================================================
-- ITSM — service-request 증분: 카탈로그 카테고리 CRUD(유지보수 요청 2026-07-16)
-- 단일 원천: docs/02_plan/database/service-request.md v0.4
-- service_catalog_item.category(자유 텍스트)를 관리자가 통제하는 고정 목록
-- service_catalog_category로 전환한다(3NF, 오타·중복 표현 방지).
-- 원본 04_srm_schema.sql은 수정하지 않고 이 파일에서 ALTER/DROP으로 정리
-- (26/33번 선례와 동일 패턴).
-- =====================================================================

-- ── service_catalog_category : 카탈로그 카테고리 고정 목록 ────────
CREATE TABLE service_catalog_category (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    sort_order  INT          NOT NULL DEFAULT 0,
    created_by  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ,
    is_deleted  BOOLEAN      NOT NULL DEFAULT false
);

-- ── service_catalog_item.category_id : 카테고리 FK ────────────────
ALTER TABLE service_catalog_item
    ADD COLUMN category_id BIGINT NULL REFERENCES service_catalog_category (id);

-- ── 백필 : 기존 category 자유 텍스트 값을 고정 목록으로 승격 ──────
--   기존 값이 없으면(전 카탈로그 항목 category NULL) 아래 두 INSERT/UPDATE는
--   0건 처리되며 에러 없이 스킵된다.
INSERT INTO service_catalog_category (name, sort_order, created_by)
SELECT d.category, (row_number() OVER (ORDER BY d.category) - 1) * 10, 'SYSTEM'
FROM (SELECT DISTINCT category FROM service_catalog_item WHERE category IS NOT NULL) d
ON CONFLICT (name) DO NOTHING;

UPDATE service_catalog_item sci
SET category_id = cat.id
FROM service_catalog_category cat
WHERE sci.category = cat.name
  AND sci.category_id IS NULL;

-- ── 시드 : 예시 카테고리 보강(tester 검증용, 매핑 없는 후보 포함) ──
--   05_srm_seed.sql 데이터 기준 위 백필에서 '하드웨어'/'계정'은 이미
--   생성·매핑되어 있다. '소프트웨어'는 매핑 대상 항목이 없는 예시로 추가.
INSERT INTO service_catalog_category (name, sort_order, created_by)
VALUES ('소프트웨어', 20, 'SYSTEM')
ON CONFLICT (name) DO NOTHING;

-- ── service_catalog_item.category : 백필 완료 후 자유 텍스트 컬럼 제거 ─
--   원본 정의: 04_srm_schema.sql 라인 26.
ALTER TABLE service_catalog_item
    DROP COLUMN category;
