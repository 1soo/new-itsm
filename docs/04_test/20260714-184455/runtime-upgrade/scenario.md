# 통합 테스트 시나리오 — runtime-upgrade (2차 라운드, 환경 제약 해소 후 재검증)

## 사전 조건
- 1차 라운드(`docs/04_test/20260714-150102/runtime-upgrade/`)에서 로컬 Docker의 registry-1.docker.io TLS 인증서 검증 실패로 미검증 처리된 3개 항목을 재검증한다.
- 재검증 시점 기준 로컬 Docker Desktop이 정상 동작 확인됨(`docker info` 성공).
- 빌드 테스트(`./gradlew clean build -x test`)는 1차 라운드에서 이미 PASS 확인됨(변경 없음) — 이번 라운드에서 재수행하지 않음.
- 신규 화면/API/DB 스키마 변경 없음(인프라 성격 유지보수) — 근거: @docs/02_plan/upgrade/backend-db-runtime-upgrade.md §1

## 시나리오

### TC-RUP-005 · Testcontainers 기반 통합 테스트 실제 실행(Docker 가용 상태)
- 근거: @docs/02_plan/upgrade/backend-db-runtime-upgrade.md §5-4(기존 통합 테스트 전체 통과), §4(PostgreSQL JDBC 드라이버 PG18 미지원 리스크)
- 전제: 1차 라운드에서 Docker 미가용으로 자동 skip 처리된 12개 도메인(13개 클래스, Auth 2종 포함)·56개 테스트 메서드
- 절차:
  1. `source/backend`에서 `./gradlew clean test` 실행
  2. `build/test-results/test/*.xml` 집계로 skipped 건수, IntegrationTest 13개 클래스의 tests/failures/errors 확인
  3. 개별 테스트 실행 시간(`time` 속성)으로 실제 컨테이너 기동 여부(수 초~수십 초 소요) 간접 확인
- 기대 결과: skipped=0(더 이상 자동 skip 아님), 13개 IntegrationTest 클래스 56개 테스트 메서드 전원 실제 실행되어 pass. 순수 단위 테스트(311건) 포함 총 367건 전원 pass

### TC-RUP-006 · docker-compose 실제 기동 및 초기화 스크립트(01~31) 적재 검증
- 근거: @docs/02_plan/upgrade/backend-db-runtime-upgrade.md §2.3(postgres:18-alpine), §2.4(01~31 fresh init 허용), §5-1
- 전제: 로컬 개발 데이터 이관 불요(fresh init 승인됨) — 근거: 설계 문서 §1
- 절차:
  1. `source/db/docker`에서 `docker compose down -v`로 기존 컨테이너·볼륨 완전 제거(fresh init 조건 확보)
  2. `docker compose up -d` 실행 후 healthcheck(`pg_isready`)가 healthy 될 때까지 대기
  3. `docker logs itsm-postgres`에서 01~31번 스크립트가 에러 없이 순차 실행됐는지 확인
  4. `psql`로 테이블 목록(`\dt`) 및 도메인별 마스터/참조 데이터(app_user, role, screen, service_catalog_item, esm_catalog_item, vulnerability, compliance_requirement 등) row count 확인
  5. 확인 완료 후 `docker compose down`으로 정리
- 기대 결과: postgres:18-alpine 컨테이너 정상 기동(healthy), 01~31 스크립트 전체 에러 없이 실행, 설계상 시딩 대상인 마스터/참조 데이터 정상 적재. 트랜잭션 데이터(asset/incident/problem 등)는 설계상 미시딩 대상이므로 0건이 정상이며, 이에 의존하는 파생 INSERT(예: infra_metric 등)도 자산 부재 시 스킵되는 것이 정상(설계 문서 및 `23_infra_monitoring_seed.sql` 주석 근거)

### TC-RUP-007 · 애플리케이션 기동 및 Swagger UI 정상 응답 확인
- 근거: @docs/02_plan/upgrade/backend-db-runtime-upgrade.md §5-3, §4(springdoc 2.x↔3.x API 불일치 리스크)
- 전제: TC-RUP-006으로 기동한 PostgreSQL 18 컨테이너에 연결 가능한 상태
- 절차:
  1. `source/backend`에서 `./gradlew bootRun` 실행, `Started ItsmApplication` 로그 확인
  2. 로그에서 Hibernate가 인식한 `Database version`이 18.x인지 확인
  3. `curl`로 `/swagger-ui/index.html`, `/v3/api-docs` HTTP 상태코드 확인
  4. playwright(새 브라우저 컨텍스트)로 `/swagger-ui/index.html` 접속, 화면 렌더링(API 문서 타이틀 노출) 확인
- 기대 결과: 애플리케이션이 PostgreSQL 18에 정상 연결되어 기동, `/swagger-ui/index.html`·`/v3/api-docs` 모두 HTTP 200, 브라우저에서 Swagger UI 정상 렌더링
