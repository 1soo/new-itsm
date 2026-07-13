# CLAUDE.md

문제(problem) 관리 도메인. 문제 접수·근본원인분석(RCA/5Why)·워크어라운드·후속조치·기지 오류(Known Error)·인시던트/변경/자산 연계 담당. 상태는 6단계 순차 전이. DDD 4계층 구조.

## 하위 디렉토리
- `application/` — 유스케이스 서비스·상태머신과 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
