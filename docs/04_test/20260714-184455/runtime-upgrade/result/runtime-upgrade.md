# 통합 테스트 결과 — runtime-upgrade (20260714-184455, 2차 라운드)

## 요약
- 총 3건 · 성공 3 · 실패 0
- 1차 라운드(`docs/04_test/20260714-150102/runtime-upgrade/`)에서 로컬 Docker 인증서 문제로 미검증 처리됐던 3개 항목을 이번 라운드에서 Docker 정상화 후 실제 실행으로 전환 검증함. 1차 라운드의 TC-RUP-001~004(빌드 성공, 정적 검토)는 이번 라운드에서 재수행하지 않았으며 그대로 유효.

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-RUP-005 | PASS | `./gradlew clean test` → `BUILD SUCCESSFUL in 2m 26s`. JUnit XML 집계: 총 367건 · **skipped 0** · failures 0 · errors 0 (1차 라운드 대비 skipped 56→0). 13개 `*IntegrationTest` 클래스(Asset 12, Auth 2종: ApprovalProcessAdminIntegrationTest 2/AuthAdminIntegrationTest 2, Change 7, Compliance 3, Esm 3, Incident 5, InfraMonitoring 5, Knowledge 4, Problem 4, Search 3, Srm 3, Vulnerability 3 = 합계 56건) 전원 실제 실행되어 pass. `AssetIntegrationTest` 소스(`postgres:18-alpine` 이미지 지정, PostgreSQLContainer) 및 실행시간(testsuite 전체 20.72초, 최초 테스트케이스 1.054초)으로 Testcontainers가 실제 postgres:18-alpine 컨테이너를 기동해 테스트했음을 확인(스킵이 아닌 실제 컨테이너 부팅 오버헤드 관측). 나머지 순수 단위 테스트 311건도 함께 재확인, 전원 pass | `build/test-results/test/TEST-*.xml` 39개 파일 집계, `TEST-com.itsm.asset.integration.AssetIntegrationTest.xml`(time="20.72") |
| TC-RUP-006 | PASS | `source/db/docker`에서 `docker compose down -v` → `up -d` 실행, 6회 재시도(약 12초) 만에 healthcheck `healthy` 전환 확인. `docker logs itsm-postgres`에 01~31번 전 스크립트가 `ERROR`/`FATAL` 없이 순차 실행(`CREATE DATABASE`/`CREATE TABLE`/`CREATE INDEX`/`INSERT` 로그만 존재). `\dt` 결과 69개 테이블 생성 확인. 마스터/참조 데이터 정상 적재: app_user 18, role 16, screen 80, service_catalog_item 2, esm_catalog_item 5, vulnerability 4, compliance_requirement 3. 트랜잭션 데이터(asset/incident/problem/change_request/service_request/knowledge_article/approval_process 등)는 설계상 미시딩 대상이라 0건(정상). `infra_metric`/`infra_metric_alert`/`uptime_target`도 0건인데, 이는 `23_infra_monitoring_seed.sql`이 `asset` 테이블을 JOIN 대상으로 삼아 자산이 없으면 자동 스킵되도록 설계된 정상 동작(해당 SQL 파일 주석 및 `source/db/sql/CLAUDE.md`에 명시된 의도된 동작, 결함 아님). 검증 완료 후 `docker compose down`으로 정리(볼륨은 유지) | `docker logs itsm-postgres` 전문 검토, `psql \dt`(69 rows), row count 쿼리 결과 |
| TC-RUP-007 | PASS | TC-RUP-006으로 기동한 PostgreSQL 18 컨테이너에 연결한 상태로 `./gradlew bootRun` 실행 → `Started ItsmApplication in 15.407 seconds`. Hibernate 로그에 `Database version: 18.4`, `Database JDBC URL [jdbc:postgresql://localhost:5432/itsm]` 확인(JDBC 드라이버가 PG18에 정상 연결). `curl http://localhost:8080/swagger-ui/index.html` → HTTP 200, `curl http://localhost:8080/v3/api-docs` → HTTP 200. playwright 새 브라우저 컨텍스트로 `/swagger-ui/index.html` 접속 후 "ITSM" 타이틀 텍스트 렌더링 확인(Swagger UI 정상 로드) | bootRun 로그(Hikari/Hibernate 연결 정보), curl HTTP 200 응답 2건, playwright snapshot |

## 미검증 항목 (환경 제약)
- 없음 — 1차 라운드의 3개 미검증 항목(Testcontainers 실행/docker-compose 기동/Swagger UI 확인)을 모두 실제 실행으로 전환 완료

## 실패 항목 분석
- 없음 (실패 0건)

## 참고 사항 (실패는 아니나 발견한 사항)
- 재검증 시작 시점에 기존 `itsm-postgres` 컨테이너(postgres:18-alpine, 약 2시간 전 기동)가 이미 healthy 상태로 실행 중이었다. Fresh init 실행 자체를 검증하기 위해 `docker compose down -v`로 볼륨을 제거한 뒤 재기동해 01~31 스크립트가 실제로 처음부터 정상 실행되는지 확인했다(설계 문서 §1의 fresh init 허용 방침에 근거).
- 1차 라운드에서 지적된 `source/db/docker/CLAUDE.md`의 "PostgreSQL 16(alpine)" 문구는 현재 확인 결과 이미 "PostgreSQL 18(alpine)"로 수정되어 있어 더 이상 불일치가 없음.
