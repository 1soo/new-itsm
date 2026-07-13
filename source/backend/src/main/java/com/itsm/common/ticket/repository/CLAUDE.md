# CLAUDE.md

티켓 공통 요소(댓글·타임라인·연계) 리포지토리 인터페이스. 승인(Approval)은 `common.approval`로 이전됨.

## 파일
- `CommentRepository.java` — 댓글(Comment) 저장·조회
- `TimelineEventRepository.java` — 타임라인 이벤트 저장·조회
- `TicketLinkRepository.java` — 티켓 간 연계(TicketLink) 저장·조회(source 기준 findBySourceTypeAndSourceId, target 기준 findByTargetTypeAndTargetId — 컴플라이언스처럼 source 아닌 target 쪽에서 역조회하는 경우 사용)
