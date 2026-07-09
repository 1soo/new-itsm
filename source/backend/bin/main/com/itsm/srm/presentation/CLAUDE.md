# CLAUDE.md

srm 도메인의 REST 컨트롤러.

## 파일
- `ServiceRequestController.java` — `/api/v1/service-requests` 요청 CRUD·상태전이·배정·댓글·CSAT API
- `ServiceCatalogController.java` — `/api/v1/service-catalog/items` 카탈로그 항목 관리 API
- `ApprovalController.java` — `/api/v1/approvals` 승인 대기 조회 API. type=service-request(기본)|change 공유 대기함(ChangeService와 공동 소유, change 도메인 분기 포함)
- `QueueController.java` — `/api/v1/queues` 큐 조회 API
- `KnowledgeSuggestionController.java` — `/api/v1/knowledge/suggestions` 지식 추천 API
