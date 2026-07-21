# API 명세서 — 지식 관리 (Knowledge)

> 도메인: knowledge · 버전: 0.3 · 작성일: 2026-07-11 · 승인 프로세스 커스텀 기능(유지보수 요청) 반영 — 게이트키퍼 단일승인을 커스텀 프로세스로 대체, 전용 승인 API(API-KM-007/008) 삭제 후 공통 승인 API([common.md](common.md) API-COM-003~005)로 대체. 매칭되는 승인 프로세스가 없으면 검토 요청 즉시 게시(PUBLISHED)

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |
| 2026-07-22 | 프랙티스별 상태(state)별 승인자 지정 확장(유지보수 요청) — 등록(DRAFT)에 신규 생성 시점 게이트 추가(API-KM-003, 표준 throwing 모델, 재승인요청은 [common.md](common.md) API-COM-006). 검토 요청(API-KM-006)의 기존 non-throwing `evaluateAndCreateIfNeeded`는 `targetState="IN_REVIEW"` 파라미터만 추가되고 "호출마다 새 인스턴스 생성" 동작은 그대로 유지(반려 후 재검토 요청 자체가 곧 재승인요청이라 API-COM-006 대상 아님). API-KM-002 상세에 `approval` 필드 신규 추가 |

## 공통 규약

- **Base Path**: `/api/v1` · 표준 오류 응답 본문은 [auth.md](auth.md) 공통 규약과 동일.
- 모든 API는 `Authorization: Bearer {accessToken}` **필요**.

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-KM-001 | 기사 검색/목록 | GET | /api/v1/knowledge/articles | 필요 |
| API-KM-002 | 기사 상세/열람 | GET | /api/v1/knowledge/articles/{id} | 필요 |
| API-KM-003 | 기사 작성 | POST | /api/v1/knowledge/articles | 필요(Contributor) |
| API-KM-004 | 기사 수정 | PATCH | /api/v1/knowledge/articles/{id} | 필요(Contributor) |
| API-KM-005 | 기사 삭제 | DELETE | /api/v1/knowledge/articles/{id} | 필요(Contributor) |
| API-KM-006 | 기사 상태 전이(검토 요청 등) | PATCH | /api/v1/knowledge/articles/{id}/status | 필요 |
| API-KM-009 | 유용성 평가/피드백 | POST | /api/v1/knowledge/articles/{id}/feedback | 필요 |
| API-KM-010 | 카테고리 목록 | GET | /api/v1/knowledge/categories | 필요 |
| API-KM-011 | KCS 티켓 연계(작성/연결) | POST | /api/v1/knowledge/articles/link | 필요 |
| API-KM-012 | 지식 지표 조회 | GET | /api/v1/knowledge/metrics | 필요 |

## 2. API 상세

### API-KM-001 · 기사 검색/목록

- **Endpoint**: `GET /api/v1/knowledge/articles?keyword=&category=&label=&status=&page=&size=`
- **인증**: 필요 (최종 사용자에게는 게시(PUBLISHED) 기사만 반환)
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "title": "string", "summary": "string", "status": "DRAFT|IN_REVIEW|PUBLISHED", "category": "string", "helpfulRate": "number", "pendingApprovalTargetState": "string|null · 열린(IN_PROGRESS) 또는 REJECTED 승인 인스턴스가 있으면 그 targetState 원본 코드, 없으면 null(N+1 방지, Contributor 화면에서만 의미)" } ],
    "page": "number", "size": "number", "totalElements": "number", "noResult": "boolean · 무결과 검색 기록 여부"
  }
  ```
- **Response Code**: 200(매칭 없으면 빈 결과·무결과 기록) / 401

### API-KM-002 · 기사 상세/열람

- **Endpoint**: `GET /api/v1/knowledge/articles/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  {
    "id": "number", "title": "string", "body": "string", "status": "string", "category": "string", "labels": ["string"], "helpful": "number", "notHelpful": "number",
    "approval": { "approvalRequestId": "number|null", "status": "IN_PROGRESS|APPROVED|REJECTED|null · null=매칭되는 승인 프로세스 없음(게이트 없이 진행)", "targetState": "string|null · 원본 코드값(도착 상태, 생성 시점 스냅샷), 라벨은 FE가 기존 statusLabel()로 resolve" }
  }
  ```
- **Response Code**: 200 / 401 / 403 미게시 기사에 최종 사용자 접근 / 404. `approval` 상세(차수별 진행 상태)는 [common.md](common.md) API-COM-004로 조회한다.

### API-KM-003 · 기사 작성

- **Endpoint**: `POST /api/v1/knowledge/articles`
- **인증**: 필요(Contributor)
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  { "title": "string · 필수", "body": "string · 필수", "categoryId": "number", "labels": ["string"] }
  ```
- **Response Body** (201): `{ "id": "number", "status": "DRAFT" }`
  > 기사 레코드는 이 API 호출로 즉시 생성·커밋된다(`TicketCreationGateSupport`가 REQUIRES_NEW로 먼저 커밋). 등록(DRAFT) 상태에 승인 게이트가 걸려 있으면 커밋 후 별도 트랜잭션에서 게이트를 평가해 409를 반환하지만 방금 커밋된 레코드는 롤백되지 않는다(다른 8개 도메인과 동일한 표준 throwing 모델 — KNOWLEDGE의 검토 요청(API-KM-006)이 쓰는 non-throwing 방식과는 다르다).
- **Response Code**: 201(매칭되는 승인 프로세스 없거나 0차 승인) / 400 제목·본문 누락 / 400 존재하지 않는 카테고리 / 403 / 409 등록(DRAFT) 상태에 승인 게이트가 걸려 승인 대기(`APPROVAL_PENDING`) — [common.md](common.md) 0절 생성 시점 게이트. 반려(`APPROVAL_REJECTED`) 시 재승인요청은 [common.md](common.md) API-COM-006

### API-KM-004 · 기사 수정

- **Endpoint**: `PATCH /api/v1/knowledge/articles/{id}`
- **인증**: 필요(Contributor)
- **Request Body**: API-KM-003 필드 부분 갱신
- **Response Code**: 200 / 400 / 403 / 404

### API-KM-005 · 기사 삭제

- **Endpoint**: `DELETE /api/v1/knowledge/articles/{id}`
- **인증**: 필요(Contributor)
- **Response Code**: 204 / 403 / 404

### API-KM-006 · 기사 상태 전이

- **Endpoint**: `PATCH /api/v1/knowledge/articles/{id}/status`
- **인증**: 필요
- **Request Body**: `{ "targetStatus": "IN_REVIEW · 검토 요청" }`
- **Response Body** (200): `{ "id": "number", "status": "IN_REVIEW|PUBLISHED", "approvalRequestId": "number|null" }`
- **Response Code**: 200 / 400 허용되지 않은 전이 / 403 / 404. [common.md](common.md) 0절 공통 게이트 로직(domain=KNOWLEDGE, **targetState="IN_REVIEW"**, 요청유형 스코프 없음) 적용 — 매칭되는 승인 프로세스가 없거나 0차 승인이면 **즉시 PUBLISHED로 전이**(승인 없이 게시, 결과 `status="PUBLISHED"`). 매칭 규칙이 있으면 승인 인스턴스를 생성하고 `status="IN_REVIEW"`(대기)로 저장한다. 인스턴스가 [common.md](common.md) API-COM-005로 APPROVED 확정되면 기사 상태를 PUBLISHED로, REJECTED 확정되면 DRAFT로 전환하고 반려 사유(결정 시 등록한 reason)를 기사에 표시한다(기존 API-KM-007 반려 동작 계승). 이 전이는 non-throwing `evaluateAndCreateIfNeeded`를 그대로 사용하므로(다른 8개 도메인의 표준 throwing `checkGate`와 다름) 이전 인스턴스가 REJECTED여도 이 API를 다시 호출하면(사용자가 "검토 요청"을 다시 누르면) 자동으로 새 인스턴스가 생성된다 — 이 자체가 재승인요청 역할을 하므로 이 전이에는 [common.md](common.md) API-COM-006(재승인요청)이 적용되지 않는다(생성 시점 게이트인 API-KM-003의 반려에만 적용).

### API-KM-009 · 유용성 평가/피드백

- **Endpoint**: `POST /api/v1/knowledge/articles/{id}/feedback`
- **인증**: 필요
- **Request Body**: `{ "helpful": "boolean", "comment": "string" }`
- **Response Body** (200): `{ "helpful": "number", "notHelpful": "number" }`
- **Response Code**: 200 / 400 미게시 기사 평가 / 403 / 404

### API-KM-010 · 카테고리 목록

- **Endpoint**: `GET /api/v1/knowledge/categories`
- **인증**: 필요
- **Response Body** (200): `[ { "id": "number", "name": "string" } ]`
- **Response Code**: 200 / 401

### API-KM-011 · KCS 티켓 연계(작성/연결)

- **Endpoint**: `POST /api/v1/knowledge/articles/link`
- **인증**: 필요
- **Request Body**:
  ```json
  { "ticketType": "SERVICE_REQUEST|INCIDENT|PROBLEM", "ticketId": "number", "articleId": "number · 기존 연결", "newArticle": { "title": "string", "body": "string" } }
  ```
- **Response Body** (200): `{ "articleId": "number", "ticketId": "number" }`
- **Response Code**: 200 / 400 존재하지 않는 티켓 / 403 / 404

### API-KM-012 · 지식 지표 조회

- **Endpoint**: `GET /api/v1/knowledge/metrics?from=&to=`
- **인증**: 필요
- **Response Body** (200):
  ```json
  { "usageCount": "number", "noResultSearchCount": "number", "helpfulRate": "number", "deflectionRate": "number", "topNoResultKeywords": ["string"] }
  ```
- **Response Code**: 200(데이터 없으면 빈 결과) / 401
