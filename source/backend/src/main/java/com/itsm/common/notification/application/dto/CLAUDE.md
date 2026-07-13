# CLAUDE.md

알림 확인처리 애플리케이션 계층 요청·응답 DTO(record).

## 파일
- `DismissNotificationsRequest.java` — 확인처리 요청(items: 1개 이상, 각 항목 notificationType/sourceId)
- `DismissResultResponse.java` — 확인처리 결과 응답(dismissedCount)
- `NotificationDismissalResponse.java` — 확인처리 이력 항목(notificationType/sourceId/dismissedAt)
- `NotificationDismissalListResponse.java` — 확인처리 이력 목록 응답(items)
