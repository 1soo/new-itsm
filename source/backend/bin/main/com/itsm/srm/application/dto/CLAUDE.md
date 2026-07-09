# CLAUDE.md

서비스 요청(srm) 도메인 애플리케이션 계층의 요청·응답 DTO(record/enum).

## 파일
- `CreateRequestRequest.java` — 서비스 요청 생성(catalogItemId, 동적 formValues)
- `RequestCreatedResponse.java` — 요청 생성 응답
- `RequestSummaryResponse.java` — 요청 목록 요약 응답(SLA 상태 포함)
- `RequestDetailResponse.java` — 요청 상세 응답(양식값·승인·SLA·연계지식·연계자산(REQ-ITAM-006)·댓글·타임라인·허용 상태전이)
- `StatusTransitionRequest.java` — 상태 전이 요청(targetStatus, note)
- `StatusResponse.java` — 상태 응답(id, status)
- `AssignRequest.java` — 담당자 배정 요청(assigneeId, 미지정 시 본인)
- `ApprovalDecision.java` — 승인 결정 enum(APPROVE, REJECT)
- `ApprovalDecisionRequest.java` — 승인 결정 요청(decision, 반려 사유)
- `ApprovalDecisionResponse.java` — 승인 결정 응답
- `PendingApprovalResponse.java` — 승인 대기 목록 응답
- `CommentCreateRequest.java` — 댓글 작성 요청(body)
- `CommentResponse.java` — 댓글 응답
- `CsatRequest.java` — 만족도(CSAT) 제출 요청(score 1~5, comment)
- `CsatResponse.java` — CSAT 응답
- `CreateCatalogItemRequest.java` — 카탈로그 항목 생성 요청(승인 여부·SLA·동적 양식)
- `UpdateCatalogItemRequest.java` — 카탈로그 항목 수정 요청(양식 전체 교체 가능)
- `CatalogItemSummaryResponse.java` — 카탈로그 항목 요약 응답
- `CatalogItemDetailResponse.java` — 카탈로그 항목 상세 응답(양식 스키마 포함)
- `FormFieldDto.java` — 동적 양식 필드 정의(key, label, type, required, options)
- `QueueResponse.java` — 큐 응답(기본 큐 여부·미종료 건수)
- `MetricsResponse.java` — SRM 지표 응답(CSAT 평균·응답/해결 평균·SLA 준수율)
- `KnowledgeSuggestionResponse.java` — 지식 추천 응답(articleId, title, score)
