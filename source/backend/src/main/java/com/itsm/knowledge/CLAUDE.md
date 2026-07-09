# CLAUDE.md

지식 관리(knowledge, Knowledge Management) 도메인. 지식 기사 작성/편집/삭제·상태 관리(DRAFT/IN_REVIEW/PUBLISHED)·게이트키퍼 검토/게시 승인·검색/셀프서비스 열람·유용성 평가·카테고리/라벨 분류·KCS 티켓 연계·무결과 검색 로그 기반 지표를 담당한다. 티켓 연계는 common.ticket.TicketLink(target_type=KNOWLEDGE)를 재사용한다. DDD 4계층 구조.

## 하위 디렉토리
- `application/` — 유스케이스 서비스와 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
