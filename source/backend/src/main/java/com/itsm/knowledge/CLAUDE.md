# CLAUDE.md

지식 관리(knowledge, Knowledge Management) 도메인. 지식 기사 작성/편집/삭제·상태 관리(DRAFT/IN_REVIEW/PUBLISHED)·검색/셀프서비스 열람·유용성 평가·카테고리/라벨 분류·KCS 티켓 연계·무결과 검색 로그 기반 지표를 담당한다. 티켓 연계는 common.ticket.TicketLink(target_type=KNOWLEDGE)를 재사용한다. DDD 4계층 구조.

게이트키퍼 전용 검토승인/반려·검토대기 API(API-KM-007/008)는 승인 프로세스 커스텀 기능(2026-07-11)으로 제거되었다 — 전 도메인 공용 승인 엔진(`common.approval`)이 대체한다. KNOWLEDGE는 SRM/CHANGE와 게이트 연동 패턴이 다르다: 검토 요청(API-KM-006)은 항상 200으로 성공하고 게이트 매칭 여부에 따라 즉시 PUBLISHED 또는 IN_REVIEW로 결과만 갈리며(`ApprovalGateService.evaluateAndCreateIfNeeded`), 승인 인스턴스가 나중에 APPROVED/REJECTED로 확정되면 `KnowledgeApprovalDecisionCallback`이 기사를 자동 전환한다(사용자가 전이를 재시도하는 구조가 아님). `knowledge_review` DB 테이블은 유지하되(설계 문서 미갱신) 대응 JPA 엔티티·리포지토리는 제거했다(신규 데이터는 쌓이지 않음).

## 하위 디렉토리
- `application/` — 유스케이스 서비스와 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
