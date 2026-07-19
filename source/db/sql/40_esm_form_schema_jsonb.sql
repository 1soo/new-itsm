-- =====================================================================
-- ITSM — esm 증분: esm_catalog_form_field/esm_request_form_value(EAV) 폐기
-- → esm_catalog_item.form_schema/esm_request.form_values(JSONB) 전환
-- (유지보수 요청 2026-07-19)
-- 단일 원천: docs/02_plan/database/esm.md 4절
-- SRM 36_srm_form_schema_jsonb.sql(JSONB 신규+백필)·38_srm_form_schema_reset.sql
-- (호환 불가 시 리셋) 선례 재사용. esm_catalog_form_field(EAV)는 8×n 그리드
-- 스키마와 구조가 호환되지 않아 form_schema는 백필 없이 빈 그리드로 신규 추가,
-- esm_request_form_value(EAV)는 key-value 맵으로 호환 가능하므로 form_values로
-- 백필한다(사용자 결정, docs/02_plan/database/esm.md "기존 데이터 리셋"/"백필" 노트).
-- =====================================================================

-- ── esm_catalog_item.form_schema : 동적 양식 스키마(JSONB, 빈 그리드로 신규) ──
ALTER TABLE esm_catalog_item
    ADD COLUMN form_schema JSONB NOT NULL DEFAULT '{"components":[],"labels":[]}';

-- ── esm_request.form_values : 양식 제출 데이터(JSONB) ─────────────
ALTER TABLE esm_request
    ADD COLUMN form_values JSONB NOT NULL DEFAULT '{}';

-- ── 백필 : esm_request_form_value(EAV) → form_values({field_key:field_value}) ──
WITH assembled AS (
    SELECT esm_request_id, jsonb_object_agg(field_key, to_jsonb(field_value)) AS values
    FROM esm_request_form_value
    WHERE is_deleted = false
    GROUP BY esm_request_id
)
UPDATE esm_request er
SET form_values = a.values
FROM assembled a
WHERE er.id = a.esm_request_id;

-- ── EAV 테이블 폐기 ────────────────────────────────────────────────
DROP TABLE esm_catalog_form_field;
DROP TABLE esm_request_form_value;
