-- =====================================================================
-- ITSM — approval_process 부분 유니크 인덱스 버그 수정(tester 발견, 2026-07-11)
-- 문제: 26번 파일의 tier=1/2 부분 유니크 인덱스가 is_deleted를 고려하지 않아,
--   soft-delete(is_deleted=true) 후 동일 스코프로 재생성 시 애플리케이션은
--   충돌 없음으로 판단하지만 DB 인덱스가 물리적으로 충돌해 500 발생.
-- 조치: 두 인덱스를 DROP 후 WHERE 절에 is_deleted = false 조건을 추가해 재생성.
-- =====================================================================

DROP INDEX uq_approval_process_domain_tier1;
DROP INDEX uq_approval_process_domain_subtype_tier2;

CREATE UNIQUE INDEX uq_approval_process_domain_tier1
    ON approval_process (domain) WHERE priority_tier = 1 AND is_deleted = false;
CREATE UNIQUE INDEX uq_approval_process_domain_subtype_tier2
    ON approval_process (domain, request_subtype_key) WHERE priority_tier = 2 AND is_deleted = false;
