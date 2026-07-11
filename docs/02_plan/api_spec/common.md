# API 명세서 — 공통 (Common)

> 도메인: common · 버전: 0.1 · 작성일: 2026-07-11 · 알림 확인처리(모두 지우기·개별 X, 유지보수 요청) 최초 작성

## 공통 규약

- **Base Path**: `/api/v1`
- **인증 헤더**: 보호 API는 `Authorization: Bearer {accessToken}` 필요.
- **표준 오류 응답 본문** (모든 4xx/5xx 공통):
  ```json
  { "code": "string · 오류 코드", "message": "string · 사용자 메시지", "timestamp": "ISO-8601" }
  ```
- **인가 실패**: 미인증 401.

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-COM-001 | 알림 확인처리(개별/일괄) | POST | /api/v1/notifications/dismissals | 필요 |
| API-COM-002 | 확인처리된 알림 목록 조회 | GET | /api/v1/notifications/dismissals | 필요 |

## 2. API 상세

### API-COM-001 · 알림 확인처리(개별/일괄)

헤더 알림 드롭다운(SCR-COM-002)의 "모두 지우기"(items에 현재 표시된 상위 8건 전체)와 개별 X 버튼(items에 1건)이 공용으로 사용한다. 확인처리는 표시 여부에만 영향을 주며, 원본 승인 대기(API-SRM-012/API-CHG-007)·자산 만료(API-ITAM-001) 데이터는 변경하지 않는다.

- **Endpoint**: `POST /api/v1/notifications/dismissals`
- **인증**: 필요(Access Token)
- **Header**:
  | 이름 | 필수 | 설명 |
  |------|------|------|
  | Authorization | Y | `Bearer {accessToken}` |
  | Content-Type | Y | application/json |
- **Request Body**:
  ```json
  {
    "items": [
      {
        "notificationType": "string · SERVICE_REQUEST_APPROVAL|CHANGE_APPROVAL|ASSET_EXPIRY",
        "sourceId": "number · 승인 대기(requestId/changeId) 또는 자산 id"
      }
    ]
  }
  ```
  > `items`는 1개 이상. 로그인 사용자(토큰의 userId) 본인 기준으로만 저장하며, Request Body에 userId를 받지 않는다. 이미 확인처리된 항목이 다시 포함돼도 중복 저장하지 않고 그대로 무시한다(멱등).
- **Response Body** (200):
  ```json
  { "dismissedCount": "number · 이번 요청으로 신규 확인처리된 건수" }
  ```
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 확인처리 완료(멱등 — 이미 처리된 항목 포함해도 오류 아님) |
  | 400 | items 누락·형식 오류 |
  | 401 | 미인증 |

### API-COM-002 · 확인처리된 알림 목록 조회

FE가 3개 도메인 API(승인 대기 2종·자산 만료)로 알림 후보를 조합한 뒤, 이 조회 결과의 (notificationType, sourceId)와 매칭되는 항목을 제외하고 상위 8건을 구성하는 데 사용한다.

- **Endpoint**: `GET /api/v1/notifications/dismissals`
- **인증**: 필요(Access Token)
- **Header**:
  | 이름 | 필수 | 설명 |
  |------|------|------|
  | Authorization | Y | `Bearer {accessToken}` |
- **Request Body**: 없음(로그인 사용자 본인 전체 확인처리 이력 조회)
- **Response Body** (200):
  ```json
  {
    "items": [
      { "notificationType": "string", "sourceId": "number", "dismissedAt": "ISO-8601" }
    ]
  }
  ```
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 정상(이력 없으면 빈 배열) |
  | 401 | 미인증 |
