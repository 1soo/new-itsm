-- =====================================================================
-- ITSM — service-request 도메인 시드 (최소 예시 + RBAC 증분)
-- 근거: docs/02_plan/database/service-request.md, screen/service-request.md,
--       security/authorization/*.md (END_USER/SERVICE_DESK_AGENT/APPROVER/PROCESS_OWNER)
-- =====================================================================

-- ── 1. queue : 기본 큐 ────────────────────────────────────────────
INSERT INTO queue (name, description, is_default, created_by) VALUES
  ('미분류',    '미분류 요청 기본 큐', true,  'SYSTEM'),
  ('IT 서비스', 'IT 인프라·계정·장비 요청', false, 'SYSTEM');

-- ── 2. service_catalog_item : 예시 요청 유형 ──────────────────────
INSERT INTO service_catalog_item
  (name, description, category, approval_required, approver_role, queue_id, sla_response_minutes, sla_resolve_minutes, created_by)
SELECT '노트북 신청', '업무용 노트북 지급 요청', '하드웨어', true, 'APPROVER', q.id, 60, 1440, 'SYSTEM'
FROM queue q WHERE q.name = 'IT 서비스';

INSERT INTO service_catalog_item
  (name, description, category, approval_required, queue_id, sla_response_minutes, sla_resolve_minutes, created_by)
SELECT '비밀번호 초기화', '계정 비밀번호 초기화 요청', '계정', false, q.id, 30, 240, 'SYSTEM'
FROM queue q WHERE q.name = 'IT 서비스';

-- ── 3. catalog_form_field : 예시 동적 양식 필드 ───────────────────
INSERT INTO catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, options, sort_order, created_by)
SELECT c.id, 'model', '희망 모델', 'select', true,
       '["MacBook Pro 14","MacBook Air 13","ThinkPad X1","Galaxy Book"]'::jsonb, 1, 'SYSTEM'
FROM service_catalog_item c WHERE c.name = '노트북 신청';

INSERT INTO catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, sort_order, created_by)
SELECT c.id, 'justification', '신청 사유', 'text', true, 2, 'SYSTEM'
FROM service_catalog_item c WHERE c.name = '노트북 신청';

INSERT INTO catalog_form_field
  (catalog_item_id, field_key, label, field_type, required, sort_order, created_by)
SELECT c.id, 'account_id', '대상 계정 ID', 'text', true, 1, 'SYSTEM'
FROM service_catalog_item c WHERE c.name = '비밀번호 초기화';

-- ── 4. screen 증분 : SCR-SRM-001~008 ──────────────────────────────
--   경로는 dev-frontend와 도메인 단계에서 정합 예정.
INSERT INTO screen (screen_code, screen_name, path, domain, created_by) VALUES
  ('SCR-SRM-001', '서비스 포털',          '/portal',                     'service-request', 'SYSTEM'),
  ('SCR-SRM-002', '요청 제출',            '/portal/requests/new',        'service-request', 'SYSTEM'),
  ('SCR-SRM-003', '내 요청 목록',         '/service-requests',           'service-request', 'SYSTEM'),
  ('SCR-SRM-004', '요청 큐(상담원)',       '/service-requests/queue',     'service-request', 'SYSTEM'),
  ('SCR-SRM-005', '요청 상세',            '/service-requests/:id',       'service-request', 'SYSTEM'),
  ('SCR-SRM-006', '승인 대기함',          '/approvals/service-requests', 'service-request', 'SYSTEM'),
  ('SCR-SRM-007', '서비스 카탈로그 관리',   '/admin/service-catalog',      'service-request', 'SYSTEM'),
  ('SCR-SRM-008', '요청 지표 대시보드',     '/service-requests/metrics',   'service-request', 'SYSTEM');

-- ── 5. screen_role 증분 : 역할정의서 "접근 가능 화면" 기준 ────────
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM (VALUES
  -- END_USER: 포털·요청 제출·내 요청 목록·요청 상세
  ('END_USER',           'SCR-SRM-001'),
  ('END_USER',           'SCR-SRM-002'),
  ('END_USER',           'SCR-SRM-003'),
  ('END_USER',           'SCR-SRM-005'),
  -- SERVICE_DESK_AGENT: 내 요청 목록·요청 큐·요청 상세
  ('SERVICE_DESK_AGENT', 'SCR-SRM-003'),
  ('SERVICE_DESK_AGENT', 'SCR-SRM-004'),
  ('SERVICE_DESK_AGENT', 'SCR-SRM-005'),
  -- APPROVER: 승인 대기함·요청 상세(읽기)
  ('APPROVER',           'SCR-SRM-006'),
  ('APPROVER',           'SCR-SRM-005'),
  -- PROCESS_OWNER: 카탈로그 관리·요청 지표
  ('PROCESS_OWNER',      'SCR-SRM-007'),
  ('PROCESS_OWNER',      'SCR-SRM-008')
) AS m(role_code, screen_code)
JOIN role r   ON r.role_code   = m.role_code
JOIN screen s ON s.screen_code = m.screen_code;
