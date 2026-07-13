---
name: react-development
description: React(CSR) 개발 표준. 전역 state는 Redux(Redux Toolkit), API 요청은 공통 apiClient 사용. 개발 후 빌드 테스트와 playwright E2E 테스트를 수행한다.
---

# React 개발 (CSR)

화면 설계서·API 명세서·인증/인가 설계 기반 React(CSR) Frontend 구현.

## MCP

- **playwright MCP**로 개발 완료 후 E2E 테스트 수행.

## 핵심 규칙

- **전역 state는 Redux(Redux Toolkit)** 사용.
- **모든 API 요청은 공통 `apiClient`** 통해 수행. (직접 fetch/axios 호출 금지)
- UI 구현은 `ui-ux-development` skill 디자인 시스템 따름.
- Frontend는 `source/frontend/` 디렉토리에서 개발, 자체 `.env` 둠.

## 개발 후 검증

1. **빌드 테스트** 수행.
2. **playwright MCP**로 주요 화면/플로우 E2E 테스트.

## docs

상세 컨벤션은 [references/conventions.md](references/conventions.md) 따름.
