# CLAUDE.md

ITSM 백엔드. Spring Boot 4.1 + Java 25 + Gradle 기반 REST API 서버. DDD·SOLID 원칙으로 도메인(auth/incident/problem/srm)별 4계층(application/domain/infrastructure/presentation) 구성, common 모듈이 설정·예외·보안·티켓 공통 요소 제공. Spring Security + JWT 인증/인가, springdoc(Swagger-UI) 문서화, PostgreSQL + JPA(ddl-auto validate), 통합 테스트는 Testcontainers 사용.

## 파일
- `build.gradle` — 의존성·빌드 설정(Spring Web/Security/JPA/Validation, jjwt, springdoc, PostgreSQL, Lombok, Testcontainers)
- `settings.gradle` — Gradle 프로젝트명(`itsm-backend`), foojay-resolver-convention(JDK25 toolchain 자동 프로비저닝)
- `gradlew`, `gradlew.bat` — Gradle Wrapper 실행 스크립트(Unix/Windows)
- `.env` — 로컬 환경변수(DB·JWT·CORS 등, 버전관리 제외)
- `.env.example` — 환경변수 템플릿
- `.gitignore` — 빌드 산출물·환경변수 등 버전관리 제외 목록

## 하위 디렉토리
- `src/` — 소스 코드(main/test)
- `gradle/` — Gradle Wrapper

> 빌드 산출물 디렉토리(`build/`, `bin/`, `.gradle/`)는 버전관리·문서화 대상이 아니다.
