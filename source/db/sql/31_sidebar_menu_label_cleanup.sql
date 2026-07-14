-- =====================================================================
-- ITSM — 사이드바 메뉴 라벨 괄호 제거(유지보수 요청, 2026-07-14)
-- 근거: 사용자 요청 — 사이드바 메뉴명에 괄호 부연설명을 넣지 않고, 괄호 안 내용도 삭제.
-- 대상: 사이드바 노출(nav_visible=true) 화면 중 괄호가 포함된 4건.
-- =====================================================================

UPDATE screen SET screen_name = '요청 큐',           screen_name_en = 'Request Queue'
  WHERE screen_code = 'SCR-SRM-004';
UPDATE screen SET screen_name = '변경 일정',          screen_name_en = 'Change Schedule'
  WHERE screen_code = 'SCR-CHG-005';
UPDATE screen SET screen_name = '부서 서비스 포털',    screen_name_en = 'Department Service Portal'
  WHERE screen_code = 'SCR-ESM-001';
UPDATE screen SET screen_name = '승인 대기함',        screen_name_en = 'Approval Inbox'
  WHERE screen_code = 'SCR-COM-014';
