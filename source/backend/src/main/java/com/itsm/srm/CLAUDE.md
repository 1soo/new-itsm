# CLAUDE.md

서비스 요청 관리(srm, Service Request Management) 도메인. 서비스 카탈로그·동적 양식·요청 접수·승인·카테고리 분류·이행·SLA·CSAT·지식 추천·자산 연계 조회 담당. 요청 분류는 큐가 아닌 카탈로그 항목의 카테고리(2026-07-18 유지보수 요청, 큐 폐지) 기준이다. DDD 4계층 구조.

## 하위 디렉토리
- `application/` — 유스케이스 서비스·상태머신·SLA 계산과 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
