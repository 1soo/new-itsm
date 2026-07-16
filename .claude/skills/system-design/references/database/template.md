# 테이블 정의서 — {도메인명}

> 도메인: {domain} · 버전: 0.1 · 작성일: {YYYY-MM-DD}
> DB 접근 방식: {JPA / MyBatis 등} — {선택 근거}

## 변경 이력

| 날짜 | 요약 |
|------|------|
| {YYYY-MM-DD} | 최초 작성 |

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| {테이블/관계} | 3NF | {이 상황에서 이 정규화가 필요한 이유} |

## 2. 공통 컬럼 규칙

모든 테이블에 아래 컬럼을 포함한다. (snake_case)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | sequence PK |
| created_by | VARCHAR | NOT NULL | 등록자 |
| created_at | TIMESTAMP | NOT NULL | 등록일 |
| updated_by | VARCHAR | NULL | 수정자 |
| updated_at | TIMESTAMP | NULL | 수정일 |
| is_deleted | BOOLEAN | NOT NULL, DEFAULT false | soft delete 여부 |

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| {table_name} | {설명} | REQ-... |

## 4. 테이블 상세

### {table_name}

{테이블 설명}

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | sequence PK |
| {column} | {type} | {NOT NULL / FK → other_table.id / UNIQUE} | {설명} |
| ...공통 컬럼... | | | (created_by/at, updated_by/at, is_deleted) |

## 5. RBAC · 화면 관리 테이블

### screen

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | 화면 sequence PK |
| screen_code | VARCHAR | UNIQUE, NOT NULL | 화면 식별 코드 |
| screen_name | VARCHAR | NOT NULL | 화면명 |
| path | VARCHAR | NOT NULL | 화면 경로 |

### user_role (사용자-역할 매핑)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| user_id | BIGINT | FK → user.id, NOT NULL | 사용자 |
| role_id | BIGINT | FK → role.id, NOT NULL | 역할 |
| | | UNIQUE(user_id, role_id) | 중복 방지 |

### screen_role (역할-화면 매핑)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| screen_id | BIGINT | FK → screen.id, NOT NULL | 화면 |
| role_id | BIGINT | FK → role.id, NOT NULL | 역할 |
| | | UNIQUE(screen_id, role_id) | 중복 방지 |

## 6. 관계 · 제약조건 요약

- {table_a}.{col} → {table_b}.id (FK)
- {제약조건 목록}
