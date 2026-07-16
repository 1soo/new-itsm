# 테이블 정의서 — 변경 관리 (Change)

> 도메인: change · 버전: 0.2 · 작성일: 2026-07-11 · 승인 프로세스 커스텀 기능(유지보수 요청) 반영 — 위험도 기반 CAB 자동 라우팅 제거, `change_request.approval_route` 컬럼 삭제

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |

변경 요청(RFC), 표준 변경 템플릿, 영향 시스템을 정의한다. 승인은 [common.md](common.md)의 `approval_process`/`approval_request` 커스텀 승인 엔진(전 도메인 공용), 인시던트/문제 연계는 `ticket_link`, 타임라인/코멘트는 `timeline_event`/`comment`를 사용한다. 변경 유형(`change_request.type`)은 승인 프로세스의 요청유형 스코프(`approval_process.request_subtype_key`)로도 사용된다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| change_affected_system | 1NF | 영향 시스템은 다건이라 반복 그룹을 별도 행으로 분리. |
| change_request ↔ change_template | 3NF | 표준 변경 사전승인 템플릿을 참조로 분리(재사용·재승인 불필요 판정). |
| 구현 결과(outcome/rolled_back) | 비정규 컬럼 | 변경당 1회성 결과라 별도 테이블 없이 change_request에 컬럼 보관(조회 단순화). |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| change_template | 표준 변경 사전승인 템플릿 | REQ-CHG-006 |
| change_request | 변경 요청(RFC) | REQ-CHG-001~008 |
| change_affected_system | 변경 영향 시스템 | REQ-CHG-001 |

## 4. 테이블 상세

### change_template

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(150) | UNIQUE, NOT NULL | 템플릿명 |
| description | VARCHAR(500) | NULL | 설명 |
| ...공통 컬럼... | | | |

### change_request

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| ticket_key | VARCHAR(20) | UNIQUE, NOT NULL | CHG-YYYY-#### |
| summary | VARCHAR(300) | NOT NULL | 요약(필수) |
| description | TEXT | NULL | 설명 |
| type | VARCHAR(15) | NOT NULL | STANDARD/NORMAL/EMERGENCY |
| risk | VARCHAR(10) | NULL | HIGH/MEDIUM/LOW(미평가 시 NULL) |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'REQUESTED' | REQUESTED/REVIEW/PLANNING/APPROVAL/IMPLEMENTATION/CLOSED |
| implementation_plan | TEXT | NULL | 예상 구현 |
| rollback_plan | TEXT | NULL | 롤백 방법 |
| scheduled_at | TIMESTAMPTZ | NULL | 예정 일시(캘린더) |
| template_id | BIGINT | FK → change_template.id, NULL | 표준 변경 템플릿 |
| outcome | VARCHAR(10) | NULL | SUCCESS/FAILURE(구현 결과) |
| rolled_back | BOOLEAN | NULL | 롤백 여부 |
| result_note | VARCHAR(500) | NULL | 결과 비고 |
| ...공통 컬럼... | | | |

### change_affected_system

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| change_id | BIGINT | FK → change_request.id, NOT NULL | 소속 변경 |
| system_name | VARCHAR(150) | NOT NULL | 영향 시스템명 |
| ...공통 컬럼... | | | |

## 5. RBAC · 화면 관리 테이블

[auth.md](auth.md) 5절 참조. 관련 화면: SCR-CHG-001~006.

## 6. 관계 · 제약조건 요약

- change_request.template_id → change_template.id (FK)
- change_affected_system.change_id → change_request.id (FK)
- scheduled_at 인덱스 권장(변경 일정 캘린더 조회)
- 승인은 common.approval_process(domain='CHANGE', request_subtype_key=change_request.type)로 매칭된 규칙에 따라 common.approval_request(ticket_type='CHANGE')가 생성, 인시던트/문제 연계는 common.ticket_link(source_type='CHANGE')
