# API 명세서 — 문제 관리 (Problem)

> 도메인: problem · 버전: 0.3 · 작성일: 2026-07-11 · 승인 프로세스 커스텀 기능(유지보수 요청) 반영 — WORKAROUND → RESOLVED_CLOSED 전이에 공통 승인 게이트([common.md](common.md) API-COM-003~005) 추가(관리자가 규칙을 설정하지 않으면 기존과 동일하게 게이트 없이 진행)

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |
| 2026-07-22 | 프랙티스별 상태(state)별 승인자 지정 확장(유지보수 요청) — 등록(DETECTION)을 포함한 모든 상태 전이 지점이 승인 게이트 대상으로 일반화(기존 RESOLVED_CLOSED 하드코딩 지점은 마이그레이션 백필로 동일하게 동작). API-PRB-002(등록)에 생성 시점 게이트 409 추가, API-PRB-001 목록에 `pendingApprovalTargetState` 추가, API-PRB-003 상세에 `approval` 필드 신규 추가, API-PRB-004 409 응답에 반려(`APPROVAL_REJECTED`) 분기 추가(재승인요청은 [common.md](common.md) API-COM-006) |

## 공통 규약

- **Base Path**: `/api/v1` · 표준 오류 응답 본문은 [auth.md](auth.md) 공통 규약과 동일.
- 모든 API는 `Authorization: Bearer {accessToken}` **필요**.

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-PRB-001 | 문제 목록 조회 | GET | /api/v1/problems | 필요 |
| API-PRB-002 | 문제 등록 | POST | /api/v1/problems | 필요 |
| API-PRB-003 | 문제 상세 조회 | GET | /api/v1/problems/{id} | 필요 |
| API-PRB-004 | 상태(6단계) 전이 | PATCH | /api/v1/problems/{id}/status | 필요 |
| API-PRB-005 | RCA 작성/수정 | PUT | /api/v1/problems/{id}/rca | 필요 |
| API-PRB-006 | 워크어라운드 등록 | POST | /api/v1/problems/{id}/workaround | 필요 |
| API-PRB-007 | 알려진 오류(KE) 생성 | POST | /api/v1/problems/{id}/known-errors | 필요 |
| API-PRB-008 | KEDB 검색 | GET | /api/v1/known-errors | 필요 |
| API-PRB-009 | 인시던트/변경 연계 | POST | /api/v1/problems/{id}/links | 필요 |
| API-PRB-010 | 후속 조치 등록 | POST | /api/v1/problems/{id}/actions | 필요 |
| API-PRB-011 | 후속 조치 상태 변경 | PATCH | /api/v1/problems/{id}/actions/{actionId} | 필요 |
| API-PRB-012 | 문제 종료 | POST | /api/v1/problems/{id}/close | 필요 |

## 2. API 상세

### API-PRB-001 · 문제 목록 조회

- **Endpoint**: `GET /api/v1/problems?status=&priority=&origin=&assignee=&from=&to=&page=&size=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "ticketKey": "string · PRB-YYYY-####", "summary": "string", "status": "string", "priority": "string", "origin": "REACTIVE|PROACTIVE", "assignee": "string", "pendingApprovalTargetState": "string|null · 열린(IN_PROGRESS) 또는 REJECTED 승인 인스턴스가 있으면 그 targetState 원본 코드, 없으면 null(N+1 방지)", "updatedAt": "ISO-8601" } ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 / 401

### API-PRB-002 · 문제 등록

- **Endpoint**: `POST /api/v1/problems`
- **인증**: 필요
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  { "summary": "string · 필수", "description": "string", "origin": "REACTIVE|PROACTIVE", "investigationReason": "string", "impact": "HIGH|MEDIUM|LOW", "urgency": "HIGH|MEDIUM|LOW", "component": "string" }
  ```
- **Response Body** (201): `{ "id": "number", "ticketKey": "string", "status": "DETECTION", "priority": "string|null" }`
  > 문제 레코드는 이 API 호출로 즉시 생성·커밋된다(`TicketCreationGateSupport`가 REQUIRES_NEW로 먼저 커밋). 등록(DETECTION) 상태에 승인 게이트가 걸려 있으면 커밋 후 별도 트랜잭션에서 게이트를 평가해 409를 반환하지만 방금 커밋된 레코드는 롤백되지 않는다.
- **Response Code**: 201(매칭되는 승인 프로세스 없거나 0차 승인) / 400 요약 누락 / 401 / 409 등록(DETECTION) 상태에 승인 게이트가 걸려 승인 대기(`APPROVAL_PENDING`) — [common.md](common.md) 0절 생성 시점 게이트. 영향도·긴급도 중 하나라도 없으면 priority=null(미산정).

### API-PRB-003 · 문제 상세 조회

- **Endpoint**: `GET /api/v1/problems/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "id": "number", "ticketKey": "string", "summary": "string", "description": "string",
    "status": "string", "priority": "string", "impact": "string", "urgency": "string",
    "rca": { "rootCause": "string", "fiveWhys": ["string"], "category": "string" },
    "workaround": "string",
    "approval": { "approvalRequestId": "number|null", "status": "IN_PROGRESS|APPROVED|REJECTED|null · null=매칭되는 승인 프로세스 없음(게이트 없이 진행)", "targetState": "string|null · 원본 코드값(도착 상태, 생성 시점 스냅샷), 라벨은 FE가 기존 statusLabel()로 resolve" },
    "linkedIncidents": [ { "id": "number", "ticketKey": "string" } ],
    "linkedChanges": [ { "id": "number", "ticketKey": "string" } ],
    "linkedAssets": [ { "id": "number", "ticketKey": "string · assetKey" } ],
    "actions": [ { "id": "number", "description": "string", "status": "IN_PROGRESS|DONE" } ]
  }
  ```
- **Response Code**: 200 / 401 / 404. `approval` 상세(차수별 진행 상태)는 [common.md](common.md) API-COM-004로 조회한다.

### API-PRB-004 · 상태(6단계) 전이

- **Endpoint**: `PATCH /api/v1/problems/{id}/status`
- **인증**: 필요
- **Request Body**: `{ "targetStatus": "CLASSIFICATION|INVESTIGATION|KNOWN_ERROR|WORKAROUND|RESOLVED_CLOSED", "note": "string" }`
- **Response Code**: 200 / 400 순서 어긋난 전이 / 403 / 404 / 409 승인 완료 전 전이 시도(`APPROVAL_PENDING`) — [common.md](common.md) 0절 공통 게이트 로직(domain=PROBLEM, targetState=전이 대상 값, 요청유형 스코프 없음) 적용. **모든 targetStatus 전이 지점이 게이트 대상**이 될 수 있다(기존 RESOLVED_CLOSED 하드코딩 지점은 마이그레이션으로 동일하게 백필됨). 매칭되는 승인 프로세스가 없거나 0차 승인이면 게이트 없이 통과 / 최신 승인 인스턴스가 반려(`REJECTED`)면 `APPROVAL_REJECTED` — 재승인요청은 [common.md](common.md) API-COM-006

### API-PRB-005 · RCA 작성/수정

- **Endpoint**: `PUT /api/v1/problems/{id}/rca`
- **인증**: 필요(문제 접근 권한)
- **Request Body**: `{ "rootCause": "string", "fiveWhys": ["string"], "category": "string" }`
- **Response Body** (200): 저장된 RCA. 개인(사람)을 근본 원인으로 강제하지 않음.
- **Response Code**: 200 / 403 접근 권한 없음 / 404

### API-PRB-006 · 워크어라운드 등록

- **Endpoint**: `POST /api/v1/problems/{id}/workaround`
- **인증**: 필요
- **Request Body**: `{ "content": "string · 필수", "linkedArticleId": "number · 선택" }`
- **Response Code**: 200 / 400 내용 빈 값 / 403 / 404

### API-PRB-007 · 알려진 오류(KE) 생성

- **Endpoint**: `POST /api/v1/problems/{id}/known-errors`
- **인증**: 필요
- **Request Body**: `{ "title": "string", "rootCause": "string", "workaround": "string" }`
- **Response Body** (201): `{ "id": "number", "title": "string" }` (KEDB 등록)
- **Response Code**: 201 / 400 / 403 / 404

### API-PRB-008 · KEDB 검색

- **Endpoint**: `GET /api/v1/known-errors?keyword=&page=&size=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  { "content": [ { "id": "number", "title": "string", "rootCause": "string", "workaround": "string", "problemKey": "string" } ], "totalElements": "number" }
  ```
- **Response Code**: 200(매칭 없으면 빈 목록) / 401

### API-PRB-009 · 인시던트/변경 연계

- **Endpoint**: `POST /api/v1/problems/{id}/links`
- **인증**: 필요
- **Request Body**:
  ```json
  { "targetType": "INCIDENT|CHANGE", "targetId": "number · 기존", "createNewChange": "boolean · CHANGE 신규 생성" }
  ```
- **Response Body** (200): 양방향 링크 결과
- **Response Code**: 200 / 400 존재하지 않는 대상 / 403 / 404

### API-PRB-010 · 후속 조치 등록

- **Endpoint**: `POST /api/v1/problems/{id}/actions`
- **인증**: 필요
- **Request Body**: `{ "description": "string", "owner": "string", "dueDate": "ISO-8601" }`
- **Response Body** (201): `{ "id": "number", "status": "IN_PROGRESS" }`
- **Response Code**: 201 / 400 / 403 / 404

### API-PRB-011 · 후속 조치 상태 변경

- **Endpoint**: `PATCH /api/v1/problems/{id}/actions/{actionId}`
- **인증**: 필요
- **Request Body**: `{ "status": "IN_PROGRESS|DONE" }`
- **Response Code**: 200 / 400 / 403 / 404

### API-PRB-012 · 문제 종료

- **Endpoint**: `POST /api/v1/problems/{id}/close`
- **인증**: 필요
- **Request Body**: `{ "force": "boolean · 미해결 후속조치 경고 무시" }`
- **Response Body** (200): `{ "id": "number", "status": "RESOLVED_CLOSED", "warning": "string|null · 미해결 후속조치 존재 시" }`
- **Response Code**: 200 / 403 / 404. 미해결 후속 조치가 있으면 warning 반환(force=false 시 경고, true 시 종료).
