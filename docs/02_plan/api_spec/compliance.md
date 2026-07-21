# API 명세서 — 컴플라이언스 관리 (Compliance Management)

> 도메인: compliance · 버전: 0.3 · 작성일: 2026-07-11 · 승인 프로세스 커스텀 기능(유지보수 요청) 반영 — 시정조치 IN_PROGRESS → RESOLVED 전이에 공통 승인 게이트([common.md](common.md) API-COM-003~005) 추가(관리자가 규칙을 설정하지 않으면 기존과 동일하게 게이트 없이 진행)

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-10 | 최초 작성 |
| 2026-07-22 | 프랙티스별 상태(state)별 승인자 지정 확장(유지보수 요청) — 승인 게이트의 대상 티켓은 시정조치(`CorrectiveAction`, ticket_type=COMPLIANCE)이며, 등록(DETECTED)을 포함한 모든 상태 전이 지점이 게이트 대상으로 일반화(기존 RESOLVED 하드코딩 지점은 마이그레이션 백필로 동일하게 동작). API-COMP-007(시정조치 등록)에 생성 시점 게이트 409 추가, API-COMP-003 상세의 `correctiveActions` 배열 항목에 `approval` 필드 신규 추가, API-COMP-008 409 응답에 반려(`APPROVAL_REJECTED`) 분기 추가(재승인요청은 [common.md](common.md) API-COM-006) |

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
    "correctiveActions": [
      {
        "id": "number", "description": "string", "status": "DETECTED|IN_PROGRESS|RESOLVED", "updatedAt": "ISO-8601",
        "approval": { "approvalRequestId": "number|null", "status": "IN_PROGRESS|APPROVED|REJECTED|null · null=매칭되는 승인 프로세스 없음(게이트 없이 진행)", "targetState": "string|null · 원본 코드값(도착 상태, 생성 시점 스냅샷), 라벨은 FE가 기존 statusLabel()로 resolve" }
      }
    ],
    "linkedChanges": [ { "id": "number", "ticketKey": "string" } ]
  }
  ```
- **Response Code**: 200 / 401 / 404. 시정조치 `approval` 상세(차수별 진행 상태)는 [common.md](common.md) API-COM-004로 조회한다.

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
  > 시정조치 레코드는 이 API 호출로 즉시 생성·커밋된다(`TicketCreationGateSupport`가 REQUIRES_NEW로 먼저 커밋). 등록(DETECTED) 상태에 승인 게이트가 걸려 있으면 커밋 후 별도 트랜잭션에서 게이트를 평가해 409를 반환하지만 방금 커밋된 레코드는 롤백되지 않는다.
- **Response Code**: 201(매칭되는 승인 프로세스 없거나 0차 승인) / 400 내용 누락 / 404 / 409 등록(DETECTED) 상태에 승인 게이트가 걸려 승인 대기(`APPROVAL_PENDING`) — [common.md](common.md) 0절 생성 시점 게이트

### API-COMP-008 · 시정조치 상태 전이

- **Endpoint**: `PATCH /api/v1/compliance/corrective-actions/{actionId}/status`
- **인증**: 필요
- **Request Body**: `{ "targetStatus": "IN_PROGRESS|RESOLVED" }`
- **Response Code**: 200 / 400 정의되지 않은 순서의 전이 / 404 / 409 승인 완료 전 전이 시도(`APPROVAL_PENDING`) — [common.md](common.md) 0절 공통 게이트 로직(domain=COMPLIANCE, targetState=전이 대상 값, 요청유형 스코프 없음) 적용. **모든 targetStatus 전이 지점이 게이트 대상**이 될 수 있다(기존 RESOLVED 하드코딩 지점은 마이그레이션으로 동일하게 백필됨). 매칭되는 승인 프로세스가 없거나 0차 승인이면 게이트 없이 통과 / 최신 승인 인스턴스가 반려(`REJECTED`)면 `APPROVAL_REJECTED` — 재승인요청은 [common.md](common.md) API-COM-006

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
