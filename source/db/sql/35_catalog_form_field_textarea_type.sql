-- =====================================================================
-- ITSM — service-request/esm 증분: 동적 양식 필드 유형에 textarea 추가
-- (유지보수 요청 2026-07-16)
-- 단일 원천: docs/02_plan/database/service-request.md v0.4
-- =====================================================================

-- ── catalog_form_field.field_type : textarea 값 허용 ──────────────
ALTER TABLE catalog_form_field
    DROP CONSTRAINT ck_catalog_form_field_type;
ALTER TABLE catalog_form_field
    ADD CONSTRAINT ck_catalog_form_field_type
        CHECK (field_type IN ('text', 'textarea', 'select', 'number', 'date', 'file'));

-- ── esm_catalog_form_field.field_type : textarea 값 허용 ──────────
ALTER TABLE esm_catalog_form_field
    DROP CONSTRAINT ck_esm_catalog_form_field_type;
ALTER TABLE esm_catalog_form_field
    ADD CONSTRAINT ck_esm_catalog_form_field_type
        CHECK (field_type IN ('text', 'textarea', 'select', 'number', 'date', 'file'));
