# CLAUDE.md

srm 도메인 REST 컨트롤러. 승인 대기·결정 API는 2026-07-11 승인 프로세스 커스텀 기능으로 `common.approval.presentation.ApprovalController`(`/api/v1/approvals`)로 완전히 이전됨(이 패키지의 옛 `ApprovalController`는 제거됨).

## 파일
- `ServiceRequestController.java` — `/api/v1/service-requests` 요청 CRUD·상태전이·배정·댓글·CSAT API. `GET /{id}/assignee-candidates`(API-SRM-017, SERVICE_DESK_AGENT)는 카탈로그 항목 지정 담당자 역할 보유 ACTIVE 사용자 목록 조회(2026-07-15)
- `ServiceCatalogController.java` — `/api/v1/service-catalog/items` 카탈로그 항목 관리 API
- `QueueController.java` — `/api/v1/queues` 큐 조회 API
- `KnowledgeSuggestionController.java` — `/api/v1/knowledge/suggestions` 지식 추천 API
