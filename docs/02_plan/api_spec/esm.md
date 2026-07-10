# API 명세서 — 엔터프라이즈 서비스 관리 (ESM)

> 도메인: esm · 버전: 0.1 · 작성일: 2026-07-10

## 공통 규약

- **Base Path**: `/api/v1` · 표준 오류 응답 본문은 [auth.md](auth.md) 공통 규약과 동일.
- 모든 API는 `Authorization: Bearer {accessToken}` **필요**.
- 부서 코드(`department`): `HR|LEGAL|FACILITIES|FINANCE|IT`

## 0. 설계 배경

- 부서 요청 처리는 서비스 요청(SRM) 도메인과 별도의 엔드포인트(`/api/v1/esm/*`)로 분리하되, 개념·상태 흐름은 단순화해 재사용한다(승인 단계는 요구사항에 없어 제외). 카탈로그 항목에 담당 부서(`department`)를 필수로 지정한다(REQ-ESM-001 Unwanted).
- 카탈로그 항목에 `checklistTemplateType`(NONE/ONBOARDING/OFFBOARDING)과 `checklistTemplate`(하위 작업 템플릿 목록)을 정의할 수 있다. 요청 제출 시(API-ESM-005) 해당 항목의 타입이 ONBOARDING/OFFBOARDING이면 시스템이 체크리스트를 자동 생성하고 템플릿의 하위 작업을 관련 부서에 배정한다. 템플릿이 비어 있으면 제출을 400으로 거부한다(REQ-ESM-005 Unwanted).
- 오프보딩 체크리스트는 생성 시 자산(ITAM) 도메인의 `GET /api/v1/assets?owner={대상자명}`([asset.md](asset.md) API-ITAM-001)을 조회해 대상자 보유 자산마다 자산 회수 하위 작업을 자동 추가한다(REQ-ESM-006). 대상자 보유 자산이 없으면 자산 회수 하위 작업 없이 체크리스트만 생성한다(FEAT-ESM-006 예외 처리).
- HR 케이스는 부서 요청과 별개 엔티티다. HR 담당자(HR_CASE_MANAGER)가 직접 접수(Intake)하며, 민감 정보이므로 해당 역할만 조회·처리할 수 있다(REQ-ESM-004).

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-ESM-001 | 부서 카탈로그 목록 조회 | GET | /api/v1/esm/catalog-items | 필요 |
| API-ESM-002 | 카탈로그 항목 상세(양식 스키마) | GET | /api/v1/esm/catalog-items/{id} | 필요 |
| API-ESM-003 | 카탈로그 항목 생성 | POST | /api/v1/esm/catalog-items | 필요(Process Owner) |
| API-ESM-004 | 카탈로그 항목 수정 | PATCH | /api/v1/esm/catalog-items/{id} | 필요(Process Owner) |
| API-ESM-005 | 부서 요청 제출 | POST | /api/v1/esm/requests | 필요 |
| API-ESM-006 | 부서 요청 목록 조회 | GET | /api/v1/esm/requests | 필요 |
| API-ESM-007 | 부서 요청 상세 조회 | GET | /api/v1/esm/requests/{id} | 필요 |
| API-ESM-008 | 부서 요청 상태 전이 | PATCH | /api/v1/esm/requests/{id}/status | 필요(담당 부서 처리자) |
| API-ESM-009 | 부서 요청 코멘트 등록 | POST | /api/v1/esm/requests/{id}/comments | 필요 |
| API-ESM-010 | HR 케이스 접수 | POST | /api/v1/esm/hr-cases | 필요(HR Case Manager) |
| API-ESM-011 | HR 케이스 목록 조회 | GET | /api/v1/esm/hr-cases | 필요(HR Case Manager) |
| API-ESM-012 | HR 케이스 상세 조회 | GET | /api/v1/esm/hr-cases/{id} | 필요(HR Case Manager) |
| API-ESM-013 | HR 케이스 상태 전이 | PATCH | /api/v1/esm/hr-cases/{id}/status | 필요(HR Case Manager) |
| API-ESM-014 | 체크리스트 상세 조회 | GET | /api/v1/esm/checklists/{id} | 필요 |
| API-ESM-015 | 내 하위 작업 목록 조회 | GET | /api/v1/esm/checklist-tasks | 필요 |
| API-ESM-016 | 하위 작업 상태 변경 | PATCH | /api/v1/esm/checklist-tasks/{taskId}/status | 필요(담당 부서) |
| API-ESM-017 | ESM 지표 조회 | GET | /api/v1/esm/metrics | 필요 |

## 2. API 상세

### API-ESM-001 · 부서 카탈로그 목록 조회

- **Endpoint**: `GET /api/v1/esm/catalog-items?department=&keyword=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  [ { "id": "number", "name": "string", "description": "string", "department": "HR|LEGAL|FACILITIES|FINANCE|IT", "checklistTemplateType": "NONE|ONBOARDING|OFFBOARDING" } ]
  ```
- **Response Code**: 200 / 401

### API-ESM-002 · 카탈로그 항목 상세(양식 스키마)

- **Endpoint**: `GET /api/v1/esm/catalog-items/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "id": "number", "name": "string", "description": "string", "department": "string",
    "checklistTemplateType": "NONE|ONBOARDING|OFFBOARDING",
    "checklistTemplate": [ { "department": "string", "taskDescription": "string" } ],
    "formSchema": [ { "key": "string", "label": "string", "type": "text|select|number|date|file", "required": "boolean", "options": ["string"] } ]
  }
  ```
- **Response Code**: 200 / 401 / 404

### API-ESM-003 · 카탈로그 항목 생성

- **Endpoint**: `POST /api/v1/esm/catalog-items`
- **인증**: 필요(Process Owner)
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "name": "string · 필수", "description": "string", "department": "HR|LEGAL|FACILITIES|FINANCE|IT · 필수",
    "checklistTemplateType": "NONE|ONBOARDING|OFFBOARDING",
    "checklistTemplate": [ { "department": "string", "taskDescription": "string" } ],
    "formSchema": [ { "key": "string", "label": "string", "type": "string", "required": "boolean", "options": ["string"] } ]
  }
  ```
- **Response Body** (201): `{ "id": "number" }`
- **Response Code**: 201 / 400 이름·담당 부서 누락 / 403

### API-ESM-004 · 카탈로그 항목 수정

- **Endpoint**: `PATCH /api/v1/esm/catalog-items/{id}`
- **인증**: 필요(Process Owner)
- **Request Body**: API-ESM-003 필드 부분 갱신
- **Response Code**: 200 / 400 / 403 / 404

### API-ESM-005 · 부서 요청 제출

- **Endpoint**: `POST /api/v1/esm/requests`
- **인증**: 필요
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  { "catalogItemId": "number · 필수", "formValues": { "key": "value" }, "targetUserName": "string · 온보딩/오프보딩 대상자명(해당 유형일 때 필수)" }
  ```
- **Response Body** (201):
  ```json
  { "id": "number", "ticketKey": "string · ESM-YYYY-####", "status": "SUBMITTED", "checklistId": "number|null" }
  ```
- **Response Code**: 201 / 400 필수 필드 미입력 / 400 체크리스트 템플릿 미정의(ONBOARDING/OFFBOARDING 유형인데 템플릿 없음) / 401

### API-ESM-006 · 부서 요청 목록 조회

- **Endpoint**: `GET /api/v1/esm/requests?scope=mine|all&department=&status=&from=&to=&page=&size=`
- **인증**: 필요 (scope=mine: 본인 요청 / all·department: 해당 부서 처리자)
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "ticketKey": "string", "catalogItemName": "string", "department": "string", "status": "string", "updatedAt": "ISO-8601" } ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 / 401 / 403

### API-ESM-007 · 부서 요청 상세 조회

- **Endpoint**: `GET /api/v1/esm/requests/{id}`
- **인증**: 필요(접근 권한 검증)
- **Response Body** (200):
  ```json
  {
    "id": "number", "ticketKey": "string", "catalogItemName": "string", "department": "string", "status": "string",
    "formValues": {}, "requester": "string", "assignee": "string",
    "checklistId": "number|null",
    "comments": [ { "id": "number", "author": "string", "body": "string", "createdAt": "ISO-8601" } ],
    "timeline": [ { "type": "string", "message": "string", "at": "ISO-8601" } ]
  }
  ```
- **Response Code**: 200 / 401 / 403 / 404

### API-ESM-008 · 부서 요청 상태 전이

- **Endpoint**: `PATCH /api/v1/esm/requests/{id}/status`
- **인증**: 필요(담당 부서 처리자)
- **Request Body**: `{ "targetStatus": "IN_PROGRESS|COMPLETED|REJECTED", "note": "string" }`
- **Response Body** (200): `{ "id": "number", "status": "string" }`
- **Response Code**: 200 / 400 허용되지 않은 전이 / 403 담당 부서 아님 / 404

### API-ESM-009 · 부서 요청 코멘트 등록

- **Endpoint**: `POST /api/v1/esm/requests/{id}/comments`
- **인증**: 필요(요청 접근 권한)
- **Request Body**: `{ "body": "string · 필수" }`
- **Response Body** (201): `{ "id": "number", "author": "string", "body": "string", "createdAt": "ISO-8601" }`
- **Response Code**: 201 / 403 / 404

### API-ESM-010 · HR 케이스 접수

- **Endpoint**: `POST /api/v1/esm/hr-cases`
- **인증**: 필요(HR Case Manager)
- **Header**: `Content-Type: application/json`
- **Request Body**: `{ "subjectUserName": "string · 대상자", "title": "string · 필수", "description": "string" }`
- **Response Body** (201): `{ "id": "number", "status": "INTAKE" }`
- **Response Code**: 201 / 400 제목 누락 / 403 HR 역할 아님

### API-ESM-011 · HR 케이스 목록 조회

- **Endpoint**: `GET /api/v1/esm/hr-cases?status=&page=&size=`
- **인증**: 필요(HR Case Manager)
- **Response Body** (200):
  ```json
  { "content": [ { "id": "number", "title": "string", "status": "INTAKE|DOCUMENTATION|INVESTIGATION|RESOLUTION", "updatedAt": "ISO-8601" } ], "page": "number", "size": "number", "totalElements": "number" }
  ```
- **Response Code**: 200 / 401 / 403

### API-ESM-012 · HR 케이스 상세 조회

- **Endpoint**: `GET /api/v1/esm/hr-cases/{id}`
- **인증**: 필요(HR Case Manager)
- **Response Body** (200):
  ```json
  {
    "id": "number", "title": "string", "description": "string", "subjectUserName": "string", "status": "string",
    "history": [ { "status": "string", "changedBy": "string", "at": "ISO-8601" } ]
  }
  ```
- **Response Code**: 200 / 401 / 403 HR 역할 아님 / 404

### API-ESM-013 · HR 케이스 상태 전이

- **Endpoint**: `PATCH /api/v1/esm/hr-cases/{id}/status`
- **인증**: 필요(HR Case Manager)
- **Request Body**: `{ "targetStatus": "DOCUMENTATION|INVESTIGATION|RESOLUTION", "note": "string" }`
- **Response Code**: 200 / 400 정의된 순서(접수→기록→조사→해결) 외 전이 / 403 / 404

### API-ESM-014 · 체크리스트 상세 조회

- **Endpoint**: `GET /api/v1/esm/checklists/{id}`
- **인증**: 필요(연계된 요청 접근 권한 또는 하위 작업 담당 부서)
- **Response Body** (200):
  ```json
  {
    "id": "number", "type": "ONBOARDING|OFFBOARDING", "targetUserName": "string", "status": "IN_PROGRESS|COMPLETED",
    "tasks": [ { "id": "number", "department": "string", "description": "string", "status": "PENDING|DONE", "relatedAssetId": "number|null", "relatedAssetKey": "string|null" } ]
  }
  ```
- **Response Code**: 200 / 401 / 403 / 404

### API-ESM-015 · 내 하위 작업 목록 조회

- **Endpoint**: `GET /api/v1/esm/checklist-tasks?scope=mine&status=&page=&size=`
- **인증**: 필요 (scope=mine: 본인 소속 부서에 배정된 하위 작업)
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "checklistId": "number", "checklistType": "ONBOARDING|OFFBOARDING", "targetUserName": "string", "description": "string", "status": "PENDING|DONE" } ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 / 401

### API-ESM-016 · 하위 작업 상태 변경

- **Endpoint**: `PATCH /api/v1/esm/checklist-tasks/{taskId}/status`
- **인증**: 필요(하위 작업 배정 부서 소속)
- **Request Body**: `{ "status": "DONE" }`
- **Response Body** (200): `{ "id": "number", "status": "DONE", "checklistStatus": "IN_PROGRESS|COMPLETED · 전체 완료 여부 반영" }`
- **Response Code**: 200 / 403 배정 부서 아님 / 404. 전체 하위 작업 완료 시 체크리스트 상태를 COMPLETED로 자동 갱신.

### API-ESM-017 · ESM 지표 조회

- **Endpoint**: `GET /api/v1/esm/metrics?from=&to=&department=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  { "requestCount": "number", "avgProcessingMinutes": "number", "onboardingCompletionRate": "number", "offboardingCompletionRate": "number" }
  ```
- **Response Code**: 200(데이터 없으면 빈 결과) / 401
