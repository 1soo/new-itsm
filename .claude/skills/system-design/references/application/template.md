# API 명세서 — {도메인명}

> 도메인: {domain} · 버전: 0.1 · 작성일: {YYYY-MM-DD}

## 변경 이력

| 날짜 | 요약 |
|------|------|
| {YYYY-MM-DD} | 최초 작성 |

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-{약어}-001 | {기능} | GET/POST/... | /api/... | 필요 / 불필요 |

## 2. API 상세

### API-{약어}-001 · {기능명}

- **Endpoint**: `{METHOD} /api/...`
- **인증**: 필요(Access Token) / 불필요
- **Header**:
  | 이름 | 필수 | 설명 |
  |------|------|------|
  | Authorization | Y/N | `Bearer {accessToken}` |
  | Content-Type | Y | application/json |
- **Request Body**:
  ```json
  {
    "field": "타입 · 설명"
  }
  ```
- **Response Body**:
  ```json
  {
    "field": "타입 · 설명"
  }
  ```
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 정상 |
  | 400 | 잘못된 요청 |
  | 401 | 인증 실패 |
  | 403 | 권한 부족 |
  | 404 | 리소스 없음 |
