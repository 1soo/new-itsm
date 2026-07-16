# 보안 설계 — JWT 인증

> 버전: 0.2 · 작성일: 2026-07-12 · JTI 저장소: DB (PostgreSQL) · 유지보수: 토큰 저장 방식 Client Memory 전환, CSRF 검증, XSS 정책 추가

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |

## 1. 개요

ITSM 플랫폼의 인증은 JWT 기반(Access Token + Refresh Token)으로 구성한다. 모든 보호 API는 Access Token을 검증하고, 서버측 JTI 세션 매핑으로 강제 로그아웃·토큰 무효화를 지원한다.

> **JTI 저장소 결정 근거**: `tech.md`의 확정 스택에 Redis가 없고(PostgreSQL·Docker만 명시), 외부 연동은 이번 범위에서 제외되므로 **DB(PostgreSQL) 기반 JTI 세션 관리**를 채택한다. Redis 도입이 확정되면 5절 매핑만 교체하면 된다.

## 2. 토큰 저장소 결정 근거

| 후보 | 위험 | 채택 여부 |
|------|------|-----------|
| Local/Session Storage | XSS 노출 위험 최고 | 미채택 |
| Client Memory (코드상 변수) | XSS 위험은 있으나 CSRF 무관, 유효기간 짧아(5분) 피해 제한적 | Access Token 채택 |
| httpOnly Cookie | CSRF 위험은 있으나 SameSite·CSRF 토큰으로 방어 용이 | Refresh Token 채택 |

## 3. 토큰 구성

| 항목 | Access Token | Refresh Token |
|------|--------------|----------------|
| 저장 위치 (Client) | Client Memory (코드상 변수) | httpOnly Cookie (`Secure`, `SameSite`) |
| 유효 기간 | 5분 | 7일 |
| Claim | `userId`, `jti`, `role` | `userId`, `jti` |
| 전송 방식 | `Authorization: Bearer {accessToken}` | Cookie 자동 전송(재발급 요청 시) |
| 주요 위협 | XSS(탈취되어도 유효기간 짧아 피해 제한적, 쿠키 미사용으로 CSRF 무관) | CSRF(`SameSite` + CSRF 토큰 검증으로 방어) |

- Access Token은 짧은 만료(5분)로 탈취 위험을 최소화하고, 새로고침 시 소실되면 Refresh Token으로 재발급한다.
- `role` claim은 다중 역할을 배열로 포함하여 RBAC 인가 판정에 사용한다.
- Refresh Token은 httpOnly Cookie로 저장하여 XSS로부터의 탈취를 방지한다.

## 4. 클라이언트 보안 조치 (XSS/CSRF 방지)

- Refresh Token 쿠키: `HttpOnly`(XSS 방지) + `Secure`(스니핑 방지) + `SameSite=Strict`(CSRF 방지) 속성을 설정한다.
- `POST /api/v1/auth/refresh` 요청에는 **CSRF 토큰 검증(더블서밋 쿠키 방식)**을 적용한다.
  - 로그인(API-AUTH-001) 성공 시 서버는 Refresh Token(httpOnly) 쿠키와 별도로, **읽기 가능한 CSRF 토큰 쿠키**(`XSRF-TOKEN`, `Secure`, `SameSite=Strict`, `HttpOnly` 아님)를 함께 발급한다.
  - FE(`apiClient.ts`)는 `/auth/refresh` 요청 시 `XSRF-TOKEN` 쿠키 값을 읽어 `X-CSRF-Token` 요청 헤더에 담아 전송한다.
  - BE(`AuthController.java`)는 요청 헤더의 `X-CSRF-Token` 값과 쿠키의 `XSRF-TOKEN` 값을 비교하여, 불일치하거나 누락되면 **403**을 반환한다.
  - 로그아웃(API-AUTH-003) 시 Refresh Token 쿠키와 함께 `XSRF-TOKEN` 쿠키도 만료 처리한다.
- **XSS 방지 정책**(현재는 사용자 입력을 HTML/링크로 렌더링하는 기능이 없어 코드 구현은 보류): 향후 사용자 입력을 HTML/링크로 렌더링하는 기능이 추가되면, **DOMPurify로 sanitize**하고 `href`/`src` 속성 URL 값을 검증하여 `http`/`https`/`mailto` 프로토콜이 아니면 `#`으로 치환하는 조치를 반드시 적용한다.

## 5. JTI 세션 관리

저장소: **DB (PostgreSQL)**

- **로그인 시**:
  - Access Token JTI → `app_user.access_token_jti` 컬럼에 저장(`{ userId → accessTokenJti }` 매핑).
  - Refresh Token은 `refresh_token` 테이블에 `jti`, `user_id`, `expires_at`, `revoked=false`로 저장.
- **로그아웃 시**:
  - `app_user.access_token_jti`를 NULL 처리.
  - 해당 세션의 `refresh_token.revoked`를 true로 갱신.
- **재발급 시**: 새 Access Token JTI로 `app_user.access_token_jti`를 갱신.

> 관련 테이블: [database/auth.md](../database/auth.md)의 `app_user.access_token_jti`, `refresh_token`.

## 6. 요청 검증 흐름

FE → BE로 향하는 **모든 보호 API 요청마다 토큰 검증**을 수행한다.

```
FE 요청 (Authorization: Bearer {accessToken})
  → BE: 서명·만료 검증
  → 토큰에서 userId, jti 추출
  → app_user.access_token_jti 조회 후 비교
     ├─ 토큰 없음/서명·만료 무효 → 401 (미인증)
     ├─ jti 불일치(다른 기기 로그인·강제 로그아웃) → 로그아웃 처리 (401)
     └─ jti 일치 → 역할(role) claim으로 인가 판정
            ├─ 권한 부족 → 403
            └─ 권한 충족 → 정상 API 수행
```

- **401 (인증 실패)**: 토큰 없음·서명 오류·만료·jti 불일치. 클라이언트는 Refresh 1회 시도 후 실패 시 재로그인.
- **403 (인가 실패)**: 인증은 되었으나 리소스 요구 역할 미보유. RBAC 매핑은 [security/authorization/](authorization/) 참조.

## 7. 토큰 재발급

- Access Token 만료(401) 시 클라이언트는 `POST /api/v1/auth/refresh`로 재발급 요청(Refresh Token은 httpOnly Cookie로 자동 전송, `X-CSRF-Token` 헤더에 `XSRF-TOKEN` 쿠키 값을 담아 함께 전송).
- 서버는 다음 순서로 검증한다:
  1. **CSRF 토큰 검증**: 요청 헤더 `X-CSRF-Token` 값과 쿠키 `XSRF-TOKEN` 값을 비교. 불일치·누락 → **403**.
  2. **Refresh Token 검증**: 서명·만료·`refresh_token.revoked` 여부 확인.
     - 유효 → 새 Access Token 발급, `app_user.access_token_jti` 갱신.
     - 만료·무효·무효화됨 → **401**, 재로그인 요구.

> CSRF 토큰 발급·검증 상세는 4절 참조. API 스펙은 [api_spec/auth.md](../api_spec/auth.md)의 API-AUTH-002 참조.

## 8. 비밀번호 저장

- 비밀번호는 **단방향 해시(BCrypt)**로만 저장하며 평문 저장을 금지한다(`app_user.password_hash`).
- 변경 시 현재 비밀번호 해시 비교로 검증 후 새 해시로 교체.

> 개인정보 양방향 암호화·비밀번호 단방향 해시의 구현 규칙은 `database-development` 표준을 따른다.
