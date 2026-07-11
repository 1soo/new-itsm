-- =====================================================================
-- ITSM — auth 도메인 증분: screen 메뉴화(Role-Menu 동적 매핑, 유지보수 요청 2026-07-11)
-- 근거: docs/02_plan/database/auth.md 5절, docs/03_develop/plan/auth.md
--       "Role-Menu 동적 매핑" 절. 단일 원천: source/frontend/src/routes/navConfig.tsx
-- 신규 테이블 없음 — 기존 screen/screen_role을 메뉴 마스터·역할-메뉴 매핑으로 확장 재사용.
-- =====================================================================

-- ── 1. screen : 사이드바 표시용 컬럼 추가 ──────────────────────────
ALTER TABLE screen
  ADD COLUMN icon_name   VARCHAR(50),
  ADD COLUMN group_code  VARCHAR(30),
  ADD COLUMN group_label VARCHAR(50),
  ADD COLUMN sort_order  INT NOT NULL DEFAULT 0,
  ADD COLUMN nav_visible BOOLEAN NOT NULL DEFAULT true;

-- ── 2. 기존 path 오류 보정 ──────────────────────────────────────────
--   실제 라우트(routes/index.tsx)로 경로가 바뀐 뒤 DB seed에 반영되지 않았던 값 정정.
--   UNIQUE 제약(4절)을 걸기 전에 선행(다른 값과 충돌 없음 확인 완료).
UPDATE screen SET path = '/knowledge/reviews'    WHERE screen_code = 'SCR-KM-004';
UPDATE screen SET path = '/assets/cis'           WHERE screen_code = 'SCR-ITAM-004';
UPDATE screen SET path = '/infra/metrics/new'    WHERE screen_code = 'SCR-IOM-001';
UPDATE screen SET path = '/infra/metrics'        WHERE screen_code = 'SCR-IOM-002';
UPDATE screen SET path = '/infra/thresholds'     WHERE screen_code = 'SCR-IOM-003';
UPDATE screen SET path = '/infra/capacity-plans' WHERE screen_code = 'SCR-IOM-004';
UPDATE screen SET path = '/infra/metrics/report' WHERE screen_code = 'SCR-IOM-005';

-- ── 3. 신규 screen ──────────────────────────────────────────────────
--   SCR-COM-010/011/012: 설계는 됐으나 지금까지 screen 테이블에 시드된 적 없던 화면
--   (dev-lead 확인 2026-07-11, 이번 파일에서 함께 시드). 010(테마 토글)은 실제 라우트가
--   없는 헤더 컨트롤이라 기존 SCR-COM-007~009와 동일한 `/_common/*` 비-라우트 관례를 따른다.
--   셋 다 사이드바 미노출(010은 라우트 자체가 없고, 011/012는 헤더 진입 전용)이라
--   nav_visible=false, screen_role 매핑도 추가하지 않는다(무매핑 = 전체 인증 사용자 공개).
--   SCR-COM-013: 대시보드("/", 설계 당시 SCR 코드 미부여로 screen 테이블에 없었음).
--   dev-lead/designer 확정(2026-07-11) — 전 인증 사용자 공통 홈이라 screen_role 매핑 없음,
--   최상단 단독 항목(group_code/group_label NULL, sort_order=0으로 가장 먼저 노출).
--   SCR-ADMIN-006: 메뉴 관리(신규 관리자 화면, 사이드바 노출).
INSERT INTO screen
  (screen_code, screen_name, path, domain, icon_name, group_code, group_label, sort_order, nav_visible, created_by)
VALUES
  ('SCR-COM-010',   '테마 토글(라이트/다크)', '/_common/theme-toggle', 'common', NULL,       NULL,    NULL,     0,   false, 'SYSTEM'),
  ('SCR-COM-011',   '통합 검색 결과',        '/search',               'common', NULL,       NULL,    NULL,     0,   false, 'SYSTEM'),
  ('SCR-COM-012',   '사용자 가이드',         '/guide',                'common', NULL,       NULL,    NULL,     0,   false, 'SYSTEM'),
  ('SCR-COM-013',   '대시보드',             '/',                     'common', NULL,       NULL,    NULL,     0,   true,  'SYSTEM'),
  ('SCR-ADMIN-006', '메뉴 관리',            '/admin/menus',          'auth',   'ListTree', 'admin', '관리자', 420, true,  'SYSTEM');

-- ── 4. screen.path UNIQUE 제약 (설계자 확정, 2026-07-11) ───────────
--   사전 확인: 2·3절 반영 후 전체 screen.path 중복 없음(NOT NULL은 01_schema.sql에서 이미 보장).
ALTER TABLE screen ADD CONSTRAINT uq_screen_path UNIQUE (path);

-- ── 5. 사이드바 표시 컬럼 백필 : 단일 원천 navConfig.tsx ───────────
--   1) 전체를 비메뉴 기본값으로 리셋
UPDATE screen SET nav_visible = false, icon_name = NULL, group_code = NULL, group_label = NULL, sort_order = 0;

--   2) navConfig.tsx 순회 순서대로 그룹·항목 백필(그룹 표시 순서는 그룹 내 최소 sort_order로
--      결정되므로, navConfig 그룹 순서를 그대로 보존하도록 10 단위 증가값을 사용).
--      navConfig에 없는 화면(로그인·상세/서브 화면·403/404·SCR-COM-001~012 등 비메뉴 화면)은
--      1)의 기본값 그대로 유지. 대시보드(SCR-COM-013)는 navConfig의 "main" 그룹 첫 항목이라
--      아래 main 절에서 함께 백필한다.

-- main(그룹 라벨 없음)
UPDATE screen SET nav_visible = true, sort_order = 0
  WHERE screen_code = 'SCR-COM-013';
UPDATE screen SET nav_visible = true, icon_name = 'User', sort_order = 10
  WHERE screen_code = 'SCR-AUTH-002';

-- srm · 서비스 요청
UPDATE screen SET nav_visible = true, icon_name = 'LayoutGrid',    group_code = 'srm', group_label = '서비스 요청', sort_order = 20 WHERE screen_code = 'SCR-SRM-001';
UPDATE screen SET nav_visible = true, icon_name = 'ClipboardList', group_code = 'srm', group_label = '서비스 요청', sort_order = 30 WHERE screen_code = 'SCR-SRM-003';
UPDATE screen SET nav_visible = true, icon_name = 'Inbox',         group_code = 'srm', group_label = '서비스 요청', sort_order = 40 WHERE screen_code = 'SCR-SRM-004';
UPDATE screen SET nav_visible = true, icon_name = 'Stamp',         group_code = 'srm', group_label = '서비스 요청', sort_order = 50 WHERE screen_code = 'SCR-SRM-006';
UPDATE screen SET nav_visible = true, icon_name = 'Settings2',     group_code = 'srm', group_label = '서비스 요청', sort_order = 60 WHERE screen_code = 'SCR-SRM-007';
UPDATE screen SET nav_visible = true, icon_name = 'ListChecks',    group_code = 'srm', group_label = '서비스 요청', sort_order = 70 WHERE screen_code = 'SCR-SRM-008';

-- inc · 인시던트
UPDATE screen SET nav_visible = true, icon_name = 'AlertTriangle', group_code = 'inc', group_label = '인시던트', sort_order = 80 WHERE screen_code = 'SCR-INC-001';
UPDATE screen SET nav_visible = true, icon_name = 'Activity',      group_code = 'inc', group_label = '인시던트', sort_order = 90 WHERE screen_code = 'SCR-INC-005';

-- prb · 문제
UPDATE screen SET nav_visible = true, icon_name = 'BookOpen',   group_code = 'prb', group_label = '문제', sort_order = 100 WHERE screen_code = 'SCR-PRB-001';
UPDATE screen SET nav_visible = true, icon_name = 'SearchCode', group_code = 'prb', group_label = '문제', sort_order = 110 WHERE screen_code = 'SCR-PRB-004';

-- chg · 변경
UPDATE screen SET nav_visible = true, icon_name = 'GitPullRequest', group_code = 'chg', group_label = '변경', sort_order = 120 WHERE screen_code = 'SCR-CHG-001';
UPDATE screen SET nav_visible = true, icon_name = 'Stamp',          group_code = 'chg', group_label = '변경', sort_order = 130 WHERE screen_code = 'SCR-CHG-004';
UPDATE screen SET nav_visible = true, icon_name = 'CalendarDays',   group_code = 'chg', group_label = '변경', sort_order = 140 WHERE screen_code = 'SCR-CHG-005';
UPDATE screen SET nav_visible = true, icon_name = 'Activity',       group_code = 'chg', group_label = '변경', sort_order = 150 WHERE screen_code = 'SCR-CHG-006';

-- km · 지식
UPDATE screen SET nav_visible = true, icon_name = 'FileText',      group_code = 'km', group_label = '지식', sort_order = 160 WHERE screen_code = 'SCR-KM-001';
UPDATE screen SET nav_visible = true, icon_name = 'ClipboardList', group_code = 'km', group_label = '지식', sort_order = 170 WHERE screen_code = 'SCR-KM-003';
UPDATE screen SET nav_visible = true, icon_name = 'Stamp',         group_code = 'km', group_label = '지식', sort_order = 180 WHERE screen_code = 'SCR-KM-004';
UPDATE screen SET nav_visible = true, icon_name = 'Activity',      group_code = 'km', group_label = '지식', sort_order = 190 WHERE screen_code = 'SCR-KM-005';

-- itam · 자산
UPDATE screen SET nav_visible = true, icon_name = 'Boxes',    group_code = 'itam', group_label = '자산', sort_order = 200 WHERE screen_code = 'SCR-ITAM-001';
UPDATE screen SET nav_visible = true, icon_name = 'Network',  group_code = 'itam', group_label = '자산', sort_order = 210 WHERE screen_code = 'SCR-ITAM-004';
UPDATE screen SET nav_visible = true, icon_name = 'Activity', group_code = 'itam', group_label = '자산', sort_order = 220 WHERE screen_code = 'SCR-ITAM-005';

-- esm · 부서 서비스
UPDATE screen SET nav_visible = true, icon_name = 'Building2',     group_code = 'esm', group_label = '부서 서비스', sort_order = 230 WHERE screen_code = 'SCR-ESM-001';
UPDATE screen SET nav_visible = true, icon_name = 'ClipboardList', group_code = 'esm', group_label = '부서 서비스', sort_order = 240 WHERE screen_code = 'SCR-ESM-003';
UPDATE screen SET nav_visible = true, icon_name = 'Inbox',         group_code = 'esm', group_label = '부서 서비스', sort_order = 250 WHERE screen_code = 'SCR-ESM-004';
UPDATE screen SET nav_visible = true, icon_name = 'ListTodo',      group_code = 'esm', group_label = '부서 서비스', sort_order = 260 WHERE screen_code = 'SCR-ESM-010';
UPDATE screen SET nav_visible = true, icon_name = 'Settings2',     group_code = 'esm', group_label = '부서 서비스', sort_order = 270 WHERE screen_code = 'SCR-ESM-006';
UPDATE screen SET nav_visible = true, icon_name = 'Activity',      group_code = 'esm', group_label = '부서 서비스', sort_order = 280 WHERE screen_code = 'SCR-ESM-011';

-- hr · HR 케이스
UPDATE screen SET nav_visible = true, icon_name = 'Lock', group_code = 'hr', group_label = 'HR 케이스', sort_order = 290 WHERE screen_code = 'SCR-ESM-007';

-- vuln · 취약점
UPDATE screen SET nav_visible = true, icon_name = 'ShieldAlert', group_code = 'vuln', group_label = '취약점', sort_order = 300 WHERE screen_code = 'SCR-VULN-001';
UPDATE screen SET nav_visible = true, icon_name = 'Activity',    group_code = 'vuln', group_label = '취약점', sort_order = 310 WHERE screen_code = 'SCR-VULN-004';

-- compliance · 컴플라이언스
UPDATE screen SET nav_visible = true, icon_name = 'FileCheck', group_code = 'compliance', group_label = '컴플라이언스', sort_order = 320 WHERE screen_code = 'SCR-COMP-001';
UPDATE screen SET nav_visible = true, icon_name = 'Activity',  group_code = 'compliance', group_label = '컴플라이언스', sort_order = 330 WHERE screen_code = 'SCR-COMP-004';

-- iom · 인프라 모니터링
UPDATE screen SET nav_visible = true, icon_name = 'Server',        group_code = 'iom', group_label = '인프라 모니터링', sort_order = 340 WHERE screen_code = 'SCR-IOM-001';
UPDATE screen SET nav_visible = true, icon_name = 'Activity',      group_code = 'iom', group_label = '인프라 모니터링', sort_order = 350 WHERE screen_code = 'SCR-IOM-002';
UPDATE screen SET nav_visible = true, icon_name = 'AlertTriangle', group_code = 'iom', group_label = '인프라 모니터링', sort_order = 360 WHERE screen_code = 'SCR-IOM-003';
UPDATE screen SET nav_visible = true, icon_name = 'Boxes',         group_code = 'iom', group_label = '인프라 모니터링', sort_order = 370 WHERE screen_code = 'SCR-IOM-004';
UPDATE screen SET nav_visible = true, icon_name = 'ListChecks',    group_code = 'iom', group_label = '인프라 모니터링', sort_order = 380 WHERE screen_code = 'SCR-IOM-005';

-- admin · 관리자 (SCR-ADMIN-006 포함, 3절 INSERT 값을 1)의 블랭킷 리셋 이후 재적용)
UPDATE screen SET nav_visible = true, icon_name = 'Users',       group_code = 'admin', group_label = '관리자', sort_order = 390 WHERE screen_code = 'SCR-ADMIN-001';
UPDATE screen SET nav_visible = true, icon_name = 'ShieldCheck', group_code = 'admin', group_label = '관리자', sort_order = 400 WHERE screen_code = 'SCR-ADMIN-004';
UPDATE screen SET nav_visible = true, icon_name = 'ScrollText',  group_code = 'admin', group_label = '관리자', sort_order = 410 WHERE screen_code = 'SCR-ADMIN-005';
UPDATE screen SET nav_visible = true, icon_name = 'ListTree',    group_code = 'admin', group_label = '관리자', sort_order = 420 WHERE screen_code = 'SCR-ADMIN-006';

-- ── 6. screen_role : SCR-ADMIN-006 → SYSTEM_ADMIN 매핑 ─────────────
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM screen s
JOIN role r ON r.role_code = 'SYSTEM_ADMIN'
WHERE s.screen_code = 'SCR-ADMIN-006';
