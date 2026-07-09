-- =====================================================================
-- ITSM — asset 도메인 시드 (RBAC 증분, 7/7 마지막 도메인)
-- 근거: docs/02_plan/screen/asset.md, docs/02_plan/database/asset.md,
--       security/authorization/asset_manager.md
--       (ASSET_MANAGER 역할은 auth 단계(02_seed.sql)에 이미 정의됨 → 재사용)
-- 트랜잭션 데이터(asset/configuration_item 등)는 기존 도메인과 동일하게
--   seed 안 함(테스트/API로 생성). 유형별 예시 속성(EAV) 시드는 선택사항이며,
--   이 원칙(트랜잭션 데이터 미시드)에 맞춰 생략한다.
-- =====================================================================

-- ── 1. screen 증분 : SCR-ITAM-001~005 ─────────────────────────────
INSERT INTO screen (screen_code, screen_name, path, domain, created_by) VALUES
  ('SCR-ITAM-001', '자산 목록',            '/assets',         'asset', 'SYSTEM'),
  ('SCR-ITAM-002', '자산 등록/수정',        '/assets/new',     'asset', 'SYSTEM'),
  ('SCR-ITAM-003', '자산 상세',            '/assets/:id',     'asset', 'SYSTEM'),
  ('SCR-ITAM-004', 'CI·CMDB 관계 뷰',      '/cis',            'asset', 'SYSTEM'),
  ('SCR-ITAM-005', '자산 지표 대시보드',    '/assets/metrics', 'asset', 'SYSTEM');

-- ── 2. screen_role 증분 : 역할정의서 "접근 가능 화면" 기준 ────────
--   ASSET_MANAGER: 목록·등록/수정·상세·CI관계뷰·지표 전부(001~005).
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM (VALUES
  ('ASSET_MANAGER', 'SCR-ITAM-001'),
  ('ASSET_MANAGER', 'SCR-ITAM-002'),
  ('ASSET_MANAGER', 'SCR-ITAM-003'),
  ('ASSET_MANAGER', 'SCR-ITAM-004'),
  ('ASSET_MANAGER', 'SCR-ITAM-005')
) AS m(role_code, screen_code)
JOIN role r   ON r.role_code   = m.role_code
JOIN screen s ON s.screen_code = m.screen_code;

-- ── asset RBAC 테스트 유저 (로컬 테스트 재현성) ───────────────────
--   password : Admin@1234  (02_seed 의 admin 과 동일한 BCrypt 해시 재사용)
--   FE E2E·통합테스트에서 ASSET_MANAGER 권한 계정 필요.
INSERT INTO app_user (email, password_hash, name, status, created_by) VALUES
  ('am@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '자산 관리자', 'ACTIVE', 'SYSTEM');

INSERT INTO user_role (user_id, role_id, created_by)
SELECT u.id, r.id, 'SYSTEM'
FROM (VALUES
  ('am@itsm.local', 'ASSET_MANAGER')
) AS m(email, role_code)
JOIN app_user u ON u.email     = m.email
JOIN role r     ON r.role_code = m.role_code;
