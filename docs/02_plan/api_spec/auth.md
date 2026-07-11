# API 명세서 — 인증/계정/권한 (Auth & RBAC)

> 도메인: auth · 버전: 0.2 · 작성일: 2026-07-11 · Role-Menu 동적 매핑(유지보수 요청) API 추가(API-AUTH-016~022, 기존 screen/screen_role 재사용)

## 공통 규약

- **Base Path**: `/api/v1`
- **인증 헤더**: 보호 API는 `Authorization: Bearer {accessToken}` 필요.
- **표준 오류 응답 본문** (모든 4xx/5xx 공통):
  ```json
  { "code": "string · 오류 코드", "message": "string · 사용자 메시지", "timestamp": "ISO-8601" }
  ```
- **인가 실패**: 미인증 401, 권한 부족 403.

## 1. API 목록

| API ID | 기능 | Method | Endpoint | 인증 |
|--------|------|--------|----------|------|
| API-AUTH-001 | 로그인 | POST | /api/v1/auth/login | 불필요 |
| API-AUTH-002 | 토큰 재발급 | POST | /api/v1/auth/refresh | 불필요(Refresh Token) |
| API-AUTH-003 | 로그아웃 | POST | /api/v1/auth/logout | 필요 |
| API-AUTH-004 | 내 정보 조회 | GET | /api/v1/auth/me | 필요 |
| API-AUTH-005 | 비밀번호 변경 | PATCH | /api/v1/auth/me/password | 필요 |
| API-AUTH-006 | 계정 목록 조회 | GET | /api/v1/admin/users | 필요(Admin) |
| API-AUTH-007 | 계정 생성 | POST | /api/v1/admin/users | 필요(Admin) |
| API-AUTH-008 | 계정 상세 조회 | GET | /api/v1/admin/users/{userId} | 필요(Admin) |
| API-AUTH-009 | 계정 수정 | PATCH | /api/v1/admin/users/{userId} | 필요(Admin) |
| API-AUTH-010 | 계정 상태 변경(비활성화/활성화) | PATCH | /api/v1/admin/users/{userId}/status | 필요(Admin) |
| API-AUTH-011 | 사용자 역할 부여 | POST | /api/v1/admin/users/{userId}/roles | 필요(Admin) |
| API-AUTH-012 | 사용자 역할 회수 | DELETE | /api/v1/admin/users/{userId}/roles/{roleId} | 필요(Admin) |
| API-AUTH-013 | 역할 목록 조회 | GET | /api/v1/admin/roles | 필요(Admin) |
| API-AUTH-014 | 역할 생성 | POST | /api/v1/admin/roles | 필요(Admin) |
| API-AUTH-015 | 감사 로그 조회 | GET | /api/v1/admin/audit-logs | 필요(Admin) |
| API-AUTH-016 | 메뉴(화면) 목록 조회 | GET | /api/v1/admin/screens | 필요(Admin) |
| API-AUTH-017 | 메뉴 생성 | POST | /api/v1/admin/screens | 필요(Admin) |
| API-AUTH-018 | 메뉴 수정 | PATCH | /api/v1/admin/screens/{screenId} | 필요(Admin) |
| API-AUTH-019 | 메뉴 삭제 | DELETE | /api/v1/admin/screens/{screenId} | 필요(Admin) |
| API-AUTH-020 | 메뉴에 역할 매핑 부여 | POST | /api/v1/admin/screens/{screenId}/roles | 필요(Admin) |
| API-AUTH-021 | 메뉴 역할 매핑 회수 | DELETE | /api/v1/admin/screens/{screenId}/roles/{roleId} | 필요(Admin) |
| API-AUTH-022 | 내 메뉴 조회 | GET | /api/v1/menus/mine | 필요 |

## 2. API 상세

### API-AUTH-001 · 로그인

- **Endpoint**: `POST /api/v1/auth/login`
- **인증**: 불필요
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  { "email": "string · 이메일", "password": "string · 비밀번호" }
  ```
- **Response Body** (200):
  ```json
  {
    "accessToken": "string · JWT",
    "refreshToken": "string · JWT",
    "tokenType": "Bearer",
    "expiresIn": "number · Access 만료(초)",
    "user": { "id": "number", "email": "string", "name": "string", "roles": ["string"] }
  }
  ```
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 로그인 성공·토큰 발급 |
  | 400 | 입력 형식 오류 |
  | 401 | 이메일/비밀번호 불일치 |
  | 403 | 비활성화 계정 |

### API-AUTH-002 · 토큰 재발급

- **Endpoint**: `POST /api/v1/auth/refresh`
- **인증**: 불필요(Body 또는 HttpOnly Cookie의 Refresh Token 검증)
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  { "refreshToken": "string · Refresh Token" }
  ```
- **Response Body** (200):
  ```json
  { "accessToken": "string · 새 JWT", "tokenType": "Bearer", "expiresIn": "number" }
  ```
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 재발급 성공 |
  | 401 | Refresh Token 만료·무효·무효화됨 → 재로그인 필요 |

### API-AUTH-003 · 로그아웃

- **Endpoint**: `POST /api/v1/auth/logout`
- **인증**: 필요(Access Token)
- **Header**: `Authorization: Bearer {accessToken}`, `Content-Type: application/json`
- **Request Body**:
  ```json
  { "refreshToken": "string · 무효화 대상(선택, 세션 식별)" }
  ```
- **Response Body** (200): `{ "message": "로그아웃 완료" }`
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 세션 무효화 완료 |
  | 401 | 미인증 |

### API-AUTH-004 · 내 정보 조회

- **Endpoint**: `GET /api/v1/auth/me`
- **인증**: 필요(Access Token)
- **Header**: `Authorization: Bearer {accessToken}`
- **Request Body**: 없음
- **Response Body** (200):
  ```json
  { "id": "number", "email": "string", "name": "string", "status": "ACTIVE|INACTIVE", "roles": ["string"] }
  ```
- **Response Code**: 200 정상 / 401 미인증

### API-AUTH-005 · 비밀번호 변경

- **Endpoint**: `PATCH /api/v1/auth/me/password`
- **인증**: 필요(Access Token)
- **Header**: `Authorization: Bearer {accessToken}`, `Content-Type: application/json`
- **Request Body**:
  ```json
  { "currentPassword": "string", "newPassword": "string · 정책 검증 대상" }
  ```
- **Response Body** (200): `{ "message": "비밀번호가 변경되었습니다" }`
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 변경 성공 |
  | 400 | 새 비밀번호 정책 위반 |
  | 401 | 현재 비밀번호 불일치/미인증 |

### API-AUTH-006 · 계정 목록 조회

- **Endpoint**: `GET /api/v1/admin/users?email=&name=&status=&role=&page=&size=`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`
- **Request Body**: 없음(쿼리 파라미터)
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "email": "string", "name": "string", "status": "string", "roles": ["string"], "createdAt": "ISO-8601" } ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 정상 / 401 미인증 / 403 권한 부족

### API-AUTH-007 · 계정 생성

- **Endpoint**: `POST /api/v1/admin/users`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`, `Content-Type: application/json`
- **Request Body**:
  ```json
  { "email": "string · 필수", "name": "string · 필수", "roleIds": ["number · 초기 역할, 1개 이상"], "initialPassword": "string · 필수, 비밀번호 정책 검증 대상" }
  ```
  > `initialPassword`는 관리자가 생성 시 지정하는 계정별 초기 비밀번호로 **단방향 해시(BCrypt)로만 저장**한다(FEAT-AUTH-001 "초기 비밀번호는 해시 저장"). 사용자는 최초 로그인 후 SCR-AUTH-003(비밀번호 변경)에서 스스로 변경한다. 전 계정 공용 고정 비밀번호는 사용하지 않는다(계정별 고유 부여). 초대메일/재설정 링크 흐름은 이번 범위 제외(외부 연동 미포함).
- **Response Body** (201):
  ```json
  { "id": "number", "email": "string", "name": "string", "status": "ACTIVE", "roles": ["string"] }
  ```
  > 응답 본문에 비밀번호·해시를 포함하지 않는다.
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 201 | 생성 성공 |
  | 400 | 필수 누락·형식 오류·비밀번호 정책 위반 |
  | 403 | 권한 부족 |
  | 409 | 이메일 중복 |

### API-AUTH-008 · 계정 상세 조회

- **Endpoint**: `GET /api/v1/admin/users/{userId}`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`
- **Response Body** (200): API-AUTH-007 응답과 동일 구조 + `createdAt`, `updatedAt`
- **Response Code**: 200 / 401 / 403 / 404 계정 없음

### API-AUTH-009 · 계정 수정

- **Endpoint**: `PATCH /api/v1/admin/users/{userId}`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`, `Content-Type: application/json`
- **Request Body**:
  ```json
  { "name": "string · 선택" }
  ```
- **Response Body** (200): 갱신된 계정 정보
- **Response Code**: 200 / 400 / 403 / 404

### API-AUTH-010 · 계정 상태 변경

- **Endpoint**: `PATCH /api/v1/admin/users/{userId}/status`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`, `Content-Type: application/json`
- **Request Body**:
  ```json
  { "status": "ACTIVE|INACTIVE" }
  ```
- **Response Body** (200): `{ "id": "number", "status": "string" }`
- **Response Code**: 200 / 400 / 403 / 404. INACTIVE 시 해당 계정 로그인 차단.

### API-AUTH-011 · 사용자 역할 부여

- **Endpoint**: `POST /api/v1/admin/users/{userId}/roles`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`, `Content-Type: application/json`
- **Request Body**:
  ```json
  { "roleId": "number · 부여할 역할" }
  ```
- **Response Body** (200): `{ "userId": "number", "roles": ["string"] }`
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 부여 성공(즉시 반영) |
  | 400 | 존재하지 않는 역할 |
  | 403 | 권한 부족 |
  | 404 | 계정 없음 |

### API-AUTH-012 · 사용자 역할 회수

- **Endpoint**: `DELETE /api/v1/admin/users/{userId}/roles/{roleId}`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`
- **Response Body** (200): `{ "userId": "number", "roles": ["string"] }`
- **Response Code**: 200 / 403 / 404

### API-AUTH-013 · 역할 목록 조회

- **Endpoint**: `GET /api/v1/admin/roles`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`
- **Response Body** (200):
  ```json
  [ { "id": "number", "roleCode": "string · 역할 코드(SYSTEM_ADMIN 등, DB role.role_code)", "name": "string · 표시명(시스템 관리자)", "description": "string", "userCount": "number" } ]
  ```
  > `roleCode`는 role claim·`user.roles`·계정목록 role 필터(API-AUTH-006 `?role=`)와 동일한 코드 체계다. FE는 `roleCode`로 `user.roles`(코드 배열)와 매핑하고, `name`은 화면 표시에 사용한다.
- **Response Code**: 200 / 401 / 403

### API-AUTH-014 · 역할 생성

- **Endpoint**: `POST /api/v1/admin/roles`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`, `Content-Type: application/json`
- **Request Body**:
  ```json
  { "roleCode": "string · 필수, 대문자 스네이크(예: ASSET_MANAGER)", "name": "string · 필수, 표시명", "description": "string" }
  ```
- **Response Body** (201): `{ "id": "number", "roleCode": "string", "name": "string", "description": "string" }`
- **Response Code**: 201 / 400 / 403 / 409 역할 코드 또는 표시명 중복 (`role.role_code` UNIQUE)

### API-AUTH-015 · 감사 로그 조회

- **Endpoint**: `GET /api/v1/admin/audit-logs?eventType=&actor=&target=&from=&to=&page=&size=`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`
- **Response Body** (200):
  ```json
  {
    "content": [ { "id": "number", "eventType": "LOGIN|LOGOUT|REFRESH|USER_CHANGE|ROLE_CHANGE", "actor": "string", "target": "string", "result": "SUCCESS|FAILURE", "occurredAt": "ISO-8601" } ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200(데이터 없으면 빈 배열) / 401 / 403

### API-AUTH-016 · 메뉴(화면) 목록 조회

Role-Menu 동적 매핑(SCR-ADMIN-006) 화면의 메뉴 목록. `screen` 테이블 전체(화면 마스터 겸 메뉴 마스터)를 조회하며, 각 항목에 현재 매핑된 역할 목록을 함께 반환한다.

- **Endpoint**: `GET /api/v1/admin/screens?groupCode=&domain=&page=&size=`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`
- **Request Body**: 없음(쿼리 파라미터)
- **Response Body** (200):
  ```json
  {
    "content": [
      {
        "id": "number", "screenCode": "string", "screenName": "string", "path": "string", "domain": "string",
        "iconName": "string|null", "groupCode": "string|null", "groupLabel": "string|null",
        "sortOrder": "number", "navVisible": "boolean",
        "roles": ["string · role.role_code, 매핑된 역할(없으면 전체 인증 사용자 공개)"]
      }
    ],
    "page": "number", "size": "number", "totalElements": "number"
  }
  ```
- **Response Code**: 200 정상 / 401 미인증 / 403 권한 부족

### API-AUTH-017 · 메뉴 생성

- **Endpoint**: `POST /api/v1/admin/screens`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`, `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "screenCode": "string · 필수, 고유 식별 코드", "screenName": "string · 필수", "path": "string · 필수", "domain": "string · 필수",
    "iconName": "string · 선택(lucide-react 컴포넌트명)", "groupCode": "string · 선택", "groupLabel": "string · 선택(groupCode와 함께 지정)",
    "sortOrder": "number · 선택, 기본 0", "navVisible": "boolean · 선택, 기본 true"
  }
  ```
- **Response Body** (201): API-AUTH-016 목록 항목과 동일 구조(`roles: []`)
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 201 | 생성 성공 |
  | 400 | 필수 누락·형식 오류 |
  | 403 | 권한 부족 |
  | 409 | screenCode 중복(`screen.screen_code` UNIQUE) 또는 path 중복(`screen.path` UNIQUE) |

### API-AUTH-018 · 메뉴 수정

- **Endpoint**: `PATCH /api/v1/admin/screens/{screenId}`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`, `Content-Type: application/json`
- **Request Body**:
  ```json
  { "screenName": "string · 선택", "path": "string · 선택", "iconName": "string · 선택", "groupCode": "string · 선택", "groupLabel": "string · 선택", "sortOrder": "number · 선택", "navVisible": "boolean · 선택" }
  ```
  > `screenCode`·`domain`은 식별자로 취급해 수정 대상에서 제외한다.
- **Response Body** (200): 갱신된 메뉴 정보(API-AUTH-016 항목 구조)
- **Response Code**: 200 / 400 / 403 / 404 메뉴 없음 / 409 path 중복(`screen.path` UNIQUE, 다른 메뉴가 이미 사용 중인 경로로 변경 시)

### API-AUTH-019 · 메뉴 삭제

- **Endpoint**: `DELETE /api/v1/admin/screens/{screenId}`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`
- **Response Body** (200): `{ "id": "number", "deleted": true }`
- **Response Code**: 200 / 403 / 404
  > soft delete(`screen.is_deleted=true`) 처리. 연결된 `screen_role` 매핑은 별도로 삭제하지 않으며, 이후 메뉴·화면 접근 조회 시 `is_deleted=false` 조건으로 자동 제외된다.

### API-AUTH-020 · 메뉴에 역할 매핑 부여

- **Endpoint**: `POST /api/v1/admin/screens/{screenId}/roles`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`, `Content-Type: application/json`
- **Request Body**:
  ```json
  { "roleId": "number · 부여할 역할" }
  ```
- **Response Body** (200): `{ "screenId": "number", "roles": ["string"] }`
- **Response Code**:
  | Code | 의미 |
  |------|------|
  | 200 | 부여 성공(즉시 반영, 사이드바에서도 즉시 노출) |
  | 400 | 존재하지 않는 역할 |
  | 403 | 권한 부족 |
  | 404 | 메뉴 없음 |
  | 409 | 이미 매핑됨(`screen_role` UNIQUE(screen_id, role_id)) |

### API-AUTH-021 · 메뉴 역할 매핑 회수

- **Endpoint**: `DELETE /api/v1/admin/screens/{screenId}/roles/{roleId}`
- **인증**: 필요(Access Token, System Admin)
- **Header**: `Authorization: Bearer {accessToken}`
- **Response Body** (200): `{ "screenId": "number", "roles": ["string"] }`
- **Response Code**: 200 / 403 / 404

### API-AUTH-022 · 내 메뉴 조회

로그인 사용자의 역할에 매핑된 사이드바 메뉴 목록(SCR-COM-003). `screen_role`에 매핑이 전혀 없는 화면(예: 대시보드·내 프로필)은 전체 인증 사용자에게 공개된 것으로 간주해 항상 포함한다.

- **Endpoint**: `GET /api/v1/menus/mine`
- **인증**: 필요(Access Token)
- **Header**: `Authorization: Bearer {accessToken}`
- **Request Body**: 없음
- **Response Body** (200):
  ```json
  {
    "groups": [
      {
        "groupCode": "string|null", "groupLabel": "string|null",
        "items": [ { "screenCode": "string", "screenName": "string", "path": "string", "iconName": "string|null" } ]
      }
    ]
  }
  ```
  > `nav_visible=true`인 화면만 포함하며, `sort_order` 오름차순으로 정렬한다. 그룹 표시 순서는 그룹별 최소 `sort_order` 값 기준으로 정렬한다(`docs/02_plan/database/auth.md` 5절).
- **Response Code**: 200(그룹 없으면 빈 배열) / 401 미인증
