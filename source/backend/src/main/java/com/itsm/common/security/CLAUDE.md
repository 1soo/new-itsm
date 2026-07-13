# CLAUDE.md

JWT 기반 인증·인가 공통 컴포넌트. 토큰 발급·검증, 인증 필터, 예외 진입점 제공.

## 파일
- `JwtTokenProvider.java` — Access/Refresh Token 생성·파싱·검증(jjwt)
- `JwtProperties.java` — JWT 설정값(secret·만료시간) 바인딩 record
- `JwtAuthenticationFilter.java` — 요청 헤더 토큰 검증·SecurityContext 주입 필터(OncePerRequestFilter)
- `AuthPrincipal.java` — 인증 주체 record(userId, email, roles, jti)
- `AccessTokenSessionChecker.java` — Access Token 세션(JTI) 유효성 검증 계약(구현은 auth.application)
- `SecurityUtils.java` — 현재 인증 주체 조회 등 정적 유틸(final)
- `RestAuthenticationEntryPoint.java` — 미인증(401) JSON 응답 처리
- `RestAccessDeniedHandler.java` — 권한 없음(403) JSON 응답 처리
