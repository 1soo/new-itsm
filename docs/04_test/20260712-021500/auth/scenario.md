# 통합 테스트 시나리오 — auth (토큰 저장 방식 Client Memory 전환 + CSRF, 유지보수)

> 대상: 로그인/토큰 재발급/로그아웃의 토큰 저장 방식을 Client Memory(Access)+httpOnly Cookie(Refresh)+더블서밋 CSRF 쿠키(XSRF-TOKEN)로 전환.
> 근거 문서: `docs/02_plan/security/authentication.md` 4절, `docs/02_plan/api_spec/auth.md`(API-AUTH-001~003, v0.4)
> 변경 파일: `source/frontend/src/lib/apiClient.ts`, `source/frontend/src/store/authSlice.ts`, `source/backend/.../auth/presentation/AuthController.java`

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- 로컬 환경: docker `itsm-postgres`, backend(:8080), frontend(:5173) 기동 상태
- 테스트 계정: `admin@itsm.local` / `Admin@1234`

## 시나리오

### TC-BUILD-001 · 빌드 테스트
- 근거: @docs/02_plan/api_spec/auth.md, @docs/02_plan/security/authentication.md
- 절차: 1) `./gradlew compileJava -q`(backend) 2) `npm run build`(frontend)
- 기대 결과: 오류 없이 성공

### TC-AUTH-LOGIN-001 · 로그인 응답 — 쿠키 발급 속성 확인
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-001), @docs/02_plan/security/authentication.md 4절
- 절차: 1) `POST /api/v1/auth/login` 2) 응답 헤더의 `Set-Cookie` 2건 확인
- 기대 결과: 200, body에 accessToken/refreshToken/user 포함. `refreshToken` 쿠키는 HttpOnly+SameSite=Strict, `XSRF-TOKEN` 쿠키는 HttpOnly 아님(읽기 가능)+SameSite=Strict

### TC-AUTH-CSRF-001 · 토큰 재발급 — 정상 CSRF(쿠키=헤더 일치)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-002)
- 전제: TC-AUTH-LOGIN-001에서 발급받은 refreshToken/XSRF-TOKEN 쿠키
- 절차: 1) `POST /api/v1/auth/refresh`(Cookie 포함, `X-CSRF-Token`=XSRF-TOKEN 쿠키 값, body 없음)
- 기대 결과: 200, 새 accessToken 발급

### TC-AUTH-CSRF-002 · 토큰 재발급 — X-CSRF-Token 헤더 누락 403
- 근거: 상동 (403 케이스)
- 절차: 1) 동일 쿠키로 `X-CSRF-Token` 헤더 없이 `POST /api/v1/auth/refresh`
- 기대 결과: 403 `CSRF_TOKEN_MISMATCH`

### TC-AUTH-CSRF-003 · 토큰 재발급 — CSRF 헤더·쿠키 불일치 403
- 근거: 상동
- 절차: 1) `X-CSRF-Token` 헤더에 쿠키 값과 다른 임의 문자열 전달
- 기대 결과: 403 `CSRF_TOKEN_MISMATCH`(Refresh Token 유효 여부와 무관하게 CSRF 검증이 먼저 수행됨)

### TC-AUTH-CSRF-004 · 토큰 재발급 — CSRF 정상 + Refresh Token 무효 401
- 근거: 상동 (401 케이스)
- 절차: 1) 유효한 CSRF 헤더/쿠키 쌍은 유지하되 refreshToken 쿠키 값을 임의 문자열로 변조 후 요청
- 기대 결과: 401(재로그인 필요), CSRF는 통과했으므로 403이 아닌 401로 구분됨

### TC-AUTH-LOGOUT-001 · 로그아웃 — 쿠키 무효화
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-003)
- 절차: 1) `POST /api/v1/auth/logout`(Authorization 헤더 포함) 2) 응답 `Set-Cookie` 확인 3) 로그아웃 후 원래 refreshToken 쿠키로 재발급 시도
- 기대 결과: 200, `refreshToken`/`XSRF-TOKEN` 쿠키 `Max-Age=0`으로 즉시 만료 지시. 이후 동일 refreshToken으로 재발급 시도 시 401(세션 무효화됨)

### TC-AUTH-FE-001 · 프론트엔드 — Access Token이 localStorage/sessionStorage에 저장되지 않음
- 근거: `source/frontend/src/lib/apiClient.ts`(Client Memory 저장)
- 절차: 1) playwright 새 컨텍스트, storage 초기화 2) UI로 로그인 3) `localStorage`/`sessionStorage` 전체 키 검사 4) `document.cookie` 확인
- 기대 결과: localStorage/sessionStorage에 access/refresh 토큰 관련 키 없음. `document.cookie`에는 `XSRF-TOKEN`만 보이고 `refreshToken`은 보이지 않음(HttpOnly)

### TC-AUTH-FE-002 · 프론트엔드 — 새로고침 후 세션 복구(쿠키 기반)
- 근거: `source/frontend/src/store/authSlice.ts`(bootstrapAuth)
- 전제: TC-AUTH-FE-001 로그인 상태
- 절차: 1) 페이지 전체 새로고침(재진입) 2) 로그인 화면으로 리다이렉트되지 않고 세션 유지되는지 확인
- 기대 결과: Access Token은 새로고침으로 메모리에서 소실되지만, `/auth/me` 401 → 쿠키 기반 `/auth/refresh` 자동 재시도로 세션이 그대로 복구되어 재로그인 없이 이용 가능

### TC-AUTH-FE-003 · 프론트엔드 — 로그아웃 후 쿠키·상태 정리
- 근거: 상동
- 절차: 1) UI에서 로그아웃 2) `document.cookie`에 `XSRF-TOKEN` 남아있는지 확인 3) 로그인 화면으로 이동했는지 확인
- 기대 결과: 로그아웃 후 `document.cookie`에서 `XSRF-TOKEN` 제거 확인, 로그인 화면으로 정상 이동
