# CLAUDE.md

전 도메인(인시던트/문제/서비스요청) 티켓 공유 공통 요소. 댓글·타임라인·티켓 연계 모델과 티켓 공통 enum.
승인은 2026-07-11 승인 프로세스 커스텀 기능으로 `common.approval`(전 도메인 공용 다차 승인 엔진)로 완전 이전됨(이 패키지의 `Approval`/`ApprovalStatus`/`ApprovalRepository` 제거됨).

## 파일
- `Comment.java` — 티켓 댓글 엔티티(BaseEntity 상속)
- `TimelineEvent.java` — 티켓 활동 타임라인 이벤트 엔티티
- `TicketLink.java` — 티켓 간 연계 엔티티(예: 인시던트↔문제)
- `TicketType.java` — 티켓 유형 enum(SERVICE_REQUEST, INCIDENT, PROBLEM, CHANGE, ASSET, CI, KNOWLEDGE, ESM_REQUEST, HR_CASE, VULNERABILITY, COMPLIANCE_REQUIREMENT, CORRECTIVE_ACTION). `CORRECTIVE_ACTION`은 Stage 6(2026-07-12) 승인 게이트 연동 시 추가됨 — 시정조치 개별 항목(compliance.CorrectiveAction)이 요구사항(COMPLIANCE_REQUIREMENT)과 별개로 승인 인스턴스를 가져 전용 타입 필요(approval_request.ticket_type 컬럼은 CHECK 제약 없는 VARCHAR라 DB 마이그레이션 불필요)
- `Visibility.java` — 공개 범위 enum(INTERNAL, EXTERNAL)

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
- `persistence/` — 리포지토리 JPA 구현체
