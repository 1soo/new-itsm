---
date: 20260718-114544
domain: srm
change_type: [modified, removed]
keywords: [요청 큐 폐지, 카테고리 실시간 조인, category-counts API, uncategorized 필터]
---

# 유지보수 이력 — SRM (서비스 요청 큐 폐지)

> 유지보수 일시: 20260718-114544 · 도메인: srm

## 1. 요구사항

`queue` 테이블, `service_catalog_item.queue_id`, `service_request.queue_id`를 완전히 제거해야 한다.
요청 분류/필터링은 `service_request.catalog_item_id → service_catalog_item.category_id`를 실시간으로 조인하는 방식으로 대체해야 한다(스냅샷 없음, 카탈로그 항목의 카테고리를 바꾸면 과거 요청 분류도 즉시 반영되어야 한다).
`category_id`는 계속 nullable로 유지해 미분류 그룹을 지원해야 한다.

## 2. 해결 방법

### DB

`source/db/sql/37_srm_queue_retirement.sql`을 신규 작성했다.
`queue` 테이블을 DROP했다.
`service_catalog_item.queue_id`, `service_request.queue_id` 컬럼을 DROP했다.
컬럼 자체를 삭제하므로 별도 백필은 불필요했다.
화면 SCR-SRM-004의 이름을 "요청 처리함"/"Request Inbox"로 개칭했으며 경로·아이콘·그룹·정렬순서는 그대로 유지했다.

### BE

`Queue` 엔티티, `QueueRepository`, `QueueJpaRepository`, `QueueService`, `QueueResponse`, `QueueController`(`/api/v1/queues`)를 전부 삭제했다.
`ServiceCatalogItem`, `ServiceRequest`의 `queueId` 필드를 제거했다.
요청 목록 조회를 `categoryId`(숫자 또는 `"uncategorized"` 리터럴) 기준 실시간 조인 필터로 전환했다.
카테고리별(+미분류) 건수를 조회하는 `GET /api/v1/service-requests/category-counts`(API-SRM-016)를 신규로 추가했다.

### FE

`RequestQueuePage.tsx`(SCR-SRM-004, 타이틀 "요청 처리함")의 좌측 목록을 큐 기반에서 카테고리 기반으로 전환했다.
`CatalogManagePage.tsx`에서 담당 큐 select를 제거했다.
`RequestDetailPage.tsx`에서 큐 표시를 제거했다.
`user-guide-content.tsx`의 옛 큐 관련 문구를 정정했다.
ESM은 여전히 큐 기반 구조를 유지하므로 변경하지 않았다.

### 코드 리뷰 중 발견·수정한 결함

`ServiceRequestController.list()`가 `categoryId`를 raw String으로 받아 `Long.valueOf()`로 파싱하는 과정에서, 숫자도 `"uncategorized"`도 아닌 값이 들어오면 `NumberFormatException`이 기존 400 처리 경로(`MethodArgumentTypeMismatchException` 핸들러)를 우회해 전역 catch-all(500)로 떨어지는 회귀가 발견됐다.
`BusinessException(VALIDATION_ERROR)`로 400을 반환하도록 수정했고 재검증을 완료했다.

## 3. 변경 파일

- `source/db/sql/37_srm_queue_retirement.sql`
- `source/backend/src/main/java/com/itsm/srm/domain/Queue.java` (삭제)
- `source/backend/src/main/java/com/itsm/srm/domain/repository/QueueRepository.java` (삭제)
- `source/backend/src/main/java/com/itsm/srm/infrastructure/persistence/QueueJpaRepository.java` (삭제)
- `source/backend/src/main/java/com/itsm/srm/application/QueueService.java` (삭제)
- `source/backend/src/main/java/com/itsm/srm/application/dto/QueueResponse.java` (삭제)
- `source/backend/src/main/java/com/itsm/srm/presentation/QueueController.java` (삭제)
- `source/backend/src/main/java/com/itsm/srm/domain/ServiceCatalogItem.java`
- `source/backend/src/main/java/com/itsm/srm/domain/ServiceRequest.java`
- `source/backend/src/main/java/com/itsm/srm/presentation/ServiceRequestController.java`
- `source/backend/src/main/java/com/itsm/srm/application/ServiceRequestService.java`
- `source/frontend/src/features/service-request/RequestQueuePage.tsx`
- `source/frontend/src/features/service-request/CatalogManagePage.tsx`
- `source/frontend/src/features/service-request/RequestDetailPage.tsx`
- `source/frontend/src/components/common/user-guide-content.tsx`
- `docs/02_plan/database/service-request.md`
- `docs/02_plan/api_spec/service-request.md`
- `docs/02_plan/screen/service-request.md`
- `docs/02_plan/security/authorization/process_owner.md`
- `docs/02_plan/security/authorization/service_desk_agent.md`
- `docs/00_context/glossary.md`

## 4. 테스트 결과

통합 테스트는 `docs/04_test/20260718-112351/srm/result/srm.md`에 기록되어 있으며 9건 중 8건 PASS, 1건 FAIL이었다.
FAIL 항목은 `/api/v1/queues` 호출 시 기대한 404 대신 500이 반환된 건으로, 원인은 `GlobalExceptionHandler`의 catch-all이 미매핑 경로 예외(`NoResourceFoundException`)까지 500으로 변환하는 앱 전역의 기존 결함이었다(임의의 원래부터 없던 경로에서도 동일하게 재현 확인됨).
이는 이번 SRM 변경으로 인한 회귀가 아니라고 판단해 범위 밖으로 분리했다.
실제 요구사항인 큐 관련 클래스·경로·테이블의 완전 제거와 실시간 조인 반영은 충족이 확인되어 최종 결론은 pass로 정리했다.
`categoryId` 파싱 버그 재검증 결과도 함께 기록되어 있다.
커밋 `f5fbbf8`으로 origin/main에 push 완료됐다.
