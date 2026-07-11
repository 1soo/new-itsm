-- =====================================================================
-- ITSM — 공용 승인 프로세스 엔진 시드(유지보수 요청 2026-07-11)
-- 근거: docs/02_plan/screen/admin.md(SCR-ADMIN-007/008), screen/common.md(SCR-COM-014),
--       docs/02_plan/screen/service-request.md·change.md(SCR-SRM-006/SCR-CHG-004 제거)
-- 경로·아이콘·그룹·정렬 값은 developer-fe-2 확정치 반영(2026-07-11).
-- 신규 role 없음(SYSTEM_ADMIN 기존 역할 재사용). approval_process* 트랜잭션 데이터는
-- seed 안 함(SYSTEM_ADMIN이 SCR-ADMIN-008에서 직접 구성).
-- =====================================================================

-- ── 1. 기존 SCR-SRM-006(승인 대기함) 제거 : SCR-COM-014로 완전 대체 ───
DELETE FROM screen_role WHERE screen_id = (SELECT id FROM screen WHERE screen_code = 'SCR-SRM-006');
DELETE FROM screen WHERE screen_code = 'SCR-SRM-006';

-- ── 2. 기존 SCR-CHG-004(CAB 승인 대기함) 제거 : SCR-COM-014로 완전 대체 ─
--   (공유 approval 테이블 제거에 따라 구 CHANGE 승인 API·화면도 함께 폐기, dev-lead 확정)
DELETE FROM screen_role WHERE screen_id = (SELECT id FROM screen WHERE screen_code = 'SCR-CHG-004');
DELETE FROM screen WHERE screen_code = 'SCR-CHG-004';

-- ── 3. 신규 screen : SCR-ADMIN-007/008(승인 프로세스 관리), SCR-COM-014(공용 승인 대기함) ─
INSERT INTO screen
  (screen_code, screen_name, path, domain, icon_name, group_code, group_label, sort_order, nav_visible, created_by)
VALUES
  ('SCR-ADMIN-007', '승인 프로세스 목록',     '/admin/approval-processes',     'admin',  'Workflow', 'admin', '관리자', 430, true,  'SYSTEM'),
  ('SCR-ADMIN-008', '승인 프로세스 생성/편집', '/admin/approval-processes/new', 'admin',  NULL,       NULL,    NULL,     0,   false, 'SYSTEM'),
  ('SCR-COM-014',   '승인 대기함(전 도메인 공용)', '/approvals',                'common', 'Stamp',    NULL,    NULL,     5,   true,  'SYSTEM');

-- ── 4. screen_role : SCR-ADMIN-007/008 → SYSTEM_ADMIN 전용 ────────────
--   SCR-COM-014는 매핑을 두지 않는다(API-AUTH-022 규칙: 무매핑=전체 인증 사용자 공개).
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM screen s
JOIN role r ON r.role_code = 'SYSTEM_ADMIN'
WHERE s.screen_code IN ('SCR-ADMIN-007', 'SCR-ADMIN-008');
