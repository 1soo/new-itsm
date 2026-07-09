# Database 개발 컨벤션

## 1. 암호화 정책

| 대상 | 방식 | 비고 |
|------|------|------|
| 개인정보(이름/연락처/이메일 등) | **양방향 암호화** | 복호화 필요. 대칭키(예: AES-256-GCM), 키는 환경변수/시크릿으로 관리 |
| 비밀번호 | **단방향 해시** | 복호화 불가. bcrypt / Argon2 등, salt 포함 |

- 암호화 대상 컬럼은 테이블 정의서에 표시하고, 저장 시 암호화·조회 시 복호화 계층을 일관 적용.

## 2. 스키마 규칙 (테이블 정의서 준수)

- snake_case 식별자.
- 공통 컬럼: `id`(seq PK), `created_by/at`, `updated_by/at`, `is_deleted`(soft delete).
- FK·UNIQUE·NOT NULL 제약조건, RBAC 매핑 테이블(`user_role`, `screen_role`, `screen`).

## 3. Local 실행 (docker-compose)

- `source/db/docker/docker-compose.yml`로 DB를 컨테이너로 띄운다.
- 접속 정보(포트·계정)는 환경변수로 관리하고, backend `.env`와 정합을 맞춘다.

## 4. SQL 자산

- `source/db/sql/`에 세팅용 SQL을 목적별로 저장. (예: `01_schema.sql`, `02_seed.sql`)
- DDL(create table)과 DML(insert)을 구분하여 관리.

## 5. 검증

- DB MCP가 있으면 MCP로 스키마 생성/쿼리 검증.
- 없으면 test code로 스키마·제약조건·암호화 저장/조회를 검증.
