# API 명세서 — 공통 (Common)

> 도메인: common · 버전: 0.6

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-22 | 프랙티스별 상태(state)별 승인자 지정 확장(유지보수 요청) — 게이트 체크에 4번째 매칭 축 `targetState` 반영, 최신 인스턴스 상태별 분기(IN_PROGRESS→409 APPROVAL_PENDING, REJECTED→409 APPROVAL_REJECTED 신규), 반려 후 재승인요청 API-COM-006 신규, 생성 시점(최초 상태) 게이트 규칙 추가, requesterId 산출을 "티켓 고정 필드"에서 "현재 호출자(SecurityUtils.currentPrincipal().userId())"로 통일, API-COM-003/004 응답에 targetState/targetStateLabel 추가, 0-1절 `canApproverView` 집계 로직을 "tier 최고 1건" 방식에서 "targetState 무관 전체 후보 role 합집합" 방식으로 변경(기존 하드코딩 게이트의 승인자 상세조회 권한 회귀 방지) |
| 2026-07-11 | 최초 작성. 전 도메인 공용 승인 대기함·결정 API(API-COM-003~005) 신규(기존 도메인별 전용 승인 API 대체) |
| 2026-07-15 | 승인 대상자 역할 기반 동적 상세조회 권한 신규(0-1절) |
| 2026-07-17 | 동적 폼 스키마·제출 데이터 공통 서버 재검증 규칙 신규(0-2절), SRM/ESM 공용 적용 |
| 2026-07-18 | 0-2절을 form.io 스키마 기반에서 SRM 자체 8×n 그리드 스키마 기반으로 재정의, SRM 전용임을 명시(ESM은 이 재검증기를 사용한 적 없음이 코드 확인됨) |
| 2026-07-18 | 0-2절 검증 절차를 "위반 전체 집계"에서 "첫 번째 위반 즉시 반환"으로 변경(FE 순차 1건 표시와 계약 통일), label 타입은 검증 대상에서 제외 |
| 2026-07-18 | 0-2절 검증 대상 제외 타입에 guide(정적 안내/가이드) 추가 |
| 2026-07-18 | 0-2절 검증 대상 제외 타입을 guide-text/guide-file로 갱신(guide 타입 폐기) |
| 2026-07-18 | 0-2절 정규식(regex) 검증을 type=text 전용으로 축소(다른 입력 타입에 regex 값이 있어도 서버가 평가하지 않음), label 타입은 그리드 배치 컴포넌트에서 폐기되고 컴포넌트에 부여하는 라벨(태그)로 대체(검증 로직과 무관한 메타데이터) |

## 공통 규약

- **Base Path**: `/api/v1`
- **인증 헤더**: 보호 API는 `Authorization: Bearer {accessToken}` 필요.
- **표준 오류 응답 본문** (모든 4xx/5xx 공통):
  ```json
  { "code": "string · 오류 코드", "message": "string · 사용자 메시지", "timestamp": "ISO-8601" }
  ```
- **인가 실패**: 미인증 401.

## 0. 설계 배경 — 공통 승인 게이트·결정 엔진

승인 프로세스 커스텀 기능에 따라 서비스요청·변경·지식뿐 아니라 인시던트·문제 등 전 도메인이 동일한 승인 엔진을 공유한다. 규칙 정의(`docs/02_plan/database/common.md` `approval_process*`)의 CRUD는 관리자 전용이라 [auth.md](auth.md)에 별도로 정의하며(API-AUTH-023~031), 이 문서는 **인스턴스(진행 중 승인 건) 조회·결정·재승인요청 API**만 다룬다.

프랙티스(도메인)의 **모든 상태 전이 지점**(최초 상태 포함)이 승인 게이트의 대상이 될 수 있다(유지보수 요청 — 기존에는 도메인당 정확히 1곳만 하드코딩 게이트였다). 각 게이트 호출은 도착하려는 상태값을 `targetState`로 함께 전달한다.

**요청자(requesterId) 산출 규칙**: 모든 게이트 호출(생성 시점·중간 전이 시점·재승인요청 공통)에서 `requesterId`는 티켓에 고정된 원 요청자 필드가 아니라 **그 전이를 지금 시도하는 현재 호출자**(`SecurityUtils.currentPrincipal().userId()`)다. 같은 티켓이라도 상태 전이마다(등록 단계=제출자, 이관 단계=SD Agent, 완료 단계=개발자 등) 승인요청자가 달라질 수 있다.

**게이트 체크(내부 공통 로직, 각 도메인 상태 전이 API 및 생성 API가 호출)**: 게이트가 걸린 target 전이를 처리하기 전(중간 전이) 또는 엔티티 커밋 후 별도 트랜잭션에서(최초 상태 생성 시점) 아래 순서로 판정한다.

1. 대상 티켓의 (도메인, **targetState**, 요청유형 스코프 값, 현재 호출자 보유 역할)로 `approval_process`를 조회한다. 후보 = `domain` 일치 AND (`target_state IS NULL` OR 대상 targetState와 일치) AND (`request_subtype_key IS NULL` OR 대상 티켓의 요청유형 값과 일치) AND (요청자 역할 스코프 없음 OR 현재 호출자가 스코프 역할 중 하나라도 보유). 후보 중 `priority_tier`가 가장 큰(가장 좁은 범위) 규칙 1개를 선택한다.
2. 매칭 규칙이 없거나, 매칭 규칙의 승인자 차수(`approval_process_step`)가 0개면 **승인 없이 그대로 전이(또는 생성)를 허용**한다(인스턴스 생성 안 함).
3. 매칭 규칙에 차수가 1개 이상이면, 해당 티켓의 **이 targetState**로 진행 중인 최신 승인 인스턴스(`findTopByTicketTypeAndTicketIdAndTargetStateOrderByIdDesc`)가 있는지 확인한다.
   - 없으면 규칙을 스냅샷하여 새 인스턴스(`approval_request`+`approval_request_step`+`approval_request_step_role`, `target_state` 포함)를 생성하고, 전이(또는 생성) 요청은 **409(`APPROVAL_PENDING`)**로 거부한다(응답에 생성된 `approvalRequestId` 포함, 승인 대기 안내). 최초 상태 게이트는 엔티티가 이미 REQUIRES_NEW로 커밋된 뒤이므로, 이 409는 마스터 레코드 자체를 롤백하지 않는다(레코드는 "임시/승인대기"로 남아 있고 이 게이트만 재시도 대상).
   - 이미 있고 `IN_PROGRESS`면 **409(`APPROVAL_PENDING`)**로 거부한다.
   - 이미 있고 `APPROVED`면 전이(또는 생성 확정)를 허용한다.
   - 이미 있고 `REJECTED`면 **409(`APPROVAL_REJECTED`, 신규 에러코드)**로 거부한다(자동으로 새 인스턴스를 만들지 않음 — 영구 차단이 아니라 아래 재승인요청 API-COM-006으로 사용자가 명시적으로 새 승인 사이클을 시작해야 한다).

**반려 후 재승인요청**은 자동 재시도가 아니라 사용자의 명시적 액션(API-COM-006)으로만 이루어진다. 자세한 내용은 2절 API-COM-006 참조.

**결정 처리(각 도메인 공용, API-COM-005)**: 차수의 `decision_mode`가 **OR**이면 그 차수에 필요한 역할 중 아무 역할이나 최초 1건의 결정이 기록되는 즉시 차수 전체가 그 결정(APPROVE/REJECT)으로 확정된다(공유 대기함 + 선처리자 결정, 기존 SRM/CHANGE 패턴과 동일). **AND**이면 차수에 필요한 각 역할마다 APPROVE가 모두 채워져야 차수가 APPROVED되며, 어느 역할이든 REJECT가 기록되면 그 즉시 차수·인스턴스 전체가 REJECTED로 확정된다. 처리자가 해당 차수에서 필요한 역할을 2개 이상 보유하면 1회 결정으로 보유한 역할 슬롯이 모두 채워진다. 차수가 APPROVED되면 다음 차수로 진행(`current_step_no` 증가)하고 마지막 차수까지 APPROVED되면 인스턴스 전체가 APPROVED로 확정되어 원래 전이가 재시도 시 허용된다. 인스턴스가 APPROVED/REJECTED로 확정되면, 해당 도메인은 후속 처리를 수행한다(예: KNOWLEDGE는 APPROVED 시 PUBLISHED로, REJECTED 시 DRAFT+반려사유로 전환 — 각 도메인 문서 참조).

## 0-1. 승인 대상자 역할 기반 동적 상세조회 권한

도메인 티켓 상세조회 API(예: SRM `GET /api/v1/service-requests/{id}`, CHANGE `GET /api/v1/changes/{id}` 등)가 자체 역할 규칙(요청자 본인·도메인 매니저 등) 외에, **승인자 역할(승인 대상자)** 보유자에게도 조회 권한을 부여할 때 공통으로 적용하는 판정 로직이다. 공용 승인 엔진(`common.approval.application.ApprovalGateService`)에 캡슐화된 `canApproverView(domain, requestSubtypeKey, requesterId)` 메서드로 제공되며, 각 도메인은 자신의 상세조회 접근 체크(예: SRM `assertCanView`, CHANGE `requireRole`)에서 기존 역할 조건과 **OR**로 호출한다.

**판정 대상**: SERVICE_REQUEST(SRM)/CHANGE/INCIDENT/PROBLEM/ASSET/VULNERABILITY/COMPLIANCE/ESM 8개 도메인(승인 프로세스가 정의될 수 있는 도메인과 동일 범위, KNOWLEDGE 제외 — KNOWLEDGE는 게이트키퍼 승인 전용 흐름이라 상세조회 RBAC 대상 아님).

**판정 절차(2026-07-22 유지보수 요청 — targetState 축 추가에 따라 집계 로직 변경)**:
1. `canApproverView`는 0절 게이트 체크가 쓰는 `matchProcess`("tier 최고 1건 선택")를 그대로 재사용하지 **않는다**. 대신 대상 티켓의 (도메인, 요청유형 스코프 값, 요청자 보유 역할) 조건에 매칭되는 **모든 `approval_process` 후보를 targetState 무관하게 전부 조회**한다 — `target_state IS NULL`인 전체 상태 공통 규칙 1건(있다면) + 그 도메인의 상태별(targetState별) 규칙 N건(있다면) 전부가 대상이다. 후보가 하나도 없으면 조회 권한 없음(false).
   > 기존 하드코딩 게이트 지점(예: SERVICE_REQUEST `IN_FULFILLMENT`)은 마이그레이션(41번)으로 `target_state`가 백필되므로, targetState를 무시하고 "전체 상태 공통 규칙만" 대상으로 하면 이미 배포된 이 규칙이 후보에서 빠져 기존 승인자의 상세조회 권한이 사라지는 회귀가 발생한다 — 반드시 상태별 규칙까지 전부 모아야 한다.
2. 1의 **모든 후보 규칙 각각**의 승인자 차수(`approval_process_step`) 전체에 걸쳐 지정된 승인 역할(`approval_process_step_role`, 특정 차수가 아니라 **전체 차수**를 대상으로 한다 — 실제 승인 인스턴스가 아직 생성되지 않아 "현재 차수"가 없는 시점에도 조회 가능해야 하므로)을 모아 **합집합**한다(어느 한 상태의 승인자이기만 해도 충분).
3. 로그인 사용자의 role claim이 2의 합집합 역할 집합과 하나라도 겹치면 조회 권한 있음(true), 아니면 없음(false).
4. 판정 기준은 **실제 승인 인스턴스(`approval_request`)가 아니라 규칙 설정(`approval_process`/`approval_process_step_role`) 자체와의 매칭**이다 — 인스턴스가 아직 생성되지 않았거나(게이트 평가 전), 이미 종료(APPROVED/REJECTED)된 뒤에도 동일하게 동작한다.

이 집계는 `ApprovalGateService`에 `matchProcess`와 별도의 신규 헬퍼(예: `collectApproverRoleCandidates(domain, requestSubtypeKey, requesterId)`, targetState 축을 필터링하지 않고 전체 후보를 순회)로 구현한다 — `checkGate`가 쓰는 "tier 최고 1건 선택" 경로와는 다른 별개 경로다.

**적용 방식(도메인별 차이)**:
- **SRM/CHANGE**: 기존에 존재하던 정적 "APPROVER 역할이면 도메인 내 모든 티켓 상세조회 가능" 권한을 폐지하고, 이 동적 판정으로 완전히 대체한다(요청자 본인 조회 등 기존 다른 조건은 유지).
- **INCIDENT**: 상세조회(API-INC-003)는 원래 백엔드에 역할 제한이 전혀 없어(FE 라우트 가드로만 SERVICE_DESK_AGENT/INCIDENT_MANAGER를 걸러내고 있었을 뿐, 인증된 사용자면 API 직접 호출로 전체조회가 가능한 결함성 상태) 이번에 **범위에 포함해 함께 정리**한다: 다른 도메인과 동일하게 백엔드에 `SecurityUtils.hasAnyRole(SERVICE_DESK_AGENT, INCIDENT_MANAGER)` 명시적 체크를 신설하고, 이 조건에 이 동적 판정을 OR로 추가한다.
- **ASSET**: 상세조회(API-ITAM-003)도 현재 백엔드 역할 제한이 없다(등록·수정·폐기·생애주기 전이만 `ASSET_MANAGER` 전용). 다만 이는 INCIDENT와 달리 **의도된 설계**다 — `AssetService` 클래스 주석에 "등록·수정·폐기·생애주기 전이만 ASSET_MANAGER 전용, 조회·CI·연계·지표는 인증된 사용자 전반 허용"이라고 명시되어 있다. 따라서 이번 유지보수에서 ASSET 상세조회를 좁히는 변경(ASSET_MANAGER 전용화)은 **포함하지 않는다** — 기존처럼 인증된 사용자 전반 허용을 유지하며, 승인자 역할 보유자는 이미 조회 가능하므로 이 동적 판정 자체가 사실상 no-op이다(백엔드 코드 변경 불필요, FE 라우트 가드만 추가).
- **PROBLEM/VULNERABILITY/COMPLIANCE/ESM**: 기존에는 각 도메인 매니저 역할(PROBLEM_MANAGER/VULNERABILITY_MANAGER/COMPLIANCE_OFFICER, ESM은 요청자 본인+DEPT_COORDINATOR) 전용으로 제한되어 있었고 APPROVER는 접근 불가였다. 이번에 이 동적 판정을 **신규로 추가**해 매칭되는 승인자 역할 보유자에게도 조회를 허용한다(기존 매니저 전용 조건은 유지, OR로 추가).

**FE 라우트 가드**: 위 7개 도메인(SRM/CHANGE는 기존부터, INCIDENT/ASSET/PROBLEM/VULNERABILITY/COMPLIANCE/ESM은 신규) 상세 화면의 `RequireRoles`(`source/frontend/src/routes/index.tsx`)에 `ROLE_APPROVER`를 추가해야 매칭된 승인자가 실제로 내비게이션할 수 있다. 라우트 가드는 역할 보유 여부만 굵게 거르는 관문이며, 실제 조회 가능 여부(매칭 여부)는 백엔드 403으로 최종 판정된다(매칭 안 되면 화면 진입 후 403 처리. 단 ASSET은 백엔드가 역할 무관 전면 허용이라 이 403 자체가 발생하지 않는다).

각 도메인의 상세조회 RBAC 최종 규칙은 [security/authorization/approver.md](../security/authorization/approver.md)(승인자 관점)와 각 역할 정의서(매니저 관점)를 함께 참조한다.

## 0-2. 동적 폼 스키마·제출 데이터 서버 재검증 (SRM 전용)

서비스 카탈로그(SRM)의 자체 8×n 그리드 폼 빌더([database/service-request.md](../database/service-request.md) `service_catalog_item.form_schema`, [screen/service-request.md](../screen/service-request.md) 5절 참조)의 제출 데이터는 클라이언트(그리드 렌더러의 정규식 검증) 검증만으로 신뢰하지 않고 **서버 재검증기**를 거친다. `common.form` 패키지에 위치하지만 현재 실제 호출자는 SRM `ServiceRequestService`뿐이다(ESM은 이 그리드 폼 빌더를 사용하지 않고 기존 레거시 EAV를 그대로 사용 — 부서 카탈로그 재구현이 확정되면 그때 ESM 연동 여부를 다시 설계한다).

- **캡슐화 위치**: `common.form.FormSubmissionValidator`(SRM `ServiceRequestService`가 요청 제출 유스케이스에서 호출).
- **입력**: 카탈로그 항목의 `form_schema`(8×n 그리드 스키마, `components` 배열)와 제출된 `formValues`(key-value) 맵.
- **검증 절차**: `form_schema.components`를 배열 순서대로 순회하며(중첩 레이아웃이 없는 평면 배열이라 재귀 순회 불필요), **첫 번째로 위반이 발견되는 컴포넌트에서 즉시 400을 반환**한다(여러 위반을 모아 반환하지 않음 — FE의 순차 1건 표시와 동일한 계약, [screen/service-request.md](../screen/service-request.md) 5.5절). `type=guide-text`/`guide-file`는 값이 없는 정적 컴포넌트라 검증 대상에서 제외한다(`guide`/그리드 배치형 `label` 타입은 폐기됨 — "라벨"은 컴포넌트에 부여하는 태그로만 남아 값·검증과 무관, [screen/service-request.md](../screen/service-request.md) 5.8절). 각 컴포넌트는 순서대로:
  1. `validation.required=true`이고 제출 값이 없으면 `REQUIRED_FIELD_MISSING`으로 즉시 거부.
  2. `validation.regex`가 지정돼 있고(**`type=text`에서만 평가** — 다른 타입에 값이 있어도 무시) 제출 값이 불일치하면 `FORM_FIELD_INVALID`로 즉시 거부(값이 없으면 1번에서 이미 거부되므로 정규식은 값이 있을 때만 평가).
  3. 위 두 검사를 모두 통과하면 다음 컴포넌트로 진행한다.
- **file 컴포넌트**: 제출 데이터에 파일을 base64 문자열로 인라인 포함하는 기존 방식을 유지한다 — 별도 업로드 API·크기 제한 검증은 이번 범위에 포함하지 않는다(대용량 첨부 필요 시 별도 유지보수로 업로드 API 설계 필요).

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-COM-001 | 알림 확인처리(개별/일괄) | POST | /api/v1/notifications/dismissals | 필요 |
| API-COM-002 | 확인처리된 알림 목록 조회 | GET | /api/v1/notifications/dismissals | 필요 |
| API-COM-003 | 승인 대기함 목록 조회(전 도메인 공용) | GET | /api/v1/approvals | 필요 |
| API-COM-004 | 승인 인스턴스 상세 조회 | GET | /api/v1/approvals/{approvalRequestId} | 필요 |
| API-COM-005 | 승인/반려 결정 | POST | /api/v1/approvals/{approvalRequestId}/decisions | 필요 |
| API-COM-006 | 반려 후 재승인요청 | POST | /api/v1/approvals/resubmit | 필요 |

## 2. API 상세

### API-COM-001 · 알림 확인처리(개별/일괄)

헤더 알림 드롭다운(SCR-COM-002)의 "모두 지우기"(items에 현재 표시 중인 알림 전체 — 5초 polling merge로 누적된 만큼, 상한 없음)와 개별 X 버튼(items에 1건)이 공용으로 사용한다. 확인처리는 표시 여부에만 영향을 주며, 원본 승인 대기(API-COM-003)·자산 만료(API-ITAM-001) 데이터는 변경하지 않는다.

- **Endpoint**: `POST /api/v1/notifications/dismissals`
- **인증**: 필요(Access Token)
- **Header**:
  | 이름 | 필수 | 설명 |
  |------|------|------|
  | Authorization | Y | `Bearer {accessToken}` |
  | Content-Type | Y | application/json |
- **Request Body**:
  ```json
  {
    "items": [
      {
        "notificationType": "string · APPROVAL(전 도메인 공용 승인 대기)|ASSET_EXPIRY",
        "sourceId": "number · APPROVAL이면 approvalRequestId, ASSET_EXPIRY면 자산 id"
      }
    ]
  }
  ```
  > `items`는 1개 이상. 로그인 사용자(토큰의 userId) 본인 기준으로만 저장하며, Request Body에 userId를 받지 않는다. 이미 확인처리된 항목이 다시 포함돼도 중복 저장하지 않고 그대로 무시한다(멱등).
- **Response Body** (200):
  ```json
  { "dismissedCount": "number · 이번 요청으로 신규 확인처리된 건수" }
  ```
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 확인처리 완료(멱등 — 이미 처리된 항목 포함해도 오류 아님) |
  | 400 | items 누락·형식 오류 |
  | 401 | 미인증 |

### API-COM-002 · 확인처리된 알림 목록 조회

FE가 알림 후보(승인 대기·자산 만료)로 조합한 뒤, 이 조회 결과의 (notificationType, sourceId)와 매칭되는 항목을 제외해 신규 표시 여부를 판별하는 데 사용한다(5초 polling마다 재조회, 표시 목록은 merge 누적 방식이라 상한 없음).

- **Endpoint**: `GET /api/v1/notifications/dismissals`
- **인증**: 필요(Access Token)
- **Header**:
  | 이름 | 필수 | 설명 |
  |------|------|------|
  | Authorization | Y | `Bearer {accessToken}` |
- **Request Body**: 없음(로그인 사용자 본인 전체 확인처리 이력 조회)
- **Response Body** (200):
  ```json
  {
    "items": [
      { "notificationType": "string", "sourceId": "number", "dismissedAt": "ISO-8601" }
    ]
  }
  ```
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 정상(이력 없으면 빈 배열) |
  | 401 | 미인증 |

### API-COM-003 · 승인 대기함 목록 조회(전 도메인 공용)

기존 도메인별 전용 승인 대기 API(API-SRM-012, API-CHG-007)를 대체하는 전 도메인 공용 엔드포인트. `scope=mine`은 로그인 사용자가 현재 대기 차수(`current_step_no`)에 필요한 역할을 보유하고 아직 그 역할 슬롯을 결정하지 않은 인스턴스만 반환한다(역할 기반 공유 대기함, 기존 SRM/CHANGE 패턴을 전 도메인으로 확장).

- **Endpoint**: `GET /api/v1/approvals?scope=mine&domain=&page=&size=`
- **인증**: 필요(Access Token)
- **Header**:
  | 이름 | 필수 | 설명 |
  |------|------|------|
  | Authorization | Y | `Bearer {accessToken}` |
- **Request Body**: 없음(쿼리 파라미터). `domain` 선택(SERVICE_REQUEST/CHANGE/KNOWLEDGE/INCIDENT/PROBLEM 등, 미지정 시 전체)
- **Response Body** (200):
  ```json
  {
    "content": [
      {
        "approvalRequestId": "number", "ticketType": "string", "ticketId": "number", "ticketKey": "string",
        "ticketSummary": "string · 알림·목록 표시용 제목(도메인별 summary/title/name 등을 그대로 노출)",
        "targetState": "string · 원본 코드값(도착 상태)", "targetStateLabel": "string · 표시명(교차 도메인 집계 화면이라 백엔드가 resolve해 함께 반환)",
        "requester": "string", "currentStepNo": "number", "requestedAt": "ISO-8601 · 인스턴스 생성 시각"
      }
    ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 / 401

### API-COM-004 · 승인 인스턴스 상세 조회

차수별 진행 상태(역할별 결정 현황 포함)를 조회한다. 도메인 상세 화면(변경/서비스요청/지식/인시던트/문제 상세)의 승인 진행 패널이 사용한다.

- **Endpoint**: `GET /api/v1/approvals/{approvalRequestId}`
- **인증**: 필요(Access Token)
- **Header**:
  | 이름 | 필수 | 설명 |
  |------|------|------|
  | Authorization | Y | `Bearer {accessToken}` |
- **Response Body** (200):
  ```json
  {
    "id": "number", "ticketType": "string", "ticketId": "number", "ticketKey": "string",
    "targetState": "string · 원본 코드값(도착 상태, 생성 시점 스냅샷)", "targetStateLabel": "string · 표시명",
    "status": "IN_PROGRESS|APPROVED|REJECTED", "currentStepNo": "number|null",
    "steps": [
      {
        "stepNo": "number", "decisionMode": "AND|OR", "status": "PENDING|APPROVED|REJECTED|SKIPPED",
        "roles": [
          { "roleCode": "string", "roleName": "string", "decision": "PENDING|APPROVE|REJECT", "decidedBy": "string|null", "reason": "string|null", "decidedAt": "ISO-8601|null" }
        ]
      }
    ]
  }
  ```
- **Response Code**: 200 / 401 / 404

### API-COM-005 · 승인/반려 결정

0절 "결정 처리" 로직에 따라 처리한다. 기존 도메인별 전용 승인 API(API-SRM-011, API-CHG-006, API-KM-007)를 대체한다.

- **Endpoint**: `POST /api/v1/approvals/{approvalRequestId}/decisions`
- **인증**: 필요(현재 대기 차수(`currentStepNo`)에 필요한 역할 보유자)
- **Header**:
  | 이름 | 필수 | 설명 |
  |------|------|------|
  | Authorization | Y | `Bearer {accessToken}` |
  | Content-Type | Y | application/json |
- **Request Body**:
  ```json
  { "decision": "APPROVE|REJECT · 필수", "reason": "string · REJECT 시 필수" }
  ```
- **Response Body** (200):
  ```json
  { "approvalRequestId": "number", "stepNo": "number", "stepStatus": "PENDING|APPROVED|REJECTED", "requestStatus": "IN_PROGRESS|APPROVED|REJECTED" }
  ```
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 결정 반영(AND면 본인이 보유한 역할 슬롯 전부, OR면 차수 전체 확정) |
  | 400 | REJECT인데 사유 누락 |
  | 403 | 현재 대기 차수에 필요한 역할 미보유 |
  | 404 | 인스턴스 없음 |
  | 409 | 이미 결정된 역할 슬롯 재처리 시도, 또는 인스턴스가 이미 APPROVED/REJECTED로 종료됨 |

### API-COM-006 · 반려 후 재승인요청

0절 "게이트 체크" 3번의 `APPROVAL_REJECTED`(409) 응답을 받은 후, 사용자가 명시적으로 새 승인 사이클을 시작하는 액션이다. 자동 재시도가 아니다 — 반려된 건은 이 API를 호출하기 전까지 계속 `APPROVAL_REJECTED`로 차단된다. 생성 시점(최초 상태) 게이트든 이후 전이 게이트든 동일한 API로 처리한다(생성 시점 게이트에서 반려된 경우, 도메인 전이 버튼과 달리 별도의 "재제출" 버튼이 없으므로 이 API가 유일한 재시도 경로다).

- **Endpoint**: `POST /api/v1/approvals/resubmit`
- **인증**: 필요(Access Token) — 별도의 "원 요청자인지" 검증은 하지 않는다. 그 티켓을 조회할 권한이 있는 사용자면 누구든 호출 가능하고, 실제 통과 여부는 호출자의 역할이 재매칭된 규칙의 승인요청자 역할 스코프와 맞는지에 달려 있다.
- **Header**:
  | 이름 | 필수 | 설명 |
  |------|------|------|
  | Authorization | Y | `Bearer {accessToken}` |
  | Content-Type | Y | application/json |
- **Request Body**:
  ```json
  { "ticketType": "string · 필수", "ticketId": "number · 필수" }
  ```
- **Response Body** (200):
  ```json
  { "approvalRequestId": "number", "ticketType": "string", "ticketId": "number", "targetState": "string", "status": "IN_PROGRESS", "currentStepNo": "number" }
  ```
  > 대상 티켓의 **targetState 무관 "티켓 전체의 최신 인스턴스"**(`findTopByTicketTypeAndTicketIdOrderByIdDesc`) 기준으로 반려 여부를 판정한다. 최신 인스턴스의 targetState·도메인·요청유형 + **지금 호출한 사용자의 requesterId**로 현재 시점 규칙을 다시 매칭(`matchProcess`)해 새 인스턴스를 생성한다(관리자가 그 사이 규칙을 바꿨거나 담당자가 바뀌어 요청자 역할이 달라졌을 수 있으므로 재매칭이 원칙). 매칭되는 규칙이 사라졌다면(관리자가 삭제) 인스턴스를 생성하지 않고 `status: "NO_RULE_MATCHED"`(승인 없이 통과 가능 상태)로 응답한다.
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 재승인요청 접수(새 인스턴스 생성 또는 매칭 규칙 소멸 안내) |
  | 400 | 최신 인스턴스 상태가 `REJECTED`가 아님("반려된 건만 재승인요청 가능") |
  | 404 | 대상 티켓의 승인 인스턴스 자체가 없음 |
