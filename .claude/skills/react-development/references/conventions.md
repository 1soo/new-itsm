# React 개발 컨벤션 (CSR)

## 1. 전역 상태 — Redux

- **Redux Toolkit**을 사용한다. (slice 기반, `configureStore`)
- 전역 상태에는 인증 상태·사용자/역할 등 앱 전역에서 공유되는 값만 둔다. 화면 로컬 상태는 컴포넌트/훅에서 관리.

## 2. API 요청 — 공통 apiClient

- 모든 API 호출은 공통 `apiClient`(예: axios 인스턴스)를 통한다.
- **요청 인터셉터**: Access Token(**Session Storage**)을 Authorization 헤더에 첨부. (인증 설계 준수)
- **응답 인터셉터**: 401 → Refresh Token(httpOnly Cookie)으로 재발급 시도, 실패 시 로그아웃. (인증 설계 준수)
- baseURL·에러 정규화(공통 에러 형태)를 apiClient에서 처리.

## 3. 인가(권한) 연동

- 로그인이 필요한 화면 이동/요청 시, **화면 이동마다 Backend 권한 확인 API를 호출**하여 접근 권한을 검증한다. (인가 설계 준수)
- 권한 부족(403) 시 접근 차단 처리.

## 4. 구조

- feature(도메인) 기반 폴더 구조 권장: `features/{domain}/{components,hooks,api,slice}`.
- 라우팅은 react-router 등 표준 라이브러리 사용. 화면 ID(SCR-...)와 라우트를 매핑.

## 5. 검증

- 빌드 테스트 통과 필수.
- playwright로 로그인·주요 화면·권한 분기 플로우를 E2E 검증.
