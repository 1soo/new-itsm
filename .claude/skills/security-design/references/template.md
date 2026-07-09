# 보안 설계 — JWT 인증

> 버전: 0.1 · 작성일: {YYYY-MM-DD} · JTI 저장소: {Redis / DB}

## 1. 개요

JWT 기반 인증(Access Token + Refresh Token) 구조.

## 2. 토큰 구성

| 항목 | Access Token | Refresh Token |
|------|--------------|----------------|
| 저장 위치 | Session Storage | httpOnly Cookie |
| 유효 기간 | 5분 | 7일 |
| Claim | user id, jti, role | {필요 시} |

## 3. JTI 세션 관리

저장소: **{Redis / DB}** (사용자 확인 결과)

- 로그인 시:
  - Redis: `{ key: user id, value: access token jti }`
  - DB: `user.jti` 컬럼에 access token jti 저장
- 로그아웃 시: 저장된 jti 제거(또는 null 처리)

## 4. 요청 검증 흐름

```
FE 요청 (Authorization: Bearer {accessToken})
  → BE: 토큰에서 user id, jti 추출
  → 저장된 id–jti 매핑과 비교
     ├─ 불일치 → 로그아웃 처리 (401)
     └─ 일치 → 정상 API 수행
```

## 5. 토큰 재발급

- Access Token 만료 시 Refresh Token(httpOnly Cookie)으로 재발급.
- Refresh Token 만료 시 재로그인.
