# CLAUDE.md

change 도메인 애플리케이션 서비스 계층.

## 파일
- `ChangeService.java` — 변경 요청(RFC) 생성·조회·6단계 전이·분류·구현결과·인시던트/문제/자산 연계·일정·템플릿·지표 유스케이스. 문제→변경 연계(API-PRB-009) 재사용 메서드(createLinkedChange/existsChange/ticketKeyOf) 포함(역할 검사 없음, 호출측 ProblemService에서 수행). 자산 연계(REQ-ITAM-006)는 AssetService.linkAsset()의 양방향 저장으로 생성되며 상세 조회 시 links에 노출. 컴플라이언스 요구사항 연계(API-COMP-005, ticket_link source_type='COMPLIANCE_REQUIREMENT'/target_type='CHANGE')는 change가 target 쪽이라 findByTargetTypeAndTargetId로 별도 조회해 상세 응답 links에 COMPLIANCE_REQUIREMENT 타입으로 노출(ComplianceRequirementRepository 참조, 역할 검사 없음). 승인 경로 자동 라우팅·승인/반려·승인대기 코드는 2026-07-11 제거됨 — Stage 2(2026-07-11)에서 IMPLEMENTATION 전이 시 공용 승인 게이트(`ApprovalGateService.checkGate(domain=CHANGE, requestSubtypeKey=change_request.type, requesterId, ...)`) 연동. 요청자는 RFC created_by(이메일)를 AppUserRepository로 역조회한 사용자로 판정. 상세 조회(API-CHG-003)의 `approval` 필드는 `ApprovalRequestRepository`로 최신 인스턴스를 조회해 노출
- `ChangeApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 CHANGE 티켓 요약(ticketKey·요약·요청자명) 어댑터(`ApprovalTicketSummaryProvider` 구현)
- `ChangeStateMachine.java` — 상태 전이 규칙(REQUESTED→REVIEW→PLANNING→APPROVAL→IMPLEMENTATION→CLOSED, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
