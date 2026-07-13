---
name: database-development
description: Database 개발 표준. 개인정보 양방향 암호화(비밀번호는 단방향), docker-compose 기반 local DB 컨테이너, DDL/DML의 source/db/sql 저장 규칙. DB MCP 또는 test code로 검증한다.
---

# Database 개발

테이블 정의서(`docs/02_plan/database/*`) 기반으로 DB 구성·구현.

## MCP

- Database가 **공식 MCP 지원 시 사용**. (예: PostgreSQL, MySQL, Supabase)
- MCP 불가 시 **test code 작성해 테스트**.

## 핵심 규칙

- **개인정보는 양방향 암호화**(복호화 필요), **비밀번호는 단방향 해시** 저장.
- 테이블 정의서 규칙(snake_case, 공통 컬럼, 제약조건, RBAC 매핑 테이블) 준수.
- local serve 위해 **docker-compose로 DB 컨테이너 실행.**
- `create table`, `insert data` 등 세팅용 **DDL/DML은 `source/db/sql/`에 `.sql` 파일로 저장**.
- docker-compose 파일은 `source/db/docker/`에 둔다.

## docs

상세 컨벤션은 [references/conventions.md](references/conventions.md)를 따른다.
