-- =====================================================================
-- ITSM — knowledge 도메인 시드 (카테고리 + RBAC 증분)
-- 근거: docs/02_plan/screen/knowledge.md, docs/02_plan/database/knowledge.md,
--       security/authorization/knowledge_gatekeeper.md, knowledge_contributor.md
--       (KNOWLEDGE_CONTRIBUTOR/KNOWLEDGE_GATEKEEPER 역할은 auth 단계(02_seed.sql)에
--        이미 정의됨 → 재사용)
-- 트랜잭션 데이터(knowledge_article 등)는 seed 안 함(테스트/API로 생성).
-- =====================================================================

-- ── 1. knowledge_category : 카테고리 예시 ─────────────────────────
INSERT INTO knowledge_category (name, created_by) VALUES
  ('네트워크',    'SYSTEM'),
  ('계정/권한',   'SYSTEM'),
  ('결제',       'SYSTEM');

-- ── 2. screen 증분 : SCR-KM-001~005 ───────────────────────────────
INSERT INTO screen (screen_code, screen_name, path, domain, created_by) VALUES
  ('SCR-KM-001', '지식베이스 검색/목록',    '/knowledge',            'knowledge', 'SYSTEM'),
  ('SCR-KM-002', '기사 열람',              '/knowledge/:id',        'knowledge', 'SYSTEM'),
  ('SCR-KM-003', '기사 작성·편집',          '/knowledge/new',        'knowledge', 'SYSTEM'),
  ('SCR-KM-004', '검토·게시 승인함',        '/approvals/knowledge',  'knowledge', 'SYSTEM'),
  ('SCR-KM-005', '지식 지표 대시보드',      '/knowledge/metrics',    'knowledge', 'SYSTEM');

-- ── 3. screen_role 증분 : 역할정의서 "접근 가능 화면" 기준 ────────
--   KNOWLEDGE_CONTRIBUTOR: 검색/목록·열람·작성편집(001/002/003).
--   KNOWLEDGE_GATEKEEPER: 검색/목록·열람·검토승인함·지표(001/002/004/005).
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM (VALUES
  ('KNOWLEDGE_CONTRIBUTOR', 'SCR-KM-001'),
  ('KNOWLEDGE_CONTRIBUTOR', 'SCR-KM-002'),
  ('KNOWLEDGE_CONTRIBUTOR', 'SCR-KM-003'),
  ('KNOWLEDGE_GATEKEEPER',  'SCR-KM-001'),
  ('KNOWLEDGE_GATEKEEPER',  'SCR-KM-002'),
  ('KNOWLEDGE_GATEKEEPER',  'SCR-KM-004'),
  ('KNOWLEDGE_GATEKEEPER',  'SCR-KM-005')
) AS m(role_code, screen_code)
JOIN role r   ON r.role_code   = m.role_code
JOIN screen s ON s.screen_code = m.screen_code;

-- ── knowledge RBAC 테스트 유저 (로컬 테스트 재현성) ───────────────
--   password : Admin@1234  (02_seed 의 admin 과 동일한 BCrypt 해시 재사용)
--   FE E2E·통합테스트에서 KNOWLEDGE_CONTRIBUTOR/KNOWLEDGE_GATEKEEPER 권한 계정 필요.
INSERT INTO app_user (email, password_hash, name, status, created_by) VALUES
  ('kc@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '지식 기여자', 'ACTIVE', 'SYSTEM'),
  ('kg@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '지식 게이트키퍼', 'ACTIVE', 'SYSTEM');

INSERT INTO user_role (user_id, role_id, created_by)
SELECT u.id, r.id, 'SYSTEM'
FROM (VALUES
  ('kc@itsm.local', 'KNOWLEDGE_CONTRIBUTOR'),
  ('kg@itsm.local', 'KNOWLEDGE_GATEKEEPER')
) AS m(email, role_code)
JOIN app_user u ON u.email     = m.email
JOIN role r     ON r.role_code = m.role_code;
