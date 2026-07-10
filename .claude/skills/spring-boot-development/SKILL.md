---
name: spring-boot-development
description: Spring / Spring Boot 백엔드 개발 표준. DDD·SOLID, 모든 예외에 대한 JUnit 테스트 필수, 공통 예외처리 모듈, Spring Security 기반 인증·인가, springdoc 기반 Swagger-UI(OpenAPI) 문서화. 개발 후 빌드 테스트와 playwright E2E 테스트를 수행한다.
---

# Spring / Spring Boot 개발

API 명세서·테이블 정의서·인증/인가 설계를 기반으로 Backend를 구현한다.

## MCP

- **playwright MCP**로 개발 완료 후 API/E2E 테스트를 수행한다.

## 핵심 규칙

- **DDD(Domain Driven Design)** 로 계층을 구성한다. (domain / application / infrastructure / presentation)
- **SOLID 원칙**을 준수한다.
- **모든 예외에 대해 JUnit 테스트를 필수**로 작성한다.
- **공통 예외처리 모듈**을 둔다. (`@RestControllerAdvice` + 표준 에러 응답)
- **Spring Security로 인증·인가**를 구현한다. (인증/인가 설계 준수)
- **DB 접근 방식(JPA/MyBatis 등)은 설계자가 결정한 테이블 정의서(`docs/02_plan/database`)를 따른다.** 임의로 선택하지 않는다.
- **Swagger-UI(OpenAPI) 문서를 작성**한다. `springdoc-openapi`(springdoc-openapi-starter-webmvc-ui)를 추가하고, 모든 API에 어노테이션(`@Tag`, `@Operation`, `@ApiResponse`, `@Schema`)으로 요청/응답·에러 코드를 명세한다. (API 명세서 `docs/02_plan` 기준)
- Backend는 `source/backend/` 디렉토리에서 개발하고 자체 `.env`를 둔다.

## 개발 후 검증

1. **빌드 테스트**(gradle/maven) 및 **JUnit 테스트** 통과.
2. **playwright MCP**로 실행 중인 API/화면 E2E 검증.

## docs

상세 컨벤션은 [references/conventions.md](references/conventions.md)를 따른다.
