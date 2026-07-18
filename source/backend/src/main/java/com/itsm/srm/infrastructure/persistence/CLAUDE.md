# CLAUDE.md

srm 도메인 리포지토리 인터페이스의 Spring Data JPA 구현체.

## 파일
- `ServiceRequestJpaRepository.java` — ServiceRequestRepository 구현. `search`/`countOpenByCategoryId`/`countOpenUncategorized`는 `ServiceCatalogItem`과 `exists` 서브쿼리로 categoryId 조인(2026-07-18 유지보수 요청 — 요청 큐 폐지, 기존 `queueId` 등치 필터 대체)
- `ServiceCatalogItemJpaRepository.java` — ServiceCatalogItemRepository 구현(categoryId 필터·countByCategoryId, 2026-07-16)
- `ServiceCatalogCategoryJpaRepository.java` — ServiceCatalogCategoryRepository 구현(2026-07-16 유지보수 요청)
- `CsatJpaRepository.java` — CsatRepository 구현
