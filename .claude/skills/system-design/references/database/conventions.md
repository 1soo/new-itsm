# 테이블 정의 컨벤션

요구사항·기능 명세서·화면 설계·역할 설계를 바탕으로 테이블 정의서를 **직관적이고 간결한** markdown으로 작성한다.

## 작성 원칙

- 모든 식별자(테이블명·컬럼명)는 **snake_case**를 사용한다.
- **정규화**: 적용한 정규화 방식(1NF/2NF/3NF 등)과 각 정규화가 필요한 상황을 명시한다. (과도한 정규화로 조회 성능을 해치지 않도록 근거를 함께 기술)
- **공통 컬럼**: 모든 테이블에 아래를 포함한다.
  - `id` (sequence PK, auto increment)
  - `created_by`, `created_at` (등록자 / 등록일)
  - `updated_by`, `updated_at` (수정자 / 수정일)
  - `is_deleted` (soft delete 여부, 기본값 false)
- **관계 · 제약조건**: 테이블 간 relation을 고려하여 FK·UNIQUE·NOT NULL 등 제약조건을 명시한다.
- **DB 접근 방식**: 사용할 ORM/쿼리 매퍼(JPA, MyBatis 등)를 설계자가 결정하여 문서 상단에 명시한다. Backend 개발 에이전트는 이 결정을 그대로 따른다.

## RBAC · 화면 관리 테이블 (필수)

정석 RBAC 2단 매핑으로 구성한다.

- `screen` — 화면 정보 테이블
- `user_role` — 사용자-역할 매핑 테이블 (사용자 ↔ 역할)
- `screen_role` — 역할-화면 매핑 테이블 (역할 ↔ 접근 가능 화면)

## Supabase 사용 시

Backend/Database로 Supabase가 지정된 경우에도 위 규칙(snake_case, 공통 컬럼, RBAC 매핑 테이블)은 동일하게 적용한다. Auth 연동(`auth.users` 참조) 등 Supabase 고유 사항은 `implementation` skill의 `references/database/supabase/conventions.md`를 따른다.

## 양식

표준 양식은 [template.md](template.md)를 사용한다.
