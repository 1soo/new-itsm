-- =====================================================================
-- ITSM — infra-monitoring 도메인 시드 (최소 예시 + RBAC 증분)
-- 근거: docs/02_plan/database/infra-monitoring.md, screen/infra-monitoring.md,
--       security/authorization/infra_operator.md
-- =====================================================================

-- ── 1. role : 신규 역할(단일) ──────────────────────────────────────
INSERT INTO role (role_code, role_name, description, created_by) VALUES
  ('INFRA_OPERATOR', '인프라 운영 담당자', '인프라 자산의 가동률·성능 지표를 등록·조회하고 임계치·알림, 용량 계획을 관리하는 운영 담당자.', 'SYSTEM');

-- ── 2. screen 증분 : SCR-IOM-001~005 ──────────────────────────────
INSERT INTO screen (screen_code, screen_name, path, domain, created_by) VALUES
  ('SCR-IOM-001', '인프라 지표 등록',        '/infra-monitoring/metrics/new',    'infra-monitoring', 'SYSTEM'),
  ('SCR-IOM-002', '지표 대시보드',           '/infra-monitoring/dashboard',      'infra-monitoring', 'SYSTEM'),
  ('SCR-IOM-003', '임계치 설정·알림 목록',    '/infra-monitoring/thresholds',     'infra-monitoring', 'SYSTEM'),
  ('SCR-IOM-004', '용량 계획 관리',          '/infra-monitoring/capacity-plans', 'infra-monitoring', 'SYSTEM'),
  ('SCR-IOM-005', '인프라 지표 리포팅',       '/infra-monitoring/report',         'infra-monitoring', 'SYSTEM');

-- ── 3. screen_role 증분 : INFRA_OPERATOR 전 화면 접근 ─────────────
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM screen s
CROSS JOIN role r
WHERE s.screen_code IN ('SCR-IOM-001', 'SCR-IOM-002', 'SCR-IOM-003', 'SCR-IOM-004', 'SCR-IOM-005')
AND r.role_code = 'INFRA_OPERATOR';

-- ── 4. screen_role : 신규 역할의 공통 기본 접근 ───────────────────
--   INFRA_OPERATOR는 02_seed.sql 시점에 존재하지 않아 전 역할
--   공통 CROSS JOIN에서 누락됨 — 별도 증분(ESM/VULN/COMP와 동일 패턴).
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM screen s
CROSS JOIN role r
WHERE s.screen_code IN (
  'SCR-COM-001', 'SCR-COM-002', 'SCR-COM-003', 'SCR-COM-004', 'SCR-COM-009',
  'SCR-AUTH-002', 'SCR-AUTH-003'
)
AND r.role_code = 'INFRA_OPERATOR';

-- ── infra-monitoring RBAC 테스트 유저 (로컬 테스트 재현성) ────────
--   password : Admin@1234  (02_seed 의 admin 과 동일한 BCrypt 해시 재사용)
INSERT INTO app_user (email, password_hash, name, status, created_by) VALUES
  ('io@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '인프라 운영 담당자', 'ACTIVE', 'SYSTEM');

INSERT INTO user_role (user_id, role_id, created_by)
SELECT u.id, r.id, 'SYSTEM'
FROM app_user u, role r
WHERE u.email = 'io@itsm.local' AND r.role_code = 'INFRA_OPERATOR';

-- ── 5. infra_metric_threshold : 임계치 사전 설정 2건 ──────────────
INSERT INTO infra_metric_threshold (metric_type, upper_limit, lower_limit, created_by) VALUES
  ('CPU', 90.00, NULL, 'SYSTEM'),
  ('RESPONSE_TIME', 500.00, NULL, 'SYSTEM');

-- ── 6. infra_metric : 기존 자산 중 존재하는 만큼(최대 3건) 지표 시드 ──
--   asset 도메인은 트랜잭션 데이터를 시드하지 않아 asset 레코드가 없을 수 있으므로,
--   자산 등록 순서(rn)에 매칭되는 만큼만 지표를 등록(자산이 없으면 전부 스킵, 에러 아님).
--   rn=1 자산의 CPU 92.30, rn=2 자산의 RESPONSE_TIME 550.00은 위 임계치 초과 케이스.
WITH target_asset AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM asset
)
INSERT INTO infra_metric (asset_id, metric_type, value, measured_at, created_by)
SELECT ta.id, v.metric_type, v.value, v.measured_at, 'SYSTEM'
FROM target_asset ta
JOIN (VALUES
  (1, 'UPTIME', 99.95::numeric(10,2), TIMESTAMPTZ '2026-07-01 09:00:00+09'),
  (1, 'UPTIME', 98.10::numeric(10,2), TIMESTAMPTZ '2026-07-05 09:00:00+09'),
  (1, 'CPU',    45.20::numeric(10,2), TIMESTAMPTZ '2026-07-01 09:00:00+09'),
  (1, 'CPU',    92.30::numeric(10,2), TIMESTAMPTZ '2026-07-05 09:00:00+09'),
  (2, 'MEMORY', 68.40::numeric(10,2), TIMESTAMPTZ '2026-07-01 09:00:00+09'),
  (2, 'RESPONSE_TIME', 250.00::numeric(10,2), TIMESTAMPTZ '2026-07-01 09:00:00+09'),
  (2, 'RESPONSE_TIME', 550.00::numeric(10,2), TIMESTAMPTZ '2026-07-05 09:00:00+09'),
  (3, 'UPTIME', 99.99::numeric(10,2), TIMESTAMPTZ '2026-07-01 09:00:00+09')
) AS v(rn, metric_type, value, measured_at)
ON v.rn = ta.rn;

-- ── 7. infra_metric_alert : 위 임계치 초과 지표 2건에 대응하는 알림 ──
--   BE는 지표 등록 시점에 알림을 자동 생성하지만, 시드는 DB 직접 삽입이라
--   위에서 등록한 초과 지표 값에 매칭해 알림도 함께 시드(자산이 없어 지표가 없으면 스킵).
INSERT INTO infra_metric_alert (metric_id, asset_id, metric_type, breached_value, threshold_type, acknowledged, created_by)
SELECT m.id, m.asset_id, m.metric_type, m.value, 'UPPER', false, 'SYSTEM'
FROM infra_metric m
WHERE m.metric_type = 'CPU' AND m.value = 92.30;

INSERT INTO infra_metric_alert (metric_id, asset_id, metric_type, breached_value, threshold_type, acknowledged, created_by)
SELECT m.id, m.asset_id, m.metric_type, m.value, 'UPPER', false, 'SYSTEM'
FROM infra_metric m
WHERE m.metric_type = 'RESPONSE_TIME' AND m.value = 550.00;

-- ── 8. uptime_target : 자산 1건 이상 가동률 목표(SLA) 설정 ────────
INSERT INTO uptime_target (asset_id, target_percentage, created_by)
SELECT id, 99.50, 'SYSTEM' FROM asset ORDER BY id LIMIT 1;

-- ── 9. capacity_plan : 용량 계획 3건(활용률 100% 초과 1건 포함) ───
INSERT INTO capacity_plan (team_or_service, capacity, demand, created_by) VALUES
  ('인프라운영팀 서버 용량',   1000.00, 650.00, 'SYSTEM'),
  ('네트워크팀 대역폭',       500.00,  480.00, 'SYSTEM'),
  ('클라우드 서비스 인스턴스', 200.00,  240.00, 'SYSTEM');   -- 활용률 120% (100% 초과 케이스)
