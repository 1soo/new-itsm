---
date: 20260712-021500
domain: auth
result: pass
keywords: [토큰 저장 방식, CSRF, 더블서밋 쿠키, XSS 방지]
---

# 통합 테스트 결과 — auth (토큰 저장 방식 Client Memory 전환 + CSRF, 20260712-021500)

## 요약
- 총 10건 · 성공 10 · 실패 0 ✅ **전 항목 통과**

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew compileJava -q`, `npm run build` 모두 성공 | |
| TC-AUTH-LOGIN-001 | PASS | 200, body에 accessToken/refreshToken/user 포함. `Set-Cookie`로 `refreshToken`(HttpOnly, SameSite=Strict)·`XSRF-TOKEN`(HttpOnly 아님, SameSite=Strict) 정상 발급 확인 | Secure 속성은 local(HTTP) 환경이라 미설정 — 코드 주석상 운영 환경 대응 예정, 설계 의도대로 |
| TC-AUTH-CSRF-001 | PASS | 정상 CSRF(쿠키=헤더 일치) 재발급 200, 새 accessToken 발급 | |
| TC-AUTH-CSRF-002 | PASS | `X-CSRF-Token` 헤더 누락 시 403 `CSRF_TOKEN_MISMATCH` | |
| TC-AUTH-CSRF-003 | PASS | 헤더 값이 쿠키 값과 불일치 시 403 `CSRF_TOKEN_MISMATCH`(Refresh Token 유효 여부와 무관하게 CSRF 검증이 먼저 수행됨) | |
| TC-AUTH-CSRF-004 | PASS | CSRF 정상 + Refresh Token 변조(garbage 값) 시 401 `INVALID_REFRESH_TOKEN`(403이 아닌 401로 명확히 구분) | |
| TC-AUTH-LOGOUT-001 | PASS | 로그아웃 응답에서 `refreshToken`/`XSRF-TOKEN` 쿠키 모두 `Max-Age=0`으로 즉시 만료 지시 확인. 로그아웃 시 Cookie 헤더로 refreshToken을 함께 전송한 경우 서버가 해당 토큰을 실제로 revoke하여, 이후 동일 토큰으로 재발급 시도 시 401로 정상 차단됨 | 최초 시도에서 curl에 Cookie 헤더를 누락해 "로그아웃 후에도 토큰이 유효한 것처럼 보이는" 현상을 발견했으나, 이는 테스트 클라이언트가 쿠키를 자동 전송하지 않은 방법론적 오류였음(브라우저는 항상 쿠키를 자동 첨부). Cookie 헤더를 정확히 포함해 재검증한 결과 정상 revoke 확인(아래 "조사 경과" 참조) |
| TC-AUTH-FE-001 | PASS | 로그인 후 `localStorage`/`sessionStorage` 완전히 비어있음(토큰 미저장), `document.cookie`에는 `XSRF-TOKEN`만 노출되고 `refreshToken`은 노출되지 않음(HttpOnly로 XSS 노출 차단 확인) | |
| TC-AUTH-FE-002 | PASS | 전체 페이지 새로고침 후에도 로그인 화면으로 리다이렉트되지 않고 세션 유지(Access Token은 메모리에서 소실됐으나 `/auth/me` 401 → 쿠키 기반 `/auth/refresh` 자동 재시도로 복구). 콘솔에 초기 401 로그 2건 표시되나 사용자에게 노출되는 에러 토스트 없음(정상 흐름의 일부) | |
| TC-AUTH-FE-003 | PASS | UI 로그아웃(확인 다이얼로그 포함) 후 `document.cookie` 완전히 비워짐(`XSRF-TOKEN` 제거), `localStorage`/`sessionStorage` 계속 비어있음, `/login`으로 정상 이동 | |

## 조사 경과 — "로그아웃 후에도 재발급이 되는 것처럼 보인 현상"(결론: 테스트 방법 오류, 결함 아님)

- **초기 관찰**: curl로 로그인 → `Authorization` 헤더만 포함해 로그아웃 호출 → 로그아웃 응답으로 받은 것과 동일한 refreshToken 값으로 재발급을 시도했더니 200이 반환되어 "로그아웃이 서버에서 토큰을 무효화하지 않는다"는 결함으로 의심했다.
- **원인 조사**: `AuthController.logout()`은 `refreshToken` 쿠키 값 또는 body의 값을 사용해 revoke를 수행하는데, 최초 curl 호출에서 `Cookie: refreshToken=...` 헤더를 실수로 포함하지 않았다(curl은 브라우저와 달리 별도 쿠키 저장소를 유지하지 않는 한 자동으로 쿠키를 재전송하지 않는다). 그 결과 서버에 전달된 토큰 값이 없어 revoke 로직(`AuthService.logout()`의 `if (StringUtils.hasText(refreshTokenValue))` 블록) 자체가 실행되지 않았다.
- **재검증**: 로그인 응답의 refreshToken을 `Cookie` 헤더에 명시적으로 포함해 로그아웃을 호출한 결과, 이후 동일 토큰으로 재발급 시도 시 401 `INVALID_REFRESH_TOKEN`으로 정상 차단됨을 확인했다. 실제 브라우저는 `withCredentials`/쿠키 저장소로 항상 쿠키를 자동 첨부하므로(TC-AUTH-FE-003에서 UI 로그아웃 후 `document.cookie`가 완전히 비워짐을 확인) 실사용 시나리오에서는 문제가 없다.

## 실패 항목 분석

없음(전 항목 통과).

## 테스트 환경 참고

- 테스트 시작 시점에 backend(:8080)·frontend(:5173) 프로세스가 모두 내려가 있어(DB는 정상) dev-lead에게 알린 뒤 직접 재기동했다(`./gradlew bootRun`, `npm run dev`, 둘 다 tool의 `run_in_background`로 기동). 재기동 후 전 시나리오 정상 수행.
