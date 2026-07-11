# API 명세서 — 변경 관리 (Change)

> 도메인: change · 버전: 0.2 · 작성일: 2026-07-11 · 승인 프로세스 커스텀 기능(유지보수 요청) 반영 — 위험도 기반 CAB 자동 라우팅 제거, 전용 승인 API(API-CHG-006/007) 삭제 후 공통 승인 API([common.md](common.md) API-COM-003~005)로 대체

## 공통 규약

- **Base Path**: `/api/v1` · 표준 오류 응답 본문은 [auth.md](auth.md) 공통 규약과 동일.
- 모든 API는 `Authorization: Bearer {accessToken}` **필요**.

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-CHG-001 | 변경 목록 조회 | GET | /api/v1/changes | 필요 |
| API-CHG-002 | 변경 요청(RFC) 생성 | POST | /api/v1/changes | 필요 |
| API-CHG-003 | 변경 상세 조회 | GET | /api/v1/changes/{id} | 필요 |
| API-CHG-004 | 상태(6단계) 전이 | PATCH | /api/v1/changes/{id}/status | 필요 |
| API-CHG-005 | 변경 유형·위험 변경 | PATCH | /api/v1/changes/{id}/classification | 필요 |
| API-CHG-008 | 구현 결과 기록 | POST | /api/v1/changes/{id}/result | 필요 |
| API-CHG-009 | 인시던트/문제 연계 | POST | /api/v1/changes/{id}/links | 필요 |
| API-CHG-010 | 변경 일정(캘린더) 조회 | GET | /api/v1/changes/schedule | 필요 |
| API-CHG-011 | 표준 변경 템플릿 목록 | GET | /api/v1/change-templates | 필요 |
| API-CHG-012 | 변경 지표 조회 | GET | /api/v1/changes/metrics | 필요 |

## 2. API 상세

### API-CHG-001 · 변경 목록 조회

- **Endpoint**: `GET /api/v1/changes?type=&status=&risk=&from=&to=&page=&size=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "ticketKey": "string · CHG-YYYY-####", "summary": "string", "type": "STANDARD|NORMAL|EMERGENCY", "status": "string", "risk": "HIGH|MEDIUM|LOW", "scheduledAt": "ISO-8601", "updatedAt": "ISO-8601" } ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 / 401

### API-CHG-002 · 변경 요청(RFC) 생성

- **Endpoint**: `POST /api/v1/changes`
- **인증**: 필요
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "summary": "string · 필수", "description": "string", "type": "STANDARD|NORMAL|EMERGENCY · 필수",
    "risk": "HIGH|MEDIUM|LOW", "implementationPlan": "string", "affectedSystems": ["string"],
    "rollbackPlan": "string", "scheduledAt": "ISO-8601", "templateId": "number · 표준 변경 시"
  }
  ```
- **Response Body** (201): `{ "id": "number", "ticketKey": "string", "status": "REQUESTED", "type": "string" }`
- **Response Code**: 201 / 400 요약·유형 누락 / 401

### API-CHG-003 · 변경 상세 조회

- **Endpoint**: `GET /api/v1/changes/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "id": "number", "ticketKey": "string", "summary": "string", "description": "string",
    "type": "string", "risk": "string", "status": "string",
    "implementationPlan": "string", "rollbackPlan": "string",
    "result": { "outcome": "SUCCESS|FAILURE|null", "rolledBack": "boolean", "note": "string" },
    "approval": { "approvalRequestId": "number|null", "status": "IN_PROGRESS|APPROVED|REJECTED|null · null=매칭되는 승인 프로세스 없음(게이트 없이 진행)" },
    "links": [ { "type": "INCIDENT|PROBLEM|ASSET|COMPLIANCE_REQUIREMENT", "targetKey": "string" } ]
  }
  ```
- **Response Code**: 200 / 401 / 404. `COMPLIANCE_REQUIREMENT` 링크는 컴플라이언스 도메인에서 생성한다([compliance.md](compliance.md) API-COMP-005, REQ-COMP-005). `approval` 상세(차수별 진행 상태)는 [common.md](common.md) API-COM-004로 조회한다.

### API-CHG-004 · 상태(6단계) 전이

- **Endpoint**: `PATCH /api/v1/changes/{id}/status`
- **인증**: 필요
- **Request Body**: `{ "targetStatus": "REVIEW|PLANNING|APPROVAL|IMPLEMENTATION|CLOSED", "note": "string" }`
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 전이 성공 |
  | 400 | 허용되지 않은 전이 |
  | 409 | 승인 완료 전 구현(IMPLEMENTATION) 전이 시도 — [common.md](common.md) 0절 공통 게이트 로직(domain=CHANGE, requestSubtypeKey=change_request.type) 적용. 매칭되는 승인 프로세스가 없거나 0차 승인이면 게이트 없이 통과(표준 변경 등) |
  | 403 / 404 | 권한 부족 / 없음 |

### API-CHG-005 · 변경 유형·위험 변경

- **Endpoint**: `PATCH /api/v1/changes/{id}/classification`
- **인증**: 필요
- **Request Body**: `{ "type": "STANDARD|NORMAL|EMERGENCY", "risk": "HIGH|MEDIUM|LOW" }`
- **Response Body** (200): `{ "id": "number", "type": "string", "risk": "string" }`
- **Response Code**: 200 / 400 정의되지 않은 유형 / 403 / 404. `type`은 승인 프로세스의 요청유형 스코프(`approval_process.request_subtype_key`)로도 사용되므로, 변경 시 이후 상태 전이의 승인 게이트 매칭 결과가 달라질 수 있다(승인 경로 자동 라우팅은 더 이상 없음 — 유지보수 요청으로 커스텀 승인 프로세스가 완전 대체).

### API-CHG-008 · 구현 결과 기록

- **Endpoint**: `POST /api/v1/changes/{id}/result`
- **인증**: 필요
- **Request Body**: `{ "outcome": "SUCCESS|FAILURE", "rolledBack": "boolean", "note": "string" }`
- **Response Body** (200): 저장된 결과
- **Response Code**: 200 / 400 승인되지 않은 변경 / 403 / 404

### API-CHG-009 · 인시던트/문제 연계

- **Endpoint**: `POST /api/v1/changes/{id}/links`
- **인증**: 필요
- **Request Body**: `{ "targetType": "INCIDENT|PROBLEM", "targetId": "number" }`
- **Response Code**: 200 / 400 존재하지 않는 대상 / 403 / 404

### API-CHG-010 · 변경 일정(캘린더) 조회

- **Endpoint**: `GET /api/v1/changes/schedule?from=&to=&type=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  [ { "id": "number", "ticketKey": "string", "summary": "string", "type": "string", "scheduledAt": "ISO-8601" } ]
  ```
- **Response Code**: 200(예정 없으면 빈 배열) / 401

### API-CHG-011 · 표준 변경 템플릿 목록

- **Endpoint**: `GET /api/v1/change-templates`
- **인증**: 필요
- **Response Body** (200):
  ```json
  [ { "id": "number", "name": "string", "description": "string" } ]
  ```
- **Response Code**: 200 / 401

### API-CHG-012 · 변경 지표 조회

- **Endpoint**: `GET /api/v1/changes/metrics?from=&to=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  { "successRate": "number", "failureRate": "number", "emergencyRate": "number", "total": "number" }
  ```
- **Response Code**: 200(데이터 없으면 빈 결과) / 401
