# CLAUDE.md

서비스 요청 관리(srm, Service Request Management) 도메인. 서비스 카탈로그·동적 양식·요청 접수·승인·큐 라우팅·이행·SLA·CSAT·지식 추천을 담당한다. DDD 4계층 구조.

## 하위 디렉토리
- `application/` — 유스케이스 서비스·상태머신·SLA 계산과 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
