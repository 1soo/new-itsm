# 테이블 정의서 — 문제 관리 (Problem)

> 도메인: problem · 버전: 0.1 · 작성일: 2026-07-09

문제 티켓, 5 Whys, 알려진 오류(KEDB), 후속 조치를 정의한다. 인시던트/변경 연계는 [common.md](common.md)의 `ticket_link`, 코멘트/타임라인은 `comment`/`timeline_event`를 사용한다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| problem_five_why / problem_action | 1NF | Why 사슬·후속 조치는 가변 개수라 별도 행으로 분리. |
| known_error | 3NF | 알려진 오류(KEDB)는 문제와 1:N이며 키워드 검색 대상이라 독립 테이블. |
| priority(영향도×긴급도) | 비정규 산정값 | 매트릭스 산정 결과를 `priority` 컬럼에 캐시(조회 성능). 입력값(impact/urgency)도 함께 보관해 재산정 가능. |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| problem | 문제 티켓 | REQ-PRB-001~006/010 |
| problem_five_why | RCA 5 Whys 단계 | REQ-PRB-004 |
| known_error | 알려진 오류(KEDB) | REQ-PRB-005 |
| problem_action | 후속(시정) 조치 | REQ-PRB-009 |

## 4. 테이블 상세

### problem

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| ticket_key | VARCHAR(20) | UNIQUE, NOT NULL | PRB-YYYY-#### |
| summary | VARCHAR(300) | NOT NULL | 요약(필수) |
| description | TEXT | NULL | 설명 |
| origin | VARCHAR(10) | NULL | REACTIVE/PROACTIVE |
| investigation_reason | VARCHAR(500) | NULL | 조사 사유 |
| impact | VARCHAR(10) | NULL | HIGH/MEDIUM/LOW |
| urgency | VARCHAR(10) | NULL | HIGH/MEDIUM/LOW |
| priority | VARCHAR(10) | NULL | 산정값(둘 중 하나라도 없으면 NULL=미산정) |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'DETECTION' | DETECTION/CLASSIFICATION/INVESTIGATION/KNOWN_ERROR/WORKAROUND/RESOLVED_CLOSED |
| root_cause | VARCHAR(1000) | NULL | 근본원인(개인 지정 강제 안 함) |
| root_cause_category | VARCHAR(100) | NULL | 근본원인 카테고리 |
| workaround | TEXT | NULL | 워크어라운드 |
| component | VARCHAR(150) | NULL | 구성요소 |
| ...공통 컬럼... | | | |

### problem_five_why

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| problem_id | BIGINT | FK → problem.id, NOT NULL | 소속 문제 |
| step_no | SMALLINT | NOT NULL | Why 순번 |
| content | VARCHAR(500) | NOT NULL | 내용 |
| | | UNIQUE(problem_id, step_no) | |
| ...공통 컬럼... | | | |

### known_error

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| problem_id | BIGINT | FK → problem.id, NOT NULL | 원 문제 |
| title | VARCHAR(300) | NOT NULL | 제목(검색 대상) |
| root_cause | VARCHAR(1000) | NULL | 근본원인 |
| workaround | TEXT | NULL | 워크어라운드 |
| ...공통 컬럼... | | | |

### problem_action

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| problem_id | BIGINT | FK → problem.id, NOT NULL | 소속 문제 |
| description | VARCHAR(500) | NOT NULL | 조치 내용 |
| owner | VARCHAR(100) | NULL | 담당 |
| due_date | DATE | NULL | 기한 |
| status | VARCHAR(15) | NOT NULL, DEFAULT 'IN_PROGRESS' | IN_PROGRESS/DONE |
| ...공통 컬럼... | | | |

## 5. RBAC · 화면 관리 테이블

[auth.md](auth.md) 5절 참조. 관련 화면: SCR-PRB-001~004.

## 6. 관계 · 제약조건 요약

- problem_five_why.problem_id / known_error.problem_id / problem_action.problem_id → problem.id (FK)
- problem_five_why UNIQUE(problem_id, step_no)
- known_error.title 검색 인덱스 권장(KEDB 키워드 조회)
- 인시던트·변경 연계는 common.ticket_link(source_type='PROBLEM'), 지식 워크어라운드 연결도 ticket_link(target_type='KNOWLEDGE')
