# CLAUDE.md

알림 확인처리 리포지토리 인터페이스(영속성 기술 비의존, 구현은 infrastructure에 위임).

## 파일
- `NotificationDismissalRepository.java` — 확인처리 이력(NotificationDismissal) 저장·조회(userId+notificationType+sourceId 중복 검사, userId 기준 이력 조회)
