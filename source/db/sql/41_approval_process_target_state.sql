-- =====================================================================
-- ITSM — 승인 프로세스 4번째 매칭 축 target_state 도입(유지보수 요청 2026-07-22)
-- 단일 원천: docs/02_plan/database/common.md 4절 approval_process/approval_request 상세
-- 1) approval_process.target_state(nullable, domain 종속) 신규 —
--    도메인 상태 enum 값 중 하나를 적용 상태로 지정(최초 상태도 가능).
--    domain이 NULL이면 target_state도 반드시 NULL(request_subtype_key와
--    동일한 종속 규칙, 기존과 동일하게 애플리케이션 레벨 검증).
--    target_state가 값을 가지면 approval_process_requester_role이
--    최소 1개 이상 있어야 하는 신규 규칙도 애플리케이션 레벨 검증
--    (request_subtype_key→domain 강제 규칙과 동일한 형태, DB 제약 없음).
-- 2) priority_tier 산정식에 target_state weight=8 추가, 기존 6개 tier값
--    (0/11/14/23/25/37)은 그대로 보존, 신규 tier는 43/55 두 가지뿐
--    (role 없이 target_state만 지정하는 조합은 검증 단계에서 차단).
--    기존 행 전체 재계산(32번 선례와 동일 패턴).
-- 3) approval_request.target_state(NOT NULL, 인스턴스 생성 시점 스냅샷)
--    신규 — 기존 행은 9개 도메인 하드코딩 게이트 지점 매핑값으로 백필
--    후 NOT NULL 제약 적용.
-- 4) 부분 유니크 인덱스는 기존 3개(tier0/11/23) 그대로 유지, 신규 추가
--    없음(tier 43/55는 기존 14/25/37과 동일하게 애플리케이션이 검증).
--    조회 인덱스만 (ticket_type, ticket_id, target_state) 신규 추가
--    (신규 findTopByTicketTypeAndTicketIdAndTargetStateOrderByIdDesc 지원).
--
-- 착수 전 로컬 DB 확인 결과(2026-07-22): approval_process 16행 중 활성
-- (is_deleted=false) 1행(id=1, domain=SERVICE_REQUEST, subtype='1',
-- role_id=2 매핑 보유, tier=37) — 나머지 15행은 QA 테스트 잔여물로 전부
-- soft-delete 상태. approval_request 6행 전부 ticket_type=SERVICE_REQUEST.
-- 따라서 SERVICE_REQUEST 외 도메인은 실제 백필 대상 데이터가 없으나,
-- 매핑 CASE문은 9개 도메인 전체를 명시해 향후 데이터 유입에도 안전하게 둔다.
-- =====================================================================

-- ── 1) approval_process.target_state 컬럼 추가 ────────────────────────
ALTER TABLE approval_process
    ADD COLUMN target_state VARCHAR(30);

-- ── 2) 기존 하드코딩 게이트 지점 → target_state 백필(도메인별 매핑) ────
--    domain이 있는 행(활성/soft-delete 무관) 전체 대상.
UPDATE approval_process
SET target_state = CASE domain
        WHEN 'SERVICE_REQUEST' THEN 'IN_FULFILLMENT'
        WHEN 'CHANGE'          THEN 'IMPLEMENTATION'
        WHEN 'INCIDENT'        THEN 'RESOLVED'
        WHEN 'PROBLEM'         THEN 'RESOLVED_CLOSED'
        WHEN 'ASSET'           THEN 'RETIREMENT'
        WHEN 'VULNERABILITY'   THEN 'REMEDIATION'
        WHEN 'COMPLIANCE'      THEN 'RESOLVED'
        WHEN 'ESM'             THEN 'COMPLETED'
        WHEN 'KNOWLEDGE'       THEN 'IN_REVIEW'
        ELSE NULL
    END
WHERE domain IS NOT NULL;

-- ── 3) priority_tier 재계산(target_state weight=8 추가, 전체 행 재산정) ─
--   has_role: approval_process_requester_role에 is_deleted=false 매핑이 1개 이상 존재
UPDATE approval_process ap
SET priority_tier =
      (
          (CASE WHEN rs.has_role THEN 1 ELSE 0 END
         + CASE WHEN ap.request_subtype_key IS NOT NULL THEN 1 ELSE 0 END
         + CASE WHEN ap.domain IS NOT NULL THEN 1 ELSE 0 END
         + CASE WHEN ap.target_state IS NOT NULL THEN 1 ELSE 0 END) * 10
        + CASE WHEN rs.has_role THEN 4 ELSE 0 END
        + CASE WHEN ap.request_subtype_key IS NOT NULL THEN 2 ELSE 0 END
        + CASE WHEN ap.domain IS NOT NULL THEN 1 ELSE 0 END
        + CASE WHEN ap.target_state IS NOT NULL THEN 8 ELSE 0 END
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

-- ── 4) approval_request.target_state 컬럼 추가(생성 시점 스냅샷) ──────
ALTER TABLE approval_request
    ADD COLUMN target_state VARCHAR(30);

UPDATE approval_request
SET target_state = CASE ticket_type
        WHEN 'SERVICE_REQUEST' THEN 'IN_FULFILLMENT'
        WHEN 'CHANGE'          THEN 'IMPLEMENTATION'
        WHEN 'INCIDENT'        THEN 'RESOLVED'
        WHEN 'PROBLEM'         THEN 'RESOLVED_CLOSED'
        WHEN 'ASSET'           THEN 'RETIREMENT'
        WHEN 'VULNERABILITY'   THEN 'REMEDIATION'
        WHEN 'COMPLIANCE'      THEN 'RESOLVED'
        WHEN 'ESM'             THEN 'COMPLETED'
        WHEN 'KNOWLEDGE'       THEN 'IN_REVIEW'
        ELSE NULL
    END;

ALTER TABLE approval_request
    ALTER COLUMN target_state SET NOT NULL;

-- ── 5) 조회 인덱스 신규 추가(특정 target_state의 최신 인스턴스 조회) ───
--    부분 유니크 인덱스(tier0/11/23)는 기존 3개 그대로 유지, 신규 추가 없음.
CREATE INDEX idx_approval_request_ticket_target_state
    ON approval_request (ticket_type, ticket_id, target_state);
