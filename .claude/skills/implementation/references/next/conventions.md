# Next 개발 컨벤션

화면 설계서·API 명세서·인증/인가 설계 기반으로 Next.js를 구현한다. **Next의 역할은 Frontend로 고정되지 않으며, `tech.md`의 CSR/SSR 결정에 따라 달라진다.**

- **CSR + Next가 Frontend로 지정된 경우**: 순수 클라이언트 렌더링 Frontend로 사용(개발-FE 역할). 데이터는 공통 apiClient로 별도 Backend를 호출한다.
- **SSR + Next가 지정된 경우**: Next가 서버 역할까지 수행한다. Route Handler·Server Action·서버 컴포넌트에서 API 명세서(`docs/02_plan`)의 엔드포인트·비즈니스 로직·DB 연동을 직접 구현하며, 별도 Backend 없이 Next 자체가 서버로 동작한다.

## MCP

- **playwright MCP**로 개발 완료 후 E2E 테스트를 수행한다.

## 핵심 규칙

- **라우팅**: 기본 **App Router** 사용. (설계상 Pages Router 필요 시에만 예외, 사용자 확인)
- **렌더링과 역할**: `tech.md`의 SSR/CSR 결정을 따른다. CSR이면 클라이언트 컴포넌트 중심의 순수 Frontend, SSR이면 서버 컴포넌트·Route Handler·Server Action으로 서버 역할까지 겸한다. 화면 특성에 맞게 서버/클라이언트 컴포넌트를 구분한다.
- **모든 API 요청(클라이언트 측)은 공통 apiClient**로 수행한다.
- UI 구현은 [references/ui-ux/conventions.md](../ui-ux/conventions.md)의 디자인 시스템을 따른다.

## 1. 라우터 선택

- 기본 App Router(`app/`)를 사용한다.
- Pages Router가 필요한 특별한 사유가 있으면 사용자에게 확인 후 결정한다.
- 결정 근거를 코드/문서에 남긴다.

## 2. 렌더링 전략 (SSR / CSR)과 역할

- `tech.md`의 결정을 따른다. Next의 역할은 Frontend로 고정되지 않고 이 결정에 따라 달라진다.
- **CSR + Next를 Frontend로 지정한 경우**: Next는 순수 클라이언트 렌더링 Frontend다. 서버 역할은 수행하지 않으며, 데이터는 공통 apiClient로 별도 Backend를 호출한다.
- **SSR + Next가 지정된 경우**: Next가 서버 역할을 겸한다. Route Handler(`app/api/**`)·Server Action·서버 컴포넌트의 데이터 패칭에서 API 명세서(`docs/02_plan`)의 엔드포인트·비즈니스 로직·DB 연동을 직접 구현한다.
- 화면별로 서버 컴포넌트(데이터 패칭·SEO 필요) / 클라이언트 컴포넌트(상호작용)를 구분한다.
- 데이터 패칭은 Next 표준 방식(server component fetch / route handler)을 사용한다.

## 3. API 요청 — 공통 apiClient

- 클라이언트 측 요청은 공통 apiClient를 통한다.
- Access Token(Session Storage) 첨부, 401 시 재발급/로그아웃. (인증 설계 준수)

## 4. 인증 · 인가 연동

- 로그인 필요 경로는 **middleware** 또는 서버 측 검증으로 접근을 제어한다.
- 화면 이동마다 Backend 권한 확인 API를 호출하여 권한을 검증하고, 403 시 차단한다. (인가 설계 준수)

## 5. 구조

- feature(도메인) 기반으로 라우트/컴포넌트를 구성한다.
- 모든 소스코드는 root의 `source/` 디렉토리 안에 둔다.
- Backend가 별도 스택(Spring Boot 등)이면 `source/frontend/`로 분리, Next 풀스택이면 `source/` 하위 단일 앱으로 구성한다.

## 개발 후 검증

1. 빌드 테스트를 수행한다.
2. playwright로 로그인·주요 화면·권한 분기 플로우를 E2E 검증한다.
