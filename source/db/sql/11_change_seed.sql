-- =====================================================================
-- ITSM — change 도메인 시드 (표준 변경 템플릿 + RBAC 증분)
-- 근거: docs/02_plan/screen/change.md, docs/02_plan/database/change.md,
--       security/authorization/change_manager.md, security/authorization/approver.md
--       (CHANGE_MANAGER/APPROVER 역할은 auth 단계(02_seed.sql)에 이미 정의됨 → 재사용)
-- 트랜잭션 데이터(change_request 등)는 seed 안 함(테스트/API로 생성).
-- =====================================================================

-- ── 1. change_template : 표준 변경 사전승인 템플릿 예시 ────────────
INSERT INTO change_template (name, description, created_by) VALUES
  ('표준 패치 배포', '정기 보안·기능 패치 배포용 사전승인 표준 변경 템플릿', 'SYSTEM');

-- ── 2. screen 증분 : SCR-CHG-001~006 ──────────────────────────────
INSERT INTO screen (screen_code, screen_name, path, domain, created_by) VALUES
  ('SCR-CHG-001', '변경 목록',           '/changes',            'change', 'SYSTEM'),
  ('SCR-CHG-002', '변경 요청(RFC) 생성', '/changes/new',        'change', 'SYSTEM'),
  ('SCR-CHG-003', '변경 상세',           '/changes/:id',        'change', 'SYSTEM'),
  ('SCR-CHG-004', 'CAB 승인 대기함',     '/approvals/changes',  'change', 'SYSTEM'),
  ('SCR-CHG-005', '변경 일정(캘린더)',   '/changes/schedule',   'change', 'SYSTEM'),
  ('SCR-CHG-006', '변경 지표 대시보드',  '/changes/metrics',    'change', 'SYSTEM');

-- ── 3. screen_role 증분 : 역할정의서 "접근 가능 화면" 기준 ────────
--   CHANGE_MANAGER: 목록·생성·상세·일정·지표(001/002/003/005/006).
--   APPROVER: CAB 승인 대기함(004) + 변경 상세 읽기(003).
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM (VALUES
  ('CHANGE_MANAGER', 'SCR-CHG-001'),
  ('CHANGE_MANAGER', 'SCR-CHG-002'),
  ('CHANGE_MANAGER', 'SCR-CHG-003'),
  ('CHANGE_MANAGER', 'SCR-CHG-005'),
  ('CHANGE_MANAGER', 'SCR-CHG-006'),
  ('APPROVER',       'SCR-CHG-004'),
  ('APPROVER',       'SCR-CHG-003')
) AS m(role_code, screen_code)
JOIN role r   ON r.role_code   = m.role_code
JOIN screen s ON s.screen_code = m.screen_code;

-- ── change RBAC 테스트 유저 (로컬 테스트 재현성) ──────────────────
--   password : Admin@1234  (02_seed 의 admin 과 동일한 BCrypt 해시 재사용)
--   FE E2E·통합테스트에서 CHANGE_MANAGER/APPROVER(CAB) 권한 계정 필요.
--   APPROVER는 srm 단계에서 이미 역할 정의됨 → 신규 계정만 추가(신규 role 아님).
INSERT INTO app_user (email, password_hash, name, status, created_by) VALUES
  ('cm@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '변경 관리자', 'ACTIVE', 'SYSTEM'),
  ('cab@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   'CAB 승인자', 'ACTIVE', 'SYSTEM');

INSERT INTO user_role (user_id, role_id, created_by)
SELECT u.id, r.id, 'SYSTEM'
FROM (VALUES
  ('cm@itsm.local',  'CHANGE_MANAGER'),
  ('cab@itsm.local', 'APPROVER')
) AS m(email, role_code)
JOIN app_user u ON u.email     = m.email
JOIN role r     ON r.role_code = m.role_code;
