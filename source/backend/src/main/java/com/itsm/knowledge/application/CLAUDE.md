# CLAUDE.md

knowledge 도메인의 애플리케이션 서비스 계층.

## 파일
- `KnowledgeService.java` — 지식 기사 검색/목록·상세·작성/수정/삭제·상태전이(공용 승인 게이트)·유용성평가·카테고리·KCS 티켓 연계·지표 유스케이스. 검토요청(API-KM-006)은 `ApprovalGateService.evaluateAndCreateIfNeeded()`로 항상 200 성공하되 매칭 여부로 즉시 PUBLISHED/IN_REVIEW 분기(구 검토승인/반려/검토대기 코드는 2026-07-11 제거)
- `KnowledgeApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 KNOWLEDGE 티켓 요약(ticketKey·제목·작성자명) 어댑터(`ApprovalTicketSummaryProvider` 구현). knowledge_article은 자연키가 없어 `ticketKey`는 "KM-{articleId}" 형태로 합성한다.
- `KnowledgeApprovalDecisionCallback.java` — 승인 인스턴스 최종 확정 시 기사를 자동 전환(APPROVED→PUBLISHED, REJECTED→DRAFT)하는 `ApprovalDecisionCallback` 구현체

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
