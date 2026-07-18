# 테이블 정의서 — 서비스 요청 관리 (Service Request)

> 도메인: service-request · 버전: 0.5

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |
| 2026-07-12 | 카탈로그 항목별 승인 필드(approval_required/approver_role) 제거 |
| 2026-07-15 | service_catalog_item.assignee_role_id 신규 |
| 2026-07-16 | service_catalog_category 신규, service_catalog_item.category → category_id(FK) 전환, catalog_form_field.field_type에 textarea 추가 |
| 2026-07-17 | catalog_form_field·service_request_form_value(EAV) 폐기, service_catalog_item.form_schema·service_request.form_values(JSONB) 신규 |
| 2026-07-18 | queue 테이블 및 service_catalog_item.queue_id/service_request.queue_id 제거, 요청 분류를 카테고리로 일원화 |
| 2026-07-18 | form.io 완전 제거, service_catalog_item.form_schema를 자체 8×n 그리드 스키마로 전면 재정의(컬럼 타입 JSONB 유지, 내부 구조만 교체). 기존 저장된 form.io 스키마는 빈 그리드로 리셋 |
| 2026-07-18 | form_schema 컴포넌트 type에 label 추가(정적 텍스트 전용, 8종). 입력 컴포넌트 type에서 label 속성 제거(컬럼 타입·기본값 변경 없음, 내부 JSON 구조만 조정) |
| 2026-07-18 | form_schema 입력 컴포넌트에 input.placeholder 필드 추가, 컴포넌트 type에 guide 추가(정적 안내/가이드+첨부파일, 9종). 첨부 가이드 파일은 base64 인라인으로 form_schema JSONB 내부에 저장(신규 컬럼·테이블 없음, 컬럼 타입 변경 없음) |
| 2026-07-18 | form_schema radio/checkbox 컴포넌트에 optionsDirection/optionsGap 필드 추가(컬럼 타입 변경 없음), guide 타입을 guide-text/guide-file 2종으로 분리(guide 타입 폐기, 팔레트 10종, 운영 데이터 없어 마이그레이션 불필요) |

서비스 카탈로그(요청 유형·동적 양식), 서비스 요청, 동적 양식 값, CSAT를 정의한다. 승인은 [common.md](common.md)의 `approval_process`/`approval_request` 커스텀 승인 엔진(전 도메인 공용), 코멘트는 `comment`를 사용한다. 카탈로그 항목(`service_catalog_item.id`)은 승인 프로세스의 요청유형 스코프(`approval_process.request_subtype_key`)로도 사용된다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| service_catalog_item.form_schema | 의도적 비정규화(JSON 문서) | 8×n 그리드 폼은 컴포넌트별 위치(그리드 좌표)·크기(칸 수)·Content 설정을 함께 갖는 배열 구조라 EAV(행의 집합)로는 배치 정보를 표현할 수 없다. 폼 전체를 하나의 JSON 문서로 저장해 자체 그리드 빌더/렌더러가 가공 없이 그대로 소비하도록 한다. |
| service_request.form_values | 의도적 비정규화(JSON 문서) | 제출 데이터는 필드별 집계·통계 로직이 없어 EAV로 분해해 얻는 실익이 없다. 컴포넌트 `key` 기준 key-value JSON을 가공 없이 그대로 저장해 조회 시 재조립 비용을 없앤다. |
| service_catalog_item ↔ service_catalog_category | 3NF | 카테고리를 자유 텍스트로 두면 표기 불일치(오타·중복 표현)가 생기므로 고정 목록 테이블로 분리. |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일. 이하 도메인 컬럼만 기술.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| service_catalog_category | 카탈로그 카테고리 고정 목록 | REQ-SRM-001 |
| service_catalog_item | 요청 유형(카탈로그 항목, 동적 양식 스키마 포함) | REQ-SRM-001 |
| service_request | 서비스 요청 티켓(양식 제출 데이터 포함) | REQ-SRM-002~008 |
| csat | 만족도 평가 | REQ-SRM-010 |

## 4. 테이블 상세

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
| category_id | BIGINT | FK → service_catalog_category.id, NULL | 카탈로그 카테고리(미지정 시 미분류) |
| assignee_role_id | BIGINT | FK → role.id, NULL | 담당자 역할(선택). 지정 시 상담원이 라우팅/배정 시점에 이 역할 보유자 후보 목록 중 수동으로 담당자를 선택하는 데 사용(자동배정 아님). 미지정이면 본인 배정만 가능 |
| sla_response_minutes | INT | NULL | 응답 SLA(분) |
| sla_resolve_minutes | INT | NULL | 해결 SLA(분) |
| form_schema | JSONB | NOT NULL, DEFAULT `{"components":[]}` | 동적 양식 스키마(자체 8×n 그리드 — `components` 배열, 각 컴포넌트는 `type`(text/textarea/select/radio/checkbox/date/file/label/guide-text/guide-file, 10종)·`key`·`position`{col,row}·`size`{w,h}·Content 설정을 포함. 7개 입력 타입은 `input.placeholder` 필드를 갖고, `radio`/`checkbox`는 추가로 `optionsDirection`(기본값 row)·`optionsGap`(기본값 1)을 갖는다. `label`/`guide-text` 타입은 표시 텍스트·정렬만 가지며, `guide-file` 타입은 첨부 가이드 파일(base64 데이터 URL 인라인)만 가진다 — 셋 다 값 입력이 없어 제출 데이터(`service_request.form_values`) 대상에서 제외된다(기존 `guide`(텍스트+파일 통합) 타입은 폐기, 운영 데이터 없어 마이그레이션 불필요). 상세 스키마는 [api_spec/service-request.md](../api_spec/service-request.md) API-SRM-002 참조). SCR-SRM-007 "Form 설정" 팝업(자체 그리드 빌더, [screen/service-request.md](../screen/service-request.md) 5절)이 편집·저장 |
| ...공통 컬럼... | | | |

> **기존 데이터 리셋**: 이전 form.io Form JSON은 신규 그리드 스키마와 구조가 호환되지 않아 자동 마이그레이션이 불가능하다. 배포 시 `service_catalog_item.form_schema`의 모든 로우를 초기값(`{"components":[]}`)으로 리셋한다(사용자 승인 완료). 프로세스 오너가 배포 후 각 항목의 폼을 새 빌더로 다시 구성해야 한다.

### service_request

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| ticket_key | VARCHAR(20) | UNIQUE, NOT NULL | SRM-YYYY-#### |
| catalog_item_id | BIGINT | FK → service_catalog_item.id, NOT NULL | 요청 유형 |
| requester_id | BIGINT | FK → app_user.id, NOT NULL | 요청자 |
| assignee_id | BIGINT | FK → app_user.id, NULL | 담당 상담원 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'SUBMITTED' | SUBMITTED/VALIDATED/ROUTED/APPROVAL_PENDING/IN_FULFILLMENT/FULFILLED/CLOSED/REJECTED |
| sla_response_due | TIMESTAMPTZ | NULL | 응답 SLA 기한 |
| sla_resolve_due | TIMESTAMPTZ | NULL | 해결 SLA 기한 |
| sla_status | VARCHAR(10) | NOT NULL, DEFAULT 'OK' | OK/WARNING/BREACHED |
| form_values | JSONB | NOT NULL, DEFAULT `{}` | 양식 제출 데이터(key=컴포넌트 `key`인 key-value 맵, 레이아웃 무관). SCR-SRM-002 그리드 렌더러 제출 시 저장 |
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

[auth.md](auth.md) 5절 참조(단일 원천). 관련 화면: SCR-SRM-001~005/007~009, SCR-COM-007/008.

## 6. 관계 · 제약조건 요약

- service_catalog_item.category_id → service_catalog_category.id (FK). 참조 중인 카탈로그 항목이 있으면 해당 카테고리 삭제는 애플리케이션이 409로 거부(자동 NULL 처리 없음)
- service_catalog_item.assignee_role_id → role.id (FK, [auth.md](auth.md) `role` 테이블 참조)
- service_catalog_item.form_schema는 JSONB 컬럼(관계형 FK 없음, 폼 전체가 카탈로그 항목 한 행에 저장)
- service_request.catalog_item_id → service_catalog_item.id, requester_id/assignee_id → app_user.id (FK)
- 요청의 카테고리는 별도 컬럼 없이 `service_request.catalog_item_id → service_catalog_item.category_id`를 실시간 조인해 조회한다(스냅샷 없음)
- service_request.form_values는 JSONB 컬럼(관계형 FK 없음, 제출 데이터가 요청 한 행에 저장)
- csat.service_request_id → service_request.id (FK, UNIQUE)
- 승인은 common.approval_process(domain='SERVICE_REQUEST', request_subtype_key=service_catalog_item.id)로 매칭된 규칙에 따라 common.approval_request(ticket_type='SERVICE_REQUEST')가 생성, 코멘트는 common.comment 사용
