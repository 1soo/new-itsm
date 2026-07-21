# API 명세서 — IT 자산 관리 / CMDB (Asset)

> 도메인: asset · 버전: 0.3 · 작성일: 2026-07-11 · 승인 프로세스 커스텀 기능(유지보수 요청) 반영 — 자산 폐기(퇴역) 전이에 공통 승인 게이트([common.md](common.md) API-COM-003~005) 추가(관리자가 규칙을 설정하지 않으면 기존과 동일하게 게이트 없이 진행)

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |
| 2026-07-22 | 프랙티스별 상태(state)별 승인자 지정 확장(유지보수 요청) — 등록(PLANNING)을 포함한 모든 생애주기 전이 지점이 승인 게이트 대상으로 일반화(기존 RETIREMENT 하드코딩 지점은 마이그레이션 백필로 동일하게 동작). API-ITAM-002(등록)에 생성 시점 게이트 409 추가, API-ITAM-001 목록에 `pendingApprovalTargetState` 추가, API-ITAM-003 상세에 `approval` 필드 신규 추가, API-ITAM-005/006 409 응답에 반려(`APPROVAL_REJECTED`) 분기 추가(재승인요청은 [common.md](common.md) API-COM-006) |

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
    "content": [ { "id": "number", "assetKey": "string · AST-####", "name": "string", "type": "HARDWARE|SOFTWARE|CLOUD", "status": "string", "owner": "string", "expiryDate": "ISO-8601|null", "expiryStatus": "OK|EXPIRING|EXPIRED", "pendingApprovalTargetState": "string|null · 열린(IN_PROGRESS) 또는 REJECTED 승인 인스턴스가 있으면 그 targetState 원본 코드, 없으면 null(N+1 방지)" } ],
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
  > 자산 레코드는 이 API 호출로 즉시 생성·커밋된다(`TicketCreationGateSupport`가 REQUIRES_NEW로 먼저 커밋). 등록(PLANNING) 상태에 승인 게이트가 걸려 있으면 커밋 후 별도 트랜잭션에서 게이트를 평가해 409를 반환하지만 방금 커밋된 레코드는 롤백되지 않는다.
- **Response Code**: 201(매칭되는 승인 프로세스 없거나 0차 승인) / 400 이름·유형·유형별 필수 속성 누락 / 403 권한 부족 / 409 등록(PLANNING) 상태에 승인 게이트가 걸려 승인 대기(`APPROVAL_PENDING`) — [common.md](common.md) 0절 생성 시점 게이트

### API-ITAM-003 · 자산 상세 조회

- **Endpoint**: `GET /api/v1/assets/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "id": "number", "assetKey": "string", "name": "string", "type": "string", "status": "string",
    "owner": "string", "location": "string", "attributes": {},
    "approval": { "approvalRequestId": "number|null", "status": "IN_PROGRESS|APPROVED|REJECTED|null · null=매칭되는 승인 프로세스 없음(게이트 없이 진행)", "targetState": "string|null · 원본 코드값(도착 상태, 생성 시점 스냅샷), 라벨은 FE가 기존 statusLabel()로 resolve" },
    "expiry": { "license": "ISO-8601|null", "warranty": "ISO-8601|null", "contract": "ISO-8601|null" },
    "lifecycleHistory": [ { "stage": "string", "at": "ISO-8601" } ],
    "linkedTickets": [ { "type": "SERVICE_REQUEST|INCIDENT|PROBLEM|CHANGE", "ticketKey": "string" } ],
    "linkedCis": [ { "ciId": "number", "name": "string" } ]
  }
  ```
- **Response Code**: 200 / 401 / 404. `approval` 상세(차수별 진행 상태)는 [common.md](common.md) API-COM-004로 조회한다.

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
- **Response Code**: 200 / 400 정의되지 않은 단계 / 403 / 404 / 409 승인 완료 전 전이 시도(`APPROVAL_PENDING`) — [common.md](common.md) 0절 공통 게이트 로직(domain=ASSET, targetState=전이 대상 값, 요청유형 스코프 없음) 적용. **모든 targetStage 전이 지점이 게이트 대상**이 될 수 있다(기존 RETIREMENT 하드코딩 지점은 마이그레이션으로 동일하게 백필됨). 매칭되는 승인 프로세스가 없거나 0차 승인이면 게이트 없이 통과 / 최신 승인 인스턴스가 반려(`REJECTED`)면 `APPROVAL_REJECTED` — 재승인요청은 [common.md](common.md) API-COM-006

### API-ITAM-006 · 자산 폐기

- **Endpoint**: `PATCH /api/v1/assets/{id}/retire`
- **인증**: 필요(Asset Manager)
- **Response Body** (200): `{ "id": "number", "status": "RETIRED" }`
- **Response Code**: 200 / 403 / 404 / 409 승인 완료 전 폐기 시도(`APPROVAL_PENDING`) — API-ITAM-005(targetStage=RETIREMENT)와 동일한 공통 게이트 로직 적용 / 최신 승인 인스턴스가 반려(`REJECTED`)면 `APPROVAL_REJECTED` — 재승인요청은 [common.md](common.md) API-COM-006

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
