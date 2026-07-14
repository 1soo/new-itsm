# Database 개발 컨벤션

테이블 정의서(`docs/02_plan/database/*`) 기반으로 DB 구성·구현.

## MCP

- Database가 **공식 MCP 지원 시 사용**한다. (예: PostgreSQL, MySQL, Supabase)
- MCP 불가 시 **test code 작성해 테스트**한다.

## 핵심 규칙

- **개인정보는 양방향 암호화**(복호화 필요), **비밀번호는 단방향 해시** 저장.
- 테이블 정의서 규칙(snake_case, 공통 컬럼, 제약조건, RBAC 매핑 테이블) 준수.
- **local 환경에서는 docker-compose로 DB 컨테이너를 실행**한다.
- `create table`, `insert data` 등 세팅용 **DDL/DML은 `source/db/sql/`에 `.sql` 파일로 저장**한다.
- docker-compose 파일은 `source/db/docker/`에 둔다.

## 1. 암호화 정책

| 대상 | 방식 | 비고 |
|------|------|------|
| 개인정보(이름/연락처/이메일 등) | **양방향 암호화** | 복호화 필요. 대칭키(예: AES-256-GCM), 키는 환경변수/시크릿으로 관리 |
| 비밀번호 | **단방향 해시** | 복호화 불가. bcrypt / Argon2 등, salt 포함 |

- 암호화 대상 컬럼은 테이블 정의서에 표시하고, 저장 시 암호화·조회 시 복호화 계층을 일관 적용한다.

## 2. 스키마 규칙 (테이블 정의서 준수)

- snake_case 식별자.
- 공통 컬럼: `id`(seq PK), `created_by/at`, `updated_by/at`, `is_deleted`(soft delete).
- FK·UNIQUE·NOT NULL 제약조건, RBAC 매핑 테이블(`user_role`, `screen_role`, `screen`).

## 3. Local 실행 (docker-compose)

- `source/db/docker/docker-compose.yml`로 DB를 컨테이너로 띄운다.
- 접속 정보(포트·계정)는 환경변수로 관리하고, backend `.env`와 정합을 맞춘다.

## 4. SQL 자산

- `source/db/sql/`에 세팅용 SQL을 목적별로 저장한다. (예: `01_schema.sql`, `02_seed.sql`)
- DDL(create table)과 DML(insert)을 구분하여 관리한다.

## Supabase 사용 시

`docs/01_analyze/tech.md`에서 Database/BaaS로 **Supabase**가 지정된 경우 [references/database/supabase/conventions.md](supabase/conventions.md)를 따른다. 인증·DB·File Storage·Realtime·자동 생성 API 중 요구사항에 필요한 기능만 선택적으로 사용한다.

## 검증

- DB MCP가 있으면 MCP로 스키마 생성/쿼리를 검증한다.
- 없으면 test code로 스키마·제약조건·암호화 저장/조회를 검증한다.
