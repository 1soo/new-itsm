-- =====================================================================
-- ITSM — service-request 증분: 서비스 카탈로그 커스텀 폼 빌더(form.io 전환)
-- (유지보수 요청 2026-07-17)
-- 단일 원천: docs/02_plan/database/service-request.md v0.5
-- catalog_form_field(EAV) → service_catalog_item.form_schema(JSONB, Form.io Form JSON)
-- service_request_form_value(EAV) → service_request.form_values(JSONB, submission.data)
-- =====================================================================

-- ── service_catalog_item.form_schema : 동적 양식 스키마(JSONB) ────
ALTER TABLE service_catalog_item
    ADD COLUMN form_schema JSONB NOT NULL DEFAULT '{"display":"form","components":[]}';

-- ── 백필 : catalog_form_field(EAV) → form_schema(Form.io Form JSON) ──
--   field_type → type 매핑: text→textfield, textarea→textarea, select→select
--   (options→data.values[].{label,value}), number→number, date→datetime
--   (enableTime:false), file→file(storage:'base64'). 공통 input:true,
--   required→validate.required. sort_order 오름차순으로 components 배열 구성.
WITH field_components AS (
    SELECT
        catalog_item_id,
        sort_order,
        jsonb_build_object(
            'key', field_key,
            'label', label,
            'type', CASE field_type
                        WHEN 'text' THEN 'textfield'
                        WHEN 'textarea' THEN 'textarea'
                        WHEN 'select' THEN 'select'
                        WHEN 'number' THEN 'number'
                        WHEN 'date' THEN 'datetime'
                        WHEN 'file' THEN 'file'
                    END,
            'input', true,
            'validate', jsonb_build_object('required', required)
        )
        || CASE field_type
               WHEN 'select' THEN jsonb_build_object('data', jsonb_build_object('values',
                    (SELECT COALESCE(jsonb_agg(jsonb_build_object('label', opt, 'value', opt)), '[]'::jsonb)
                     FROM jsonb_array_elements_text(COALESCE(options, '[]'::jsonb)) AS opt)))
               WHEN 'date' THEN jsonb_build_object('enableTime', false)
               WHEN 'file' THEN jsonb_build_object('storage', 'base64')
               ELSE '{}'::jsonb
           END AS component
    FROM catalog_form_field
    WHERE is_deleted = false
),
aggregated AS (
    SELECT catalog_item_id, jsonb_agg(component ORDER BY sort_order) AS components
    FROM field_components
    GROUP BY catalog_item_id
)
UPDATE service_catalog_item sci
SET form_schema = jsonb_build_object('display', 'form', 'components', a.components)
FROM aggregated a
WHERE sci.id = a.catalog_item_id;

-- ── service_request.form_values : 양식 제출 데이터(JSONB) ─────────
ALTER TABLE service_request
    ADD COLUMN form_values JSONB NOT NULL DEFAULT '{}';

-- ── 백필 : service_request_form_value(EAV) → form_values({key:value}) ──
WITH assembled AS (
    SELECT service_request_id, jsonb_object_agg(field_key, to_jsonb(field_value)) AS values
    FROM service_request_form_value
    WHERE is_deleted = false
    GROUP BY service_request_id
)
UPDATE service_request sr
SET form_values = a.values
FROM assembled a
WHERE sr.id = a.service_request_id;

-- ── EAV 테이블 폐기 ────────────────────────────────────────────────
DROP TABLE catalog_form_field;
DROP TABLE service_request_form_value;
