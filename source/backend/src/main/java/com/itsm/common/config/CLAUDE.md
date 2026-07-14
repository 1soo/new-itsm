# CLAUDE.md

애플리케이션 전역 설정 클래스(@Configuration).

## 파일
- `SecurityConfig.java` — Spring Security 필터체인·인가 규칙·CORS·JWT 필터 등록. `RoleHierarchy` 빈으로 SYSTEM_ADMIN이 `@PreAuthorize(hasRole/hasAnyRole)` 체크를 항상 통과하도록 단일 지점 처리(신규 역할 추가 시 계층 문자열에 한 줄만 추가)
- `OpenApiConfig.java` — springdoc OpenAPI(Swagger-UI) 문서·인증 스키마 설정
- `JpaAuditingConfig.java` — JPA Auditing 활성화(생성/수정자·시각 자동 기록)
- `JacksonConfig.java` — 전역 Jackson 커스터마이징(LocalDate 필드가 FE의 전체 ISO-8601 datetime 직렬화도 허용하도록 완화)
- `LenientLocalDateDeserializer.java` — LocalDate 커스텀 역직렬화기("yyyy-MM-dd" 또는 전체 ISO-8601 datetime 모두 파싱)
- `DotenvEnvironmentPostProcessor.java` — `.env` 파일을 Environment에 로드(`io.github.cdimascio:dotenv-java` 직접 사용, `META-INF/spring.factories`로 등록). spring-dotenv(me.paulschwarz)가 Boot 4의 `ConfigurableBootstrapContext` 패키지 이동으로 무동작하게 되어 대체(2026-07-14 런타임 업그레이드 유지보수)
