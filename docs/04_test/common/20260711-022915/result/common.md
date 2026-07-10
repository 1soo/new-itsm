# 통합 테스트 결과 — Common (헤더 알림 팝오버 v2 · 2줄 레이아웃 재테스트) (20260711-022915)

## 요약
- 총 10건 · 성공 10 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | FE `npm run build`(tsc -b && vite build) 성공. BE `gradlew.bat build`(컴파일) 성공 + `gradlew.bat test --rerun-tasks` 전체 재실행 BUILD SUCCESSFUL(테스트 실패 없음, DTO 필드 추가분 포함) | - |
| TC-NOTIF-101 | PASS | cab@itsm.local 알림 팝오버 항목이 1행(도메인 라벨+우측 시간)/2행(제목) 2줄 구조로 렌더링. "상세 보기" 버튼 존재 | shots/tc-notif-101-102-103-structure.png |
| TC-NOTIF-102 | PASS | "서비스요청 승인" 항목 2행 제목="노트북 신청"(catalogItemName, v1의 조합 문구 아님), 1행 우측 시간="1일 전"(requestedAt 2026-07-09 기준, 7일 미만) | 위와 동일 스크린샷 |
| TC-NOTIF-103 | PASS | "변경 승인" 항목 2행 제목="E2E RFC 생성 테스트"(summary), 1행 우측 시간="1일 전"(createdAt 기준) | 위와 동일 스크린샷 |
| TC-NOTIF-104 | PASS | am@itsm.local 자산 만료 4건 모두 1행 우측이 상대 시간이 아닌 "{expiryDate} 만료" 형식(예: "2026. 7. 20. 만료"), 2행 제목이 자산명과 일치(예: "Retest AWS Reserved Instance") | shots/tc-notif-104-107-assets.png |
| TC-NOTIF-105 | PASS | cab 계정, 최초 팝오버 오픈 시 "1일 전" 표시 → 팝오버 닫고 `Date.now`를 +8일 오프셋으로 오버라이드 후 재오픈 시 "2026. 7. 9."(절대 날짜)로 변경 확인 — 데이터 로딩 시점이 아닌 팝오버 오픈 시점 기준 재계산 확인 | shots/tc-notif-105-recalc.png |
| TC-NOTIF-106 | PASS(v1 회귀) | cab 계정 뱃지="알림 2건", "변경 승인" 항목 "상세 보기" 클릭 시 `/changes/9`로 이동하고 팝오버 닫힘(정렬 서비스요청→변경 순서는 TC-NOTIF-101~103에서 이미 확인) | shots/tc-notif-106-chg-detail.png |
| TC-NOTIF-107 | PASS(v1 회귀) | am 계정 뱃지="알림 4건", 팝오버 폭 320px, 첫 자산 항목 "상세 보기" 클릭 시 `/assets/8`로 이동 및 팝오버 닫힘 | shots/tc-notif-104-107-assets.png, tc-notif-107-asset-detail.png |
| TC-NOTIF-108 | PASS(v1 회귀) | user@itsm.local(END_USER) 뱃지 미노출(`aria-label="알림"`), 팝오버 열면 "새로운 알림이 없습니다" | shots/tc-notif-108-empty.png |
| TC-NOTIF-109 | PASS(v1 회귀) | admin@itsm.local(SYSTEM_ADMIN) 뱃지="알림 4건", 팝오버 항목 전부 "자산 만료" 카테고리만 노출(개인 배정 승인 대기 0건) | shots/tc-notif-109-admin.png |

## 실패 항목 분석
- 없음

## 참고 · 데이터 제약
- v2의 2행 제목(catalogItemName/summary/자산명)은 v1의 조합 문구보다 짧아, 현재 시드 데이터(모두 40자 이하)로는 2행 truncate(40자 초과)를 실 데이터로 재현하지 못했다. `truncateNotificationText` 함수 자체는 v1 재테스트(`docs/04_test/common/20260711-015654/`)에서 42자 입력으로 이미 실증 PASS했고 이번 변경에서 해당 함수는 그대로(변경 없음)이므로 회귀 위험은 낮다고 판단, 코드 검토로 대체하고 별도 TC는 실행하지 않았다.
- v1에서 남아있던 동일한 데이터 제약(8건 상한 slice 실증 불가)도 이번 v2에서 유지된다.
