# CLAUDE.md

srm 도메인 엔티티·enum·리포지토리 계약.

## 파일
- `ServiceRequest.java` — 서비스 요청 엔티티(상태·SLA·담당자·요청자)
- `ServiceCatalogItem.java` — 서비스 카탈로그 항목 엔티티(승인 여부·SLA·큐·담당자 역할 assigneeRoleId, 2026-07-15 요청 유형별 담당자 역할 지정 유지보수). `category`(자유 텍스트) → `categoryId`(FK `service_catalog_category`, 2026-07-16 카탈로그 카테고리 CRUD 유지보수)로 전환
- `ServiceCatalogCategory.java` — 카탈로그 카테고리 엔티티(name UNIQUE, sortOrder, 2026-07-16 유지보수 요청 — 관리자가 통제하는 고정 목록)
- `CatalogFormField.java` — 카탈로그 동적 양식 필드 정의 엔티티
- `ServiceRequestFormValue.java` — 요청별 양식 입력 값 엔티티
- `Queue.java` — 요청 처리 큐 엔티티
- `Csat.java` — 고객 만족도(CSAT) 엔티티
- `RequestStatus.java` — 요청 상태 enum(SUBMITTED, VALIDATED, ROUTED, IN_FULFILLMENT, FULFILLED, CLOSED). 승인 게이트는 상태값이 아니라 IN_FULFILLMENT 전이 시도 시 공용 승인 엔진이 판정
- `SlaStatus.java` — SLA 상태 enum(OK, WARNING, BREACHED)

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
