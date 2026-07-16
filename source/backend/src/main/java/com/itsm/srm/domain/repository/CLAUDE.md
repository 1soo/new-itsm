# CLAUDE.md

srm 도메인 리포지토리 인터페이스.

## 파일
- `ServiceRequestRepository.java` — 서비스 요청(ServiceRequest) 저장·조회(통합 검색용 키워드 검색 포함)
- `ServiceCatalogItemRepository.java` — 카탈로그 항목 저장·조회. `search(categoryId, keyword)`(2026-07-16, 기존 자유 텍스트 category 필터 대체), `countByCategoryId`(카테고리 목록 itemCount·삭제 시 CATEGORY_IN_USE 판정 공용)
- `ServiceCatalogCategoryRepository.java` — 카탈로그 카테고리 저장·조회(2026-07-16 유지보수 요청). sortOrder 오름차순 목록, 이름 중복 검사(existsByName/existsByNameAndIdNot)
- `CatalogFormFieldRepository.java` — 카탈로그 양식 필드 저장·조회
- `ServiceRequestFormValueRepository.java` — 요청 양식 값 저장·조회
- `QueueRepository.java` — 큐(Queue) 저장·조회
- `CsatRepository.java` — 만족도(Csat) 저장·조회
