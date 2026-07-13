# CLAUDE.md

통합 검색(search) 도메인. 지식(KNOWLEDGE)+티켓(SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE) 교차 도메인 키워드 검색 담당. 자체 엔티티·리포지토리 없이 각 도메인 기존 리포지토리를 조회·병합해 단일 페이지네이션으로 반환하는 조회 전용 도메인(DDD 4계층 중 application/presentation만 구성).

## 하위 디렉토리
- `application/` — 유스케이스 서비스와 DTO
- `presentation/` — REST 컨트롤러
