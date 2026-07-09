-- =====================================================================
-- ITSM — auth 도메인 시드 데이터
-- 근거: docs/02_plan/security/authorization/*.md (역할·화면 접근),
--       docs/02_plan/screen/*.md (SCR-* 화면 코드),
--       docs/02_plan/database/auth.md (단일 원천)
-- 등록자(created_by)는 시스템 초기 세팅이므로 'SYSTEM'.
-- =====================================================================

-- ── 1. role : 역할 11종 ───────────────────────────────────────────
INSERT INTO role (role_code, role_name, description, created_by) VALUES
  ('SYSTEM_ADMIN',          '시스템 관리자',   '플랫폼의 사용자 계정·역할·접근제어·감사 로그를 관리하는 최고 관리자.',        'SYSTEM'),
  ('END_USER',              '최종 사용자',     '서비스 포털에서 요청을 제출·추적하고 지식베이스를 셀프서비스로 활용하는 사용자.', 'SYSTEM'),
  ('SERVICE_DESK_AGENT',    '서비스 데스크 상담원', '요청을 배정받아 이행하고 요청자와 소통하는 1차 대응 담당자. 인시던트 접수 수행.', 'SYSTEM'),
  ('APPROVER',              '승인자',         '서비스 요청 및 변경(RFC)에 대한 승인/반려 권한을 가진 의사결정자(CAB 멤버 포함).', 'SYSTEM'),
  ('INCIDENT_MANAGER',      '인시던트 관리자', '인시던트 대응을 총괄하며 대응 역할 배정·심각도·상태·해결·포스트모템을 관리.',   'SYSTEM'),
  ('PROBLEM_MANAGER',       '문제 관리자',     '근본 원인을 조사·진단하고 알려진 오류(KEDB)·워크어라운드를 관리하며 재발 예방.', 'SYSTEM'),
  ('CHANGE_MANAGER',        '변경 관리자',     '변경 요청(RFC)의 분류·프로세스·승인 경로·일정·구현 결과를 총괄하고 CAB 조율.',   'SYSTEM'),
  ('KNOWLEDGE_CONTRIBUTOR', '지식 기여자',     '지식 기사를 작성·수정·삭제하고 검토를 요청하며 티켓 처리 중 기사를 작성/연결.',  'SYSTEM'),
  ('KNOWLEDGE_GATEKEEPER',  '지식 게이트키퍼', '검토 상태 기사를 검토해 게시 승인/반려하며 지식베이스 품질·최신성을 유지.',     'SYSTEM'),
  ('ASSET_MANAGER',         '자산 관리자',     'HW/SW/클라우드 자산의 전 생애주기와 CI·CMDB 관계를 관리하고 만료를 추적.',       'SYSTEM'),
  ('PROCESS_OWNER',         '프로세스 오너',   '서비스 카탈로그를 정의·정리하고 서비스 요청 지표를 모니터링하는 프로세스 책임자.', 'SYSTEM');

-- ── 2. app_user : SYSTEM_ADMIN 초기 계정 ──────────────────────────
--   email    : admin@itsm.local
--   password : Admin@1234   (평문은 저장하지 않음, BCrypt 단방향 해시만 저장)
INSERT INTO app_user (email, password_hash, name, status, created_by) VALUES
  ('admin@itsm.local',
   '$2b$10$o2LkO9LEJIwkldKs7q0UW.R0.Ji3I3u2w5sVL8R.z2ZtiKENVMbiy',
   '시스템 관리자',
   'ACTIVE',
   'SYSTEM');

-- 초기 계정에 SYSTEM_ADMIN 역할 부여
INSERT INTO user_role (user_id, role_id, created_by)
SELECT u.id, r.id, 'SYSTEM'
FROM app_user u
JOIN role r ON r.role_code = 'SYSTEM_ADMIN'
WHERE u.email = 'admin@itsm.local';

-- ── 3. screen : auth 단계 화면 (SCR-AUTH/ADMIN/COM/ERR) ───────────
--   dev-lead 범위 지시: auth 단계는 auth 화면코드만 seed, 타 도메인 화면은
--   각 도메인 개발 단계에서 추가한다(중복 금지).
--   SCR-COM-* 공통 레이아웃/패턴은 라우팅 경로가 없어 path 를 `/_common/*`
--   비-라우트 식별자로 둔다(SCR-COM-006 403 은 실제 라우트 /403).
INSERT INTO screen (screen_code, screen_name, path, domain, created_by) VALUES
  -- auth (본인 인증/계정)
  ('SCR-AUTH-001',  '로그인',                    '/login',                  'auth',   'SYSTEM'),
  ('SCR-AUTH-002',  '내 프로필',                 '/profile',                'auth',   'SYSTEM'),
  ('SCR-AUTH-003',  '비밀번호 변경',              '/profile/password',       'auth',   'SYSTEM'),
  -- admin (관리자)
  ('SCR-ADMIN-001', '계정 목록',                 '/admin/users',            'admin',  'SYSTEM'),
  ('SCR-ADMIN-002', '계정 생성',                 '/admin/users/new',        'admin',  'SYSTEM'),
  ('SCR-ADMIN-003', '계정 상세·수정',             '/admin/users/:id',        'admin',  'SYSTEM'),
  ('SCR-ADMIN-004', '역할 관리',                 '/admin/roles',            'admin',  'SYSTEM'),
  ('SCR-ADMIN-005', '감사 로그 조회',             '/admin/audit-logs',       'admin',  'SYSTEM'),
  -- common (공통 레이아웃·가드·패턴)
  ('SCR-COM-001',   '앱 셸 레이아웃',             '/_common/app-shell',      'common', 'SYSTEM'),
  ('SCR-COM-002',   '글로벌 헤더',               '/_common/header',         'common', 'SYSTEM'),
  ('SCR-COM-003',   '사이드바 내비게이션',         '/_common/sidebar',        'common', 'SYSTEM'),
  ('SCR-COM-004',   '푸터',                      '/_common/footer',         'common', 'SYSTEM'),
  ('SCR-COM-005',   '인증 가드 / 401 리다이렉트',  '/_common/auth-guard',     'common', 'SYSTEM'),
  ('SCR-COM-006',   '403 접근 거부',             '/403',                    'common', 'SYSTEM'),
  ('SCR-COM-007',   '공통 티켓 목록/필터 패턴',     '/_common/list-pattern',   'common', 'SYSTEM'),
  ('SCR-COM-008',   '공통 티켓 상세 패턴',         '/_common/detail-pattern', 'common', 'SYSTEM'),
  ('SCR-COM-009',   '토스트·확인 다이얼로그',       '/_common/toast',          'common', 'SYSTEM'),
  -- error
  ('SCR-ERR-404',   '404 Not Found',            '/404',                    'common', 'SYSTEM');

-- ── 4. screen_role : auth 단계 역할-화면 매핑 ─────────────────────
--   근거: security/authorization/*.md. auth 단계에 존재하는 화면만 매핑
--   (타 도메인 화면은 해당 도메인 단계에서 추가).
--   SYSTEM_ADMIN 고유 접근: system_admin.md "2. 접근 가능 화면"의 SCR-ADMIN-*.
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM (VALUES
  ('SYSTEM_ADMIN', 'SCR-ADMIN-001'),
  ('SYSTEM_ADMIN', 'SCR-ADMIN-002'),
  ('SYSTEM_ADMIN', 'SCR-ADMIN-003'),
  ('SYSTEM_ADMIN', 'SCR-ADMIN-004'),
  ('SYSTEM_ADMIN', 'SCR-ADMIN-005')
) AS m(role_code, screen_code)
JOIN role r   ON r.role_code   = m.role_code
JOIN screen s ON s.screen_code = m.screen_code;

-- 전 역할 공통 기본 접근 (역할정의서 "공통 기본 접근 (전 역할 공통)"):
--   앱셸/헤더/사이드바/푸터·토스트(SCR-COM-001~004/009), 내 프로필·비밀번호 변경(SCR-AUTH-002/003).
--   로그인(SCR-AUTH-001)·404(SCR-ERR-404)·가드/403/패턴(SCR-COM-005~008)은
--   인증 이전/공용/비-라우트라 역할 매핑 없음.
INSERT INTO screen_role (screen_id, role_id, created_by)
SELECT s.id, r.id, 'SYSTEM'
FROM screen s
CROSS JOIN role r
WHERE s.screen_code IN (
  'SCR-COM-001', 'SCR-COM-002', 'SCR-COM-003', 'SCR-COM-004', 'SCR-COM-009',
  'SCR-AUTH-002', 'SCR-AUTH-003'
);
