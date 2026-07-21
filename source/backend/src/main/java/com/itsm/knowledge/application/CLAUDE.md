# CLAUDE.md

knowledge 도메인 애플리케이션 서비스 계층.

## 파일
- `KnowledgeService.java` — 지식 기사 검색/목록·상세·작성/수정/삭제·상태전이(공용 승인 게이트)·유용성평가·카테고리·KCS 티켓 연계·지표 유스케이스. 검토요청(API-KM-006)은 `ApprovalGateService.evaluateAndCreateIfNeeded(..., requesterId=SecurityUtils.currentPrincipal().userId(), targetState="IN_REVIEW")`로 항상 200 성공, 매칭 여부로 즉시 PUBLISHED/IN_REVIEW 분기(구 검토승인/반려/검토대기 코드 2026-07-11 제거, 2026-07-22 requesterId를 article.authorId 고정 참조에서 호출자 기준으로 통일). `create()`(API-KM-003, 2026-07-22 신규): `TicketCreationGateSupport.createThenGate`로 REQUIRES_NEW 분리(엔티티+라벨 커밋 후 targetState="DRAFT" throwing 게이트, 매칭 시 409 APPROVAL_PENDING이어도 기사는 DRAFT로 남음). `search()` 목록은 `pendingApprovalTargetStatesOf`로 배치 조회한 targetState를 `ArticleSummaryResponse.pendingApprovalTargetState`에 채움(N+1 방지). `toDetail()`의 `ApprovalInfo`에 `targetState`(최신 인스턴스 스냅샷) 포함
- `KnowledgeApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004) 노출용 KNOWLEDGE 티켓 요약(ticketKey·제목·작성자명) 어댑터(`ApprovalTicketSummaryProvider` 구현). knowledge_article은 자연키 없어 `ticketKey`="KM-{articleId}" 합성.
- `KnowledgeApprovalDecisionCallback.java` — 승인 인스턴스 최종 확정 시 기사 자동 전환(APPROVED→PUBLISHED, REJECTED→DRAFT)하는 `ApprovalDecisionCallback` 구현체

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
