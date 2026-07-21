# API 명세서 — 인시던트 관리 (Incident)

> 도메인: incident · 버전: 0.5

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |
| 2026-07-22 | 프랙티스별 상태(state)별 승인자 지정 확장(유지보수 요청) — 등록(NEW)을 포함한 모든 상태 전이 지점이 승인 게이트 대상으로 일반화(기존 RESOLVED 하드코딩 지점은 마이그레이션 백필로 동일하게 동작). API-INC-002(등록)에 생성 시점 게이트 409 추가, API-INC-001 목록에 `pendingApprovalTargetState` 추가, API-INC-003 상세에 `approval` 필드 신규 추가, API-INC-005/009 409 응답에 반려(`APPROVAL_REJECTED`) 분기 추가(재승인요청은 [common.md](common.md) API-COM-006) |
| 2026-07-12 | IN_PROGRESS→RESOLVED 전이(API-INC-005/009 두 경로)에 공통 승인 게이트([common.md](common.md) API-COM-003~005) 추가 |
| 2026-07-16 | API-INC-003 응답 `timeline` 항목에 `actor` 필드 추가; `STATUS_*` 타임라인 기본 메시지의 상태 코드를 라벨로 정리 |

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
    "content": [ { "id": "number", "ticketKey": "string · INC-YYYY-####", "summary": "string", "severity": "SEV1|SEV2|SEV3", "status": "NEW|IN_PROGRESS|RESOLVED|CLOSED", "assignee": "string", "postmortemRequired": "boolean", "pendingApprovalTargetState": "string|null · 열린(IN_PROGRESS) 또는 REJECTED 승인 인스턴스가 있으면 그 targetState 원본 코드, 없으면 null(N+1 방지)", "updatedAt": "ISO-8601" } ],
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
  > 인시던트 레코드는 이 API 호출로 즉시 생성·커밋된다(`TicketCreationGateSupport`가 REQUIRES_NEW로 먼저 커밋). 등록(NEW) 상태에 승인 게이트가 걸려 있으면 커밋 후 별도 트랜잭션에서 게이트를 평가해 409를 반환하지만 방금 커밋된 레코드는 롤백되지 않는다.
- **Response Code**: 201(매칭되는 승인 프로세스 없거나 0차 승인) / 400 요약·심각도 누락 / 401 / 409 등록(NEW) 상태에 승인 게이트가 걸려 승인 대기(`APPROVAL_PENDING`) — [common.md](common.md) 0절 생성 시점 게이트

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
    "approval": { "approvalRequestId": "number|null", "status": "IN_PROGRESS|APPROVED|REJECTED|null · null=매칭되는 승인 프로세스 없음(게이트 없이 진행)", "targetState": "string|null · 원본 코드값(도착 상태, 생성 시점 스냅샷), 라벨은 FE가 기존 statusLabel()로 resolve" },
    "metrics": { "mttdMinutes": "number|null", "mttaMinutes": "number|null", "mttrMinutes": "number|null" },
    "links": [ { "type": "PROBLEM|ASSET", "targetKey": "string" } ],
    "timeline": [ { "type": "string", "visibility": "INTERNAL|EXTERNAL", "message": "string", "actor": "string · 행위 수행 주체자 표시명(createdBy 이메일을 이름으로 resolve, 실패 시 이메일 그대로)", "at": "ISO-8601" } ]
  }
  ```
  > `STATUS_*` 타임라인 이벤트의 기본 메시지(사용자가 별도 note를 지정하지 않은 경우)는 상태 enum 코드(`target.name()`) 대신 상태 라벨을 사용한다(예: `"상태가 '대응중'으로 변경되었습니다"`).
- **Response Code**: 200 / 401 / 404. `approval` 상세(차수별 진행 상태)는 [common.md](common.md) API-COM-004로 조회한다.

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
- **Response Code**: 200 / 400 허용되지 않은 전이 / 403 / 404 / 409 승인 완료 전 전이 시도(`APPROVAL_PENDING`) — [common.md](common.md) 0절 공통 게이트 로직(domain=INCIDENT, targetState=전이 대상 값, 요청유형 스코프 없음) 적용. **모든 targetStatus 전이 지점이 게이트 대상**이 될 수 있다(기존 RESOLVED 하드코딩 지점은 마이그레이션으로 동일하게 백필됨). 매칭되는 승인 프로세스가 없거나 0차 승인이면 게이트 없이 통과 / 최신 승인 인스턴스가 반려(`REJECTED`)면 `APPROVAL_REJECTED` — 재승인요청은 [common.md](common.md) API-COM-006

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
- **Response Code**: 200(시각 없으면 해당 지표 null=미산정) / 403 / 404 / 409 승인 완료 전 RESOLVED 전이 시도(`APPROVAL_PENDING`) — API-INC-005와 동일한 [common.md](common.md) 0절 공통 게이트 로직(domain=INCIDENT, targetState=RESOLVED) 적용. 본 API도 IN_PROGRESS→RESOLVED 전이를 수행하므로 API-INC-005와 동일 게이트를 거친다(우회 경로 방지). 매칭되는 승인 프로세스가 없거나 0차 승인이면 게이트 없이 통과 / 최신 승인 인스턴스가 반려(`REJECTED`)면 `APPROVAL_REJECTED` — 재승인요청은 [common.md](common.md) API-COM-006

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
