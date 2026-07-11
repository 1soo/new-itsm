# API 명세서 — 인시던트 관리 (Incident)

> 도메인: incident · 버전: 0.2 · 작성일: 2026-07-11 · 승인 프로세스 커스텀 기능(유지보수 요청) 반영 — IN_PROGRESS → RESOLVED 전이에 공통 승인 게이트([common.md](common.md) API-COM-003~005) 추가(관리자가 규칙을 설정하지 않으면 기존과 동일하게 게이트 없이 진행)

## 공통 규약

- **Base Path**: `/api/v1` · 표준 오류 응답 본문은 [auth.md](auth.md) 공통 규약과 동일.
- 모든 API는 `Authorization: Bearer {accessToken}` **필요**.

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-INC-001 | 인시던트 목록 조회 | GET | /api/v1/incidents | 필요 |
| API-INC-002 | 인시던트 등록 | POST | /api/v1/incidents | 필요 |
| API-INC-003 | 인시던트 상세 조회 | GET | /api/v1/incidents/{id} | 필요 |
| API-INC-004 | 심각도·우선순위 변경 | PATCH | /api/v1/incidents/{id}/severity | 필요 |
| API-INC-005 | 상태 전이 | PATCH | /api/v1/incidents/{id}/status | 필요 |
| API-INC-006 | 대응 역할 배정 | POST | /api/v1/incidents/{id}/roles | 필요(Incident Manager) |
| API-INC-007 | 에스컬레이션 | POST | /api/v1/incidents/{id}/escalate | 필요 |
| API-INC-008 | 상태 업데이트(타임라인) | POST | /api/v1/incidents/{id}/updates | 필요 |
| API-INC-009 | 해결 처리·시간 지표 | POST | /api/v1/incidents/{id}/resolve | 필요 |
| API-INC-010 | 포스트모템 조회 | GET | /api/v1/incidents/{id}/postmortem | 필요 |
| API-INC-011 | 포스트모템 작성/수정 | PUT | /api/v1/incidents/{id}/postmortem | 필요 |
| API-INC-012 | 문제 연계(링크) | POST | /api/v1/incidents/{id}/links | 필요 |
| API-INC-013 | 인시던트 지표 조회 | GET | /api/v1/incidents/metrics | 필요 |

## 2. API 상세

### API-INC-001 · 인시던트 목록 조회

- **Endpoint**: `GET /api/v1/incidents?status=&severity=&assignee=&keyword=&from=&to=&page=&size=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "ticketKey": "string · INC-YYYY-####", "summary": "string", "severity": "SEV1|SEV2|SEV3", "status": "NEW|IN_PROGRESS|RESOLVED|CLOSED", "assignee": "string", "postmortemRequired": "boolean", "updatedAt": "ISO-8601" } ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 / 401

### API-INC-002 · 인시던트 등록

- **Endpoint**: `POST /api/v1/incidents`
- **인증**: 필요
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  { "summary": "string · 필수", "description": "string", "severity": "SEV1|SEV2|SEV3 · 필수", "affectedService": "string", "affectedProduct": "string" }
  ```
- **Response Body** (201): `{ "id": "number", "ticketKey": "string", "status": "NEW" }`
- **Response Code**: 201 / 400 요약·심각도 누락 / 401

### API-INC-003 · 인시던트 상세 조회

- **Endpoint**: `GET /api/v1/incidents/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "id": "number", "ticketKey": "string", "summary": "string", "description": "string",
    "severity": "string", "priority": "string", "status": "string",
    "affectedService": "string", "affectedProduct": "string",
    "responders": [ { "userId": "number", "name": "string", "role": "TECH_LEAD|COMMS|SCRIBE" } ],
    "metrics": { "mttdMinutes": "number|null", "mttaMinutes": "number|null", "mttrMinutes": "number|null" },
    "links": [ { "type": "PROBLEM|ASSET", "targetKey": "string" } ],
    "timeline": [ { "type": "string", "visibility": "INTERNAL|EXTERNAL", "message": "string", "at": "ISO-8601" } ]
  }
  ```
- **Response Code**: 200 / 401 / 404

### API-INC-004 · 심각도·우선순위 변경

- **Endpoint**: `PATCH /api/v1/incidents/{id}/severity`
- **인증**: 필요
- **Request Body**: `{ "severity": "SEV1|SEV2|SEV3", "priority": "P1|P2|P3|P4" }`
- **Response Body** (200): 갱신 값 + 변경 이력 기록
- **Response Code**: 200 / 400 정의되지 않은 값 / 403 / 404

### API-INC-005 · 상태 전이

- **Endpoint**: `PATCH /api/v1/incidents/{id}/status`
- **인증**: 필요
- **Request Body**: `{ "targetStatus": "IN_PROGRESS|RESOLVED|CLOSED", "note": "string" }`
- **Response Code**: 200 / 400 허용되지 않은 전이 / 403 / 404 / 409 승인 완료 전 RESOLVED 전이 시도 — [common.md](common.md) 0절 공통 게이트 로직(domain=INCIDENT, 요청유형 스코프 없음) 적용. 매칭되는 승인 프로세스가 없거나 0차 승인이면 게이트 없이 통과(기존과 동일)

### API-INC-006 · 대응 역할 배정

- **Endpoint**: `POST /api/v1/incidents/{id}/roles`
- **인증**: 필요(Incident Manager)
- **Request Body**: `{ "userId": "number", "role": "TECH_LEAD|COMMS|SCRIBE" }`
- **Response Body** (200): 배정 결과
- **Response Code**: 200 / 400 / 403 IM 권한 아님 / 404

### API-INC-007 · 에스컬레이션

- **Endpoint**: `POST /api/v1/incidents/{id}/escalate`
- **인증**: 필요
- **Request Body**: `{ "targetUserId": "number", "type": "HIERARCHICAL|FUNCTIONAL", "reason": "string" }`
- **Response Body** (200): 이관 결과·이력
- **Response Code**: 200 / 400 대상 없음 / 403 / 404

### API-INC-008 · 상태 업데이트(타임라인)

- **Endpoint**: `POST /api/v1/incidents/{id}/updates`
- **인증**: 필요(접근 권한)
- **Request Body**: `{ "message": "string", "visibility": "INTERNAL|EXTERNAL" }`
- **Response Body** (201): `{ "id": "number", "at": "ISO-8601" }`
- **Response Code**: 201 / 403 접근 권한 없음 / 404

### API-INC-009 · 해결 처리·시간 지표

- **Endpoint**: `POST /api/v1/incidents/{id}/resolve`
- **인증**: 필요
- **Request Body**:
  ```json
  { "impactStartAt": "ISO-8601 · 선택", "detectedAt": "ISO-8601 · 선택", "impactEndAt": "ISO-8601 · 선택", "resolutionNote": "string" }
  ```
- **Response Body** (200):
  ```json
  { "id": "number", "status": "RESOLVED", "metrics": { "mttdMinutes": "number|null", "mttaMinutes": "number|null", "mttrMinutes": "number|null" } }
  ```
- **Response Code**: 200(시각 없으면 해당 지표 null=미산정) / 403 / 404

### API-INC-010 · 포스트모템 조회

- **Endpoint**: `GET /api/v1/incidents/{id}/postmortem`
- **인증**: 필요
- **Response Body** (200): 포스트모템 본문(없으면 404)
- **Response Code**: 200 / 404 미작성

### API-INC-011 · 포스트모템 작성/수정

- **Endpoint**: `PUT /api/v1/incidents/{id}/postmortem`
- **인증**: 필요
- **Request Body**:
  ```json
  {
    "summary": "string", "timeline": "string",
    "fiveWhys": ["string · Why 사슬"], "rootCause": "string · 필수",
    "actionItems": [ { "description": "string", "owner": "string", "dueDate": "ISO-8601", "status": "OPEN|DONE" } ]
  }
  ```
- **Response Body** (200): 저장된 포스트모템
- **Response Code**: 200 / 400 근본원인 누락 / 403 / 404

### API-INC-012 · 문제 연계(링크)

- **Endpoint**: `POST /api/v1/incidents/{id}/links`
- **인증**: 필요
- **Request Body**: `{ "problemId": "number · 기존 문제", "createNewProblem": "boolean · true면 신규 생성" }`
- **Response Body** (200): `{ "incidentId": "number", "problemId": "number" }`
- **Response Code**: 200 / 400 존재하지 않는 문제 / 403 / 404

### API-INC-013 · 인시던트 지표 조회

- **Endpoint**: `GET /api/v1/incidents/metrics?from=&to=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  { "count": "number", "severityDistribution": { "SEV1": "number", "SEV2": "number", "SEV3": "number" }, "avgMttrMinutes": "number" }
  ```
- **Response Code**: 200(데이터 없으면 빈 결과) / 401
