# 테이블 정의서 — 인시던트 관리 (Incident)

> 도메인: incident · 버전: 0.1 · 작성일: 2026-07-09

인시던트 티켓, 대응 역할 배정, 심각도 변경 이력, 포스트모템(5 Whys·조치항목)을 정의한다. 타임라인은 [common.md](common.md)의 `timeline_event`, 문제/자산 링크는 `ticket_link`를 사용한다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| incident_responder | 3NF | 한 인시던트에 복수 대응 역할(Tech Lead·Comms·Scribe)이 붙으므로 매핑 테이블 분리. |
| postmortem_five_why / action_item | 1NF | Why 사슬·조치항목은 가변 개수라 별도 행으로 분리(반복 그룹 제거). |
| severity_history | 3NF | 심각도/우선순위 변경 이력을 별도 테이블에 append하여 현재값과 이력 분리. |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일. 이하 도메인 컬럼만 기술.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| incident | 인시던트 티켓 | REQ-INC-001~007 |
| incident_responder | 대응 역할 배정 | REQ-INC-004 |
| incident_severity_history | 심각도·우선순위 변경 이력 | REQ-INC-002 |
| postmortem | 포스트모템 | REQ-INC-008 |
| postmortem_five_why | 5 Whys 단계 | REQ-INC-008 |
| postmortem_action_item | 조치항목 | REQ-INC-008 |

## 4. 테이블 상세

### incident

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| ticket_key | VARCHAR(20) | UNIQUE, NOT NULL | INC-YYYY-#### |
| summary | VARCHAR(300) | NOT NULL | 요약(필수) |
| description | TEXT | NULL | 설명 |
| severity | VARCHAR(10) | NOT NULL | SEV1/SEV2/SEV3 |
| priority | VARCHAR(10) | NULL | P1~P4(심각도와 독립) |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'NEW' | NEW/IN_PROGRESS/RESOLVED/CLOSED |
| affected_service | VARCHAR(150) | NULL | 영향 서비스 |
| affected_product | VARCHAR(150) | NULL | 영향 제품 |
| impact_start_at | TIMESTAMPTZ | NULL | 영향 시작 |
| detected_at | TIMESTAMPTZ | NULL | 탐지 |
| impact_end_at | TIMESTAMPTZ | NULL | 영향 종료 |
| resolved_at | TIMESTAMPTZ | NULL | 해결 시각 |
| mttd_minutes | INT | NULL | 계산값(미산정 시 NULL) |
| mtta_minutes | INT | NULL | 계산값 |
| mttr_minutes | INT | NULL | 계산값 |
| ...공통 컬럼... | | | |

### incident_responder

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| incident_id | BIGINT | FK → incident.id, NOT NULL | 인시던트 |
| user_id | BIGINT | FK → app_user.id, NOT NULL | 배정 사용자 |
| response_role | VARCHAR(20) | NOT NULL | TECH_LEAD/COMMS/SCRIBE |
| | | UNIQUE(incident_id, user_id, response_role) | |
| ...공통 컬럼... | | | |

### incident_severity_history

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| incident_id | BIGINT | FK → incident.id, NOT NULL | 인시던트 |
| old_severity | VARCHAR(10) | NULL | 이전 심각도 |
| new_severity | VARCHAR(10) | NULL | 변경 심각도 |
| old_priority | VARCHAR(10) | NULL | 이전 우선순위 |
| new_priority | VARCHAR(10) | NULL | 변경 우선순위 |
| ...공통 컬럼... | | | |

### postmortem

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| incident_id | BIGINT | FK → incident.id, UNIQUE, NOT NULL | 대상 인시던트(1:1) |
| summary | TEXT | NULL | 요약 |
| timeline_summary | TEXT | NULL | 타임라인 요약 |
| root_cause | VARCHAR(500) | NOT NULL | 근본원인(필수, 개인 지정 강제 안 함) |
| ...공통 컬럼... | | | |

### postmortem_five_why

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| postmortem_id | BIGINT | FK → postmortem.id, NOT NULL | 소속 포스트모템 |
| step_no | SMALLINT | NOT NULL | Why 순번 |
| content | VARCHAR(500) | NOT NULL | 내용 |
| | | UNIQUE(postmortem_id, step_no) | |
| ...공통 컬럼... | | | |

### postmortem_action_item

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| postmortem_id | BIGINT | FK → postmortem.id, NOT NULL | 소속 포스트모템 |
| description | VARCHAR(500) | NOT NULL | 조치 내용 |
| owner | VARCHAR(100) | NULL | 담당 |
| due_date | DATE | NULL | 기한 |
| status | VARCHAR(10) | NOT NULL, DEFAULT 'OPEN' | OPEN/DONE |
| ...공통 컬럼... | | | |

## 5. RBAC · 화면 관리 테이블

[auth.md](auth.md) 5절 참조. 관련 화면: SCR-INC-001~005.

## 6. 관계 · 제약조건 요약

- incident_responder.incident_id → incident.id, user_id → app_user.id (FK), UNIQUE(incident_id, user_id, response_role)
- incident_severity_history.incident_id → incident.id (FK)
- postmortem.incident_id → incident.id (FK, UNIQUE 1:1)
- postmortem_five_why.postmortem_id / postmortem_action_item.postmortem_id → postmortem.id (FK)
- 문제/자산 연계는 common.ticket_link(source_type='INCIDENT'), 타임라인은 common.timeline_event 사용
