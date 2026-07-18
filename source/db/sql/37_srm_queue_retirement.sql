-- service-request 증분(요청 큐 폐지 → 카테고리 기반 분류 일원화, 유지보수 요청 2026-07-18)
-- queue 관련 컬럼/테이블 제거. 스냅샷 컬럼이 아니라 백필 불필요.

ALTER TABLE service_catalog_item DROP COLUMN queue_id;
ALTER TABLE service_request DROP COLUMN queue_id;

DROP TABLE queue;

UPDATE screen
SET screen_name = '요청 처리함', screen_name_en = 'Request Inbox'
WHERE screen_code = 'SCR-SRM-004';
