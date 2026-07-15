-- =====================================================================
-- ITSM — service-request 증분: 요청 유형별 담당자 역할 지정(유지보수 요청 2026-07-15)
-- 근거: docs/02_plan/database/service-request.md v0.3
-- =====================================================================

-- ── service_catalog_item.assignee_role_id : 담당자 역할(선택) ─────
--   지정 시 상담원이 라우팅/배정 시점에 이 역할 보유자 후보 목록 중
--   수동으로 담당자를 선택하는 데 사용(자동배정 아님). 미지정이면
--   기존과 동일하게 본인 배정만 가능.
ALTER TABLE service_catalog_item
  ADD COLUMN assignee_role_id BIGINT NULL REFERENCES role (id);

-- ── 시드 : 예시 카탈로그 항목 1건에 담당자 역할 지정 ──────────────
--   '노트북 신청'에 SERVICE_DESK_AGENT 지정, 나머지는 NULL 유지.
UPDATE service_catalog_item
SET assignee_role_id = r.id
FROM role r
WHERE service_catalog_item.name = '노트북 신청'
  AND r.role_code = 'SERVICE_DESK_AGENT';
