# Spring / Spring Boot 개발 컨벤션

## 1. DDD 계층 구조

도메인 중심으로 계층을 분리한다.

| 계층 | 책임 |
|------|------|
| presentation | Controller, 요청/응답 DTO |
| application | UseCase/Service, 트랜잭션 경계 |
| domain | Entity, 도메인 로직, 도메인 서비스, Repository 인터페이스 |
| infrastructure | Repository 구현, 외부 연동, 설정 |

- 도메인 로직은 domain 계층에 두고, 다른 계층에 누출시키지 않는다.

## 2. SOLID

- 단일 책임·의존성 역전을 우선한다. Service는 인터페이스에 의존, 구현은 주입.

## 3. 예외 처리

- **공통 예외처리 모듈**: `@RestControllerAdvice`로 전역 처리, 표준 에러 응답 형태(code, message) 통일.
- 도메인/비즈니스 예외는 명시적 예외 타입으로 정의.
- **모든 예외 케이스에 대해 JUnit 테스트를 작성**한다. (정상 + 예외 분기 모두 검증)

## 4. 인증 · 인가 (Spring Security)

- 인증 설계 준수: JWT 필터로 Access Token 검증, 토큰에서 user id·jti 추출 후 저장된 매핑(Redis/DB)과 비교, 불일치 시 로그아웃(401).
- Access Token claim: user id, jti, role.
- 인가 설계 준수: 요청/화면 권한을 role 기반으로 검증, 권한 부족 시 403.
- 로그아웃 시 저장된 jti 제거(또는 null 처리).

## 5. API 문서화 (Swagger-UI / OpenAPI)

- **`springdoc-openapi-starter-webmvc-ui`** 의존성을 추가하여 Swagger-UI를 제공한다. (`/swagger-ui.html`, OpenAPI 스펙 `/v3/api-docs`)
- 모든 Controller/엔드포인트를 어노테이션으로 명세한다.
  - `@Tag` (도메인/컨트롤러 단위 그룹), `@Operation`(summary·description), `@Parameter`, `@Schema`(DTO 필드), `@ApiResponse`(성공/에러 코드).
  - 문서 내용은 **API 명세서(`docs/02_plan`)와 일치**시킨다(엔드포인트·메서드·요청/응답·응답 코드·토큰 필요 여부).
- **인증이 필요한 API는 JWT Bearer 스킴**을 등록한다. (`@SecurityScheme(type = HTTP, scheme = "bearer", bearerFormat = "JWT")`) → Swagger-UI에서 Authorize로 토큰 주입 가능.
- **Spring Security 설정**에서 Swagger 경로(`/swagger-ui/**`, `/v3/api-docs/**`)를 인증 예외로 허용한다. (운영환경 노출 정책은 인프라/보안 설계를 따른다.)

## 6. 검증

- gradle/maven 빌드 테스트 통과 필수.
- JUnit 테스트(예외 포함) 통과 필수.
- playwright로 실행 중인 엔드포인트/화면 E2E 검증.
- **Swagger-UI(`/swagger-ui.html`) 정상 렌더링 및 주요 API 노출 확인.**
