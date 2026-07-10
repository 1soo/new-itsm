# API 명세서 — 컴플라이언스 관리 (Compliance Management)

> 도메인: compliance · 버전: 0.1 · 작성일: 2026-07-10

## 공통 규약

- **Base Path**: `/api/v1` · 표준 오류 응답 본문은 [auth.md](auth.md) 공통 규약과 동일.
- 모든 API는 `Authorization: Bearer {accessToken}` **필요**.

## 0. 설계 배경

- 요구사항의 준수 상태(`complianceStatus`)는 별도 컬럼으로 저장하지 않고 **미해결(탐지/조치중) 시정조치 존재 여부로 조회 시점에 계산**한다(있으면 NON_COMPLIANT, 없으면 COMPLIANT). 상태를 별도로 관리하면 시정조치 처리와 동기화가 어긋날 수 있어 계산값으로 일원화한다.
- 컴플라이언스 관련 활동(요구사항 등록·수정·시정조치 상태 전이)은 [auth.md](auth.md)의 공용 `audit_log` 테이블에 `event_type`(COMPLIANCE_REQ_CREATE/COMPLIANCE_REQ_UPDATE/COMPLIANCE_ACTION_STATUS_CHANGE)으로 기록한다(REQ-COMP-004). 감사 로그 기록은 원본 작업과 같은 트랜잭션으로 처리해, 로그 기록 실패 시 원본 작업도 롤백한다.
- 변경 요청 연계는 [common.md](common.md)의 `ticket_link`(source_type='COMPLIANCE_REQUIREMENT', target_type='CHANGE')를 사용하며, 변경 상세 조회 API(API-CHG-003, [change.md](change.md))의 `links` 응답에 `COMPLIANCE_REQUIREMENT` 타입으로 함께 노출된다.
- 감사 로그 조회(API-COMP-009)는 SYSTEM_ADMIN 전용 전체 로그 조회(API-AUTH-015)와 별개로, COMPLIANCE_OFFICER가 컴플라이언스 관련 이벤트(`event_type LIKE 'COMPLIANCE_%'`)만 열람할 수 있도록 범위를 제한한 전용 엔드포인트다(최소 권한 원칙).

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-COMP-001 | 요구사항 목록 조회 | GET | /api/v1/compliance/requirements | 필요 |
| API-COMP-002 | 요구사항 등록 | POST | /api/v1/compliance/requirements | 필요 |
| API-COMP-003 | 요구사항 상세 조회 | GET | /api/v1/compliance/requirements/{id} | 필요 |
| API-COMP-004 | 요구사항 수정 | PATCH | /api/v1/compliance/requirements/{id} | 필요 |
| API-COMP-005 | 변경 요청 연계 | POST | /api/v1/compliance/requirements/{id}/links | 필요 |
| API-COMP-006 | 책임자 지정 | POST | /api/v1/compliance/requirements/{id}/owner | 필요 |
| API-COMP-007 | 시정조치 등록 | POST | /api/v1/compliance/requirements/{id}/corrective-actions | 필요 |
| API-COMP-008 | 시정조치 상태 전이 | PATCH | /api/v1/compliance/corrective-actions/{actionId}/status | 필요 |
| API-COMP-009 | 컴플라이언스 감사 로그 조회 | GET | /api/v1/compliance/audit-logs | 필요 |
| API-COMP-010 | 준수 현황 조회 | GET | /api/v1/compliance/metrics | 필요 |

## 2. API 상세

### API-COMP-001 · 요구사항 목록 조회

- **Endpoint**: `GET /api/v1/compliance/requirements?complianceStatus=&ownerAssigned=&keyword=&page=&size=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "requirementKey": "string · COMP-YYYY-####", "name": "string", "basis": "string", "owner": "string|null", "complianceStatus": "COMPLIANT|NON_COMPLIANT", "updatedAt": "ISO-8601" } ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 / 401

### API-COMP-002 · 요구사항 등록

- **Endpoint**: `POST /api/v1/compliance/requirements`
- **인증**: 필요
- **Header**: `Content-Type: application/json`
- **Request Body**: `{ "name": "string · 필수", "basis": "string · 필수(규제 조항/내부 정책)", "scope": "string" }`
- **Response Body** (201): `{ "id": "number", "requirementKey": "string" }`
- **Response Code**: 201 / 400 이름·근거 누락 / 401

### API-COMP-003 · 요구사항 상세 조회

- **Endpoint**: `GET /api/v1/compliance/requirements/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "id": "number", "requirementKey": "string", "name": "string", "basis": "string", "scope": "string",
    "owner": "string|null", "complianceStatus": "COMPLIANT|NON_COMPLIANT",
    "correctiveActions": [ { "id": "number", "description": "string", "status": "DETECTED|IN_PROGRESS|RESOLVED", "updatedAt": "ISO-8601" } ],
    "linkedChanges": [ { "id": "number", "ticketKey": "string" } ]
  }
  ```
- **Response Code**: 200 / 401 / 404

### API-COMP-004 · 요구사항 수정

- **Endpoint**: `PATCH /api/v1/compliance/requirements/{id}`
- **인증**: 필요
- **Request Body**: `{ "name": "string", "basis": "string", "scope": "string" }`
- **Response Code**: 200 / 400 / 404

### API-COMP-005 · 변경 요청 연계

- **Endpoint**: `POST /api/v1/compliance/requirements/{id}/links`
- **인증**: 필요
- **Request Body**: `{ "changeId": "number" }`
- **Response Code**: 200 / 400 존재하지 않는 변경 요청 / 404

### API-COMP-006 · 책임자 지정

- **Endpoint**: `POST /api/v1/compliance/requirements/{id}/owner`
- **인증**: 필요
- **Request Body**: `{ "ownerId": "number" }`
- **Response Body** (200): `{ "id": "number", "owner": "string" }`
- **Response Code**: 200 / 400 존재하지 않는 사용자 / 404

### API-COMP-007 · 시정조치 등록

- **Endpoint**: `POST /api/v1/compliance/requirements/{id}/corrective-actions`
- **인증**: 필요
- **Request Body**: `{ "description": "string · 필수" }`
- **Response Body** (201): `{ "id": "number", "status": "DETECTED" }`
- **Response Code**: 201 / 400 내용 누락 / 404

### API-COMP-008 · 시정조치 상태 전이

- **Endpoint**: `PATCH /api/v1/compliance/corrective-actions/{actionId}/status`
- **인증**: 필요
- **Request Body**: `{ "targetStatus": "IN_PROGRESS|RESOLVED" }`
- **Response Code**: 200 / 400 정의되지 않은 순서의 전이 / 404

### API-COMP-009 · 컴플라이언스 감사 로그 조회

- **Endpoint**: `GET /api/v1/compliance/audit-logs?requirementId=&from=&to=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  [ { "eventType": "string", "actor": "string", "target": "string", "result": "SUCCESS|FAILURE", "occurredAt": "ISO-8601" } ]
  ```
- **Response Code**: 200(데이터 없으면 빈 배열) / 401

### API-COMP-010 · 준수 현황 조회

- **Endpoint**: `GET /api/v1/compliance/metrics?from=&to=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  { "totalRequirements": "number", "compliantCount": "number", "nonCompliantCount": "number", "openCorrectiveActionCount": "number", "complianceRate": "number" }
  ```
- **Response Code**: 200(데이터 없으면 빈 결과) / 401
