# CLAUDE.md

변경 관리(change, Change Management) 도메인. 변경 요청(RFC) 등록·6단계 프로세스·유형/위험 분류·승인 경로(자동/동료검토/CAB)·승인/반려·구현 결과 기록·인시던트/문제 연계·일정(캘린더)·표준 변경 템플릿·지표를 담당한다. 승인은 common.ticket.Approval(ticket_type=CHANGE), 인시던트/문제 연계는 common.ticket.TicketLink(source_type/target_type=CHANGE)를 재사용한다. DDD 4계층 구조.

## 하위 디렉토리
- `application/` — 유스케이스 서비스·상태머신과 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
