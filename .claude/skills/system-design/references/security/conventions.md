# Security 설계 컨벤션 (JWT 인증)

JWT 기반 인증 구조를 **직관적이고 간결한** markdown으로 설계한다.

> Backend Auth를 **Supabase Auth**로 대체하는 경우, 아래 자체 JWT 발급/JTI 세션 관리 대신 Supabase Auth의 세션·토큰 관리를 따른다. 이 문서에는 Supabase Auth 설정(Provider, 토큰 저장 위치, RLS 연동)을 명시한다. (`implementation` skill의 `references/database/supabase/conventions.md` 참고)

## 인증 구조 (고정 규칙)

- **JWT 기반 인증**으로 구성. **Access Token + Refresh Token**.
- **저장소 결정 근거**: Local/Session Storage는 XSS 노출 위험이 가장 커 **사용하지 않음**.
- **저장 위치**
  - Access Token → **Client Memory** (코드상 변수. 새로고침 시 소실 → Refresh Token으로 재발급)
    - 쿠키 미사용이므로 **CSRF 공격 대상 아님**.
    - XSS로 탈취되어도 유효기간(5분) 짧아 피해 시간 제한적.
  - Refresh Token → **httpOnly Cookie** (`Secure`, `SameSite` 속성 필수)
    - `HttpOnly`로 **XSS 통한 탈취 차단**.
    - Refresh 요청 빈도 낮아 CSRF 노출 표면 작음 → **CSRF 토큰 검증** 병행.
- **유효 기간**
  - Access Token → **5분**
  - Refresh Token → **7일**
- **Access Token claim**: `user id`, `jti`, `role`

## 클라이언트 보안 조치 (XSS/CSRF 방지)

- **DOMPurify**로 렌더링 대상 HTML sanitize.
- `href`/`src` 속성 URL 값 검증, `http`/`https`/`mailto` 프로토콜 아니면 `#`으로 치환.
- Refresh Token 쿠키: `HttpOnly`(XSS 방지) + `Secure`(스니핑 방지) + `SameSite`(CSRF 방지) 설정.
- Refresh Token 재발급 요청에 **CSRF 토큰 검증** 적용.

## JTI 세션 관리

- 사용자에게 **Redis 사용 여부 확인**해 사용자 ↔ Access Token JTI 매핑 정보 저장.
- **Redis 사용 시**: 로그인 시 `{ key: user id, value: access token jti }`로 저장.
- **DB 사용 시**: 로그인 시 사용자 테이블 `jti` 컬럼에 access token jti 저장.
- **로그아웃 시**: Redis 또는 DB 저장 jti 제거(또는 null 처리).

## 요청 검증 흐름

- FE → BE 향하는 **모든 요청마다 token 검증** 수행.
  1. 요청 token에서 `user id`와 `jti` 추출.
  2. 저장된 id–jti 매핑 정보와 비교.
  3. **다르면 로그아웃 처리**, **같으면 정상 API 수행**.

## 양식

표준 양식은 [template.md](template.md)를 사용한다.
