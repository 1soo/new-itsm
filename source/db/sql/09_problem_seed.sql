-- =====================================================================
-- ITSM — problem 도메인 시드 (RBAC 증분)
-- 근거: docs/02_plan/screen/problem.md, security/authorization/problem_manager.md
--       (PROBLEM_MANAGER: SCR-PRB-001~004 전부 접근)
-- 트랜잭션 데이터(problem 등)는 seed 안 함(테스트/API로 생성).
-- =====================================================================

-- ── screen 증분 : SCR-PRB-001~004 ─────────────────────────────────
INSERT INTO screen (screen_code, screen_name, path, domain, created_by) VALUES
  ('SCR-PRB-001', '문제 목록',  '/problems',      'problem', 'SYSTEM'),
  ('SCR-PRB-002', '문제 등록',  '/problems/new',  'problem', 'SYSTEM'),
  ('SCR-PRB-003', '문제 상세',  '/problems/:id',  'problem', 'SYSTEM'),
  ('SCR-PRB-004', 'KEDB 검색',  '/known-errors',  'problem', 'SYSTEM');

-- ── screen_role 증분 : 역할정의서 "접근 가능 화면" 기준 ───────────
--   PROBLEM_MANAGER: 문제 목록·등록·상세·KEDB 검색 전부.
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM (VALUES
  ('PROBLEM_MANAGER', 'SCR-PRB-001'),
  ('PROBLEM_MANAGER', 'SCR-PRB-002'),
  ('PROBLEM_MANAGER', 'SCR-PRB-003'),
  ('PROBLEM_MANAGER', 'SCR-PRB-004')
) AS m(role_code, screen_code)
JOIN role r   ON r.role_code   = m.role_code
JOIN screen s ON s.screen_code = m.screen_code;

-- ── problem RBAC 테스트 유저 (로컬 테스트 재현성) ─────────────────
--   password : Admin@1234  (02_seed 의 admin 과 동일한 BCrypt 해시 재사용)
--   FE E2E·통합테스트에서 PROBLEM_MANAGER 권한 계정 필요.
INSERT INTO app_user (email, password_hash, name, status, created_by) VALUES
  ('pm@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '문제 관리자', 'ACTIVE', 'SYSTEM');

INSERT INTO user_role (user_id, role_id, created_by)
SELECT u.id, r.id, 'SYSTEM'
FROM (VALUES
  ('pm@itsm.local', 'PROBLEM_MANAGER')
) AS m(email, role_code)
JOIN app_user u ON u.email     = m.email
JOIN role r     ON r.role_code = m.role_code;
