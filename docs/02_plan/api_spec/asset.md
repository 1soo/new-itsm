# API 명세서 — IT 자산 관리 / CMDB (Asset)

> 도메인: asset · 버전: 0.1 · 작성일: 2026-07-09

## 공통 규약

- **Base Path**: `/api/v1` · 표준 오류 응답 본문은 [auth.md](auth.md) 공통 규약과 동일.
- 모든 API는 `Authorization: Bearer {accessToken}` **필요**.

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-ITAM-001 | 자산 목록 조회 | GET | /api/v1/assets | 필요 |
| API-ITAM-002 | 자산 등록 | POST | /api/v1/assets | 필요(Asset Manager) |
| API-ITAM-003 | 자산 상세 조회 | GET | /api/v1/assets/{id} | 필요 |
| API-ITAM-004 | 자산 수정 | PATCH | /api/v1/assets/{id} | 필요(Asset Manager) |
| API-ITAM-005 | 생애주기 단계 전이 | PATCH | /api/v1/assets/{id}/lifecycle | 필요(Asset Manager) |
| API-ITAM-006 | 자산 폐기 | PATCH | /api/v1/assets/{id}/retire | 필요(Asset Manager) |
| API-ITAM-007 | 자산 티켓 연계 | POST | /api/v1/assets/{id}/links | 필요 |
| API-ITAM-008 | CI 목록 조회 | GET | /api/v1/cis | 필요 |
| API-ITAM-009 | CI 등록 | POST | /api/v1/cis | 필요 |
| API-ITAM-010 | CI 관계 등록 | POST | /api/v1/cis/{id}/relations | 필요 |
| API-ITAM-011 | CI 영향 범위 조회 | GET | /api/v1/cis/{id}/impact | 필요 |
| API-ITAM-012 | 자산 지표 조회 | GET | /api/v1/assets/metrics | 필요 |

## 2. API 상세

### API-ITAM-001 · 자산 목록 조회

- **Endpoint**: `GET /api/v1/assets?type=&status=&owner=&expiringWithinDays=&keyword=&page=&size=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "assetKey": "string · AST-####", "name": "string", "type": "HARDWARE|SOFTWARE|CLOUD", "status": "string", "owner": "string", "expiryDate": "ISO-8601|null", "expiryStatus": "OK|EXPIRING|EXPIRED" } ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 / 401

### API-ITAM-002 · 자산 등록

- **Endpoint**: `POST /api/v1/assets`
- **인증**: 필요(Asset Manager)
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "name": "string · 필수", "type": "HARDWARE|SOFTWARE|CLOUD · 필수", "owner": "string", "location": "string",
    "purchaseDate": "ISO-8601", "cost": "number",
    "licenseExpiry": "ISO-8601", "warrantyExpiry": "ISO-8601", "contractExpiry": "ISO-8601",
    "attributes": { "key": "value · 유형별 표준 속성" }
  }
  ```
- **Response Body** (201): `{ "id": "number", "assetKey": "string", "status": "PLANNING" }`
- **Response Code**: 201 / 400 이름·유형·유형별 필수 속성 누락 / 403 권한 부족

### API-ITAM-003 · 자산 상세 조회

- **Endpoint**: `GET /api/v1/assets/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "id": "number", "assetKey": "string", "name": "string", "type": "string", "status": "string",
    "owner": "string", "location": "string", "attributes": {},
    "expiry": { "license": "ISO-8601|null", "warranty": "ISO-8601|null", "contract": "ISO-8601|null" },
    "lifecycleHistory": [ { "stage": "string", "at": "ISO-8601" } ],
    "linkedTickets": [ { "type": "SERVICE_REQUEST|INCIDENT|PROBLEM|CHANGE", "ticketKey": "string" } ],
    "linkedCis": [ { "ciId": "number", "name": "string" } ]
  }
  ```
- **Response Code**: 200 / 401 / 404

### API-ITAM-004 · 자산 수정

- **Endpoint**: `PATCH /api/v1/assets/{id}`
- **인증**: 필요(Asset Manager)
- **Request Body**: API-ITAM-002 필드 부분 갱신
- **Response Code**: 200 / 400(만료일 과거 시 warning 포함) / 403 / 404

### API-ITAM-005 · 생애주기 단계 전이

- **Endpoint**: `PATCH /api/v1/assets/{id}/lifecycle`
- **인증**: 필요(Asset Manager)
- **Request Body**: `{ "targetStage": "PLANNING|PROCUREMENT|OPERATION|MAINTENANCE|RETIREMENT" }`
- **Response Body** (200): `{ "id": "number", "status": "string" }` (이력 기록)
- **Response Code**: 200 / 400 정의되지 않은 단계 / 403 / 404

### API-ITAM-006 · 자산 폐기

- **Endpoint**: `PATCH /api/v1/assets/{id}/retire`
- **인증**: 필요(Asset Manager)
- **Response Body** (200): `{ "id": "number", "status": "RETIRED" }`
- **Response Code**: 200 / 403 / 404

### API-ITAM-007 · 자산 티켓 연계

- **Endpoint**: `POST /api/v1/assets/{id}/links`
- **인증**: 필요
- **Request Body**: `{ "ticketType": "SERVICE_REQUEST|INCIDENT|PROBLEM|CHANGE", "ticketId": "number" }`
- **Response Code**: 200 / 400 존재하지 않는 티켓 / 403 / 404

### API-ITAM-008 · CI 목록 조회

- **Endpoint**: `GET /api/v1/cis?keyword=&type=&page=&size=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  { "content": [ { "id": "number", "name": "string", "type": "string" } ], "totalElements": "number" }
  ```
- **Response Code**: 200 / 401

### API-ITAM-009 · CI 등록

- **Endpoint**: `POST /api/v1/cis`
- **인증**: 필요
- **Request Body**: `{ "name": "string · 필수", "type": "string", "assetId": "number · 연결 자산(선택)" }`
- **Response Body** (201): `{ "id": "number", "name": "string" }`
- **Response Code**: 201 / 400 / 403

### API-ITAM-010 · CI 관계 등록

- **Endpoint**: `POST /api/v1/cis/{id}/relations`
- **인증**: 필요
- **Request Body**: `{ "targetCiId": "number", "relationType": "DEPENDS_ON|RUNS_ON|CONNECTS_TO" }`
- **Response Body** (200): 저장된 관계
- **Response Code**: 200 / 400 존재하지 않는 CI / 403 / 404

### API-ITAM-011 · CI 영향 범위 조회

- **Endpoint**: `GET /api/v1/cis/{id}/impact`
- **인증**: 필요
- **Response Body** (200):
  ```json
  [ { "ciId": "number", "name": "string", "relationType": "string", "depth": "number" } ]
  ```
- **Response Code**: 200(관계 없으면 빈 목록) / 401 / 404

### API-ITAM-012 · 자산 지표 조회

- **Endpoint**: `GET /api/v1/assets/metrics?from=&to=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  { "utilizationRate": "number", "expiringCount": "number", "typeDistribution": { "HARDWARE": "number", "SOFTWARE": "number", "CLOUD": "number" } }
  ```
- **Response Code**: 200(데이터 없으면 빈 결과) / 401
