---
name: security-design
description: JWT 기반 인증(Access/Refresh Token) 보안 설계 구조를 직관적이고 간결한 markdown으로 작성하는 방법과 표준 양식. 토큰 저장(Access Token→Client Memory, Refresh Token→httpOnly Cookie)·만료·claim·JTI 세션 관리(Redis/DB)·요청 검증 흐름·XSS/CSRF 방지(DOMPurify, URL 검증, Cookie 속성)를 포함한 Security 설계가 필요할 때 사용한다.
---

# Security 설계 (보안 설계 구조)

JWT 기반 인증 구조를 **직관적이고 간결한** markdown으로 설계한다.

## 인증 구조 (고정 규칙)

- **JWT 기반 인증**으로 구성한다. **Access Token + Refresh Token**.
- **저장소 결정 근거**: Local/Session Storage는 XSS 노출 위험이 가장 커서 **사용하지 않는다**.
- **저장 위치**
  - Access Token → **Client Memory** (코드상 변수. 새로고침 시 소실 → Refresh Token으로 재발급)
    - 쿠키를 사용하지 않으므로 **CSRF 공격 대상이 아니다**.
    - XSS로 탈취되어도 유효기간(5분)이 짧아 피해 시간이 제한적이다.
  - Refresh Token → **httpOnly Cookie** (`Secure`, `SameSite` 속성 필수)
    - `HttpOnly`로 **XSS를 통한 탈취를 차단**한다.
    - Refresh 요청 빈도가 낮아 CSRF 노출 표면이 작다 → **CSRF 토큰 검증**을 병행한다.
- **유효 기간**
  - Access Token → **5분**
  - Refresh Token → **7일**
- **Access Token claim**: `user id`, `jti`, `role`

## 클라이언트 보안 조치 (XSS/CSRF 방지)

- **DOMPurify**로 렌더링 대상 HTML을 sanitize한다.
- `href`/`src` 속성에 들어가는 URL 값을 검증하여, `http`/`https`/`mailto` 프로토콜이 아니면 `#`으로 치환한다.
- Refresh Token 쿠키: `HttpOnly`(XSS 방지) + `Secure`(스니핑 방지) + `SameSite`(CSRF 방지) 설정.
- Refresh Token 재발급 요청에는 **CSRF 토큰 검증**을 적용한다.

## JTI 세션 관리

- 사용자에게 **Redis 사용 여부를 확인**하여 사용자 ↔ Access Token JTI 매핑 정보를 저장한다.
- **Redis 사용 시**: 로그인 시 `{ key: user id, value: access token jti }`로 저장.
- **DB 사용 시**: 로그인 시 사용자 테이블의 `jti` 컬럼에 access token jti 저장.
- **로그아웃 시**: Redis 또는 DB에 저장된 jti를 제거(또는 null 처리).

## 요청 검증 흐름

- FE → BE로 향하는 **모든 요청마다 token 검증**을 수행한다.
  1. 요청 token에서 `user id`와 `jti`를 추출한다.
  2. 저장된 id–jti 매핑 정보와 비교한다.
  3. **다르면 로그아웃 처리**, **같으면 정상 API 수행**.

## 산출물 저장 위치

- `docs/02_plan/security/authentication.md`

## 양식

표준 양식은 [references/template.md](references/template.md)를 사용한다.
