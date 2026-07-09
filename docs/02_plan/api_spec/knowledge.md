# API 명세서 — 지식 관리 (Knowledge)

> 도메인: knowledge · 버전: 0.1 · 작성일: 2026-07-09

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
| API-KM-007 | 검토·게시 승인/반려 | POST | /api/v1/knowledge/articles/{id}/review | 필요(Gatekeeper) |
| API-KM-008 | 검토 대기 목록 | GET | /api/v1/knowledge/reviews | 필요(Gatekeeper) |
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
    "content": [ { "id": "number", "title": "string", "summary": "string", "status": "DRAFT|IN_REVIEW|PUBLISHED", "category": "string", "helpfulRate": "number" } ],
    "page": "number", "size": "number", "totalElements": "number", "noResult": "boolean · 무결과 검색 기록 여부"
  }
  ```
- **Response Code**: 200(매칭 없으면 빈 결과·무결과 기록) / 401

### API-KM-002 · 기사 상세/열람

- **Endpoint**: `GET /api/v1/knowledge/articles/{id}`
- **인증**: 필요
- **Response Body** (200):
  ```json
  { "id": "number", "title": "string", "body": "string", "status": "string", "category": "string", "labels": ["string"], "helpful": "number", "notHelpful": "number" }
  ```
- **Response Code**: 200 / 401 / 403 미게시 기사에 최종 사용자 접근 / 404

### API-KM-003 · 기사 작성

- **Endpoint**: `POST /api/v1/knowledge/articles`
- **인증**: 필요(Contributor)
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  { "title": "string · 필수", "body": "string · 필수", "categoryId": "number", "labels": ["string"] }
  ```
- **Response Body** (201): `{ "id": "number", "status": "DRAFT" }`
- **Response Code**: 201 / 400 제목·본문 누락 / 400 존재하지 않는 카테고리 / 403

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
- **Response Code**: 200 / 400 허용되지 않은 전이 / 403 / 404

### API-KM-007 · 검토·게시 승인/반려

- **Endpoint**: `POST /api/v1/knowledge/articles/{id}/review`
- **인증**: 필요(Gatekeeper)
- **Request Body**: `{ "decision": "APPROVE|REJECT", "reason": "string · 반려 시 필수" }`
- **Response Body** (200): `{ "id": "number", "status": "PUBLISHED|DRAFT" }`
- **Response Code**: 200 / 400 반려 사유 누락 / 403 게이트키퍼 권한 없음 / 404. 반려 시 초안 복귀·사유 표시.

### API-KM-008 · 검토 대기 목록

- **Endpoint**: `GET /api/v1/knowledge/reviews?scope=mine`
- **인증**: 필요(Gatekeeper)
- **Response Body** (200):
  ```json
  [ { "articleId": "number", "title": "string", "author": "string", "requestedAt": "ISO-8601" } ]
  ```
- **Response Code**: 200 / 401 / 403

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
