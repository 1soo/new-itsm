# 유지보수 이력 — common

> 유지보수 일시: 20260711-161421 · 도메인: common

## 1. 요구사항

헤더 알림(SCR-COM-002) 조회 방식을 로그인 세션당 1회 조회에서 5초 간격 polling으로 변경해야 한다.
Polling 대상 범위는 기존과 동일하게 역할별로 유지한다(APPROVER→서비스요청/변경 승인 대기, ASSET_MANAGER→자산 만료 임박).
탭이 백그라운드(비활성)일 때는 polling을 정지하고, 포그라운드로 복귀하면 재개해야 한다.
팝오버가 열려 있는 동안에도 polling은 계속 진행되어 신규 알림은 추가되어야 하지만, 이미 화면에 표시된 알림 항목은 dismiss(개별 X 또는 모두 지우기) 하기 전까지는 polling 재조회만으로 절대 사라지면 안 된다.
확인처리 이력(dismissals)도 알림 목록과 함께 매 polling마다 재조회해야 한다.
API 연속 실패 시 별도 backoff나 중단 로직 없이 5초 고정 간격 재시도를 유지해야 한다.
신규 merge 항목은 기존 표시 순서를 유지한 채 끝에 추가해야 한다.
배지 카운트는 표시 목록의 누적 여부와 무관하게 매 polling마다 서버 기준 실시간 총합으로 재계산해야 한다.
기존 "팝오버 상위 8건 상한(NOTIFICATION_PREVIEW_SIZE=8)" 규칙은 merge 정책(기존 항목 미제거)과 충돌하므로 폐지하고, 누적된 만큼 전부 표시해야 한다(팝오버는 기존 스크롤 지원 그대로 사용).

## 2. 해결 방법

`source/frontend/src/routes/AppLayout.tsx`에 5초 간격 polling 로직을 구현했다.
기존 로그인 세션당 1회만 조회하던 `useEffect(..., [roles])`를 setInterval 기반 polling으로 변경하고, Page Visibility API로 탭이 백그라운드일 때 polling을 정지했다가 포그라운드 복귀 시 재개하도록 했다.
알림 목록은 신규 조회 결과를 기존 표시 항목과 merge하는 방식으로 처리해, 신규 항목은 기존 순서를 유지한 채 끝에 추가하고 기존 표시 항목은 dismiss 액션 전까지 제거하지 않도록 구현했다.
확인처리 이력(`commonApi.listDismissals()`)도 알림 목록과 함께 매 polling마다 재조회해 확인처리 상태를 최신으로 유지했다.
배지 카운트는 표시 목록의 누적 여부와 무관하게 매 polling마다 서버 기준 실시간 총합(확인처리 이력 제외)으로 재계산하도록 했다.
기존 팝오버 상위 8건 상한(`NOTIFICATION_PREVIEW_SIZE=8`) truncate 로직을 제거해 누적된 알림을 전부 표시하도록 하고, 팝오버의 기존 스크롤(max-h-80 overflow-auto)로 대응했다.
API 연속 실패 시에도 별도 backoff나 중단 로직 없이 5초 고정 간격 재시도를 유지하도록 구현했다.
`source/frontend/src/components/layout/header.tsx`는 JSDoc 주석만 정정했다.
이번 변경은 FE 단독 범위로, BE/DB/UI 변경 없이 기존 API(API-SRM-012, API-CHG-007, API-ITAM-001, API-COM-002)를 재사용했다.

## 3. 변경 파일

- `source/frontend/src/routes/AppLayout.tsx`
- `source/frontend/src/components/layout/header.tsx`

## 4. 테스트 결과

통합 테스트 7건 전부 PASS했다(빌드, 5초 간격 실측, 백그라운드 정지·재개, 팝오버 오픈 중 merge 유지, 개별 X·모두 지우기+뱃지 재계산, 8건 cap 폐지 확인, API 연속 실패 시 backoff 없이 유지 후 복구).
테스트 중 `/api/v1/notifications/dismissals`가 간헐적으로 401 응답 후 다음 poll에 자동 복구되는 현상이 관찰됐다.
코드가 실패를 빈 이력으로 처리하도록 설계되어 있어 화면 기능에는 영향이 없었고, 완료 기준 7개 항목과 무관해 FAIL 처리하지는 않았다.
다만 세션당 1회 조회에서 5초 polling으로 API 호출 빈도가 늘면서 access token 만료 시점과의 race가 더 자주 노출될 가능성이 있다는 소견이 있었다(이번 요구사항 범위 밖이라 별도 조치는 하지 않음).
커밋 `6f14377`로 반영했다.
