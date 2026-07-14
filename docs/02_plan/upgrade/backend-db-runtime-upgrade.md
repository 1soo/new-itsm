# 백엔드/DB 런타임 버전 업그레이드 설계

> 유지보수 요청 기반 설계 · 작성일: 2026-07-14
> 신규 화면/API/DB 스키마 변경 없음. Java·Spring Boot·PostgreSQL 런타임/프레임워크 버전 업그레이드만 다룬다.

## 1. 배경 및 범위

- 대상: `source/backend/`, `source/db/`
- 로컬 개발 환경(docker volume) 기존 데이터 이관 불요 — fresh init 허용 (`itsm_pgdata` 볼륨 재생성 가능)
- `source/db/sql/` 내 DDL/DML 파일은 점검만 하고 원칙적으로 유지
- 영향 없음: `source/frontend/`, `docs/02_plan/screen`·`api_spec`·`database`·`security` 기존 설계 문서(스키마·API 계약 불변)

> **주의**: 본 설계 작성 시 context7 MCP(Spring Boot 4.1.x / springdoc / Gradle 최신 버전 확인용)가 네트워크 오류로 응답하지 않았다. 아래 정확한 patch 버전은 확정값이 아니라 **최소 요구 라인**이며, 구현 시 BE/DB 개발자가 Maven Central·Spring Initializr(https://start.spring.io/actuator/info)에서 실제 최신 patch 버전을 재확인해 고정해야 한다.

## 2. 대상 파일별 변경 전/후

### 2.1 `source/backend/build.gradle`

| 항목 | 현재 | 변경 후 | 비고 |
|------|------|---------|------|
| Java toolchain | 17 | **25** | LTS |
| `org.springframework.boot` plugin | 3.3.5 | **4.1.x** | 구현 시 최신 4.1 patch 확인 |
| `io.spring.dependency-management` | 1.1.6 | 1.1.x 최신 | 필요 시에만 상향, 미상향해도 대체로 호환 |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui` | 2.6.0 | **3.x** | 2.x는 Spring Framework 6(Boot3)용, Framework 7(Boot4) 대응은 3.x 라인 |
| `io.jsonwebtoken:jjwt-api/-impl/-jackson` | 0.12.6 | 0.12.x 최신 patch | Java/Boot 버전 무관 순수 라이브러리, 보안 패치 목적만 |
| `me.paulschwarz:spring-dotenv` | 4.0.0 | 4.0.0 유지 | `EnvironmentPostProcessor` API 안정. 단, Boot 4.1 자체 dotenv 지원 기능 존재 시 구현 중 중복 제거 검토 |
| `org.postgresql:postgresql` (버전 미지정, Boot BOM 관리) | - | 변경 불요 | Boot 4.1 BOM 승격 버전이 PG18 지원하는지만 확인 |
| `org.testcontainers:junit-jupiter`, `:postgresql` (버전 미지정, Boot BOM 관리) | - | 변경 불요 | Boot 4.1 BOM 승격 버전 자동 적용 |
| Lombok (버전 미지정, Boot BOM 관리) | - | 변경 불요 | Boot BOM 관리 버전이 JDK25 애노테이션 프로세싱 지원하는지 빌드로 확인 |
| `spring-boot-starter-web/-security/-data-jpa/-validation` | - | 변경 불요 | Boot 4.1 BOM 자동 승격 |

### 2.2 `source/backend/gradle/wrapper/gradle-wrapper.properties`

| 항목 | 현재 | 변경 후 |
|------|------|---------|
| Gradle distribution | 8.14 | Spring Boot 4.1 Gradle 플러그인 최소 요구 버전 이상 + JDK25 툴체인 지원 버전(구현 시점 최신 안정판으로 고정, 8.14 미만으로 낮추지 않음) |

### 2.3 `source/db/docker/docker-compose.yml`

| 항목 | 현재 | 변경 후 |
|------|------|---------|
| `postgres` image | `postgres:16-alpine` | `postgres:18-alpine` |

`TZ`/`PGTZ` 환경변수, healthcheck(`pg_isready`), `../sql` 초기화 마운트 방식은 변경 불요.

### 2.4 `source/db/sql/*.sql` (01~31번 전체)

- `OIDS`, `CREATE RULE`, `abstime`/`reltime`/`tinterval` 등 PG18에서 제거된 구문 점검 결과 **사용 없음** → 수정 불요.
- 기존 볼륨 삭제 후 fresh init 허용됨(사용자 확인) — 마이그레이션 스크립트 별도 작성 불요.

## 3. 의존성 호환 매트릭스

| 구성 요소 | Boot 3.3.5 라인 | Boot 4.1.x 라인 | 변경 필요 여부 |
|-----------|-----------------|-------------------|----------------|
| Java | 17 | 25 | 변경 |
| Spring Framework | 6.1.x | 7.x | Boot 플러그인 승격으로 자동 |
| Jakarta EE 네임스페이스 | 유지(9→10 이내) | 유지(변경 없음, 이미 jakarta.* 사용 중) | 코드 변경 불요 |
| springdoc-openapi | 2.6.0 (Framework6 대응) | 3.x (Framework7 대응) | 변경 |
| jjwt | 0.12.6 | 0.12.x 최신 | 선택(보안 patch) |
| PostgreSQL JDBC driver | Boot3 BOM 관리 | Boot4.1 BOM 관리 | 자동 승격, PG18 지원 여부만 빌드로 확인 |
| Testcontainers | Boot3 BOM 관리 | Boot4.1 BOM 관리 | 자동 승격 |
| PostgreSQL 서버 | 16-alpine | 18-alpine | 변경 |
| Gradle | 8.14 | 8.14 이상(최신 확인) | 확인 후 필요 시 상향 |

## 4. 리스크 및 검증 방법

| 리스크 | 영향 | 검증 방법 |
|--------|------|-----------|
| springdoc 2.x↔3.x API 불일치로 Swagger UI 미기동 | BE 문서화 기능 상실 | 빌드 후 `/swagger-ui/index.html` 접속 확인 |
| Boot BOM의 PostgreSQL JDBC 드라이버가 PG18 미지원 | DB 커넥션 실패 | Testcontainers 기반 통합 테스트 전체 통과 여부 |
| Gradle 버전이 Boot 4.1 플러그인 요구 버전 미달 | 빌드 자체 실패 | `./gradlew build` 성공 여부 |
| Lombok이 JDK25 애노테이션 프로세싱 미지원 | 컴파일 실패 | `./gradlew compileJava` 성공 여부 |
| spring-dotenv가 Boot4 `EnvironmentPostProcessor` 계약 변경으로 미동작 | `.env` 값 미주입 | 기동 후 DB 연결·JWT 설정값 정상 로드 확인 |
| fresh init 시 seed 데이터 미적재 | 로컬 개발 데이터 없음 | `docker-compose up` 후 시드 테이블 row count 확인 |

## 5. 구현 후 확인 절차 (BE/DB 개발자, tester 통합 테스트 이전 선행 확인)

1. `docker-compose up`으로 `postgres:18-alpine` 기동 + `source/db/sql` 초기화 스크립트 정상 실행(에러 없이 01~31번 순차 적용) 확인
2. `./gradlew build` (Java 25 toolchain) 성공
3. 애플리케이션 기동 후 `/swagger-ui/index.html` 정상 응답
4. 기존 Testcontainers 기반 통합 테스트 전체 통과

## 6. 미변경 대상

- 신규 화면/API/DB 스키마 설계 없음 (`docs/02_plan/screen`·`api_spec`·`database` 기존 문서 유지)
- `source/frontend/` 변경 없음
- 배포 환경(local) 및 CSP 인프라 아키텍처 변경 없음
