# CLAUDE.md

IT 자산 관리(asset, ITAM) 도메인. 자산(HW/SW/클라우드) 등록·조회·수정·폐기, 생애주기(5단계) 관리, 유형별 속성(EAV), 만료 추적(라이선스/보증/계약), 구성 항목(CI)·CMDB 의존 관계, 영향 범위 조회, 티켓 연계(SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE, `common.ticket.TicketType` 재사용), 자산 지표 담당. 7/7 마지막 도메인. DDD 4계층 구조.

## 하위 디렉토리
- `application/` — 유스케이스 서비스(AssetService)와 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
