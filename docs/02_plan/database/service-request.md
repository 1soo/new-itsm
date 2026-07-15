# 테이블 정의서 — 서비스 요청 관리 (Service Request)

> 도메인: service-request · 버전: 0.3 · 작성일: 2026-07-15 · 요청 유형별 담당자 역할 지정 기능(유지보수 요청) 반영 — `service_catalog_item.assignee_role_id` 신규(자동배정 아님, 라우팅/배정 시점 후보 목록 선정용)
>
> 이전 버전: 승인 프로세스 커스텀 기능(유지보수 요청) 반영 — 카탈로그 항목별 승인 필드(`approval_required`/`approver_role`) 제거

서비스 카탈로그(요청 유형·동적 양식), 큐, 서비스 요청, 동적 양식 값, CSAT를 정의한다. 승인은 [common.md](common.md)의 `approval_process`/`approval_request` 커스텀 승인 엔진(전 도메인 공용), 코멘트는 `comment`를 사용한다. 카탈로그 항목(`service_catalog_item.id`)은 승인 프로세스의 요청유형 스코프(`approval_process.request_subtype_key`)로도 사용된다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| catalog_form_field | 1NF | 요청 유형별 동적 양식 필드는 가변 개수라 카탈로그 항목에서 별도 행으로 분리(반복 그룹 제거). |
| service_request_form_value | 1NF·EAV | 요청별 양식 값도 필드 수가 유형마다 달라 (요청, 필드키, 값) EAV로 저장. |
| service_catalog_item ↔ queue | 3NF | 큐를 참조로 분리하여 큐 정보 중복 제거. |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일. 이하 도메인 컬럼만 기술.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| queue | 처리 큐 | REQ-SRM-004/006 |
| service_catalog_item | 요청 유형(카탈로그 항목) | REQ-SRM-001 |
| catalog_form_field | 요청 유형 동적 양식 필드 | REQ-SRM-001/002 |
| service_request | 서비스 요청 티켓 | REQ-SRM-002~008 |
| service_request_form_value | 요청 양식 입력 값 | REQ-SRM-002 |
| csat | 만족도 평가 | REQ-SRM-010 |

## 4. 테이블 상세

### queue

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(100) | UNIQUE, NOT NULL | 큐명 |
| description | VARCHAR(255) | NULL | 설명 |
| is_default | BOOLEAN | NOT NULL, DEFAULT false | 미분류 기본 큐 여부 |
| ...공통 컬럼... | | | |

### service_catalog_item

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(150) | NOT NULL | 요청 유형명 |
| description | VARCHAR(500) | NULL | 설명 |
| category | VARCHAR(100) | NULL | 카탈로그 그룹 |
| queue_id | BIGINT | FK → queue.id, NULL | 담당 큐(미지정 시 요청 생성 시점에 기본 큐로 배정, 미분류) |
| assignee_role_id | BIGINT | FK → role.id, NULL | 담당자 역할(2026-07-15 유지보수 요청, 선택). 지정 시 상담원이 라우팅/배정 시점에 이 역할 보유자 후보 목록 중 수동으로 담당자를 선택하는 데 사용(자동배정 아님). 미지정이면 기존과 동일하게 본인 배정만 가능 |
| sla_response_minutes | INT | NULL | 응답 SLA(분) |
| sla_resolve_minutes | INT | NULL | 해결 SLA(분) |
| ...공통 컬럼... | | | |

### catalog_form_field

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| catalog_item_id | BIGINT | FK → service_catalog_item.id, NOT NULL | 소속 요청 유형 |
| field_key | VARCHAR(50) | NOT NULL | 필드 키 |
| label | VARCHAR(150) | NOT NULL | 표시 라벨 |
| field_type | VARCHAR(20) | NOT NULL | text/select/number/date/file |
| required | BOOLEAN | NOT NULL, DEFAULT false | 필수 여부 |
| options | JSONB | NULL | select 옵션 목록 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 표시 순서 |
| | | UNIQUE(catalog_item_id, field_key) | 키 중복 방지 |
| ...공통 컬럼... | | | |

### service_request

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| ticket_key | VARCHAR(20) | UNIQUE, NOT NULL | SRM-YYYY-#### |
| catalog_item_id | BIGINT | FK → service_catalog_item.id, NOT NULL | 요청 유형 |
| requester_id | BIGINT | FK → app_user.id, NOT NULL | 요청자 |
| assignee_id | BIGINT | FK → app_user.id, NULL | 담당 상담원 |
| queue_id | BIGINT | FK → queue.id, NULL | 배정 큐 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'SUBMITTED' | SUBMITTED/VALIDATED/ROUTED/APPROVAL_PENDING/IN_FULFILLMENT/FULFILLED/CLOSED/REJECTED |
| sla_response_due | TIMESTAMPTZ | NULL | 응답 SLA 기한 |
| sla_resolve_due | TIMESTAMPTZ | NULL | 해결 SLA 기한 |
| sla_status | VARCHAR(10) | NOT NULL, DEFAULT 'OK' | OK/WARNING/BREACHED |
| ...공통 컬럼... | | | |

### service_request_form_value

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| service_request_id | BIGINT | FK → service_request.id, NOT NULL | 소속 요청 |
| field_key | VARCHAR(50) | NOT NULL | 양식 필드 키 |
| field_value | TEXT | NULL | 입력 값 |
| | | UNIQUE(service_request_id, field_key) | |
| ...공통 컬럼... | | | |

### csat

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| service_request_id | BIGINT | FK → service_request.id, UNIQUE, NOT NULL | 대상 요청(1:1) |
| score | SMALLINT | NOT NULL, CHECK (score BETWEEN 1 AND 5) | 만족도 점수 |
| comment | VARCHAR(500) | NULL | 코멘트 |
| ...공통 컬럼... | | | |

## 5. RBAC · 화면 관리 테이블

[auth.md](auth.md) 5절 참조(단일 원천). 관련 화면: SCR-SRM-001~008, SCR-COM-007/008.

## 6. 관계 · 제약조건 요약

- service_catalog_item.queue_id → queue.id (FK)
- service_catalog_item.assignee_role_id → role.id (FK, [auth.md](auth.md) `role` 테이블 참조)
- catalog_form_field.catalog_item_id → service_catalog_item.id (FK), UNIQUE(catalog_item_id, field_key)
- service_request.catalog_item_id → service_catalog_item.id, requester_id/assignee_id → app_user.id, queue_id → queue.id (FK)
- service_request_form_value.service_request_id → service_request.id (FK), UNIQUE(service_request_id, field_key)
- csat.service_request_id → service_request.id (FK, UNIQUE)
- 승인은 common.approval_process(domain='SERVICE_REQUEST', request_subtype_key=service_catalog_item.id)로 매칭된 규칙에 따라 common.approval_request(ticket_type='SERVICE_REQUEST')가 생성, 코멘트는 common.comment 사용
