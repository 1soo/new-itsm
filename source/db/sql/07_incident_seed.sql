-- =====================================================================
-- ITSM — incident 도메인 시드 (RBAC 증분)
-- 근거: docs/02_plan/screen/incident.md, security/authorization/*.md
--       (SERVICE_DESK_AGENT / INCIDENT_MANAGER)
-- 트랜잭션 데이터(incident 등)는 seed 안 함(테스트/API로 생성).
-- =====================================================================

-- ── screen 증분 : SCR-INC-001~005 ─────────────────────────────────
INSERT INTO screen (screen_code, screen_name, path, domain, created_by) VALUES
  ('SCR-INC-001', '인시던트 목록',        '/incidents',                'incident', 'SYSTEM'),
  ('SCR-INC-002', '인시던트 등록',        '/incidents/new',            'incident', 'SYSTEM'),
  ('SCR-INC-003', '인시던트 상세',        '/incidents/:id',            'incident', 'SYSTEM'),
  ('SCR-INC-004', '포스트모템 편집',       '/incidents/:id/postmortem', 'incident', 'SYSTEM'),
  ('SCR-INC-005', '인시던트 지표 대시보드', '/incidents/metrics',        'incident', 'SYSTEM');

-- ── screen_role 증분 : 역할정의서 "접근 가능 화면" 기준 ───────────
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM (VALUES
  -- SERVICE_DESK_AGENT: 목록·등록·상세 (접수/에스컬레이션)
  ('SERVICE_DESK_AGENT', 'SCR-INC-001'),
  ('SERVICE_DESK_AGENT', 'SCR-INC-002'),
  ('SERVICE_DESK_AGENT', 'SCR-INC-003'),
  -- INCIDENT_MANAGER: 목록·등록·상세·포스트모템·지표
  ('INCIDENT_MANAGER',   'SCR-INC-001'),
  ('INCIDENT_MANAGER',   'SCR-INC-002'),
  ('INCIDENT_MANAGER',   'SCR-INC-003'),
  ('INCIDENT_MANAGER',   'SCR-INC-004'),
  ('INCIDENT_MANAGER',   'SCR-INC-005')
) AS m(role_code, screen_code)
JOIN role r   ON r.role_code   = m.role_code
JOIN screen s ON s.screen_code = m.screen_code;

-- ── incident RBAC 테스트 유저 (로컬 테스트 재현성) ────────────────
--   password : Admin@1234  (02_seed 의 admin 과 동일한 BCrypt 해시 재사용)
--   FE E2E·통합테스트에서 INCIDENT_MANAGER / SERVICE_DESK_AGENT 권한 계정 필요.
INSERT INTO app_user (email, password_hash, name, status, created_by) VALUES
  ('im@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '인시던트 매니저', 'ACTIVE', 'SYSTEM'),
  ('agent@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '서비스데스크 담당자', 'ACTIVE', 'SYSTEM');

INSERT INTO user_role (user_id, role_id, created_by)
SELECT u.id, r.id, 'SYSTEM'
FROM (VALUES
  ('im@itsm.local',    'INCIDENT_MANAGER'),
  ('agent@itsm.local', 'SERVICE_DESK_AGENT')
) AS m(email, role_code)
JOIN app_user u ON u.email     = m.email
JOIN role r     ON r.role_code = m.role_code;
