# CLAUDE.md

change 도메인의 애플리케이션 서비스 계층.

## 파일
- `ChangeService.java` — 변경 요청(RFC) 생성·조회·6단계 전이·분류(승인경로 산정)·승인/반려·승인대기·구현결과·인시던트/문제/자산 연계·일정·템플릿·지표 유스케이스. 문제→변경 연계(API-PRB-009) 재사용 메서드(createLinkedChange/existsChange/ticketKeyOf) 포함(역할 검사 없음, 호출측 ProblemService에서 수행). 자산 연계(REQ-ITAM-006)는 AssetService.linkAsset()의 양방향 저장으로 생성되며 상세 조회 시 links에 노출. 컴플라이언스 요구사항 연계(API-COMP-005, ticket_link source_type='COMPLIANCE_REQUIREMENT'/target_type='CHANGE')는 change가 target 쪽이라 findByTargetTypeAndTargetId로 별도 조회해 상세 응답 links에 COMPLIANCE_REQUIREMENT 타입으로 노출(ComplianceRequirementRepository 참조, 역할 검사 없음)
- `ChangeStateMachine.java` — 상태 전이 규칙(REQUESTED→REVIEW→PLANNING→APPROVAL→IMPLEMENTATION→CLOSED, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
