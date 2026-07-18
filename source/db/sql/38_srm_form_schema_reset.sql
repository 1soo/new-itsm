-- =====================================================================
-- ITSM — service-request 증분: form.io 완전 제거 → 자체 8×n 그리드 폼 빌더
-- (유지보수 요청 2026-07-18)
-- 단일 원천: docs/02_plan/database/service-request.md "기존 데이터 리셋" 노트
-- form.io Form JSON({display,components})과 신규 그리드 스키마는 구조가
-- 호환되지 않아 자동 마이그레이션 불가. 전체 로우를 빈 그리드로 리셋(사용자 승인 완료).
-- =====================================================================

-- ── 리셋 : 기존 form.io 스키마 → 빈 그리드 ───────────────────────────
UPDATE service_catalog_item
SET form_schema = '{"components":[]}'::jsonb;

-- ── 기본값 정정 : 36번이 설정한 구 기본값({display,components}) 제거 ──
ALTER TABLE service_catalog_item
    ALTER COLUMN form_schema SET DEFAULT '{"components":[]}';
