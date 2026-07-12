# 유지보수 이력 — auth

> 유지보수 일시: 20260712-111803 · 도메인: auth

## 1. 요구사항

Access Token 저장 방식을 sessionStorage에서 Client Memory로 전환해야 한다.
CSRF 공격을 방지하기 위한 조치(더블서밋 쿠키 패턴)가 추가되어야 한다.

## 2. 해결 방법

FE `apiClient.ts`에서 Access Token을 sessionStorage 대신 Client Memory(모듈 변수)에 저장하도록 전환하고, XSRF-TOKEN 쿠키를 읽어 요청 헤더 `X-CSRF-Token`으로 첨부하도록 구현했다.
`authSlice.ts` 등 관련 문서를 갱신했다.
BE `AuthController.java`에서 XSRF-TOKEN 쿠키를 더블서밋 방식으로 발급하고, `/auth/refresh` 엔드포인트에서 CSRF 토큰 검증을 수행하도록 구현했다.
로그아웃 시 XSRF-TOKEN 쿠키가 만료 처리되도록 구현했다.
`ErrorCode`에 `CSRF_TOKEN_MISMATCH`를 신규 추가했다.
설계 문서 `docs/02_plan/security/authentication.md`(v0.2), `docs/02_plan/api_spec/auth.md`(v0.4)를 갱신했다.

## 3. 변경 파일

- `source/frontend/src/lib/apiClient.ts`
- `source/frontend/src/store/authSlice.ts`
- `source/backend/.../auth/presentation/AuthController.java`
- `source/backend/.../common/exception/ErrorCode.java`
- `AuthControllerTest.java`(신규)
- `docs/02_plan/security/authentication.md`
- `docs/02_plan/api_spec/auth.md`

## 4. 테스트 결과

통합 테스트 10건 전부 PASS했다(`docs/04_test/auth/20260712-021500`).
커밋 `883c652`(main)로 반영했다.
