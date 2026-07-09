# API 명세서 — 서비스 요청 관리 (Service Request)

> 도메인: service-request · 버전: 0.1 · 작성일: 2026-07-09

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
| API-SRM-011 | 요청 승인/반려 | POST | /api/v1/service-requests/{id}/approval | 필요(Approver) |
| API-SRM-012 | 승인 대기 목록 | GET | /api/v1/approvals | 필요(Approver) |
| API-SRM-013 | 요청 코멘트 등록 | POST | /api/v1/service-requests/{id}/comments | 필요 |
| API-SRM-014 | CSAT 제출 | POST | /api/v1/service-requests/{id}/csat | 필요(요청자) |
| API-SRM-015 | 요청 지표 조회 | GET | /api/v1/service-requests/metrics | 필요 |
| API-SRM-016 | 큐 목록·건수 조회 | GET | /api/v1/queues | 필요(Agent) |

## 2. API 상세

### API-SRM-001 · 서비스 카탈로그 목록

- **Endpoint**: `GET /api/v1/service-catalog/items?category=&keyword=`
- **인증**: 필요(Access Token)
- **Response Body** (200):
  ```json
  [ { "id": "number", "name": "string", "description": "string", "category": "string", "approvalRequired": "boolean" } ]
  ```
- **Response Code**: 200 / 401

### API-SRM-002 · 카탈로그 항목 상세(양식 스키마)

- **Endpoint**: `GET /api/v1/service-catalog/items/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "id": "number", "name": "string", "description": "string", "approvalRequired": "boolean",
    "slaResponseMinutes": "number", "slaResolveMinutes": "number",
    "formSchema": [ { "key": "string", "label": "string", "type": "text|select|number|date|file", "required": "boolean", "options": ["string"] } ]
  }
  ```
- **Response Code**: 200 / 401 / 404

### API-SRM-003 · 카탈로그 항목 생성

- **Endpoint**: `POST /api/v1/service-catalog/items`
- **인증**: 필요(Process Owner)
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "name": "string · 필수", "description": "string", "approvalRequired": "boolean",
    "approverRole": "string · 승인 담당 역할코드(기본 APPROVER), approvalRequired=true 시 사용",
    "queueId": "number", "slaResponseMinutes": "number", "slaResolveMinutes": "number",
    "formSchema": [ { "key": "string", "label": "string", "type": "string", "required": "boolean", "options": ["string"] } ]
  }
  ```
- **Response Body** (201): 생성된 항목
- **Response Code**: 201 / 400 이름·양식 누락 / 403 권한 부족

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
    "content": [ { "id": "number", "ticketKey": "string", "catalogItemName": "string", "status": "string", "slaStatus": "OK|WARNING|BREACHED", "assignee": "string", "updatedAt": "ISO-8601" } ],
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
    "approval": { "required": "boolean", "status": "PENDING|APPROVED|REJECTED", "reason": "string" },
    "sla": { "responseStatus": "string", "resolveStatus": "string" },
    "linkedArticles": [ { "articleId": "number", "title": "string" } ],
    "comments": [ { "id": "number", "author": "string", "body": "string", "createdAt": "ISO-8601" } ],
    "timeline": [ { "type": "string", "message": "string", "at": "ISO-8601" } ]
  }
  ```
- **Response Code**: 200 / 401 / 403 / 404

### API-SRM-009 · 요청 담당자 배정

- **Endpoint**: `POST /api/v1/service-requests/{id}/assign`
- **인증**: 필요(Agent)
- **Request Body**: `{ "assigneeId": "number · 미지정 시 본인" }`
- **Response Code**: 200 / 403 권한 없는 배정 / 404

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
  | 409 | 승인 대기 중 이행 전이 시도 |

### API-SRM-011 · 요청 승인/반려

- **Endpoint**: `POST /api/v1/service-requests/{id}/approval`
- **인증**: 필요(approval.approver_role 보유자)
- **Request Body**: `{ "decision": "APPROVE|REJECT", "reason": "string · 반려 시 필수" }`
- **Response Body** (200): `{ "id": "number", "approvalStatus": "string" }`
- **Response Code**: 200 / 400 반려 사유 누락 / 403 approver_role 미보유 / 404 / 409 이미 결정됨. **역할 기반 승인**: approver_role을 가진 사용자면 처리 가능하며, 먼저 처리한 사용자가 결정자로 기록(decided_by_id). role claim에 approver_role 미포함 시 403.

### API-SRM-012 · 승인 대기 목록

- **Endpoint**: `GET /api/v1/approvals?scope=mine&type=service-request`
- **인증**: 필요(Approver 계열 역할)
- **Response Body** (200):
  ```json
  [ { "requestId": "number", "ticketKey": "string", "requester": "string", "requestedAt": "ISO-8601" } ]
  ```
- **Response Code**: 200 / 401 / 403. `scope=mine` = 현재 사용자의 role claim에 approval.approver_role이 포함된 **PENDING 승인 공유 목록**(특정 개인 배정이 아닌 역할 기반 공유함).

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
