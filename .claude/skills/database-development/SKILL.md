---
name: database-development
description: Database 개발 표준. 개인정보 양방향 암호화(비밀번호는 단방향), docker-compose 기반 local DB 컨테이너, DDL/DML의 source/db/sql 저장 규칙. DB MCP 또는 test code로 검증한다.
---

# Database 개발

테이블 정의서(`docs/02_plan/database/*`)를 기반으로 DB를 구성·구현한다.

## MCP

- 사용 Database가 **공식 MCP를 지원하면 사용**한다. (예: PostgreSQL, MySQL, Supabase)
- MCP 사용이 불가하면 **test code를 작성하여 테스트**한다.

## 핵심 규칙

- **개인정보는 양방향 암호화**(복호화 필요), **비밀번호는 단방향 해시**로 저장한다.
- 테이블 정의서의 규칙(snake_case, 공통 컬럼, 제약조건, RBAC 매핑 테이블)을 준수한다.
- 서비스를 local에서 serve하기 위해 **docker-compose로 DB를 컨테이너로 띄운다.**
- `create table`, `insert data` 등 프로젝트 세팅용 **DDL/DML은 `source/db/sql/`에 `.sql` 파일로 저장**한다.
- docker-compose 파일은 `source/db/docker/`에 둔다.

## docs

상세 컨벤션은 [references/conventions.md](references/conventions.md)를 따른다.
