-- =====================================================================
-- ITSM — approval_process 범위 우선순위 재설계(유지보수 요청 2026-07-15)
-- 단일 원천: docs/02_plan/database/common.md 4절 approval_process 상세
-- 1) domain을 NOT NULL → NULL 허용(미지정=전체 도메인 적용)
-- 2) priority_tier 산정식을 3축(도메인/요청유형/역할) 독립 스코프 기반으로
--    재정의(축개수×10 + 역할4 + 요청유형2 + 도메인1)하고 기존 행을 재계산
-- 3) 26번 파일의 tier=1/2 캐치올·도메인 부분 유니크 인덱스(28번에서
--    is_deleted 조건 보정)를 신규 tier값(0/11/23) 기준으로 재작성
--    (tier=14/25/37은 역할 조합 교집합 판정이 필요해 DB 제약 불가,
--    애플리케이션이 검증 — common.md 4절 참조)
-- =====================================================================

-- ── 1) domain nullable화 ──────────────────────────────────────────────
ALTER TABLE approval_process
    ALTER COLUMN domain DROP NOT NULL;

-- ── 2) priority_tier 재계산(기존 행 백필) ────────────────────────────
--   has_role: approval_process_requester_role에 is_deleted=false 매핑이 1개 이상 존재
UPDATE approval_process ap
SET priority_tier =
      (
          (CASE WHEN rs.has_role THEN 1 ELSE 0 END
         + CASE WHEN ap.request_subtype_key IS NOT NULL THEN 1 ELSE 0 END
         + CASE WHEN ap.domain IS NOT NULL THEN 1 ELSE 0 END) * 10
        + CASE WHEN rs.has_role THEN 4 ELSE 0 END
        + CASE WHEN ap.request_subtype_key IS NOT NULL THEN 2 ELSE 0 END
        + CASE WHEN ap.domain IS NOT NULL THEN 1 ELSE 0 END
      )
FROM (
    SELECT ap0.id,
           EXISTS (
               SELECT 1 FROM approval_process_requester_role r
               WHERE r.approval_process_id = ap0.id AND r.is_deleted = false
           ) AS has_role
    FROM approval_process ap0
) rs
WHERE ap.id = rs.id;

-- ── 3) 부분 유니크 인덱스 재작성(신규 tier 0/11/23, 모두 is_deleted=false 조건 포함) ─
DROP INDEX IF EXISTS uq_approval_process_domain_tier1;
DROP INDEX IF EXISTS uq_approval_process_domain_subtype_tier2;

CREATE UNIQUE INDEX uq_approval_process_tier0
    ON approval_process (priority_tier) WHERE priority_tier = 0 AND is_deleted = false;
CREATE UNIQUE INDEX uq_approval_process_domain_tier11
    ON approval_process (domain) WHERE priority_tier = 11 AND is_deleted = false;
CREATE UNIQUE INDEX uq_approval_process_domain_subtype_tier23
    ON approval_process (domain, request_subtype_key) WHERE priority_tier = 23 AND is_deleted = false;
