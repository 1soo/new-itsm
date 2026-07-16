# 보안 설계 — JWT 인증

> 버전: 0.1 · 작성일: {YYYY-MM-DD} · JTI 저장소: {Redis / DB}

## 변경 이력

| 날짜 | 요약 |
|------|------|
| {YYYY-MM-DD} | 최초 작성 |

## 1. 개요

JWT 기반 인증(Access Token + Refresh Token) 구조.

## 2. 토큰 저장소 결정 근거

| 후보 | 위험 | 채택 여부 |
|------|------|-----------|
| Local/Session Storage | XSS 노출 위험 최고 | 미채택 |
| Client Memory (변수) | XSS 위험은 있으나 CSRF 무관, 유효기간 짧아 피해 제한적 | Access Token 채택 |
| httpOnly Cookie | CSRF 위험은 있으나 SameSite/CSRF 토큰으로 방어 용이 | Refresh Token 채택 |

## 3. 토큰 구성

| 항목 | Access Token | Refresh Token |
|------|--------------|----------------|
| 저장 위치 | Client Memory (변수) | httpOnly Cookie (Secure, SameSite) |
| 유효 기간 | 5분 | 7일 |
| Claim | user id, jti, role | {필요 시} |
| 주요 위협 | XSS (탈취되어도 유효기간 짧아 피해 제한적) | CSRF (SameSite + CSRF 토큰으로 방어) |

## 4. 클라이언트 보안 조치

- DOMPurify로 렌더링 대상 HTML을 sanitize한다.
- `href`/`src` 속성 URL 값 검증: `http`/`https`/`mailto` 프로토콜이 아니면 `#`으로 치환한다.
- Refresh Token 쿠키: `HttpOnly` + `Secure` + `SameSite` 설정.
- Refresh Token 재발급 요청: CSRF 토큰 검증.

## 5. JTI 세션 관리

저장소: **{Redis / DB}** (사용자 확인 결과)

- 로그인 시:
  - Redis: `{ key: user id, value: access token jti }`
  - DB: `user.jti` 컬럼에 access token jti 저장
- 로그아웃 시: 저장된 jti 제거(또는 null 처리)

## 6. 요청 검증 흐름

```
FE 요청 (Authorization: Bearer {accessToken})
  → BE: 토큰에서 user id, jti 추출
  → 저장된 id–jti 매핑과 비교
     ├─ 불일치 → 로그아웃 처리 (401)
     └─ 일치 → 정상 API 수행
```

## 7. 토큰 재발급

- Access Token 만료 시 Refresh Token(httpOnly Cookie)으로 재발급.
- Refresh Token 만료 시 재로그인.
