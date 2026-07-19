# 테이블 정의서 — 엔터프라이즈 서비스 관리 (ESM)

> 도메인: esm · 버전: 0.4

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-10 | 최초 작성 |
| 2026-07-16 | esm_catalog_form_field.field_type에 textarea 추가(SRM catalog_form_field와 공유하는 FormFieldType 계약) |
| 2026-07-18 | 문서 정정 — 이전 버전이 실제 구현되지 않은 SRM 스타일 form.io 전환(esm_catalog_item.form_schema/esm_request.form_values JSONB, esm_catalog_form_field/esm_request_form_value 폐기)을 반영하고 있었음을 확인(코드 조사, `source/db/sql/16_esm_schema.sql` 기준). ESM은 이 전환이 적용된 적이 없으므로 실제 구현(레거시 EAV: esm_catalog_form_field/esm_request_form_value 유지)에 맞게 되돌림 |
| 2026-07-19 | 유지보수 요청 — `esm_catalog_form_field`/`esm_request_form_value`(EAV) 폐기하고 SRM과 동일한 `esm_catalog_item.form_schema`/`esm_request.form_values`(JSONB) 신규(4절). 백엔드 `FormSubmissionValidator`/`FormJsonMapper`(`com.itsm.common.form`)를 ESM도 재사용(SRM 전용에서 SRM/ESM 공용으로 전환). 기존 카탈로그 항목은 `form_schema`를 빈 그리드로 리셋(자동 배치 없음, 사용자 결정), 기존 제출 데이터(`esm_request_form_value`)는 `form_values`로 백필 후 EAV 테이블 DROP — SRM `36_srm_form_schema_jsonb.sql` 패턴 재사용 |

부서별 서비스 카탈로그(체크리스트 템플릿 포함), 부서 요청, HR 케이스, 온보딩/오프보딩 체크리스트·하위 작업을 정의한다. 코멘트·타임라인(상태 이력)은 [common.md](common.md)의 `comment`/`timeline_event`(ticket_type='ESM_REQUEST' 또는 'HR_CASE')를 재사용한다. 동적 양식은 [service-request.md](service-request.md)의 `form_schema`/`form_values`(JSONB) 아키텍처와 백엔드 서버 재검증 모듈(`com.itsm.common.form.FormSubmissionValidator`/`FormJsonMapper`)을 SRM과 공용으로 재사용한다(신규 컬럼 구조·검증 로직 중복 구현 없음).

**DB 접근 방식**: JPA(Spring Data JPA) — 기존 7개 코어 도메인과 동일(`source/backend` 기존 구현 확인).

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| esm_catalog_item.form_schema | 의도적 비정규화(JSON 문서) | [service-request.md](service-request.md) 1절과 동일한 근거 — 8×n 그리드 폼은 컴포넌트별 위치·크기·Content 설정을 함께 갖는 배열 구조라 EAV(행의 집합)로는 배치 정보를 표현할 수 없다. SRM과 동일 스키마를 그대로 소비한다. |
| esm_request.form_values | 의도적 비정규화(JSON 문서) | 제출 데이터는 필드별 집계·통계 로직이 없어 EAV로 분해해 얻는 실익이 없다. 컴포넌트 `key` 기준 key-value JSON을 가공 없이 저장한다(SRM `service_request.form_values`와 동일 패턴). |
| esm_checklist_template_task | 1NF | 카탈로그 항목당 체크리스트 하위 작업 템플릿이 가변 개수라 별도 행으로 분리. |
| esm_checklist_task | 3NF | 체크리스트(1) : 하위 작업(N) — 하위 작업은 체크리스트 생성 시점에 템플릿을 복제해 개별 상태를 갖는 실행 인스턴스이므로 템플릿 테이블과 분리. |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일. 이하 도메인 컬럼만 기술.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| esm_catalog_item | 부서별 요청 유형(카탈로그 항목, 동적 양식 스키마 포함) | REQ-ESM-001 |
| esm_checklist_template_task | 카탈로그 항목의 체크리스트 하위 작업 템플릿 | REQ-ESM-005/006 |
| esm_request | 부서 요청 티켓(양식 제출 데이터 포함) | REQ-ESM-002 |
| esm_hr_case | HR 케이스 | REQ-ESM-003/004 |
| esm_checklist | 온보딩/오프보딩 체크리스트 | REQ-ESM-005/006/007 |
| esm_checklist_task | 체크리스트 하위 작업(실행 인스턴스) | REQ-ESM-005/006/007 |

> **폐기**: `esm_catalog_form_field`(EAV)·`esm_request_form_value`(EAV)는 4절 `esm_catalog_item.form_schema`/`esm_request.form_values`(JSONB)로 대체되어 DROP한다.

## 4. 테이블 상세

### esm_catalog_item

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(150) | NOT NULL | 요청 유형명 |
| description | VARCHAR(500) | NULL | 설명 |
| department | VARCHAR(20) | NOT NULL | HR/LEGAL/FACILITIES/FINANCE |
| checklist_template_type | VARCHAR(15) | NOT NULL, DEFAULT 'NONE' | NONE/ONBOARDING/OFFBOARDING |
| form_schema | JSONB | NOT NULL, DEFAULT `{"components":[],"labels":[]}` | 동적 양식 스키마. [service-request.md](service-request.md) `service_catalog_item.form_schema`와 **완전히 동일한 구조**(자체 8×n 그리드 — `components`+최상위 `labels` 배열, 팔레트 9종, 상세 스키마는 [api_spec/service-request.md](../api_spec/service-request.md) API-SRM-002 참조). ESM 전용 필드·타입 추가 없음(`number` 타입 미도입, 기존 number 필드는 `text`+`validation.regex`로 흡수). SCR-ESM-006 "Form 설정" 팝업(SRM과 공용 컴포넌트)이 편집·저장 |
| ...공통 컬럼... | | | |

> **기존 데이터 리셋**: 기존 `esm_catalog_form_field`(EAV)는 `form_schema`와 구조가 호환되지 않아 자동 배치가 불가능하다(사용자 결정). 배포 시 전체 로우를 초기값(`{"components":[],"labels":[]}`)으로 리셋하고, 프로세스 오너가 배포 후 각 항목의 폼을 새 그리드 빌더로 다시 구성한다(SRM `38_srm_form_schema_reset.sql` 선례와 동일 패턴).

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
| form_values | JSONB | NOT NULL, DEFAULT `{}` | 양식 제출 데이터(key=그리드 컴포넌트 `key`인 key-value 맵, 레이아웃 무관). [service-request.md](service-request.md) `service_request.form_values`와 동일 구조. SCR-ESM-002 그리드 렌더러 제출 시 저장 |
| ...공통 컬럼... | | | |

> **기존 데이터 백필**: 기존 `esm_request_form_value`(EAV) 행을 `esm_request_id`별 `{field_key: field_value}` 객체로 조립해 `form_values`로 백필한 뒤 EAV 테이블을 DROP한다(SRM `36_srm_form_schema_jsonb.sql` 선례와 동일 패턴). 상세화면은 신규/과거 요청 구분 없이 단일 공용 렌더러로 조회한다.

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

- esm_catalog_item.form_schema는 JSONB 컬럼(관계형 FK 없음, 폼 전체가 카탈로그 항목 한 행에 저장 — SRM `service_catalog_item.form_schema`와 동일 패턴)
- esm_checklist_template_task.catalog_item_id → esm_catalog_item.id (FK)
- esm_request.catalog_item_id → esm_catalog_item.id, requester_id/assignee_id → app_user.id, checklist_id → esm_checklist.id (FK, UNIQUE)
- esm_request.form_values는 JSONB 컬럼(관계형 FK 없음, 제출 데이터가 요청 한 행에 저장)
- esm_checklist_task.checklist_id → esm_checklist.id (FK), related_asset_id → asset.id (FK, nullable)
- 코멘트는 common.comment(ticket_type='ESM_REQUEST'), 상태 이력은 common.timeline_event(ticket_type='ESM_REQUEST' 또는 'HR_CASE') 사용
- **사용자-부서 매핑**: 부서 요청 처리자·체크리스트 하위 작업 담당자의 "소속 부서" 판정을 위해 [auth.md](auth.md)의 `app_user`에 `department` 컬럼을 추가했다(HR/LEGAL/FACILITIES/FINANCE/IT, NULL 허용). 부서 요청 목록/처리 큐·내 하위 작업 목록 조회 시 `app_user.department = esm_request.department`(또는 `esm_checklist_task.department`) 조건으로 필터링한다.
