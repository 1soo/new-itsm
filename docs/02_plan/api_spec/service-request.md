# API 명세서 — 서비스 요청 관리 (Service Request)

> 도메인: service-request · 버전: 0.4
>
> **변경 이력**
> - 2026-07-16: 카탈로그 카테고리 CRUD API(API-SRM-018~021) 신규, 카탈로그 CRUD(API-SRM-001~004)의 `category`(자유 텍스트) 필드를 `categoryId`/`categoryName`으로 전환, `formSchema.type`에 `textarea` 추가, API-SRM-008 응답 `timeline` 항목에 `actor` 필드 추가
> - 2026-07-15: 카탈로그 CRUD(API-SRM-002/003/004)에 `assigneeRoleId` 필드 추가, 담당자 후보 목록 조회 API(API-SRM-017) 신규
> - 2026-07-12: 카탈로그 항목별 approvalRequired/approverRole 필드 제거, 전용 승인 API(API-SRM-011/012) 삭제 후 공통 승인 API([common.md](common.md) API-COM-003~005)로 대체

## 공통 규약

- **Base Path**: `/api/v1` · 표준 오류 응답 본문은 [auth.md](auth.md) 공통 규약과 동일.
- 별도 표기 없는 API는 모두 `Authorization: Bearer {accessToken}` **필요**.

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-SRM-001 | 서비스 카탈로그 목록 | GET | /api/v1/service-catalog/items | 필요 |
| API-SRM-002 | 카탈로그 항목 상세(양식 스키마) | GET | /api/v1/service-catalog/items/{id} | 필요 |
| API-SRM-003 | 카탈로그 항목 생성 | POST | /api/v1/service-catalog/items | 필요(Process Owner) |
| API-SRM-004 | 카탈로그 항목 수정 | PATCH | /api/v1/service-catalog/items/{id} | 필요(Process Owner) |
| API-SRM-005 | 지식 기사 추천 | GET | /api/v1/knowledge/suggestions | 필요 |
| API-SRM-006 | 요청 생성(제출) | POST | /api/v1/service-requests | 필요 |
| API-SRM-007 | 요청 목록 조회 | GET | /api/v1/service-requests | 필요 |
| API-SRM-008 | 요청 상세 조회 | GET | /api/v1/service-requests/{id} | 필요 |
| API-SRM-009 | 요청 담당자 배정 | POST | /api/v1/service-requests/{id}/assign | 필요(Agent) |
| API-SRM-010 | 요청 상태 전이(검증/이행/종료) | PATCH | /api/v1/service-requests/{id}/status | 필요 |
| API-SRM-013 | 요청 코멘트 등록 | POST | /api/v1/service-requests/{id}/comments | 필요 |
| API-SRM-014 | CSAT 제출 | POST | /api/v1/service-requests/{id}/csat | 필요(요청자) |
| API-SRM-015 | 요청 지표 조회 | GET | /api/v1/service-requests/metrics | 필요 |
| API-SRM-016 | 큐 목록·건수 조회 | GET | /api/v1/queues | 필요(Agent) |
| API-SRM-017 | 요청 담당자 후보 목록 조회 | GET | /api/v1/service-requests/{id}/assignee-candidates | 필요(Agent) |
| API-SRM-018 | 카탈로그 카테고리 목록 조회 | GET | /api/v1/service-catalog/categories | 필요 |
| API-SRM-019 | 카탈로그 카테고리 생성 | POST | /api/v1/service-catalog/categories | 필요(Process Owner) |
| API-SRM-020 | 카탈로그 카테고리 수정 | PATCH | /api/v1/service-catalog/categories/{id} | 필요(Process Owner) |
| API-SRM-021 | 카탈로그 카테고리 삭제 | DELETE | /api/v1/service-catalog/categories/{id} | 필요(Process Owner) |

## 2. API 상세

### API-SRM-001 · 서비스 카탈로그 목록

- **Endpoint**: `GET /api/v1/service-catalog/items?categoryId=&keyword=`
- **인증**: 필요(Access Token)
- **Response Body** (200):
  ```json
  [ { "id": "number", "name": "string", "description": "string", "categoryId": "number|null", "categoryName": "string|null" } ]
  ```
  > `categoryId`는 [database/service-request.md](../database/service-request.md) `service_catalog_category`를 참조하는 FK다. `categoryName`은 조회 편의를 위한 resolve 값(SCR-SRM-001 카드 뱃지 표시용).
- **Response Code**: 200 / 401
  > 승인 필요 여부는 더 이상 카탈로그 항목의 고정 속성이 아니다(요청자의 보유 역할에 따라 매칭되는 승인 프로세스가 달라질 수 있음). 개별 요청의 승인 여부는 상세 조회(API-SRM-008) `approval` 필드로 확인한다.

### API-SRM-002 · 카탈로그 항목 상세(양식 스키마)

- **Endpoint**: `GET /api/v1/service-catalog/items/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "id": "number", "name": "string", "description": "string",
    "categoryId": "number|null", "categoryName": "string|null · categoryId 표시용",
    "queueId": "number|null", "assigneeRoleId": "number|null", "assigneeRoleName": "string|null · assigneeRoleId 표시용",
    "slaResponseMinutes": "number", "slaResolveMinutes": "number",
    "formSchema": [ { "key": "string", "label": "string", "type": "text|textarea|select|number|date|file", "required": "boolean", "options": ["string"] } ]
  }
  ```
  > `type`의 `textarea`(여러 줄 텍스트)는 저장·검증 방식이 `text`와 동일, FE 렌더링만 다르다(`components/common/dynamic-form.tsx`가 `<textarea>`로 렌더).
- **Response Code**: 200 / 401 / 404

### API-SRM-003 · 카탈로그 항목 생성

- **Endpoint**: `POST /api/v1/service-catalog/items`
- **인증**: 필요(Process Owner)
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "name": "string · 필수", "description": "string",
    "categoryId": "number · 선택(미지정 시 미분류)",
    "queueId": "number · 선택(미지정 시 요청 생성 시점 기본 큐로 배정, 미분류)",
    "assigneeRoleId": "number · 선택(2026-07-15 유지보수 요청, 담당자 역할. 지정 시 라우팅/배정 시점 후보 목록 조회(API-SRM-017)에 사용, 자동배정 아님)",
    "slaResponseMinutes": "number", "slaResolveMinutes": "number",
    "formSchema": [ { "key": "string", "label": "string", "type": "text|textarea|select|number|date|file", "required": "boolean", "options": ["string"] } ]
  }
  ```
  > 승인 필요 여부·승인 담당 역할은 더 이상 카탈로그 항목 생성 시 지정하지 않는다(승인 프로세스 커스텀 기능으로 완전 대체 — [auth.md](auth.md) API-AUTH-027에서 SYSTEM_ADMIN이 도메인=SERVICE_REQUEST, 요청유형=이 카탈로그 항목으로 별도 설정). `assigneeRoleId`는 [auth.md](auth.md) API-AUTH-030(역할 목록 조회)로 후보를 조회해 선택하고, `categoryId`는 API-SRM-018(카테고리 목록 조회)로 후보를 조회해 선택한다.
- **Response Body** (201): 생성된 항목
- **Response Code**: 201 / 400 이름·양식 누락 / 403 권한 부족 / 404 존재하지 않는 categoryId

### API-SRM-004 · 카탈로그 항목 수정

- **Endpoint**: `PATCH /api/v1/service-catalog/items/{id}`
- **인증**: 필요(Process Owner)
- **Request Body**: API-SRM-003 필드 부분 갱신
- **Response Code**: 200 / 400 / 403 / 404

### API-SRM-005 · 지식 기사 추천

- **Endpoint**: `GET /api/v1/knowledge/suggestions?catalogItemId=&keyword=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  [ { "articleId": "number", "title": "string", "score": "number" } ]
  ```
- **Response Code**: 200(매칭 없으면 빈 배열) / 401

### API-SRM-006 · 요청 생성(제출)

- **Endpoint**: `POST /api/v1/service-requests`
- **인증**: 필요
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  { "catalogItemId": "number · 필수", "formValues": { "key": "value · 양식 필드 값" } }
  ```
- **Response Body** (201):
  ```json
  { "id": "number", "ticketKey": "string · SRM-YYYY-####", "status": "SUBMITTED", "createdAt": "ISO-8601" }
  ```
- **Response Code**: 201 / 400 필수 필드 미입력 / 401

### API-SRM-007 · 요청 목록 조회

- **Endpoint**: `GET /api/v1/service-requests?scope=mine|all&queue=&status=&from=&to=&page=&size=`
- **인증**: 필요 (scope=mine: 본인 요청 / all·queue: 상담원 이상)
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "ticketKey": "string", "catalogItemName": "string", "status": "string", "slaStatus": "OK|WARNING|BREACHED", "assignee": "string", "assigneeId": "number|null · 2026-07-15 유지보수 요청, 요청 큐(SCR-SRM-004) 배정 버튼 노출 조건(본인 배정 여부) 판정용", "updatedAt": "ISO-8601" } ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 / 401 / 403

### API-SRM-008 · 요청 상세 조회

- **Endpoint**: `GET /api/v1/service-requests/{id}`
- **인증**: 필요(접근 권한 검증)
- **Response Body** (200):
  ```json
  {
    "id": "number", "ticketKey": "string", "catalogItemName": "string", "status": "string",
    "formValues": {}, "requester": "string", "assignee": "string", "queue": "string",
    "approval": { "approvalRequestId": "number|null", "status": "IN_PROGRESS|APPROVED|REJECTED|null · null=매칭되는 승인 프로세스 없음(게이트 없이 진행)" },
    "sla": { "responseStatus": "string", "resolveStatus": "string" },
    "linkedArticles": [ { "articleId": "number", "title": "string" } ],
    "linkedAssets": [ { "id": "number", "assetKey": "string" } ],
    "comments": [ { "id": "number", "author": "string", "body": "string", "createdAt": "ISO-8601" } ],
    "timeline": [ { "type": "string", "message": "string", "actor": "string · 행위 수행 주체자 표시명(createdBy 이메일을 이름으로 resolve, 실패 시 이메일 그대로)", "at": "ISO-8601" } ]
  }
  ```
  > `STATUS_*` 타임라인 이벤트의 기본 메시지(사용자가 별도 note를 지정하지 않은 경우)는 상태 enum 코드(`target.name()`) 대신 상태 라벨을 사용한다(예: `"상태가 '이행 중'으로 변경되었습니다"`).
- **Response Code**: 200 / 401 / 403 / 404. `approval` 상세(차수별 진행 상태)는 [common.md](common.md) API-COM-004로 조회한다.

### API-SRM-009 · 요청 담당자 배정

- **Endpoint**: `POST /api/v1/service-requests/{id}/assign`
- **인증**: 필요(Agent)
- **Request Body**: `{ "assigneeId": "number · 미지정 시 본인" }`
  > 2026-07-15 유지보수 요청부터 요청 큐(SCR-SRM-004)의 담당자 배정 UI가 API-SRM-017 후보 목록 중 하나를 선택해 `assigneeId`로 명시 전달하는 방식이 추가되었다(자동배정 아님, 계약 자체는 변경 없음 — 기존처럼 미지정 시 본인도 그대로 지원).
- **Response Code**: 200 / 403 권한 없는 배정 / 404

### API-SRM-017 · 요청 담당자 후보 목록 조회

- **Endpoint**: `GET /api/v1/service-requests/{id}/assignee-candidates`
- **인증**: 필요(Agent) — 요청 큐(SCR-SRM-004) 담당자 배정 팝업이 사용.
- **Response Body** (200):
  ```json
  [ { "id": "number", "name": "string" } ]
  ```
  > 대상 요청의 카탈로그 항목(`service_catalog_item.assignee_role_id`)에 지정된 역할을 보유한 ACTIVE 상태 사용자 목록(2026-07-15 유지보수 요청). 카탈로그 항목에 담당자 역할이 지정되지 않았으면 빈 배열을 반환한다(이 경우 FE는 본인 배정만 노출).
- **Response Code**: 200(빈 배열 가능) / 401 / 403 / 404

### API-SRM-018 · 카탈로그 카테고리 목록 조회

- **Endpoint**: `GET /api/v1/service-catalog/categories`
- **인증**: 필요(Access Token, 역할 제한 없음 — SCR-SRM-007 카테고리 select 후보·SCR-SRM-009 관리 목록 공용)
- **Response Body** (200):
  ```json
  [ { "id": "number", "name": "string", "sortOrder": "number", "itemCount": "number · 이 카테고리를 참조하는 카탈로그 항목 수(SCR-SRM-009 목록 표시용)" } ]
  ```
- **Response Code**: 200(`sortOrder` 오름차순) / 401

### API-SRM-019 · 카탈로그 카테고리 생성

- **Endpoint**: `POST /api/v1/service-catalog/categories`
- **인증**: 필요(Process Owner)
- **Header**: `Content-Type: application/json`
- **Request Body**: `{ "name": "string · 필수", "sortOrder": "number · 선택(미지정 시 0)" }`
- **Response Body** (201): `{ "id": "number", "name": "string", "sortOrder": "number" }`
- **Response Code**: 201 / 400 이름 누락 / 403 / 409 이름 중복

### API-SRM-020 · 카탈로그 카테고리 수정

- **Endpoint**: `PATCH /api/v1/service-catalog/categories/{id}`
- **인증**: 필요(Process Owner)
- **Request Body**: API-SRM-019 필드 부분 갱신
- **Response Code**: 200 / 400 / 403 / 404 / 409 이름 중복

### API-SRM-021 · 카탈로그 카테고리 삭제

- **Endpoint**: `DELETE /api/v1/service-catalog/categories/{id}`
- **인증**: 필요(Process Owner)
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 204 | 삭제 완료(소프트 삭제) |
  | 403 | 권한 부족 |
  | 404 | 존재하지 않는 카테고리 |
  | 409 | `CATEGORY_IN_USE` — 하나 이상의 카탈로그 항목(`service_catalog_item.category_id`)이 참조 중. 참조 중인 항목의 카테고리를 먼저 변경해야 삭제 가능(자동 미분류 처리 없음) |

### API-SRM-010 · 요청 상태 전이

- **Endpoint**: `PATCH /api/v1/service-requests/{id}/status`
- **인증**: 필요(전이별 권한 상이)
- **Request Body**:
  ```json
  { "targetStatus": "VALIDATED|ROUTED|IN_FULFILLMENT|FULFILLED|CLOSED", "note": "string" }
  ```
- **Response Body** (200): `{ "id": "number", "status": "string" }`
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 전이 성공 |
  | 400 | 허용되지 않은 전이 / 이미 종료된 요청 재종료 |
  | 403 | 권한 부족(이행 등) |
  | 409 | 담당자 미배정 상태로 라우팅(ROUTED) 전이 시도(2026-07-15 유지보수 요청, VULNERABILITY 도메인의 `ASSIGNEE_REQUIRED_FOR_REMEDIATION`과 동일 패턴 재사용 — `ASSIGNEE_REQUIRED_FOR_ROUTING`) / 승인 완료 전 이행(IN_FULFILLMENT) 전이 시도 — [common.md](common.md) 0절 공통 게이트 로직(domain=SERVICE_REQUEST, requestSubtypeKey=service_catalog_item.id) 적용. 매칭되는 승인 프로세스가 없거나 0차 승인이면 게이트 없이 통과 |

### API-SRM-013 · 요청 코멘트 등록

- **Endpoint**: `POST /api/v1/service-requests/{id}/comments`
- **인증**: 필요(요청 접근 권한)
- **Request Body**: `{ "body": "string · 필수" }`
- **Response Body** (201): `{ "id": "number", "author": "string", "body": "string", "createdAt": "ISO-8601" }`
- **Response Code**: 201 / 403 접근 권한 없음 / 404

### API-SRM-014 · CSAT 제출

- **Endpoint**: `POST /api/v1/service-requests/{id}/csat`
- **인증**: 필요(요청자)
- **Request Body**: `{ "score": "number · 1~5", "comment": "string" }`
- **Response Body** (200): `{ "id": "number", "score": "number" }`
- **Response Code**: 200 / 400 종료되지 않은 요청 / 403 / 404

### API-SRM-015 · 요청 지표 조회

- **Endpoint**: `GET /api/v1/service-requests/metrics?from=&to=`
- **인증**: 필요(PROCESS_OWNER) — RBAC 단일 원천 security/authorization/process_owner.md 기준. SCR-SRM-008도 PROCESS_OWNER 전용.
- **Response Body** (200):
  ```json
  { "csatAvg": "number", "avgResponseMinutes": "number", "avgResolveMinutes": "number", "slaComplianceRate": "number" }
  ```
- **Response Code**: 200(데이터 없으면 0값) / 401 / 403

### API-SRM-016 · 큐 목록·건수 조회

- **Endpoint**: `GET /api/v1/queues`
- **인증**: 필요(Agent 이상) — SCR-SRM-004 좌측 큐 목록 렌더용.
- **Request Body**: 없음
- **Response Body** (200):
  ```json
  [ { "id": "number", "name": "string", "isDefault": "boolean · 미분류 기본 큐", "openCount": "number · 미종료 요청 건수" } ]
  ```
- **Response Code**: 200 / 401 / 403
