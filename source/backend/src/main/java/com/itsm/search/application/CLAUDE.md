# CLAUDE.md

search 도메인의 애플리케이션 서비스 계층.

## 파일
- `SearchService.java` — 통합 검색(API-SEARCH-001) 유스케이스. 역할별 도메인 접근 가능 여부(1차 필터)·행 단위 스코프(2차 필터, KNOWLEDGE/SERVICE_REQUEST)를 적용해 도메인별 상한(100건) 조회 후 인메모리 병합·updatedAt 내림차순 정렬·페이지네이션

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
