-- =====================================================================
-- ITSM — auth 도메인 증분: screen 사이드바 메뉴 i18n(유지보수 요청, 2026-07-13)
-- 근거: docs/02_plan/database/auth.md 5절 "i18n 미적용 결함 수정"
-- 신규 테이블 없음 — 기존 screen에 영문 메뉴명/그룹명 컬럼 추가(이중언어 방식).
-- =====================================================================

-- ── 1. screen : 영문 컬럼 추가(백필 전이라 잠정 NULL 허용) ──────────
ALTER TABLE screen
  ADD COLUMN screen_name_en VARCHAR(100),
  ADD COLUMN group_label_en VARCHAR(50);

-- ── 2. screen_name_en 백필 : screen_name(한국어) 번역값 ────────────
UPDATE screen SET screen_name_en = v.name_en FROM (VALUES
  ('SCR-AUTH-001',  'Login'),
  ('SCR-AUTH-002',  'My Profile'),
  ('SCR-AUTH-003',  'Change Password'),
  ('SCR-ADMIN-001', 'User List'),
  ('SCR-ADMIN-002', 'Create User'),
  ('SCR-ADMIN-003', 'User Detail / Edit'),
  ('SCR-ADMIN-004', 'Role Management'),
  ('SCR-ADMIN-005', 'Audit Log'),
  ('SCR-ADMIN-006', 'Menu Management'),
  ('SCR-ADMIN-007', 'Approval Process List'),
  ('SCR-ADMIN-008', 'Create/Edit Approval Process'),
  ('SCR-COM-001',   'App Shell Layout'),
  ('SCR-COM-002',   'Global Header'),
  ('SCR-COM-003',   'Sidebar Navigation'),
  ('SCR-COM-004',   'Footer'),
  ('SCR-COM-005',   'Auth Guard / 401 Redirect'),
  ('SCR-COM-006',   '403 Access Denied'),
  ('SCR-COM-007',   'Common Ticket List/Filter Pattern'),
  ('SCR-COM-008',   'Common Ticket Detail Pattern'),
  ('SCR-COM-009',   'Toast / Confirm Dialog'),
  ('SCR-COM-010',   'Theme Toggle (Light/Dark)'),
  ('SCR-COM-011',   'Unified Search Results'),
  ('SCR-COM-012',   'User Guide'),
  ('SCR-COM-013',   'Dashboard'),
  ('SCR-COM-014',   'Approval Inbox (All Domains)'),
  ('SCR-ERR-404',   '404 Not Found'),
  ('SCR-SRM-001',   'Service Portal'),
  ('SCR-SRM-002',   'Submit Request'),
  ('SCR-SRM-003',   'My Requests'),
  ('SCR-SRM-004',   'Request Queue (Agent)'),
  ('SCR-SRM-005',   'Request Detail'),
  ('SCR-SRM-007',   'Service Catalog Management'),
  ('SCR-SRM-008',   'Request Metrics Dashboard'),
  ('SCR-INC-001',   'Incident List'),
  ('SCR-INC-002',   'Create Incident'),
  ('SCR-INC-003',   'Incident Detail'),
  ('SCR-INC-004',   'Postmortem Edit'),
  ('SCR-INC-005',   'Incident Metrics Dashboard'),
  ('SCR-PRB-001',   'Problem List'),
  ('SCR-PRB-002',   'Create Problem'),
  ('SCR-PRB-003',   'Problem Detail'),
  ('SCR-PRB-004',   'KEDB Search'),
  ('SCR-CHG-001',   'Change List'),
  ('SCR-CHG-002',   'Create Change Request (RFC)'),
  ('SCR-CHG-003',   'Change Detail'),
  ('SCR-CHG-005',   'Change Schedule (Calendar)'),
  ('SCR-CHG-006',   'Change Metrics Dashboard'),
  ('SCR-KM-001',    'Knowledge Base Search/List'),
  ('SCR-KM-002',    'View Article'),
  ('SCR-KM-003',    'Write/Edit Article'),
  ('SCR-KM-005',    'Knowledge Metrics Dashboard'),
  ('SCR-ITAM-001',  'Asset List'),
  ('SCR-ITAM-002',  'Create/Edit Asset'),
  ('SCR-ITAM-003',  'Asset Detail'),
  ('SCR-ITAM-004',  'CI/CMDB Relationship View'),
  ('SCR-ITAM-005',  'Asset Metrics Dashboard'),
  ('SCR-ESM-001',   'Department Service Portal (Catalog Browse)'),
  ('SCR-ESM-002',   'Submit Department Request (Dynamic Form)'),
  ('SCR-ESM-003',   'My Department Requests'),
  ('SCR-ESM-004',   'Department Request Processing Queue'),
  ('SCR-ESM-005',   'Department Request Detail'),
  ('SCR-ESM-006',   'Department Catalog Management'),
  ('SCR-ESM-007',   'HR Case List'),
  ('SCR-ESM-008',   'HR Case Detail'),
  ('SCR-ESM-009',   'Onboarding/Offboarding Checklist Detail'),
  ('SCR-ESM-010',   'My Subtasks'),
  ('SCR-ESM-011',   'ESM Metrics Dashboard'),
  ('SCR-VULN-001',  'Vulnerability List'),
  ('SCR-VULN-002',  'Register Vulnerability'),
  ('SCR-VULN-003',  'Vulnerability Detail'),
  ('SCR-VULN-004',  'Vulnerability Metrics Dashboard'),
  ('SCR-COMP-001',  'Compliance Requirement List'),
  ('SCR-COMP-002',  'Register Requirement'),
  ('SCR-COMP-003',  'Requirement Detail'),
  ('SCR-COMP-004',  'Compliance Status Dashboard'),
  ('SCR-IOM-001',   'Register Infra Metric'),
  ('SCR-IOM-002',   'Metrics Dashboard'),
  ('SCR-IOM-003',   'Threshold Settings / Alert List'),
  ('SCR-IOM-004',   'Capacity Plan Management'),
  ('SCR-IOM-005',   'Infra Metrics Reporting')
) AS v(screen_code, name_en)
WHERE screen.screen_code = v.screen_code;

-- 백필 누락 검증(신규 screen_code 추가 후 이 파일을 잊고 못 채운 경우 마이그레이션 중단)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM screen WHERE screen_name_en IS NULL) THEN
    RAISE EXCEPTION 'screen_name_en 백필 누락: 30_auth_screen_i18n.sql 갱신 필요';
  END IF;
END $$;

-- ── 3. group_label_en 백필 : group_code(24_auth_menu_columns 기준) 매핑 ──
UPDATE screen SET group_label_en = CASE group_code
  WHEN 'srm'         THEN 'Service Requests'
  WHEN 'inc'         THEN 'Incidents'
  WHEN 'prb'         THEN 'Problems'
  WHEN 'chg'         THEN 'Changes'
  WHEN 'km'          THEN 'Knowledge'
  WHEN 'itam'        THEN 'Assets'
  WHEN 'esm'         THEN 'Department Services'
  WHEN 'hr'          THEN 'HR Cases'
  WHEN 'vuln'        THEN 'Vulnerabilities'
  WHEN 'compliance'  THEN 'Compliance'
  WHEN 'iom'         THEN 'Infrastructure Monitoring'
  WHEN 'admin'       THEN 'Admin'
  ELSE NULL
END
WHERE group_code IS NOT NULL;

-- ── 4. screen_name_en NOT NULL 제약(설계자 확정, auth.md 5절) ──────
ALTER TABLE screen ALTER COLUMN screen_name_en SET NOT NULL;
