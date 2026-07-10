# CLAUDE.md

전 도메인(인시던트/문제/서비스요청) 티켓이 공유하는 공통 요소. 댓글·타임라인·승인·티켓 연계 모델과 티켓 공통 enum.

## 파일
- `Comment.java` — 티켓 댓글 엔티티(BaseEntity 상속)
- `TimelineEvent.java` — 티켓 활동 타임라인 이벤트 엔티티
- `Approval.java` — 승인 엔티티(승인 상태·결정자·사유)
- `TicketLink.java` — 티켓 간 연계 엔티티(예: 인시던트↔문제)
- `TicketType.java` — 티켓 유형 enum(SERVICE_REQUEST, INCIDENT, PROBLEM, CHANGE, ASSET, CI, KNOWLEDGE, ESM_REQUEST, HR_CASE)
- `ApprovalStatus.java` — 승인 상태 enum(PENDING, APPROVED, REJECTED)
- `Visibility.java` — 공개 범위 enum(INTERNAL, EXTERNAL)

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
- `persistence/` — 리포지토리 JPA 구현체
