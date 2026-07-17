# 테이블 정의서 — 서비스 요청 관리 (Service Request)

> 도메인: service-request · 버전: 0.5
>
> **변경 이력**
> - 2026-07-17: 서비스 카탈로그 커스텀 폼 빌더(form.io 스타일 완전 자유배치) 유지보수 요청 — `catalog_form_field`(EAV) 폐기, `service_catalog_item.form_schema`(JSONB, Form.io Form JSON 전체) 신규. `service_request_form_value`(EAV) 폐기, `service_request.form_values`(JSONB, `submission.data` 그대로) 신규. ESM은 [esm.md](esm.md)에 동일 패턴 적용
> - 2026-07-16: `service_catalog_category` 테이블 신규(카탈로그 카테고리 고정 목록), `service_catalog_item.category`(자유 텍스트) → `category_id`(FK) 전환, `catalog_form_field.field_type`에 `textarea` 추가
> - 2026-07-15: `service_catalog_item.assignee_role_id` 신규(자동배정 아님, 라우팅/배정 시점 후보 목록 선정용)
> - 2026-07-12: 카탈로그 항목별 승인 필드(`approval_required`/`approver_role`) 제거

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |

서비스 카탈로그(요청 유형·동적 양식), 큐, 서비스 요청, 동적 양식 값, CSAT를 정의한다. 승인은 [common.md](common.md)의 `approval_process`/`approval_request` 커스텀 승인 엔진(전 도메인 공용), 코멘트는 `comment`를 사용한다. 카탈로그 항목(`service_catalog_item.id`)은 승인 프로세스의 요청유형 스코프(`approval_process.request_subtype_key`)로도 사용된다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| service_catalog_item.form_schema | 의도적 비정규화(JSON 문서) | Form.io 스타일 폼은 컬럼/패널/탭 등 임의 깊이로 중첩되는 트리 구조라 EAV(행의 집합)로는 레이아웃 계층을 표현할 수 없다(2026-07-17 유지보수 요청, 폼 빌더 완전 자유배치 전환). 폼 전체를 하나의 JSON 문서로 저장해 `@formio/react`의 `FormBuilder`/`Form`이 가공 없이 그대로 소비하도록 한다. 기존 `catalog_form_field`(1NF 행 기반 EAV)는 폐기. |
| service_request.form_values | 의도적 비정규화(JSON 문서) | 제출 데이터는 필드별 집계·통계 로직이 없음을 확인(maintainer, 2026-07-17)했으므로 EAV로 분해해 얻는 실익이 없다. Form.io `submission.data`(key-value JSON)를 가공 없이 그대로 저장해 조회 시 재조립 비용을 없앤다. 기존 `service_request_form_value`(EAV)는 폐기. |
| service_catalog_item ↔ queue | 3NF | 큐를 참조로 분리하여 큐 정보 중복 제거. |
| service_catalog_item ↔ service_catalog_category | 3NF | 카테고리를 자유 텍스트로 두면 표기 불일치(오타·중복 표현)가 생기므로 고정 목록 테이블로 분리. |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일. 이하 도메인 컬럼만 기술.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| queue | 처리 큐 | REQ-SRM-004/006 |
| service_catalog_category | 카탈로그 카테고리 고정 목록 | REQ-SRM-001 |
| service_catalog_item | 요청 유형(카탈로그 항목, 동적 양식 스키마 포함) | REQ-SRM-001 |
| ~~catalog_form_field~~ | ~~요청 유형 동적 양식 필드~~ — **제거됨**, `service_catalog_item.form_schema`(JSONB)로 흡수(2026-07-17 유지보수 요청) | REQ-SRM-001/002 |
| service_request | 서비스 요청 티켓(양식 제출 데이터 포함) | REQ-SRM-002~008 |
| ~~service_request_form_value~~ | ~~요청 양식 입력 값~~ — **제거됨**, `service_request.form_values`(JSONB)로 흡수(2026-07-17 유지보수 요청) | REQ-SRM-002 |
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

### service_catalog_category

카탈로그 항목의 카테고리를 관리자가 통제하는 고정 목록으로 관리한다.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(100) | UNIQUE, NOT NULL | 카테고리명 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 표시 순서(SCR-SRM-001 카드 그룹·SCR-SRM-007/009 select 후보 정렬 기준) |
| ...공통 컬럼... | | | |

### service_catalog_item

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(150) | NOT NULL | 요청 유형명 |
| description | VARCHAR(500) | NULL | 설명 |
| category_id | BIGINT | FK → service_catalog_category.id, NULL | 카탈로그 카테고리 |
| queue_id | BIGINT | FK → queue.id, NULL | 담당 큐(미지정 시 요청 생성 시점에 기본 큐로 배정, 미분류) |
| assignee_role_id | BIGINT | FK → role.id, NULL | 담당자 역할(선택). 지정 시 상담원이 라우팅/배정 시점에 이 역할 보유자 후보 목록 중 수동으로 담당자를 선택하는 데 사용(자동배정 아님). 미지정이면 본인 배정만 가능 |
| sla_response_minutes | INT | NULL | 응답 SLA(분) |
| sla_resolve_minutes | INT | NULL | 해결 SLA(분) |
| form_schema | JSONB | NOT NULL, DEFAULT `{"display":"form","components":[]}` | 동적 양식 스키마(Form.io Form JSON 전체 — `display`·`components` 트리, 컬럼/패널/탭 등 레이아웃 컴포넌트 포함). SCR-SRM-007 폼 빌더(`@formio/react` `FormBuilder`)가 편집·저장(2026-07-17 유지보수 요청, `catalog_form_field` 대체) |
| ...공통 컬럼... | | | |

### ~~catalog_form_field~~ (제거됨, 2026-07-17 유지보수 요청)

`service_catalog_item.form_schema`(JSONB)로 흡수되어 테이블 자체를 삭제한다.

**마이그레이션 방향**: `catalog_item_id`로 그룹핑해 각 행을 `sort_order` 오름차순으로 Form.io 컴포넌트 객체로 변환한다 — `field_key`→`key`, `label`→`label`, `field_type`→`type`(`text`→`textfield`, `textarea`→`textarea`, `select`→`select`(`options`→`data.values[].{label,value}`), `number`→`number`, `date`→`datetime`(`enableTime:false`), `file`→`file`(`storage:'base64'`, Form.io Enterprise 유료 스토리지 프로바이더 없이 제출 데이터에 파일을 base64로 인라인 포함 — 대용량 첨부가 필요해지면 별도 업로드 API 설계 추가 필요)), `required`→`validate.required`, 공통 `input:true`. 변환된 배열을 `{ "display": "form", "components": [...] }`로 감싸 `service_catalog_item.form_schema`에 백필한 뒤 테이블을 삭제한다.

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
| form_values | JSONB | NOT NULL, DEFAULT `{}` | 양식 제출 데이터(Form.io `submission.data` 그대로, key=컴포넌트 `key`). SCR-SRM-002 렌더러(`@formio/react` `Form`) 제출 시 저장(2026-07-17 유지보수 요청, `service_request_form_value` 대체) |
| ...공통 컬럼... | | | |

### ~~service_request_form_value~~ (제거됨, 2026-07-17 유지보수 요청)

`service_request.form_values`(JSONB)로 흡수되어 테이블 자체를 삭제한다.

**마이그레이션 방향**: `service_request_id`로 그룹핑해 `{ field_key: field_value }` 형태의 JSON 객체로 조립한 뒤 `service_request.form_values`에 백필하고 테이블을 삭제한다.

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

- service_catalog_item.category_id → service_catalog_category.id (FK). 참조 중인 카탈로그 항목이 있으면 해당 카테고리 삭제는 애플리케이션이 409로 거부(자동 NULL 처리 없음)
- service_catalog_item.queue_id → queue.id (FK)
- service_catalog_item.assignee_role_id → role.id (FK, [auth.md](auth.md) `role` 테이블 참조)
- service_catalog_item.form_schema는 JSONB 컬럼(관계형 FK 없음, 폼 전체가 카탈로그 항목 한 행에 저장)
- service_request.catalog_item_id → service_catalog_item.id, requester_id/assignee_id → app_user.id, queue_id → queue.id (FK)
- service_request.form_values는 JSONB 컬럼(관계형 FK 없음, 제출 데이터가 요청 한 행에 저장)
- csat.service_request_id → service_request.id (FK, UNIQUE)
- 승인은 common.approval_process(domain='SERVICE_REQUEST', request_subtype_key=service_catalog_item.id)로 매칭된 규칙에 따라 common.approval_request(ticket_type='SERVICE_REQUEST')가 생성, 코멘트는 common.comment 사용
