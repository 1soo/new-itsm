---
date: 20260722-040618
domain: approval-engine
result: partial
keywords: [targetState 4번째 매칭 축, 요청자=현재 호출자, 반려 후 재승인요청, 생성시점 게이트, 담당자별 역할매칭]
---

# 통합 테스트 결과 — approval-engine (상태별 승인자 지정 확장, 2026-07-22 유지보수) (20260722-040618)

## 요약

- 총 18건 · 성공 16 · 실패 2
- **핵심 신규 메커니즘(신규 상태 게이트/생성 시점 게이트/반려-재승인요청/담당자별 역할매칭/canApproverView)은 전부 정상 동작(PASS)** — 이번 유지보수의 설계 의도 자체는 올바르게 구현됨
- 그러나 **① 기존 게이트 지점의 실질적 회귀(TC-REGRESSION-001, CRITICAL)** 와 **② SRM 상세화면 전이 버튼 disable 로직 미일반화(TC-FE-001)** 2건 실패 — 둘 다 "요청자=현재 호출자" 방침 확장의 파급 효과를 코드 전반에 일관되게 반영하지 못한 데서 기인

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | `./gradlew build -x test`, `npm run build` 모두 오류 없이 성공 | |
| TC-STATES-001 | PASS | `GET /domains/SERVICE_REQUEST/states` → 200, SUBMITTED/VALIDATED/ROUTED/IN_FULFILLMENT/FULFILLED/CLOSED 6개 반환 | |
| TC-ADMVALID-001 | PASS | targetState=VALIDATED + requesterRoleIds=[] → 400 "targetState를 지정하려면 requesterRoleIds가 최소 1개 이상이어야 합니다." | |
| TC-ADMVALID-002 | PASS | targetState="NOT_A_REAL_STATE" → 400 "정의되지 않은 targetState입니다." | |
| TC-ADM-CREATE-001 | PASS | domain=SERVICE_REQUEST/targetState=SUBMITTED/subtype=2(비밀번호 초기화)/requesterRoleIds=[END_USER]/1차 AND[APPROVER] → 201(id=22) | |
| TC-ADM-CREATE-002 | PASS | targetState=VALIDATED/subtype=2/requesterRoleIds=[SERVICE_DESK_AGENT]/1차 AND[APPROVER] → 201(id=23) | |
| **TC-REGRESSION-001** | **FAIL** | 기존 규칙(id=1, domain=SERVICE_REQUEST, subtype=1"노트북 신청", 요청자 스코프=END_USER, target_state=IN_FULFILLMENT 백필)로 신규 SR(SRM-2026-0042) 생성 후 agent@itsm.local(SERVICE_DESK_AGENT)이 SUBMITTED→VALIDATED→ROUTED→IN_FULFILLMENT 순차 전이 → **IN_FULFILLMENT 전이가 409 없이 200으로 통과**(회귀). 아래 "실패 항목 분석" 참조 | |
| TC-NEWGATE-001 | PASS | user@itsm.local이 item2로 신규 제출(SRM-2026-0043) → 409(`APPROVAL_PENDING`, approvalRequestId=8) 확인 후, po@itsm.local로 `GET /api/v1/service-requests/43` 조회 시 200 + `status=SUBMITTED` + `approval.status=IN_PROGRESS`(생성 시점 게이트가 막혀도 마스터 레코드 롤백 없음 확인) | |
| TC-VIEWAUTH-001 | PASS | cab@itsm.local(APPROVER 단독 보유, AGENT/PROCESS_OWNER 아님)이 `GET /api/v1/service-requests/43` 200 조회 성공(매칭 규칙의 승인자 역할이라 동적 조회 권한 인정, `canApproverView`) | |
| TC-REJECT-001 | PASS | cab@itsm.local(1차 APPROVER)이 approvalRequestId=8을 REJECT → 200, 인스턴스 REJECTED. (최초 한글 reason 시도 시 400 발생했으나 원인은 테스트 셸(bash/curl, UTF-8 인코딩)측 문제로 확인, ASCII reason 재시도 시 정상 200 — 애플리케이션 결함 아님) | |
| TC-RESUBMIT-001 | PASS | user@itsm.local이 `POST /api/v1/approvals/resubmit {SERVICE_REQUEST, 43}` → 200, 신규 approvalRequestId=9(기존 8과 다름, targetState=SUBMITTED 유지) → cab@itsm.local APPROVE → 200 APPROVED, SR 43 상세 `approval.status=APPROVED` 확인 | |
| TC-ACTORROLE-001 | PASS | SR 43에 agent@itsm.local 배정 후 VALIDATED 전이 시도 → 409(`APPROVAL_PENDING`, approvalRequestId=10, id=23 규칙이 호출자 agent(SERVICE_DESK_AGENT)와 매칭) — **동일 티켓의 SUBMITTED 게이트는 요청자(user, END_USER)로, VALIDATED 게이트는 전이자(agent, SERVICE_DESK_AGENT)로 서로 다른 역할이 매칭됨을 확인(담당자별 승인요청자 역할 매칭 핵심 시나리오)** | |
| TC-ACTORROLE-002 | PASS | cab@itsm.local이 approvalRequestId=10 APPROVE → 200 → agent@itsm.local이 VALIDATED 재시도 → 200 `status=VALIDATED` | |
| TC-NOROUTE-001 | PASS | agent@itsm.local이 SR 43을 ROUTED→IN_FULFILLMENT→FULFILLED 순차 전이(item2는 이 구간에 매칭 규칙 없음) → 전 구간 200, 게이트 없음(회귀 없음) | |
| TC-CROSSDOMAIN-001 | PASS | cm@itsm.local이 CHANGE RFC 신규 생성(CHG-2026-0003, 201) 및 REQUESTED→REVIEW 전이(200) — 활성 규칙 없는 도메인은 게이트 없이 정상 동작 | |
| **TC-FE-001** | **FAIL** | agent@itsm.local로 SRM-2026-0044(VALIDATED 게이트 IN_PROGRESS 상태, approvalRequestId=12) 상세 진입 → 상태 배지 "검증됨(승인대기)"는 정상 노출되었으나, **"검증 완료"(VALIDATED 전이) 버튼이 disabled 처리되지 않고 클릭 가능한 상태로 남아있음**(클릭 시 토스트 "승인 대기 중에는 이행할 수 없습니다"는 정상 노출 — 백엔드 데이터 무결성 자체는 안전, FE UX만 결함). 아래 "실패 항목 분석" 참조 | |
| TC-FE-002 | PASS | user@itsm.local로 SRM-2026-0045(SUBMITTED 게이트 REJECTED, approvalRequestId=13) 상세 진입 → 배지 "제출됨(반려됨)" + 반려 사유("1차 반려 사유: fe test reject") + "재승인요청" 버튼 노출 확인 → 클릭 → 토스트 "재승인요청이 접수되었습니다" + 새로고침 없이 배지가 "제출됨(승인대기)"로 즉시 갱신(신규 approvalRequestId 발급 확인) | |
| TC-FE-003 | PASS | admin@itsm.local로 `/admin/approval-processes/new` 진입 → 도메인 "서비스 요청" 선택 → 적용 상태를 "전체 상태 공통" 아닌 "라우팅됨"으로 선택 → 승인 요청자 역할 미지정 상태에서 "이 상태에서 요청할 역할을 지정하세요" 인라인 에러 + "생성 완료" 저장 버튼 `disabled` 확인 | |
| TC-FE-004 | PASS | admin@itsm.local로 `/admin/approval-processes` 목록에서 "적용 상태" 컬럼에 TC-ADM-CREATE-001/002 규칙이 각각 "제출됨"/"검증됨"으로 정상 노출, 기존 id=1 규칙도 "이행 중"으로 정상 표시 확인 | |

## 실패 항목 분석

### TC-REGRESSION-001 (CRITICAL) — 기존 게이트 지점의 실질적 회귀

- **현상**: 마이그레이션 전부터 존재하던 유일한 활성 규칙(`approval_process.id=1`, domain=SERVICE_REQUEST, request_subtype_key='1'/"노트북 신청", 요청자 스코프=**END_USER**, 41번 마이그레이션으로 target_state=IN_FULFILLMENT 백필, tier 37→55)이, 이번 유지보수 이후 **더 이상 실제로 게이트를 걸지 못한다.** 신규 SR(SRM-2026-0042)을 생성해 agent@itsm.local이 SUBMITTED→VALIDATED→ROUTED→IN_FULFILLMENT까지 전이했을 때 IN_FULFILLMENT 전이가 409 없이 200으로 통과했다.
- **원인**: `common.md` 0절 확정 방침 5("요청자=그 전이를 지금 시도하는 현재 호출자")에 따라 `ServiceRequestService.transition()`의 `checkGate` 호출 시 `requesterId`가 `sr.getRequesterId()`(원 요청자)에서 `principal.userId()`(현재 호출자)로 교체되었다(`ServiceRequestService.java:313`). 그런데 `ServiceRequestService.assertTransitionRole()`(`:362-371`)에 따르면 **CLOSED를 제외한 모든 전이(VALIDATED/ROUTED/IN_FULFILLMENT/FULFILLED)는 SERVICE_DESK_AGENT 역할만 호출 가능**하고 END_USER는 애초에 호출 권한이 없다. 즉 IN_FULFILLMENT 전이의 실제 호출자는 항상 SERVICE_DESK_AGENT이지, 요청자 스코프로 지정된 END_USER가 될 수 없다. 기존 규칙(id=1)의 요청자 스코프가 END_USER로 남아있는 한, `matchProcess`의 요청자 역할 매칭에서 항상 실패해 규칙이 선택되지 않고(`candidate==null`), 결과적으로 게이트가 조용히 사라진다.
- **영향 범위**: 이 원인은 SRM 국한이 아니라 **9개 도메인 모두에 구조적으로 동일하게 적용**된다 — "생성 시점(요청자=END_USER 등 요청 주체)"과 "이후 전이 시점(요청자=담당 에이전트/관리자)"의 호출자 역할이 다른 모든 도메인에서, 마이그레이션 이전에 "원 요청자" 기준으로 구성된 기존 규칙은 이후 전이 시점에는 더 이상 매칭되지 않게 된다. 다만 로컬 DB 확인 결과(41번 마이그레이션 파일 주석, 본 세션 DB 조회로 재확인) **실사용 활성 규칙은 SRM id=1 하나뿐**이라 다른 8개 도메인에서는 즉시 재현되는 실사용 데이터가 없다.
- **판단 필요 사항(dev-lead 확인 요청)**: 이는 코드 결함이라기보다, "요청자=현재 호출자"로의 의미 전환이 **기존에 등록된 규칙의 요청자 스코프까지는 자동으로 보정하지 않는다**는 설계 공백에 가깝다. 아래 중 하나의 대응이 필요해 보인다.
  1. 41번 마이그레이션(또는 별도 후속 마이그레이션)에서 기존 하드코딩 게이트 지점 규칙의 요청자 스코프를, 실제로 그 전이를 호출할 수 있는 역할(SRM은 SERVICE_DESK_AGENT)로 함께 보정.
  2. 위 보정이 데이터 성격상 어렵다면(요청자 스코프가 관리자가 명시적으로 설정한 값이라 임의 변경이 부적절), 관리자에게 "이 규칙은 이 상태로의 전이를 호출할 수 있는 역할과 요청자 스코프가 달라 더 이상 매칭되지 않을 수 있습니다" 같은 경고를 관리자 화면(SCR-ADMIN-007/008)에서 노출.
  3. 의도된 동작(관리자가 마이그레이션 후 직접 규칙을 재확인·수정해야 함)이라면 유지보수 이력 문서에 이 부작용을 명시적으로 기록.
- **재현 방법**: `PATCH /api/v1/service-requests/{id}/status {targetStatus:"IN_FULFILLMENT"}`을 agent@itsm.local(SERVICE_DESK_AGENT)로 호출, 대상 SR은 catalogItemId=1("노트북 신청")로 생성.

### TC-FE-001 — SRM 상세화면 전이 버튼 disable 로직이 신규 targetState로 일반화되지 않음

- **현상**: SRM-2026-0044(catalogItemId=2, VALIDATED 게이트 IN_PROGRESS)의 상세화면에서 상태 배지는 "검증됨(승인대기)"로 정상 노출되지만, "검증 완료"(VALIDATED로의 전이) 버튼이 `disabled` 처리되지 않고 클릭 가능한 상태로 남아있다. 클릭하면 백엔드가 정상적으로 409를 반환하고 토스트가 뜨므로 **데이터 무결성 문제는 없으나**, 이전 단계(2026-07-11 최초 승인 엔진 도입 이후 IN_FULFILLMENT 단일 게이트 기준)에서는 승인 대기 중 전이 버튼을 사전에 `disabled`+tooltip 처리하는 것이 확립된 UX 패턴이었다(예: `docs/04_test/20260711-*` 각 라운드의 "전이 버튼 disabled+tooltip" 검증 항목).
- **원인**: `source/frontend/src/features/service-request/RequestDetailPage.tsx:241`
  ```
  const transitions = (detail.allowedTransitions ?? fallbackTransitions(detail, isAgent, isEndUser)).filter(
    (target) => !(target === "IN_FULFILLMENT" && approvalPending),
  );
  ```
  이 필터가 **"IN_FULFILLMENT"를 하드코딩**하고 있다. 2026-07-22 유지보수로 게이트가 임의 상태(targetState)에 걸릴 수 있도록 일반화됐지만, 이 필터는 여전히 옛 단일 하드코딩 게이트 지점만 가정한다. `detail.approval.targetState`(이미 응답에 포함됨, `ApprovalInfo.targetState`)와 비교하도록 `target === detail.approval.targetState`로 바꿔야 신규 상태(VALIDATED 등)에 대해서도 올바르게 버튼을 숨기거나 비활성화할 수 있다. 같은 파일 70번 줄의 `fallbackTransitions` 내부에도 `else if (s === "ROUTED" && !approvalPending) out.push("IN_FULFILLMENT")`로 동일한 가정이 남아있어 함께 확인이 필요하다.
- **영향 범위**: 이 하드코딩 패턴은 `RequestDetailPage.tsx`(SRM)에서만 발견됨(grep 확인, 다른 8개 도메인 상세 페이지에는 이런 클라이언트 사이드 필터 자체가 없어 이 결함의 대상이 아님 — 다른 도메인은 애초에 이 필터링을 하지 않고 백엔드 409+토스트에만 의존하는 것으로 보임, 필요 시 개발 에이전트가 재확인).
- **판단 필요 사항**: 기능적 안전(백엔드 게이트)은 보장되므로 severity는 UX 결함 수준이나, "승인대기 중 전이 버튼 사전 차단"이 기존에 확립된 패턴이라 회귀로 분류.

## 테스트 환경 조성 참고

- 신규 계정 생성 없이 기존 시드 계정만 사용: `admin@itsm.local`(SYSTEM_ADMIN), `user@itsm.local`(END_USER), `agent@itsm.local`(SERVICE_DESK_AGENT), `po@itsm.local`(PROCESS_OWNER), `cab@itsm.local`(APPROVER), `cm@itsm.local`(CHANGE_MANAGER) — 공통 비밀번호 `Admin@1234`.
- 관리자 API로 신규 규칙 2건 생성: `approval_process.id=22`(SERVICE_REQUEST/SUBMITTED/subtype=2/END_USER), `id=23`(SERVICE_REQUEST/VALIDATED/subtype=2/SERVICE_DESK_AGENT) — 둘 다 1차 승인자=APPROVER(AND). dev-lead 확인 후 정리 여부 결정 필요(삭제 시 soft-delete라 이력에는 남음).
- 테스트로 생성된 잔여 데이터: SR 42(SRM-2026-0042, item1, FULFILLED까지 진행, TC-REGRESSION-001 재현용)/43(SRM-2026-0043, item2, FULFILLED)/44(SRM-2026-0044, item2, VALIDATED 게이트 IN_PROGRESS 상태로 남음, TC-FE-001 재현 가능하도록 의도적으로 미승인 유지)/45(SRM-2026-0045, item2, SUBMITTED, resubmit 후 APPROVED 안 함·승인대기 상태로 남음). CHG-2026-0003(REVIEW 상태).
- 한글 `reason` 파라미터를 bash/curl로 전달할 때 인코딩 깨짐으로 400이 발생하는 현상을 확인(TC-REJECT-001) — 이는 로컬 테스트 셸 환경 문제이며 애플리케이션 결함이 아님. 이후 결정 관련 API 호출은 ASCII 텍스트로 재시도.
- Playwright는 매 화면 전환(FE-001/FE-002/FE-003 사이)마다 `localStorage`/`sessionStorage`/쿠키를 초기화한 뒤 재로그인해 세션을 격리했다.
