# 유지보수 이력 — srm

> 유지보수 일시: 20260715-031710 · 도메인: srm

## 1. 요구사항

관리자가 승인 프로세스를 생성한 이후, 그 조건에 해당하는 서비스 요청을 등록했는데도 승인 목록에서 조회되지 않는다는 사용자 문의가 있었다.
확인 결과 이는 매칭 로직 버그가 아니라, 서비스 요청이 상태 전이(IN_FULFILLMENT) 게이트 평가 시점 이전(등록 직후)이라 승인 인스턴스가 아직 생성되지 않은 정상 동작이었다.
다만 이 상태에서 서비스 요청 상세 화면에 "이 요청에는 승인 절차가 없습니다"라는 문구가 노출되어, 실제로는 규칙이 있을 수 있는데도 "규칙 자체가 없다"고 오해하게 만드는 문제가 있어 UX 개선이 필요했다.

## 2. 해결 방법

서비스 요청 상세(SCR-SRM-005)의 승인 패널 문구를 요청 상태에 따라 분기하도록 수정했다.
게이트 평가 이전 상태(SUBMITTED/VALIDATED/ROUTED)에서는 "아직 승인 대상 여부가 결정되지 않았습니다" 류의 중립 문구를 표시한다.
게이트 평가 이후 상태(IN_FULFILLMENT 이상)에서 승인 인스턴스가 없는 경우에는 기존 "이 요청에는 승인 절차가 없습니다" 문구를 그대로 유지한다.

## 3. 변경 파일

- `source/frontend/src/features/service-request/RequestDetailPage.tsx`
- `source/frontend/src/i18n/locales/ko/service-request.json`
- `source/frontend/src/i18n/locales/en/service-request.json`

## 4. 테스트 결과

통합 테스트 14건 전부 PASS했다(`docs/04_test/20260715-112437/srm/`).
SUBMITTED/VALIDATED/ROUTED 상태와 IN_FULFILLMENT 이상 상태 각각에서 승인 패널 문구가 올바르게 분기 표시됨을 확인했다.
커밋 `35c2375`(본 기능), `82a173c`(stale tier 하드코딩 정리)로 반영했다.
