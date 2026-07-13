# CLAUDE.md

인시던트(incident) 도메인 애플리케이션 서비스 계층.

## 파일
- `IncidentService.java` — 인시던트 생성·상태전이·심각도변경·대응자배정·에스컬레이션·해결·타임라인·문제연계·포스트모템 유스케이스. Stage 4(2026-07-11)에서 IN_PROGRESS→RESOLVED 전이 시 공용 승인 게이트(`ApprovalGateService.checkGate(domain=INCIDENT, requestSubtypeKey=null, requesterId, ...)`) 연동(요청자는 인시던트 created_by 이메일로 역조회). API-INC-005(상태전이)뿐 아니라 동일 전이를 수행하는 API-INC-009(resolve)도 우회 경로 방지 위해 같은 게이트 통과(2026-07-12 추가). 상세 조회(API-INC-003)의 `approval` 필드도 동일 노출
- `IncidentApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 INCIDENT 티켓 요약(ticketKey·요약·요청자명) 어댑터(`ApprovalTicketSummaryProvider` 구현, 자연키 ticketKey 그대로 사용)
- `IncidentStateMachine.java` — 상태 전이 규칙(NEW→IN_PROGRESS→RESOLVED→CLOSED, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
