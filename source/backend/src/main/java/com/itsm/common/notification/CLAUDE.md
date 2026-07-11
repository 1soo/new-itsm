# CLAUDE.md

알림 확인처리(헤더 알림 드롭다운의 "모두 지우기"/개별 X). common 도메인 최초 백엔드 구현이라 auth와 동일한 DDD 4계층(application/domain/infrastructure/presentation) 구조를 그대로 따른다.

## 하위 디렉토리
- `application/` — 유스케이스 서비스와 DTO
- `domain/` — 엔티티·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
