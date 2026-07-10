-- =====================================================================
-- ITSM — compliance 도메인 시드 (최소 예시 + RBAC 증분)
-- 근거: docs/02_plan/database/compliance.md, screen/compliance.md,
--       security/authorization/compliance_officer.md
-- =====================================================================

-- ── 1. role : 신규 역할(단일) ──────────────────────────────────────
INSERT INTO role (role_code, role_name, description, created_by) VALUES
  ('COMPLIANCE_OFFICER', '컴플라이언스 담당자', '컴플라이언스 요구사항을 등록·관리하고 책임자 지정, 시정조치 추적, 준수 현황을 조회하는 담당자.', 'SYSTEM');

-- ── 2. screen 증분 : SCR-COMP-001~004 ─────────────────────────────
INSERT INTO screen (screen_code, screen_name, path, domain, created_by) VALUES
  ('SCR-COMP-001', '컴플라이언스 요구사항 목록', '/compliance/requirements',      'compliance', 'SYSTEM'),
  ('SCR-COMP-002', '요구사항 등록',             '/compliance/requirements/new',  'compliance', 'SYSTEM'),
  ('SCR-COMP-003', '요구사항 상세',             '/compliance/requirements/:id',  'compliance', 'SYSTEM'),
  ('SCR-COMP-004', '준수 현황 대시보드',         '/compliance/metrics',           'compliance', 'SYSTEM');

-- ── 3. screen_role 증분 : COMPLIANCE_OFFICER 전 화면 접근 ─────────
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM screen s
CROSS JOIN role r
WHERE s.screen_code IN ('SCR-COMP-001', 'SCR-COMP-002', 'SCR-COMP-003', 'SCR-COMP-004')
AND r.role_code = 'COMPLIANCE_OFFICER';

-- ── 4. screen_role : 신규 역할의 공통 기본 접근 ───────────────────
--   COMPLIANCE_OFFICER는 02_seed.sql 시점에 존재하지 않아 전 역할
--   공통 CROSS JOIN에서 누락됨 — 별도 증분(ESM/VULN과 동일 패턴).
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM screen s
CROSS JOIN role r
WHERE s.screen_code IN (
  'SCR-COM-001', 'SCR-COM-002', 'SCR-COM-003', 'SCR-COM-004', 'SCR-COM-009',
  'SCR-AUTH-002', 'SCR-AUTH-003'
)
AND r.role_code = 'COMPLIANCE_OFFICER';

-- ── compliance RBAC 테스트 유저 (로컬 테스트 재현성) ──────────────
--   password : Admin@1234  (02_seed 의 admin 과 동일한 BCrypt 해시 재사용)
INSERT INTO app_user (email, password_hash, name, status, created_by) VALUES
  ('co@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '컴플라이언스 담당자', 'ACTIVE', 'SYSTEM');

INSERT INTO user_role (user_id, role_id, created_by)
SELECT u.id, r.id, 'SYSTEM'
FROM app_user u, role r
WHERE u.email = 'co@itsm.local' AND r.role_code = 'COMPLIANCE_OFFICER';

-- ── 5. compliance_requirement : 예시 요구사항 3건 ─────────────────
--   0001: 책임자 지정 + 시정조치 없음 → COMPLIANT
--   0002: 책임자 미지정 + 시정조치 미해결(DETECTED) → NON_COMPLIANT
--   0003: 책임자 지정 + 시정조치 전부 RESOLVED → COMPLIANT(해결 이력 존재해도 준수)
INSERT INTO compliance_requirement (requirement_key, name, basis, scope, owner_id, created_by)
SELECT 'COMP-2026-0001', '개인정보보호법 준수 점검', '개인정보보호법 제29조(안전조치의무)', '전사 고객정보 처리 시스템', u.id, 'SYSTEM'
FROM app_user u WHERE u.email = 'co@itsm.local';

INSERT INTO compliance_requirement (requirement_key, name, basis, scope, owner_id, created_by) VALUES
  ('COMP-2026-0002', '정보보안 관리체계(ISMS-P) 인증 유지', 'ISMS-P 인증 심사 기준 2.5 인증 및 권한관리', '전사 정보시스템', NULL, 'SYSTEM');

INSERT INTO compliance_requirement (requirement_key, name, basis, scope, owner_id, created_by)
SELECT 'COMP-2026-0003', '접근 통제 정책 준수', '내부 정보보안 정책 제12조(접근권한 관리)', '사내 그룹웨어·ERP', u.id, 'SYSTEM'
FROM app_user u WHERE u.email = 'co@itsm.local';

-- ── 6. corrective_action : 0002(미해결 DETECTED), 0003(해결 RESOLVED) ──
INSERT INTO corrective_action (requirement_id, description, status, created_by)
SELECT r.id, 'ISMS-P 취약점 점검 결과 미비 항목 보완 조치 필요', 'DETECTED', 'SYSTEM'
FROM compliance_requirement r WHERE r.requirement_key = 'COMP-2026-0002';

INSERT INTO corrective_action (requirement_id, description, status, created_by)
SELECT r.id, '퇴직자 계정 접근권한 회수 조치 완료', 'RESOLVED', 'SYSTEM'
FROM compliance_requirement r WHERE r.requirement_key = 'COMP-2026-0003';

-- ── 7. ticket_link : COMP-2026-0001을 변경 요청 1건과 연계(변경 연계 조회 테스트용) ──
--   change 도메인은 트랜잭션 데이터를 시드하지 않아 change_request 레코드가 없을 수 있으므로,
--   변경 요청이 하나라도 존재할 때만 연계(없으면 스킵, 에러 아님).
INSERT INTO ticket_link (source_type, source_id, target_type, target_id, link_type, created_by)
SELECT 'COMPLIANCE_REQUIREMENT', r.id, 'CHANGE', c.id, 'RELATED', 'SYSTEM'
FROM compliance_requirement r, change_request c
WHERE r.requirement_key = 'COMP-2026-0001'
ORDER BY c.id
LIMIT 1;
