# React 개발 컨벤션 (CSR)

화면 설계서·API 명세서·인증/인가 설계 기반 React(CSR) Frontend 구현.

## MCP

- **playwright MCP**로 개발 완료 후 E2E 테스트를 수행한다.

## 핵심 규칙

- **핵심 로직(hooks, utils, slice reducer 등)은 test-first로 작성**한다: 실패하는 테스트를 먼저 작성해 확인하고(Red) → 통과하는 최소 구현을 작성하고(Green) → 통과 유지한 채 구조를 정리한다(Refactor). 테스트 러너는 프로젝트에 구성된 도구(Jest/Vitest 등)를 사용한다.
- **전역 state는 Redux(Redux Toolkit)** 사용.
- **모든 API 요청은 공통 `apiClient`** 통해 수행한다. (직접 fetch/axios 호출 금지)
- UI 구현은 [references/ui-ux/conventions.md](../ui-ux/conventions.md)의 디자인 시스템을 따른다.
- Frontend는 `source/frontend/` 디렉토리에서 개발하고, 자체 `.env`를 둔다.

## 1. 전역 상태 — Redux

- Redux Toolkit을 사용한다. (slice 기반, `configureStore`)
- 전역 상태에는 인증 상태·사용자/역할 등 앱 전역에서 공유되는 값만 둔다. 화면 로컬 상태는 컴포넌트/훅에서 관리한다.

## 2. API 요청 — 공통 apiClient

- 모든 API 호출은 공통 `apiClient`(예: axios 인스턴스)를 통한다.
- **요청 인터셉터**: Access Token(Session Storage)을 Authorization 헤더에 첨부한다. (인증 설계 준수)
- **응답 인터셉터**: 401 → Refresh Token(httpOnly Cookie)으로 재발급 시도, 실패 시 로그아웃. (인증 설계 준수)
- baseURL·에러 정규화(공통 에러 형태)를 apiClient에서 처리한다.

## 3. 인가(권한) 연동

- 로그인이 필요한 화면 이동/요청 시, 화면 이동마다 Backend 권한 확인 API를 호출하여 접근 권한을 검증한다. (인가 설계 준수)
- 권한 부족(403) 시 접근을 차단한다.

## 4. 구조

- feature(도메인) 기반 폴더 구조를 권장한다: `features/{domain}/{components,hooks,api,slice}`.
- 라우팅은 react-router 등 표준 라이브러리를 사용한다. 화면 ID(SCR-...)와 라우트를 매핑한다.

## 개발 후 검증

1. 빌드 테스트를 수행한다.
2. playwright MCP로 주요 화면/플로우 E2E 테스트를 수행한다.
