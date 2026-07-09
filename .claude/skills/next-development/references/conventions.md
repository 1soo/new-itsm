# Next.js 개발 컨벤션

## 1. 라우터 선택

- 기본 **App Router**(`app/`)를 사용한다.
- Pages Router가 필요한 특별한 사유가 있으면 사용자에게 확인 후 결정한다.
- 결정 근거를 코드/문서에 남긴다.

## 2. 렌더링 전략 (SSR / CSR)

- `tech.md`의 결정을 따른다.
- 화면별로 서버 컴포넌트(데이터 패칭·SEO 필요) / 클라이언트 컴포넌트(상호작용)를 구분한다.
- 데이터 패칭은 Next 표준 방식(server component fetch / route handler)을 사용.

## 3. API 요청 — 공통 apiClient

- 클라이언트 측 요청은 공통 apiClient를 통한다.
- Access Token(Session Storage) 첨부, 401 시 재발급/로그아웃. (인증 설계 준수)

## 4. 인증 · 인가 연동

- 로그인 필요 경로는 **middleware** 또는 서버 측 검증으로 접근을 제어한다.
- 화면 이동마다 Backend 권한 확인 API를 호출하여 권한을 검증하고, 403 시 차단. (인가 설계 준수)

## 5. 구조

- feature(도메인) 기반으로 라우트/컴포넌트를 구성한다.
- 모든 소스코드는 root의 `source/` 디렉토리 안에 둔다.
- Backend가 별도 스택(Spring Boot 등)이면 `source/frontend/`로 분리, Next 풀스택이면 `source/` 하위 단일 앱으로 구성.

## 6. 검증

- 빌드 테스트 통과 필수.
- playwright로 로그인·주요 화면·권한 분기 플로우를 E2E 검증.
