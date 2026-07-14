# 통합 테스트 시나리오 — runtime-upgrade (BE/DB 런타임 버전 업그레이드)

## 사전 조건
- 빌드 테스트 통과
- 신규 화면/API/DB 스키마 변경 없음(인프라 성격 유지보수) — 근거: @docs/02_plan/upgrade/backend-db-runtime-upgrade.md §1
- **환경 제약**: 로컬 Docker 데몬이 registry-1.docker.io TLS 인증서 검증 실패로 이미지 pull 불가 → Testcontainers 기반 테스트, `docker-compose up` 실제 PG18 기동 검증은 이번 라운드 제외(사용자 승인)

## 시나리오

### TC-RUP-001 · Gradle 빌드(컴파일) 성공 확인
- 근거: @docs/02_plan/upgrade/backend-db-runtime-upgrade.md §5-2 (`./gradlew build` 성공), §4 (Gradle/Lombok 리스크)
- 전제: `source/backend`에 build.gradle(Boot 4.1.0/Java25) 적용됨
- 절차:
  1. `source/backend`에서 `./gradlew clean build -x test` 실행
  2. 실패 시 `./gradlew compileJava compileTestJava`로 대체 확인
- 기대 결과: BUILD SUCCESSFUL, 컴파일 에러 0건 (Lombok JDK25 애노테이션 프로세싱 정상 동작 포함)

### TC-RUP-002 · Docker 미의존 단위 테스트 회귀 확인
- 근거: @docs/02_plan/upgrade/backend-db-runtime-upgrade.md §5-4 (기존 통합 테스트 통과), §4 (jjwt/springdoc 변경 리스크)
- 전제: 기존 JUnit 테스트 스위트 존재
- 절차:
  1. `./gradlew test` 전체 실행
  2. Testcontainers(PostgreSQL 컨테이너 기동 필요) 의존 테스트 실패는 "환경 제약(로컬 Docker 인증서 문제)"로 분류
  3. Testcontainers 비의존 테스트(순수 단위 테스트, jjwt 토큰 발급/검증 등) 결과만 pass/fail 판정
- 기대 결과: Docker 미의존 테스트 전원 통과. Testcontainers 의존 테스트는 환경 제약으로 미검증 처리(실패로 집계하지 않음)

### TC-RUP-003 · build.gradle / gradle-wrapper.properties 정적 검토
- 근거: @docs/02_plan/upgrade/backend-db-runtime-upgrade.md §2.1, §2.2 (변경 전/후 표)
- 전제: 없음
- 절차:
  1. `source/backend/build.gradle`의 plugin 버전(Boot 4.1.x), Java toolchain(25), springdoc(3.x), jjwt(0.12.x) 확인
  2. `source/backend/gradle/wrapper/gradle-wrapper.properties`의 distributionUrl이 8.14 이상인지 확인
- 기대 결과: 설계 문서의 변경 후 값과 일치

### TC-RUP-004 · docker-compose.yml PostgreSQL 이미지 태그 확인
- 근거: @docs/02_plan/upgrade/backend-db-runtime-upgrade.md §2.3 (postgres:16-alpine → postgres:18-alpine)
- 전제: 없음
- 절차:
  1. `source/db/docker/docker-compose.yml`의 `image:` 값 확인 (파일 검토만, 실제 기동은 환경 제약으로 생략)
- 기대 결과: `postgres:18-alpine`로 반영됨
