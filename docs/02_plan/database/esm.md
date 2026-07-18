# 테이블 정의서 — 엔터프라이즈 서비스 관리 (ESM)

> 도메인: esm · 버전: 0.3

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-10 | 최초 작성 |
| 2026-07-16 | esm_catalog_form_field.field_type에 textarea 추가(SRM catalog_form_field와 공유하는 FormFieldType 계약) |
| 2026-07-18 | 문서 정정 — 이전 버전이 실제 구현되지 않은 SRM 스타일 form.io 전환(esm_catalog_item.form_schema/esm_request.form_values JSONB, esm_catalog_form_field/esm_request_form_value 폐기)을 반영하고 있었음을 확인(코드 조사, `source/db/sql/16_esm_schema.sql` 기준). ESM은 이 전환이 적용된 적이 없으므로 실제 구현(레거시 EAV: esm_catalog_form_field/esm_request_form_value 유지)에 맞게 되돌림 |

부서별 서비스 카탈로그(체크리스트 템플릿 포함), 부서 요청, HR 케이스, 온보딩/오프보딩 체크리스트·하위 작업을 정의한다. 코멘트·타임라인(상태 이력)은 [common.md](common.md)의 `comment`/`timeline_event`(ticket_type='ESM_REQUEST' 또는 'HR_CASE')를 재사용한다.

**DB 접근 방식**: JPA(Spring Data JPA) — 기존 7개 코어 도메인과 동일(`source/backend` 기존 구현 확인).

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| esm_catalog_item ↔ esm_catalog_form_field | 1NF(EAV) | 카탈로그 항목별 동적 양식 필드 개수가 가변이라 별도 행으로 분리한다. |
| esm_request ↔ esm_request_form_value | 1NF(EAV) | 요청 제출 값도 필드 개수가 가변이며 필드별 집계·통계 로직이 없어 행 기반 key-value로 충분하다. |
| esm_checklist_template_task | 1NF | 카탈로그 항목당 체크리스트 하위 작업 템플릿이 가변 개수라 별도 행으로 분리. |
| esm_checklist_task | 3NF | 체크리스트(1) : 하위 작업(N) — 하위 작업은 체크리스트 생성 시점에 템플릿을 복제해 개별 상태를 갖는 실행 인스턴스이므로 템플릿 테이블과 분리. |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일. 이하 도메인 컬럼만 기술.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| esm_catalog_item | 부서별 요청 유형(카탈로그 항목) | REQ-ESM-001 |
| esm_catalog_form_field | 요청 유형 동적 양식 필드(EAV) | REQ-ESM-001/002 |
| esm_checklist_template_task | 카탈로그 항목의 체크리스트 하위 작업 템플릿 | REQ-ESM-005/006 |
| esm_request | 부서 요청 티켓 | REQ-ESM-002 |
| esm_request_form_value | 요청 양식 입력 값(EAV) | REQ-ESM-002 |
| esm_hr_case | HR 케이스 | REQ-ESM-003/004 |
| esm_checklist | 온보딩/오프보딩 체크리스트 | REQ-ESM-005/006/007 |
| esm_checklist_task | 체크리스트 하위 작업(실행 인스턴스) | REQ-ESM-005/006/007 |

## 4. 테이블 상세

### esm_catalog_item

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(150) | NOT NULL | 요청 유형명 |
| description | VARCHAR(500) | NULL | 설명 |
| department | VARCHAR(20) | NOT NULL | HR/LEGAL/FACILITIES/FINANCE |
| checklist_template_type | VARCHAR(15) | NOT NULL, DEFAULT 'NONE' | NONE/ONBOARDING/OFFBOARDING |
| ...공통 컬럼... | | | |

### esm_catalog_form_field

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| catalog_item_id | BIGINT | FK → esm_catalog_item.id, NOT NULL | 소속 카탈로그 항목 |
| field_key | VARCHAR(50) | NOT NULL | 필드 식별자 |
| label | VARCHAR(150) | NOT NULL | 필드 라벨 |
| field_type | VARCHAR(20) | NOT NULL | text/select/number/date/file/textarea |
| required | BOOLEAN | NOT NULL, DEFAULT false | 필수 여부 |
| options | JSONB | NULL | select 옵션 목록 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 표시 순서 |
| ...공통 컬럼... | | | |
| | | UNIQUE(catalog_item_id, field_key) | 카탈로그 항목 내 필드 키 중복 방지 |

### esm_checklist_template_task

`checklist_template_type != NONE`인 카탈로그 항목에만 존재. 요청 제출 시 이 템플릿을 복제해 `esm_checklist_task`를 생성한다.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| catalog_item_id | BIGINT | FK → esm_catalog_item.id, NOT NULL | 소속 카탈로그 항목 |
| department | VARCHAR(20) | NOT NULL | 배정 부서 |
| task_description | VARCHAR(300) | NOT NULL | 작업 설명 |
| sort_order | INT | NOT NULL, DEFAULT 0 | 표시 순서 |
| ...공통 컬럼... | | | |

### esm_request

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| ticket_key | VARCHAR(20) | UNIQUE, NOT NULL | ESM-YYYY-#### |
| catalog_item_id | BIGINT | FK → esm_catalog_item.id, NOT NULL | 요청 유형 |
| requester_id | BIGINT | FK → app_user.id, NOT NULL | 요청자 |
| assignee_id | BIGINT | FK → app_user.id, NULL | 담당 처리자 |
| department | VARCHAR(20) | NOT NULL | 담당 부서(카탈로그 항목에서 복제, 필터 성능용) |
| target_user_name | VARCHAR(100) | NULL | 온보딩/오프보딩 대상자명 |
| checklist_id | BIGINT | FK → esm_checklist.id, NULL, UNIQUE | 연계 체크리스트(1:1, 있을 때만) |
| status | VARCHAR(15) | NOT NULL, DEFAULT 'SUBMITTED' | SUBMITTED/IN_PROGRESS/COMPLETED/REJECTED |
| ...공통 컬럼... | | | |

### esm_request_form_value

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| esm_request_id | BIGINT | FK → esm_request.id, NOT NULL | 소속 요청 |
| field_key | VARCHAR(50) | NOT NULL | 필드 식별자(`esm_catalog_form_field.field_key`) |
| field_value | TEXT | NULL | 제출 값 |
| ...공통 컬럼... | | | |
| | | UNIQUE(esm_request_id, field_key) | 요청 내 필드 키 중복 방지 |

### esm_hr_case

민감 정보. RBAC상 HR_CASE_MANAGER 역할만 조회·수정 가능(애플리케이션 레벨 강제, REQ-ESM-004).

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| title | VARCHAR(200) | NOT NULL | 제목 |
| description | TEXT | NULL | 내용 |
| subject_user_name | VARCHAR(100) | NULL | 대상자명 |
| status | VARCHAR(15) | NOT NULL, DEFAULT 'INTAKE' | INTAKE/DOCUMENTATION/INVESTIGATION/RESOLUTION |
| ...공통 컬럼... | | | |

### esm_checklist

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| type | VARCHAR(15) | NOT NULL | ONBOARDING/OFFBOARDING |
| target_user_name | VARCHAR(100) | NOT NULL | 대상자명 |
| status | VARCHAR(15) | NOT NULL, DEFAULT 'IN_PROGRESS' | IN_PROGRESS/COMPLETED |
| ...공통 컬럼... | | | |

### esm_checklist_task

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| checklist_id | BIGINT | FK → esm_checklist.id, NOT NULL | 소속 체크리스트 |
| department | VARCHAR(20) | NOT NULL | 배정 부서 |
| description | VARCHAR(300) | NOT NULL | 작업 설명 |
| status | VARCHAR(10) | NOT NULL, DEFAULT 'PENDING' | PENDING/DONE |
| related_asset_id | BIGINT | FK → asset.id, NULL | 오프보딩 자산 회수 작업의 대상 자산([asset.md](asset.md)) |
| ...공통 컬럼... | | | |

## 5. RBAC · 화면 관리 테이블

[auth.md](auth.md) 5절 참조(단일 원천). 관련 화면: SCR-ESM-001~011.

- 신규 역할 `HR_CASE_MANAGER`는 `role`(auth.md) 테이블에 추가되는 seed 데이터이며, `esm_hr_case` 접근은 애플리케이션 레벨에서 role claim 검증으로 처리한다(테이블 자체에 RBAC 컬럼을 두지 않음, REQ-ESM-004).
- 부서 요청/체크리스트 하위 작업의 "담당 부서" 접근 제어는 사용자-부서 매핑이 필요하다(6절 참고).

## 6. 관계 · 제약조건 요약

- esm_catalog_form_field.catalog_item_id → esm_catalog_item.id (FK), UNIQUE(catalog_item_id, field_key)
- esm_checklist_template_task.catalog_item_id → esm_catalog_item.id (FK)
- esm_request.catalog_item_id → esm_catalog_item.id, requester_id/assignee_id → app_user.id, checklist_id → esm_checklist.id (FK, UNIQUE)
- esm_request_form_value.esm_request_id → esm_request.id (FK), UNIQUE(esm_request_id, field_key)
- esm_checklist_task.checklist_id → esm_checklist.id (FK), related_asset_id → asset.id (FK, nullable)
- 코멘트는 common.comment(ticket_type='ESM_REQUEST'), 상태 이력은 common.timeline_event(ticket_type='ESM_REQUEST' 또는 'HR_CASE') 사용
- **사용자-부서 매핑**: 부서 요청 처리자·체크리스트 하위 작업 담당자의 "소속 부서" 판정을 위해 [auth.md](auth.md)의 `app_user`에 `department` 컬럼을 추가했다(HR/LEGAL/FACILITIES/FINANCE/IT, NULL 허용). 부서 요청 목록/처리 큐·내 하위 작업 목록 조회 시 `app_user.department = esm_request.department`(또는 `esm_checklist_task.department`) 조건으로 필터링한다.
