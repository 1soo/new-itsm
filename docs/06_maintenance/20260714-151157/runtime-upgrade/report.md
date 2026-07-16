---
date: 20260714-151157
domain: runtime-upgrade
change_type: [modified]
keywords: [java25, spring-boot4, postgresql18, gradle-upgrade, jackson3]
---

# 유지보수 이력 — runtime-upgrade

> 유지보수 일시: 20260714-151157 · 도메인: runtime-upgrade

## 1. 요구사항

java 25 + Spring Boot 4.1.x + Postgres 18로 런타임/프레임워크 버전을 업그레이드해야 한다.
migration 폴더(source/db/sql)를 제외한 기존 DB 데이터는 옮기지 않아도 된다.

## 2. 해결 방법

source/backend/build.gradle의 Spring Boot 플러그인을 3.3.5에서 4.1.0으로, Java toolchain을 17에서 25로 올렸다.
dependency-management 플러그인을 1.1.7로, springdoc-openapi-starter-webmvc-ui를 2.6.0에서 3.0.3으로, jjwt를 0.12.6에서 0.12.7로 올리고, testcontainers 아티팩트명을 testcontainers-junit-jupiter/testcontainers-postgresql로 변경했다.
source/backend/settings.gradle에 foojay-resolver-convention 1.0.0을 추가해 JDK25 toolchain을 자동 프로비저닝하도록 했다.
source/backend/gradle/wrapper의 Gradle 버전을 8.14에서 9.6.1로 올렸다.
Spring Boot 4 / Spring Framework 7 전환에 따른 컴파일 에러만 수정했다(동작 변경 없음).
JacksonConfig/LenientLocalDateDeserializer를 Jackson2에서 Jackson3(tools.jackson) 계열로 전환했다.
SecurityConfig의 RoleHierarchy 설정 방식을 setAuthorizationManagerFactory로 교체했다.
SearchIntegrationTest에서 제거된 TestRestTemplate 대신 RestTemplate을 사용하도록 대체했다.
13개 통합테스트의 PostgreSQLContainer 패키지를 변경하고 이미지 태그를 16에서 18-alpine으로 올렸다.
source/db/docker/docker-compose.yml의 postgres 이미지를 16-alpine에서 18-alpine으로 올렸다.
source/db/sql 01~31 전체 파일을 재검토해 OIDS/CREATE RULE/abstime 등 PostgreSQL 18에서 제거된 구문이 없음을 확인했다(수정 불요).

## 3. 변경 파일

- `source/backend/build.gradle`
- `source/backend/settings.gradle`
- `source/backend/gradle/wrapper/gradle-wrapper.properties`
- `source/backend/.../common/config/JacksonConfig.java`
- `source/backend/.../common/.../LenientLocalDateDeserializer.java`
- `source/backend/.../common/config/SecurityConfig.java`
- `source/backend/.../SearchIntegrationTest.java`
- `source/backend/.../*IntegrationTest.java`(PostgreSQLContainer 패키지 변경 + 이미지 태그 16→18-alpine, 13개)
- `source/db/docker/docker-compose.yml`

## 4. 테스트 결과

`./gradlew clean build` BUILD SUCCESSFUL, 단위테스트 367개 전부 pass했다(failures=0, errors=0).
testcontainers 의존 통합테스트 56개는 로컬 Docker Desktop의 registry-1.docker.io TLS 인증서 검증 실패(사내망 프록시 인증서 신뢰 문제)로 이미지 pull이 불가해 skip 처리했다(코드 문제 아님, 컴파일·로직은 재확인 결과 정상).
docker-compose 문법 검증(`docker compose config`)은 통과했으나, 실제 컨테이너 기동(docker-compose up)과 seed 데이터 적재는 동일한 환경 제약으로 보류했다.
tester 결과 상세: `docs/04_test/20260714-150102/runtime-upgrade/`
커밋 `4c80541`("chore(infra): Java 25 + Spring Boot 4.1 + PostgreSQL 18 업그레이드")으로 push 완료했다.
