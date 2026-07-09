# CLAUDE.md

인시던트(incident) 관리 도메인. 장애 접수·심각도/우선순위·대응자 배정·에스컬레이션·해결·타임라인·포스트모템(5Why·액션아이템)·문제 연계를 담당한다. DDD 4계층 구조.

## 하위 디렉토리
- `application/` — 유스케이스 서비스·상태머신과 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
