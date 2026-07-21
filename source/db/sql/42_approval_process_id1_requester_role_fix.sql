-- =====================================================================
-- ITSM — 회귀 수정: approval_process.id=1 요청자 스코프 END_USER→SERVICE_DESK_AGENT
-- 근거: TC-REGRESSION-001(CRITICAL, 2026-07-22), dev-lead 결정
-- 41번 마이그레이션이 기존 활성 규칙(id=1, SERVICE_REQUEST/subtype='1'
-- "노트북 신청"/target_state=IN_FULFILLMENT)의 요청자 스코프를 그대로
-- 보존했으나, 이 스코프는 "요청자=티켓 고정 원 요청자(END_USER)"였던
-- 구버전 requesterId 산출 버그 때문에 우연히 END_USER로 매칭되던 것뿐이다.
-- "요청자=현재 호출자" 전환(ServiceRequestService.transition()의
-- checkGate 호출부 수정) 이후에는 IN_FULFILLMENT 전이를 실제로 호출하는
-- 주체가 항상 SERVICE_DESK_AGENT(assertTransitionRole의 AGENT 상수,
-- CLOSED 제외 전 전이는 AGENT만 호출 가능)라 END_USER 스코프와 영구
-- 불일치해 게이트가 조용히 무력화된다. 관리자의 실제 의도(해당 상태로의
-- 전이는 게이트를 건다)를 보존하도록 요청자 스코프를 SERVICE_DESK_AGENT로
-- 보정한다.
-- =====================================================================

UPDATE approval_process_requester_role r
SET role_id = sd.id,
    updated_by = 'SYSTEM_MIGRATION',
    updated_at = now()
FROM role eu, role sd
WHERE r.approval_process_id = 1
  AND r.is_deleted = false
  AND r.role_id = eu.id
  AND eu.role_code = 'END_USER'
  AND sd.role_code = 'SERVICE_DESK_AGENT';
