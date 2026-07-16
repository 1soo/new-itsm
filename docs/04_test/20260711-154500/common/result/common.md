---
date: 20260711-154500
domain: common
result: pass
keywords: [알림polling, 백그라운드정지, merge정책]
---

# 통합 테스트 결과 — common (헤더 알림 5초 polling 전환, 20260711-154500)

> 환경: React CSR(:5173) / Spring Boot(:8080) / PostgreSQL(itsm-postgres, docker `itsm-postgres`)
> 대상: `source/frontend/src/routes/AppLayout.tsx`(FE 단독, BE/DB/API 계약 변경 없음)

## 요약

- 총 7건 · 성공 7 · 실패 0 ✅ **전 항목 통과**

## 상세

| TC ID | 결과 | 실제 동작 | 비고(증적) |
|-------|------|-----------|------------|
| TC-BUILD-001 | PASS | `npm run build`(tsc -b && vite build) 오류 없이 성공(`✓ built in 6.26s`) | 콘솔 출력 |
| TC-POLL-001 | PASS | cab@itsm.local(APPROVER) 로그인 후 `/api/v1/approvals?type=service-request`·`/api/v1/approvals?type=change`·`/api/v1/notifications/dismissals`가 정확히 5초 간격(응답 `date` 헤더 06:46:24→06:46:29 등)으로 반복 호출됨. am@itsm.local(ASSET_MANAGER)은 `/api/v1/assets?expiringWithinDays=30&size=8`만 5초 간격 호출되고 승인 대기 API는 호출되지 않아 역할별 대상 범위가 유지됨을 확인 | 네트워크 요청 로그(index 217~235: cab / 220~231: am) |
| TC-POLL-002 | PASS | am 세션에서 `document.hidden=true`+`visibilitychange` 강제 발생 후 12초(2주기 이상) 대기 동안 `/api/v1/assets` 요청 0건(정지 확인). `document.hidden=false` 복귀 시 즉시 1회 재조회(06:50:27) 후 5초 뒤 정상 주기(06:50:32) 재개 확인 | 네트워크 요청 로그(index 227 이후 12초간 정지 → 228/229 즉시 재조회 → 230/231 5초 후 재개) |
| TC-POLL-003 | PASS | cab 세션에서 팝오버를 연 채로 백엔드 API를 통해 신규 변경 승인 대기 2건(Polling test change 8·9)을 생성 → 팝오버를 닫지 않은 상태로 8초 후 목록 뒤에 자동 추가됨(기존 7건 순서 그대로 유지, 뱃지 7→9). 반대로 표시 중이던 항목(changeId 19, "Polling test change 1")을 백엔드 API로 승인 처리(원본에서 제거)한 뒤 다음 poll(8초 후)에도 팝오버에서 사라지지 않고 그대로 남아있음을 확인(뱃지만 9→8로 서버 기준 감소, 표시 목록은 9건 유지) | 스냅샷(팝오버 오픈 상태로 신규 2건 append 확인 및 승인 처리 후에도 항목 잔존 확인) |
| TC-POLL-004 | PASS | 개별 X 클릭 시 해당 항목 즉시 제거+뱃지 -1(8→7), 이후 poll(6초)에도 재등장하지 않음(확인처리 이력 필터링 확인). "모두 지우기" 클릭 시 표시 중이던 전체(8건)가 즉시 확인처리되어 "새로운 알림이 없습니다"로 전환·뱃지 숨김. 벨 뱃지가 표시 목록 크기(9건)와 무관하게 매 poll마다 서버 기준(확인처리 제외 전체 대기 합계)으로 재계산됨을 TC-POLL-003의 8건 변화에서 확인 | 스냅샷(개별 X 후 재등장 없음, 모두 지우기 후 빈 상태) |
| TC-POLL-005 | PASS | cab 기준 사전 조성한 9건(SR 1건 + CHG 8건, 그중 이미 확인처리된 건 제외 실질 표시 9건)이 팝오버에 잘리지 않고 전체 렌더링됨(`ul.scrollHeight=518px` > `clientHeight=320px`로 스크롤 필요 확인, 기존 "상위 8건" cap 폐지 확인) | `browser_evaluate` 결과(scrollHeight/clientHeight), 스냅샷(9개 listitem) |
| TC-POLL-006 | PASS | am 세션에서 `page.route`로 `/api/v1/assets` 요청을 강제 실패(`net::ERR_FAILED`)시킨 뒤 15초(3주기) 대기 — 요청은 backoff 없이 5초 고정 간격으로 계속 재시도(index 239~251, 5초 간격 실패 반복)되었고, 화면의 알림 목록(4건)·뱃지가 그대로 유지되었으며 오류 토스트도 표시되지 않음. `unroute` 후 다음 주기(6초 후)에 정상 200 응답으로 자동 복구됨 | 네트워크 요청 로그(index 239~255 FAILED, 257부터 200 복구), `shots/am-notifications-after-failure-recovery.png` |

## 실패 항목 분석

없음(전 항목 통과).

## 참고 관찰(실패 아님, dev-lead 참고용)

- 테스트 중 `/api/v1/notifications/dismissals` 요청이 간헐적으로 401(Unauthorized)을 반환한 뒤 바로 다음 poll 주기에는 정상 200으로 복구되는 현상을 다건 관찰(예: index 245, 280, 288). 코드상 `commonApi.listDismissals()` 실패는 `.catch(() => new Set())`로 빈 이력 취급하도록 이미 설계되어 있어 화면 표시(알림 목록·뱃지)에 실질적 영향은 없었고 에러 토스트도 발생하지 않았다(관찰 범위 내 기능 저하 없음). 다만 세션당 1회 조회이던 기존 방식보다 5초 polling으로 API 호출 빈도가 크게 늘면서 access token 만료 시점과 겹치는 race(동시 4개 API 병렬 호출 중 일부만 401→refresh 타이밍 어긋남 추정)가 이전보다 더 자주 노출될 가능성이 있다. 이번 유지보수 요청의 완료 기준(7개 항목)에는 포함되지 않는 별개 영역(apiClient 토큰 재발급 로직)이라 실패로 판정하지 않았으나, 향후 회귀 시 참고할 수 있도록 기록한다.

## 사전 데이터 조성 내역(테스트 목적, backend API 직접 호출)

- cm@itsm.local로 변경 요청 9건 생성 후 REVIEW→PLANNING→APPROVAL 전이(changeId 19~27, "Polling test change 1~9") → cab@itsm.local 승인 대기함에 8건 cap 폐지·merge 검증용 데이터 확보.
- changeId 19는 테스트 중 cab@itsm.local 계정으로 API 직접 승인 처리(원본 제거 후에도 표시 유지되는지 검증 목적).
- 위 테스트용 변경 요청들은 실제 업무 데이터가 아니므로 후속 정리가 필요하면 dev-lead에 확인 요청.
