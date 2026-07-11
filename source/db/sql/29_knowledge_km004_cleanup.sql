-- =====================================================================
-- ITSM — SCR-KM-004(검토·게시 승인함) 정리(Stage 3 KNOWLEDGE, 2026-07-11)
-- developer-fe-2 확인: 해당 페이지(ReviewInboxPage.tsx)·라우트 이미 삭제,
--   SCR-COM-014(전 도메인 공용 승인 대기함)로 완전 대체됨.
-- 원본 정의: 13_knowledge_seed.sql 라인 21(screen)/35(screen_role).
-- Stage 1의 SCR-SRM-006/SCR-CHG-004 정리와 동일하게 hard DELETE로 처리.
-- =====================================================================

DELETE FROM screen_role WHERE screen_id = (SELECT id FROM screen WHERE screen_code = 'SCR-KM-004');
DELETE FROM screen WHERE screen_code = 'SCR-KM-004';
