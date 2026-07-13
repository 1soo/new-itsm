---
name: next-development
description: Next.js 개발 표준. App Router/Pages Router 및 SSR/CSR 선택 기준, 데이터 패칭·인증·인가 연동. 개발 후 빌드 테스트와 playwright E2E 테스트를 수행한다.
---

# Next 개발

화면 설계서·API 명세서·인증/인가 설계 기반으로 Next.js Frontend를 구현.

## MCP

- **playwright MCP**로 개발 완료 후 E2E 테스트 수행.

## 핵심 규칙

- **라우팅**: 기본 **App Router** 사용. (설계상 Pages Router 필요 시에만 예외, 사용자 확인)
- **렌더링**: `tech.md`의 SSR/CSR 결정을 따른다. 화면 특성에 맞게 서버/클라이언트 컴포넌트 구분.
- **모든 API 요청은 공통 apiClient**로 수행.
- UI 구현은 `ui-ux-development` skill의 디자인 시스템을 따른다.

## 개발 후 검증

1. **빌드 테스트** 수행.
2. **playwright MCP**로 주요 화면/플로우 E2E 테스트.

## docs

상세 컨벤션은 [references/conventions.md](references/conventions.md)를 따른다.
