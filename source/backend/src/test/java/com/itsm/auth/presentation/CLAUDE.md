# CLAUDE.md

auth 도메인 프레젠테이션 계층(컨트롤러)의 단위 테스트(JUnit, Mockito).

## 파일
- `AuthControllerTest.java` — `/auth/login`·`/auth/refresh`·`/auth/logout`의 Refresh Token·XSRF-TOKEN 쿠키 발급/만료 및 `/auth/refresh` CSRF 헤더-쿠키 검증(불일치·누락 시 403 `CSRF_TOKEN_MISMATCH`) 테스트
