-- =====================================================================
-- ITSM — service-request 증분: label 태그 개편(4차) — 기존 label 배치 로우 조건부 리셋
-- (유지보수 요청 2026-07-18, 4차)
-- 단일 원천: docs/02_plan/database/service-request.md "기존 데이터 리셋
-- (2026-07-18 후속 유지보수 요청 4차, label 태그 개편)"
-- 그리드에 직접 배치하던 type:"label" 컴포넌트 폐기 → 최상위 labels 배열
-- (컴포넌트별 labelId 참조)로 대체. type:"label" 컴포넌트를 하나라도
-- 포함한 로우만 조건부 리셋(전체 블랭킷 리셋 아님, 사용자 확답 완료).
-- =====================================================================

-- ── 조건부 리셋 : components 배열에 type="label"이 하나라도 있는 로우만 ──
UPDATE service_catalog_item
SET form_schema = '{"components":[],"labels":[]}'::jsonb
WHERE EXISTS (
    SELECT 1
    FROM jsonb_array_elements(COALESCE(form_schema -> 'components', '[]'::jsonb)) AS c
    WHERE c ->> 'type' = 'label'
);

-- ── 기본값 정정 : 38번이 설정한 구 기본값({components}) → labels 배열 추가 ──
ALTER TABLE service_catalog_item
    ALTER COLUMN form_schema SET DEFAULT '{"components":[],"labels":[]}';
