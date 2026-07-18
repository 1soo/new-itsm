# CLAUDE.md

srm 도메인 REST 컨트롤러. 승인 대기·결정 API는 2026-07-11 승인 프로세스 커스텀 기능으로 `common.approval.presentation.ApprovalController`(`/api/v1/approvals`)로 완전히 이전됨(이 패키지의 옛 `ApprovalController`는 제거됨).

## 파일
- `ServiceRequestController.java` — `/api/v1/service-requests` 요청 CRUD·상태전이·배정·댓글·CSAT API. `GET /{id}/assignee-candidates`(API-SRM-017, SERVICE_DESK_AGENT)는 카탈로그 항목 지정 담당자 역할 보유 ACTIVE 사용자 목록 조회(2026-07-15). 목록 조회(`GET`)의 `queue=` 파라미터는 `categoryId=`(숫자 또는 리터럴 `"uncategorized"`)로 전환(2026-07-18 유지보수 요청 — 요청 큐 폐지). 신규 `GET /category-counts`(API-SRM-016, SERVICE_DESK_AGENT·PROCESS_OWNER)가 기존 `/api/v1/queues`(큐 목록·건수 조회)를 완전 대체
- `ServiceCatalogController.java` — `/api/v1/service-catalog/items` 카탈로그 항목 관리 API. 목록 필터는 `category`(자유 텍스트) 대신 `categoryId`(FK, 2026-07-16 유지보수 요청)
- `ServiceCatalogCategoryController.java` — `/api/v1/service-catalog/categories` 카탈로그 카테고리 CRUD API(GET은 인증만, POST/PATCH/DELETE는 PROCESS_OWNER, 2026-07-16 유지보수 요청)
- `KnowledgeSuggestionController.java` — `/api/v1/knowledge/suggestions` 지식 추천 API
