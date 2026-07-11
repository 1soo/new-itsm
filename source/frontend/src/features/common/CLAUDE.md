# CLAUDE.md

공통(알림 확인처리) 기능. 헤더 알림 팝오버(SCR-COM-002)의 "모두 지우기"·개별 X 버튼이 사용하는 확인처리 API·타입을 제공한다. 특정 도메인에 속하지 않는 교차 기능(현재는 알림 확인처리만)을 모아두는 디렉토리다. API 계약은 `api_spec/common.md`(API-COM-001/002) 기준.

## 파일
- `api.ts` — common API 호출(`commonApi`: `dismissNotifications`(개별/일괄 확인처리, 멱등)·`listDismissals`(확인처리 이력 조회)). 공통 apiClient 경유.
- `types.ts` — common 도메인 타입(`NotificationType`/`DismissalItem`/`DismissResult`/`NotificationDismissalListResponse`).
