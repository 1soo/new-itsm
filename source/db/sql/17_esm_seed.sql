-- =====================================================================
-- ITSM — esm 도메인 시드 (최소 예시 + RBAC 증분)
-- 근거: docs/02_plan/database/esm.md, screen/esm.md,
--       security/authorization/hr_case_manager.md, dept_coordinator.md,
--       process_owner.md(증분), end_user.md(증분)
-- =====================================================================

-- ── 1. role : 신규 역할 2종 ────────────────────────────────────────
INSERT INTO role (role_code, role_name, description, created_by) VALUES
  ('HR_CASE_MANAGER', 'HR 케이스 담당자', '민감한 인사 이슈를 접수·기록·조사·해결 단계로 관리하는 HR 부서 담당자.', 'SYSTEM'),
  ('DEPT_COORDINATOR', '부서 처리 담당자', '소속 부서의 부서 요청을 처리하고 온보딩/오프보딩 체크리스트 중 자기 부서 하위 작업을 완료 처리하는 담당자.', 'SYSTEM');

-- ── 2. screen 증분 : SCR-ESM-001~011 ──────────────────────────────
--   경로는 dev-frontend와 도메인 단계에서 정합 예정(SRM 명명 규칙 재사용: /esm/*, /admin/esm-catalog).
INSERT INTO screen (screen_code, screen_name, path, domain, created_by) VALUES
  ('SCR-ESM-001', '부서 서비스 포털(카탈로그 브라우즈)',    '/esm/portal',                'esm', 'SYSTEM'),
  ('SCR-ESM-002', '부서 요청 제출(동적 양식)',              '/esm/portal/requests/new',   'esm', 'SYSTEM'),
  ('SCR-ESM-003', '내 부서 요청 목록',                      '/esm/requests',              'esm', 'SYSTEM'),
  ('SCR-ESM-004', '부서 요청 처리 큐',                      '/esm/requests/queue',        'esm', 'SYSTEM'),
  ('SCR-ESM-005', '부서 요청 상세',                         '/esm/requests/:id',          'esm', 'SYSTEM'),
  ('SCR-ESM-006', '부서별 카탈로그 관리',                    '/admin/esm-catalog',         'esm', 'SYSTEM'),
  ('SCR-ESM-007', 'HR 케이스 목록',                         '/esm/hr-cases',              'esm', 'SYSTEM'),
  ('SCR-ESM-008', 'HR 케이스 상세',                         '/esm/hr-cases/:id',          'esm', 'SYSTEM'),
  ('SCR-ESM-009', '온보딩/오프보딩 체크리스트 상세',           '/esm/checklists/:id',        'esm', 'SYSTEM'),
  ('SCR-ESM-010', '내 하위 작업 목록',                       '/esm/checklist-tasks',       'esm', 'SYSTEM'),
  ('SCR-ESM-011', 'ESM 지표 대시보드',                       '/esm/metrics',               'esm', 'SYSTEM');

-- ── 3. screen_role 증분 : 역할정의서 "접근 가능 화면" 기준 ────────
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM (VALUES
  -- END_USER: 부서 포털·제출·내 요청 목록·상세·체크리스트 조회(end_user.md 증분)
  ('END_USER',          'SCR-ESM-001'),
  ('END_USER',          'SCR-ESM-002'),
  ('END_USER',          'SCR-ESM-003'),
  ('END_USER',          'SCR-ESM-005'),
  ('END_USER',          'SCR-ESM-009'),
  -- DEPT_COORDINATOR: 처리 큐·상세·체크리스트 조회·내 하위 작업
  ('DEPT_COORDINATOR',  'SCR-ESM-004'),
  ('DEPT_COORDINATOR',  'SCR-ESM-005'),
  ('DEPT_COORDINATOR',  'SCR-ESM-009'),
  ('DEPT_COORDINATOR',  'SCR-ESM-010'),
  -- PROCESS_OWNER: 카탈로그 관리·지표 대시보드(process_owner.md 증분)
  ('PROCESS_OWNER',     'SCR-ESM-006'),
  ('PROCESS_OWNER',     'SCR-ESM-011'),
  -- HR_CASE_MANAGER: HR 케이스 목록·상세(HR 전용)
  ('HR_CASE_MANAGER',   'SCR-ESM-007'),
  ('HR_CASE_MANAGER',   'SCR-ESM-008')
) AS m(role_code, screen_code)
JOIN role r   ON r.role_code   = m.role_code
JOIN screen s ON s.screen_code = m.screen_code;

-- ── 4. screen_role : 신규 역할의 공통 기본 접근 ───────────────────
--   HR_CASE_MANAGER/DEPT_COORDINATOR는 02_seed.sql 시점에 존재하지 않아
--   전 역할 공통 CROSS JOIN에서 누락됨 — 신규 역할분만 별도 부여.
--   (앱셸/헤더/사이드바/푸터·토스트, 내 프로필·비밀번호 변경)
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM screen s
CROSS JOIN role r
WHERE s.screen_code IN (
  'SCR-COM-001', 'SCR-COM-002', 'SCR-COM-003', 'SCR-COM-004', 'SCR-COM-009',
  'SCR-AUTH-002', 'SCR-AUTH-003'
)
AND r.role_code IN ('HR_CASE_MANAGER', 'DEPT_COORDINATOR');

-- ── esm RBAC 테스트 유저 (로컬 테스트 재현성) ─────────────────────
--   password : Admin@1234  (02_seed 의 admin 과 동일한 BCrypt 해시 재사용)
--   부서별 접근 제어(department 일치 검증) 테스트를 위해 서로 다른 부서의
--   DEPT_COORDINATOR 계정을 시드(LEGAL/FACILITIES/IT). IT는 오프보딩 자산
--   회수 하위 작업(department='IT' 고정)의 완료 처리·체크리스트 전체(4/4)
--   자동완료 시나리오 검증에 필요(dev_lead 요청). PROCESS_OWNER는
--   05_srm_seed의 po@itsm.local 계정을 재사용(테이블 변경 없음).
INSERT INTO app_user (email, password_hash, name, status, department, created_by) VALUES
  ('hr@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   'HR 케이스 담당자', 'ACTIVE', 'HR', 'SYSTEM'),
  ('legal-coord@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '법무 처리 담당자', 'ACTIVE', 'LEGAL', 'SYSTEM'),
  ('facilities-coord@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '시설 처리 담당자', 'ACTIVE', 'FACILITIES', 'SYSTEM'),
  ('it-coord@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   'IT 처리 담당자', 'ACTIVE', 'IT', 'SYSTEM');

INSERT INTO user_role (user_id, role_id, created_by)
SELECT u.id, r.id, 'SYSTEM'
FROM (VALUES
  ('hr@itsm.local',              'HR_CASE_MANAGER'),
  ('legal-coord@itsm.local',     'DEPT_COORDINATOR'),
  ('facilities-coord@itsm.local','DEPT_COORDINATOR'),
  ('it-coord@itsm.local',        'DEPT_COORDINATOR')
) AS m(email, role_code)
JOIN app_user u ON u.email     = m.email
JOIN role r     ON r.role_code = m.role_code;

-- ── 5. esm_catalog_item : 부서별 예시 카탈로그(HR/LEGAL/FACILITIES/FINANCE 각 1건 이상) ──
INSERT INTO esm_catalog_item (name, description, department, checklist_template_type, created_by) VALUES
  ('신규 입사자 온보딩',      '신규 입사자 인사 서류·계정/장비 지급 처리', 'HR',          'ONBOARDING',  'SYSTEM'),
  ('퇴사자 오프보딩 처리',     '퇴사자 계정 비활성화·출입카드/자산 회수 처리', 'HR',        'OFFBOARDING', 'SYSTEM'),
  ('계약서 검토 요청',        '사내 계약서 법무 검토 요청',                'LEGAL',       'NONE',        'SYSTEM'),
  ('좌석 배정 요청',          '신규/이동 좌석 배정 요청',                  'FACILITIES',  'NONE',        'SYSTEM'),
  ('법인카드 발급 요청',      '업무용 법인카드 발급 요청',                  'FINANCE',     'NONE',        'SYSTEM');

-- ── 6. esm_catalog_form_field : 예시 동적 양식 필드 ───────────────
INSERT INTO esm_catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, sort_order, created_by)
SELECT c.id, 'start_date', '입사일', 'date', true, 1, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '신규 입사자 온보딩';

INSERT INTO esm_catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, sort_order, created_by)
SELECT c.id, 'position', '직책', 'text', true, 2, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '신규 입사자 온보딩';

INSERT INTO esm_catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, sort_order, created_by)
SELECT c.id, 'last_working_day', '최종 근무일', 'date', true, 1, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '퇴사자 오프보딩 처리';

INSERT INTO esm_catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, sort_order, created_by)
SELECT c.id, 'contract_title', '계약서 제목', 'text', true, 1, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '계약서 검토 요청';

INSERT INTO esm_catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, sort_order, created_by)
SELECT c.id, 'review_deadline', '검토 희망일', 'date', false, 2, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '계약서 검토 요청';

INSERT INTO esm_catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, options, sort_order, created_by)
SELECT c.id, 'preferred_floor', '희망 층', 'select', false,
       '["3F","4F","5F"]'::jsonb, 1, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '좌석 배정 요청';

INSERT INTO esm_catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, options, sort_order, created_by)
SELECT c.id, 'card_type', '카드 유형', 'select', true,
       '["개인카드","팀카드"]'::jsonb, 1, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '법인카드 발급 요청';

INSERT INTO esm_catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, sort_order, created_by)
SELECT c.id, 'monthly_limit', '월 한도(원)', 'number', false, 2, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '법인카드 발급 요청';

-- ── 7. esm_checklist_template_task : 온보딩/오프보딩 템플릿(서로 다른 부서 배정) ──
INSERT INTO esm_checklist_template_task
  (catalog_item_id, department, task_description, sort_order, created_by)
SELECT c.id, 'HR', '인사 서류 접수 확인', 1, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '신규 입사자 온보딩';

INSERT INTO esm_checklist_template_task
  (catalog_item_id, department, task_description, sort_order, created_by)
SELECT c.id, 'IT', '계정·장비 지급', 2, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '신규 입사자 온보딩';

INSERT INTO esm_checklist_template_task
  (catalog_item_id, department, task_description, sort_order, created_by)
SELECT c.id, 'IT', '계정 비활성화', 1, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '퇴사자 오프보딩 처리';

INSERT INTO esm_checklist_template_task
  (catalog_item_id, department, task_description, sort_order, created_by)
SELECT c.id, 'FACILITIES', '출입카드 회수', 2, 'SYSTEM'
FROM esm_catalog_item c WHERE c.name = '퇴사자 오프보딩 처리';
