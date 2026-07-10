# CLAUDE.md

티켓 공통 요소(댓글·타임라인·승인·연계)의 리포지토리 인터페이스.

## 파일
- `CommentRepository.java` — 댓글(Comment) 저장·조회
- `TimelineEventRepository.java` — 타임라인 이벤트 저장·조회
- `ApprovalRepository.java` — 승인(Approval) 저장·조회
- `TicketLinkRepository.java` — 티켓 간 연계(TicketLink) 저장·조회(source 기준 findBySourceTypeAndSourceId, target 기준 findByTargetTypeAndTargetId — 컴플라이언스처럼 source가 아닌 target 쪽에서 역조회해야 하는 경우에 사용)
