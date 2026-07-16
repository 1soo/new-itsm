---
date: 20260711-125730
domain: common
change_type: [new]
keywords: [알림확인처리, notification_dismissal, 모두지우기]
---

# 유지보수 이력 — common

> 유지보수 일시: 20260711-125730 · 도메인: common

## 1. 요구사항

알림창 우측 최상단에 "모두 지우기" 버튼을 클릭하여 도착한 알림을 확인처리 할 수 있어야 한다.
각 알림 아이템 1행 우측 끝 timing 우측에 X 버튼을 넣어 해당 알림을 개별 확인처리 할 수 있어야 한다.

## 2. 해결 방법

헤더 알림 팝오버(SCR-COM-002)에 "모두 지우기" 버튼과 개별 X 버튼을 추가했다.
확인처리 대상은 팝오버에 표시되는 상위 8건으로 한정했다.
확인처리된 알림은 배지 카운트에서도 제외되도록 했다.
DB에는 `source/db/sql/25_common_notification_dismissal.sql`로 `notification_dismissal` 테이블을 신규 생성했다.
이 테이블은 append-only 구조이며 `UNIQUE(user_id, notification_type, source_id)` 제약으로 중복 확인처리를 방지해 멱등성을 보장한다.
BE는 `com.itsm.common.notification` 패키지를 신규 생성했다(common 도메인 최초 백엔드, auth와 동일한 DDD 4계층 구조로 신설).
NotificationDismissalService(멱등 확인처리·이력조회)와 NotificationDismissalController(API-COM-001/002, common 도메인 첫 API 명세서)를 구현했다.
FE는 `features/common/` 디렉토리를 신규 생성하고 commonApi를 추가했다.
AppLayout.tsx가 확인처리 이력으로 알림 후보와 뱃지 카운트를 필터링하도록 변경했다.
header.tsx에 "모두 지우기"와 개별 X 버튼을 추가했다(UI 에이전트 미소집 상태라 FE가 직접 구현, 신규 프리미티브 없이 기존 마크업을 확장).

## 3. 변경 파일

- `source/db/sql/25_common_notification_dismissal.sql`
- `com.itsm.common.notification` 하위 NotificationDismissalService, NotificationDismissalController
- `source/frontend/src/features/common/` (신규 디렉토리, commonApi 포함)
- `source/frontend/src/routes/AppLayout.tsx`
- `source/frontend/src/components/layout/header.tsx`

## 4. 테스트 결과

통합 테스트 20건 전부 PASS했다.
커밋 `8775547`로 반영했다.
