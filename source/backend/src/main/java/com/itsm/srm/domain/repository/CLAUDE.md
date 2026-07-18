# CLAUDE.md

srm 도메인 리포지토리 인터페이스.

## 파일
- `ServiceRequestRepository.java` — 서비스 요청(ServiceRequest) 저장·조회(통합 검색용 키워드 검색 포함). `search(categoryId, uncategorized, ...)`·`countOpenByCategoryId`/`countOpenUncategorized`(2026-07-18 유지보수 요청 — 요청 큐 폐지, 기존 `queueId` 필터·`countOpenByQueueId` 대체. `service_catalog_item.category_id` 실시간 조인으로 판정, 스냅샷 없음)
- `ServiceCatalogItemRepository.java` — 카탈로그 항목 저장·조회. `search(categoryId, keyword)`(2026-07-16, 기존 자유 텍스트 category 필터 대체), `countByCategoryId`(카테고리 목록 itemCount·삭제 시 CATEGORY_IN_USE 판정 공용)
- `ServiceCatalogCategoryRepository.java` — 카탈로그 카테고리 저장·조회(2026-07-16 유지보수 요청). sortOrder 오름차순 목록, 이름 중복 검사(existsByName/existsByNameAndIdNot)
- `CsatRepository.java` — 만족도(Csat) 저장·조회
