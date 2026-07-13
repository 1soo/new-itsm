# CLAUDE.md

knowledge 도메인 REST 컨트롤러.

## 파일
- `KnowledgeArticleController.java` — `/api/v1/knowledge/articles` 기사 검색/목록·CRUD·상태전이(공용 승인 게이트)·유용성평가·KCS 연계 API
- `KnowledgeCategoryController.java` — `/api/v1/knowledge/categories` 카테고리 목록 API
- `KnowledgeMetricsController.java` — `/api/v1/knowledge/metrics` 지식 지표 API

> 검토 대기 목록·검토승인/반려 API(`KnowledgeReviewController`, API-KM-007/008)는 2026-07-11 제거 — `common.approval.presentation.ApprovalController`(`/api/v1/approvals`)로 대체.
