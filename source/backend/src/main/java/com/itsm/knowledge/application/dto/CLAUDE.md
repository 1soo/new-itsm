# CLAUDE.md

knowledge 도메인 애플리케이션 계층 요청·응답 DTO(record).

## 파일
- `CreateArticleRequest.java` — 기사 작성 요청(제목·본문 필수, 카테고리, 라벨명 목록)
- `UpdateArticleRequest.java` — 기사 수정 요청(부분 갱신)
- `ArticleCreatedResponse.java` — 작성 응답(id, status)
- `ArticleSummaryResponse.java` — 목록 요약 응답(요약·상태·카테고리·유용성 비율·pendingApprovalTargetState(진행 중 승인 인스턴스 targetState, 2026-07-22 신규))
- `ArticleListResponse.java` — 검색/목록 응답(페이지 정보 + 무결과 검색 여부)
- `ArticleDetailResponse.java` — 상세/열람 응답(본문·라벨·유용성 집계·승인 정보(approvalRequestId/status/targetState, common.approval 조회, 2026-07-22 targetState 추가))
- `StatusTransitionRequest.java` — 상태 전이 요청(targetStatus)
- `StatusResponse.java` — 상태 응답(id, status). 기사 수정(API-KM-004)에서 사용
- `StatusTransitionResponse.java` — 검토 요청(API-KM-006) 응답(id, status, approvalRequestId — 매칭 규칙 없으면 null)
- `FeedbackRequest.java` — 유용성 평가 요청(helpful, comment)
- `FeedbackResponse.java` — 유용성 평가 응답(집계)
- `CategoryResponse.java` — 카테고리 응답
- `LinkArticleRequest.java` — KCS 티켓 연계 요청(기존 기사 연결 또는 신규 작성)
- `LinkArticleResponse.java` — 연계 응답(articleId, ticketId)
- `KnowledgeMetricsResponse.java` — 지식 지표 응답(usageCount/noResultSearchCount/helpfulRate/deflectionRate/topNoResultKeywords)
