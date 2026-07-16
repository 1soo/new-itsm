---
date: 20260714-150102
domain: runtime-upgrade
result: pass
keywords: [java25, spring-boot4, postgresql18, gradle-upgrade, testcontainers]
---

# 통합 테스트 결과 — runtime-upgrade (BE/DB 런타임 버전 업그레이드) (20260714-150102)

## 요약
- 총 4건 · 성공 4 · 실패 0
- Testcontainers 기반 12개 통합 테스트 클래스(56개 테스트 메서드)는 로컬 Docker 인증서 문제로 **미검증(환경 제약)** 처리, 실패 집계에서 제외

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-RUP-001 | PASS | `./gradlew clean build -x test` → `BUILD SUCCESSFUL in 9s` (compileJava/processResources/classes/bootJar/jar/assemble/check/build 전 태스크 성공). Lombok(JDK25 애노테이션 프로세싱) 정상 동작(경고는 `sun.misc.Unsafe` deprecation 뿐, 컴파일 에러 없음). 추가로 `./gradlew compileTestJava` 별도 실행해 테스트 소스 컴파일도 확인 → `BUILD SUCCESSFUL in 6s` | 표준출력 캡처(본문 하단) |
| TC-RUP-002 | PASS (일부 미검증) | `./gradlew test` 전체 실행 → `BUILD SUCCESSFUL in 7m 2s`. JUnit XML 집계: 총 367건 · skipped 56 · failures 0 · errors 0. skipped 56건은 전량 `@Testcontainers(disabledWithoutDocker = true)`가 붙은 12개 `*IntegrationTest` 클래스(Asset/Auth 2종/Change/Compliance/Esm/Incident/InfraMonitoring/Knowledge/Problem/Search/Srm/Vulnerability)로, Testcontainers가 Docker 미가용을 감지해 정상적으로 skip 처리(에러 아님). 나머지 311건(순수 단위 테스트: 서비스/도메인 로직, jjwt 토큰 발급·검증 등)은 전원 통과 | `build/test-results/test/TEST-*.xml` 38개 파일 집계 |
| TC-RUP-003 | PASS | `source/backend/build.gradle`: `org.springframework.boot` plugin `4.1.0`, Java toolchain `25`, `springdoc-openapi-starter-webmvc-ui:3.0.3`, `jjwt-api/-impl/-jackson:0.12.7` — 설계 문서(§2.1) 변경 후 값과 일치. `gradle-wrapper.properties`: `distributionUrl=...gradle-9.6.1-bin.zip` — 8.14 이상(설계 문서 §2.2 조건 충족) | build.gradle:1-53, gradle-wrapper.properties:3 |
| TC-RUP-004 | PASS | `source/db/docker/docker-compose.yml`의 `image:` 값이 `postgres:18-alpine`로 반영됨(설계 문서 §2.3과 일치). 실제 컨테이너 기동은 환경 제약으로 생략 | docker-compose.yml:3 |

## 미검증 항목 (환경 제약)
- **Testcontainers 기반 통합 테스트 실제 실행**(56개 테스트 메서드, 12개 클래스): 로컬 Docker 데몬이 `registry-1.docker.io` TLS 인증서 검증 실패로 이미지 pull 불가 → Docker 자동 skip으로 대체 확인, 실제 PostgreSQL 18 컨테이너 대상 실행은 미검증
- **`docker-compose up`을 통한 PostgreSQL 18 실제 기동 및 `source/db/sql` 초기화 스크립트(01~31번) 자동 실행 확인**(설계 문서 §5-1): 환경 제약으로 미검증
- **애플리케이션 기동 후 `/swagger-ui/index.html` 정상 응답 확인**(설계 문서 §5-3): 애플리케이션 기동에 DB 연결이 필요해 위 Docker 제약과 동일 사유로 미검증

## 실패 항목 분석
- 없음 (실패 0건)

## 참고 사항 (실패는 아니나 발견한 사항)
- `source/db/docker/CLAUDE.md`가 아직 "PostgreSQL 16(alpine) 컨테이너"로 기술되어 있어 실제 `docker-compose.yml`(postgres:18-alpine)과 불일치. 코드/설정 변경은 아니므로 실패로 분류하지 않으나, 문서 정합성을 위해 갱신 권장
