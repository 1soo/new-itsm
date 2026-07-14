---
name: implementation
description: UI/UX, React, Next.js, Spring Boot, Database(Supabase 포함)를 구현하는 개발 표준. 설계 산출물(docs/02_plan)과 기술스택(docs/01_analyze/tech.md) 기반 실제 코드 구현이 필요할 때 사용한다.
---

# 구현 (개발 단계)

`developer`가 사용하는 개발 단계 skill. 담당 역할(UI/FE/BE/DB)과 `tech.md`의 기술스택 결정에 따라 `references`의 하위 폴더를 참조한다.

## 참조 안내

| 역할 | 참조 조건 | 참고 문서 |
|------|-----------|-----------|
| UI | 모든 화면 개발의 공통 기반(디자인 시스템·공통 컴포넌트) | [references/ui-ux/conventions.md](references/ui-ux/conventions.md) |
| FE | Frontend가 React(CSR)인 경우 | [references/react/conventions.md](references/react/conventions.md) |
| FE 또는 BE | Next가 지정된 경우(CSR: Frontend 역할 / SSR: 서버 역할까지 겸함) | [references/next/conventions.md](references/next/conventions.md) |
| BE | Backend가 Spring/Spring Boot인 경우 | [references/spring-boot/conventions.md](references/spring-boot/conventions.md) |
| DB | 모든 경우(DB 구성) | [references/database/conventions.md](references/database/conventions.md) |
| DB | `tech.md`에서 Database/BaaS로 **Supabase**가 지정된 경우(인증·DB·File Storage·Realtime·자동 생성 API 중 요구사항에 필요한 기능만 선택적으로 사용) | [references/database/supabase/conventions.md](references/database/supabase/conventions.md) |

`next`(references/next)는 Frontend 역할로 고정되지 않는다. CSR에서 Next가 Frontend로 지정된 경우 순수 클라이언트 Frontend로 사용하고, SSR에서 Next가 지정된 경우 Route Handler·Server Action으로 API·비즈니스 로직·DB 연동까지 구현하는 서버 역할로 사용한다.

## 공통 원칙

- `docs/02_plan`에 명시된 내용만 구현한다.
- 모든 소스코드는 root의 `source/` 디렉토리 안에 저장한다.
- 개발 후 검증은 각 참고 문서의 "개발 후 검증"/"검증" 절을 따른다(빌드 테스트, JUnit, playwright E2E, DB MCP 또는 test code 등).
