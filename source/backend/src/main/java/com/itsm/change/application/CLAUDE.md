# CLAUDE.md

change 도메인의 애플리케이션 서비스 계층.

## 파일
- `ChangeService.java` — 변경 요청(RFC) 생성·조회·6단계 전이·분류(승인경로 산정)·승인/반려·승인대기·구현결과·인시던트/문제 연계·일정·템플릿·지표 유스케이스. 문제→변경 연계(API-PRB-009) 재사용 메서드(createLinkedChange/existsChange/ticketKeyOf) 포함(역할 검사 없음, 호출측 ProblemService에서 수행)
- `ChangeStateMachine.java` — 상태 전이 규칙(REQUESTED→REVIEW→PLANNING→APPROVAL→IMPLEMENTATION→CLOSED, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
