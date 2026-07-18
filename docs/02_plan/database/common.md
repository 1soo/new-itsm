# 테이블 정의서 — 공통 (Common)

> 도메인: common · 버전: 0.4

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |
| 2026-07-11 | 승인 프로세스 커스텀 기능 반영 — 기존 단일 승인 테이블 `approval`을 전 도메인 대상 커스텀 다차 승인 엔진(`approval_process*`/`approval_request*`/`approval_decision`)으로 대체 |
| 2026-07-15 | 승인 프로세스 범위 우선순위 재설계 — `approval_process.domain`을 nullable로 변경(도메인 미지정=전체 도메인 적용), `priority_tier` 산정식을 3축(도메인/요청유형/요청자역할) 독립 스코프 기반으로 재정의, 부분 유니크 제약에 `is_deleted=false` 조건 명시 |

여러 도메인이 공유하는 교차 관심사 테이블을 정의한다. 티켓 간 링크(인시던트↔문제↔변경, 자산/CI↔티켓, 지식↔티켓), 코멘트, 타임라인 이벤트, 승인 프로세스 커스텀 엔진(전 도메인 공용)을 다형(polymorphic) 구조로 관리한다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| ticket_link | 3NF·다형 참조 | 도메인마다 링크 테이블을 두면 중복되므로 `(source_type, source_id)`~`(target_type, target_id)` 다형 참조로 단일화. DB FK 대신 애플리케이션에서 대상 존재 검증(400 처리). |
| comment / timeline_event | 3NF·다형 참조 | 코멘트·타임라인은 티켓 유형 무관 동일 구조라 공용 테이블로 정규화. |
| approval_process ↔ approval_process_requester_role (다대다) | 3NF | 승인요청자 스코프 역할은 0~n개라 매핑 테이블로 분리(ANY 매칭 — 그중 하나라도 보유하면 매칭). |
| approval_process ↔ approval_process_step (1:N) | 1NF | 승인자 차수(n차, 최대 10차)는 가변 개수라 별도 행으로 분리, 승인자 박스 간 순서 교체(Drag&Drop)는 `step_no` 값 재기록으로 처리. |
| approval_process_step ↔ approval_process_step_role (다대다) | 3NF | 차수별 승인 역할은 1개 이상 복수 지정 가능이라 매핑 테이블로 분리. |
| approval_request_step / approval_request_step_role | 스냅샷 정규화 | 승인 인스턴스는 생성 시점 규칙(차수 수·역할·AND/OR)을 그대로 고정 보관해야 이후 규칙 정의가 바뀌어도 진행 중인 인스턴스에 영향을 주지 않는다. 정의 테이블(`approval_process*`)과 별도로 인스턴스 스냅샷 테이블을 둔다. |
| approval_decision | 3NF·append-only | AND 집계("각 역할 보유자 중 최소 1인씩 총 N명 모두 승인")를 판정하려면 차수 전체가 아니라 역할 단위로 결정을 기록해야 한다. |
| notification_dismissal | 1NF·정규화 최소화 | 알림은 별도 저장 엔티티 없이 여러 도메인 API를 FE가 조합해 구성하므로(단일 소스 없음), 사용자별 "확인처리했다"는 사실만 append-only로 기록하고 알림 원본(notification_type, source_id)은 비정규 보관(추적 대상 최소화, audit_log와 동일 성격). |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일(id, created_by/at, updated_by/at, is_deleted). 이하 도메인 컬럼만 기술. `approval_decision`은 append-only 성격이라 `updated_*`/`is_deleted` 미사용(`audit_log`·`refresh_token`과 동일 규칙).

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| ticket_link | 티켓/자산/지식 간 다형 링크 | REQ-INC-009, REQ-PRB-007/008, REQ-CHG-009, REQ-ITAM-006, REQ-KM-008 |
| comment | 티켓 공용 코멘트 | REQ-SRM-009 등 |
| timeline_event | 티켓 공용 타임라인 이벤트 | REQ-INC-006 등 |
| approval_process | 승인 프로세스 정의(규칙 헤더, 전 도메인 공용) | 유지보수 요청(승인 프로세스 커스텀) |
| approval_process_requester_role | 규칙의 승인요청자 역할 스코프(매칭용) | 상동 |
| approval_process_step | 규칙의 승인자 차수(n차) | 상동 |
| approval_process_step_role | 차수별 승인 역할 | 상동 |
| approval_request | 승인 인스턴스(티켓별 진행 헤더) | 상동 |
| approval_request_step | 인스턴스 차수 스냅샷 | 상동 |
| approval_request_step_role | 인스턴스 차수별 역할 스냅샷 | 상동 |
| approval_decision | 역할별 승인/반려 결정 기록 | 상동 |
| notification_dismissal | 사용자별 알림 확인처리(dismiss) 이력 | Main 요청(헤더 알림 확인처리) |

## 4. 테이블 상세

### ticket_link

두 엔티티 간 다형 링크. `*_type`은 SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE/ASSET/CI/KNOWLEDGE/VULNERABILITY/COMPLIANCE_REQUIREMENT.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| source_type | VARCHAR(25) | NOT NULL | 출발 엔티티 유형 |
| source_id | BIGINT | NOT NULL | 출발 엔티티 id |
| target_type | VARCHAR(25) | NOT NULL | 대상 엔티티 유형 |
| target_id | BIGINT | NOT NULL | 대상 엔티티 id |
| link_type | VARCHAR(30) | NULL | 관계 유형(RELATED/CAUSED_BY 등) |
| | | UNIQUE(source_type, source_id, target_type, target_id) | 중복 링크 방지 |
| ...공통 컬럼... | | | |

### comment

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| ticket_type | VARCHAR(20) | NOT NULL | 대상 티켓 유형 |
| ticket_id | BIGINT | NOT NULL | 대상 티켓 id |
| author_id | BIGINT | FK → app_user.id, NOT NULL | 작성자 |
| body | TEXT | NOT NULL | 코멘트 본문 |
| ...공통 컬럼... | | | |

### timeline_event

상태 변경·배정·업데이트 이력. 인시던트 내부/외부 공개 구분 포함.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| ticket_type | VARCHAR(20) | NOT NULL | 대상 티켓 유형 |
| ticket_id | BIGINT | NOT NULL | 대상 티켓 id |
| event_type | VARCHAR(30) | NOT NULL | STATUS_CHANGE/ASSIGN/UPDATE/ESCALATE 등 |
| message | TEXT | NULL | 이벤트 내용 |
| visibility | VARCHAR(10) | NOT NULL, DEFAULT 'INTERNAL' | INTERNAL / EXTERNAL |
| occurred_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 발생 시각 |
| ...공통 컬럼... | | | |

### approval_process

승인 프로세스 정의(관리자가 SCR-ADMIN-007에서 생성하는 규칙 헤더). **전 도메인 공용**이며, 도메인·요청유형·승인요청자 역할 3축을 각각 독립적으로 지정할 수 있고 각 축이 비어있으면(NULL 또는 역할 매핑 0개) 해당 축의 모든 값에 매칭되는 것으로 간주한다. 단, `request_subtype_key`는 도메인별 하위유형 어휘를 참조하므로 **`domain`이 NULL이면 `request_subtype_key`도 반드시 NULL**이어야 한다(도메인 없이 하위유형만 지정하는 조합은 허용하지 않음, 애플리케이션 검증). 런타임에는 대상 티켓의 (도메인, 요청유형값, 요청자 보유 역할)에 매칭되는 규칙 중 `priority_tier`가 가장 큰(가장 좁은 범위) 규칙 하나만 적용한다. 매칭되는 규칙이 없거나, 매칭된 규칙에 승인자 차수(`approval_process_step`)가 0개면 승인 없이 바로 진행한다(승인 인스턴스 미생성).

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| domain | VARCHAR(30) | NULL | 대상 도메인 코드(SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE/KNOWLEDGE 등, `ticket_link.*_type`과 동일 코드 체계). NULL=도메인 무관(전체 도메인 적용) |
| request_subtype_key | VARCHAR(50) | NULL | 요청 유형 스코프 값. 하위유형 개념이 있는 도메인만 사용(SRM=`service_catalog_item.id` 문자열화, CHANGE=`change_request.type` 코드값). NULL=하위유형 무관(전체). 하위유형 개념이 없는 도메인(INCIDENT/PROBLEM 등)과 `domain`이 NULL인 경우는 항상 NULL |
| priority_tier | SMALLINT | NOT NULL | 우선순위 캐시(축별 지정 여부로 저장 시 산정, 조회 성능 목적의 재계산 가능한 비정규 캐시). **산정식**: `priority_tier = (지정된 축 개수 × 10) + (요청자역할 지정 시 4) + (요청유형 지정 시 2) + (도메인 지정 시 1)`. 축 개수를 최상위 자릿수로 둬 "지정 축이 많을수록 우선"을 보장하고, 동일 개수 내에서는 가중치(역할4 > 요청유형2 > 도메인1)로 "역할 > 요청유형 > 도메인" 동률 우선순위를 보장한다. 실제 발생 가능한 값(요청유형은 도메인 지정 시에만 존재): 전체 미지정=0, 도메인만=11, 역할만=14, 도메인+요청유형=23, 도메인+역할=25, 도메인+요청유형+역할=37 (값이 클수록 우선 적용) |
| name | VARCHAR(150) | NOT NULL | 프로세스명(관리자 식별용) |
| description | VARCHAR(500) | NULL | 설명 |
| ...공통 컬럼... | | | |

> **부분 유니크 제약(PostgreSQL partial unique index, 모든 조건에 `is_deleted=false` 명시)**:
> - `UNIQUE(priority_tier) WHERE priority_tier=0 AND is_deleted=false` (전체 미지정 캐치올 규칙은 전 시스템에 1개만)
> - `UNIQUE(domain) WHERE priority_tier=11 AND is_deleted=false` (도메인만 지정 규칙은 도메인당 1개)
> - `UNIQUE(domain, request_subtype_key) WHERE priority_tier=23 AND is_deleted=false` (도메인+요청유형 지정 규칙은 도메인+유형 조합당 1개)
> - `priority_tier`가 14(역할만)/25(도메인+역할)/37(도메인+요청유형+역할)인 경우는 역할 "조합"의 교집합 여부를 판정해야 하므로 DB 제약만으로 표현 불가 — 생성/수정 시 애플리케이션이 **동일 `priority_tier` 버킷 + 동일 (domain, request_subtype_key) 매칭 조건**(tier=14는 domain도 request_subtype_key도 모두 NULL인 규칙 전체가 대상, tier=25는 동일 domain의 request_subtype_key NULL 규칙들이 대상, tier=37은 동일 (domain, request_subtype_key) 규칙들이 대상) 내 기존 규칙들의 `approval_process_requester_role.role_id` 집합과 겹치는지 조회해 검증하고, 겹치면 409로 저장을 막는다.

### approval_process_requester_role

규칙의 승인요청자 역할 스코프(승인 요청자 박스에서 선택한 역할). 0개면 요청자 무관(도메인/요청유형 tier), 1개 이상이면 그중 하나라도 보유한 사용자가 요청한 티켓에 매칭(ANY — AND/OR 개념 없음).

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| approval_process_id | BIGINT | FK → approval_process.id, NOT NULL | 소속 규칙 |
| role_id | BIGINT | FK → role.id, NOT NULL | 스코프 역할 |
| | | UNIQUE(approval_process_id, role_id) | 중복 방지 |
| ...공통 컬럼... | | | |

### approval_process_step

규칙의 승인자 차수(n차 승인자 박스). 최대 10차. 승인자 박스 간 Drag&Drop으로 순서를 교체하면 두 행의 `step_no` 값을 맞바꿔 저장한다(요청자 박스는 이 테이블에 포함되지 않으며 드래그·드롭 대상도 아님).

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| approval_process_id | BIGINT | FK → approval_process.id, NOT NULL | 소속 규칙 |
| step_no | SMALLINT | NOT NULL, CHECK (step_no BETWEEN 1 AND 10) | 차수(순서) |
| decision_mode | VARCHAR(5) | NOT NULL, DEFAULT 'OR' | AND/OR(역할이 2개 이상일 때만 의미, 1개면 무관) |
| | | UNIQUE(approval_process_id, step_no) | 차수 중복 방지 |
| ...공통 컬럼... | | | |

### approval_process_step_role

차수별 승인 역할(1개 이상).

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| step_id | BIGINT | FK → approval_process_step.id, NOT NULL | 소속 차수 |
| role_id | BIGINT | FK → role.id, NOT NULL | 승인 역할 |
| | | UNIQUE(step_id, role_id) | 중복 방지 |
| ...공통 컬럼... | | | |

### approval_request

승인 인스턴스 헤더(기존 `approval` 테이블 대체). 티켓이 승인 게이트가 걸린 전이를 시도할 때, 매칭된 `approval_process`에 승인자 차수가 1개 이상 있으면 생성된다(0차면 인스턴스를 만들지 않고 바로 진행). **전 도메인 공용 다형 참조**(`ticket_type`+`ticket_id`)로 어떤 도메인 티켓이든 가리킬 수 있다.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| ticket_type | VARCHAR(20) | NOT NULL | 대상 티켓 유형(다형 참조, `ticket_link`과 동일 코드 체계) |
| ticket_id | BIGINT | NOT NULL | 대상 티켓 id |
| approval_process_id | BIGINT | FK → approval_process.id, NOT NULL | 매칭 적용된 규칙(조회용 참조. 규칙은 soft delete라 삭제 후에도 참조 무결성 유지) |
| status | VARCHAR(15) | NOT NULL, DEFAULT 'IN_PROGRESS' | IN_PROGRESS/APPROVED/REJECTED |
| current_step_no | SMALLINT | NULL | 현재 대기 중인 차수(전체 승인 완료 시 NULL) |
| ...공통 컬럼... | | | |

> `(ticket_type, ticket_id)`에 조회 인덱스 권장. 티켓당 동시에 진행 중(`status='IN_PROGRESS'`)인 인스턴스는 애플리케이션이 1건으로 유지(하드 UNIQUE 제약을 두지 않는 이유는 반려 후 재제출 시 새 인스턴스가 이력으로 함께 남아야 하기 때문 — 과거 인스턴스는 `IN_PROGRESS`가 아니므로 공존 가능).

### approval_request_step

인스턴스 차수 스냅샷(규칙 정의가 이후 바뀌어도 진행 중 인스턴스는 생성 시점 값을 그대로 사용).

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| approval_request_id | BIGINT | FK → approval_request.id, NOT NULL | 소속 인스턴스 |
| step_no | SMALLINT | NOT NULL | 차수(규칙의 step_no 스냅샷) |
| decision_mode | VARCHAR(5) | NOT NULL | AND/OR 스냅샷 |
| status | VARCHAR(10) | NOT NULL, DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED/SKIPPED(이전 차수 반려로 이후 차수 진행 안 함) |
| | | UNIQUE(approval_request_id, step_no) | |
| ...공통 컬럼... | | | |

### approval_request_step_role

인스턴스 차수별 필요 역할 스냅샷(AND 집계의 분모).

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| step_id | BIGINT | FK → approval_request_step.id, NOT NULL | 소속 차수 |
| role_id | BIGINT | FK → role.id, NOT NULL | 필요 역할 스냅샷 |
| | | UNIQUE(step_id, role_id) | |
| ...공통 컬럼... | | | |

### approval_decision

역할별 승인/반려 결정 기록(append-only). **OR**: 차수 내 어느 역할이든 최초 1건이 기록되는 순간 해당 차수 전체가 그 결정(APPROVE/REJECT)으로 완료된다(기존 "공유 대기함 + 선처리자 결정" 패턴과 동일, 이후 처리 시도는 409). **AND**: 차수의 `approval_request_step_role` 각 역할마다 APPROVE 결정이 모두 쌓여야 차수가 APPROVED되며, 그중 하나라도 REJECT가 기록되면 즉시 차수 전체가 REJECTED된다(단, 다른 역할은 이미 결정된 것을 취소하지 않고 그대로 두되 차수·인스턴스 상태만 REJECTED로 확정). 처리 대상 사용자가 한 차수에서 요구하는 역할을 2개 이상 보유하면, 그 사용자의 1회 결정으로 보유한 모든 대상 역할 슬롯이 함께 채워진다.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| step_id | BIGINT | FK → approval_request_step.id, NOT NULL | 대상 차수 |
| role_id | BIGINT | FK → role.id, NOT NULL | 결정이 채우는 역할 슬롯(AND 집계 단위, OR도 동일 구조 재사용) |
| decided_by_id | BIGINT | FK → app_user.id, NOT NULL | 실제 결정한 사용자 |
| decision | VARCHAR(10) | NOT NULL | APPROVE/REJECT |
| reason | VARCHAR(500) | NULL | 승인/반려 의견·사유 |
| decided_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 결정 시각 |
| created_by | VARCHAR(100) | NOT NULL | |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | |
| | | UNIQUE(step_id, role_id) | 동일 역할 슬롯 중복 결정 방지(이미 결정된 슬롯 재처리 시도 → 409) |

### notification_dismissal

사용자가 헤더 알림 드롭다운(SCR-COM-002)에서 "모두 지우기"·개별 X로 확인처리한 알림 이력. append-only(수정 없음)라 `updated_*`/`is_deleted`는 미사용(`audit_log`·`refresh_token`과 동일 성격). 알림 원본은 별도 테이블 없이 여러 도메인 API로 조합되므로 `notification_type`+`source_id`로 식별해 비정규 보관한다.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| user_id | BIGINT | FK → app_user.id, NOT NULL | 확인처리한 사용자(본인) |
| notification_type | VARCHAR(30) | NOT NULL | APPROVAL(전 도메인 공용 승인 대기, 승인 프로세스 커스텀 기능으로 SERVICE_REQUEST_APPROVAL/CHANGE_APPROVAL 등 도메인별 값에서 통합) / ASSET_EXPIRY |
| source_id | BIGINT | NOT NULL | 알림 원본 식별자(APPROVAL이면 approval_request.id, ASSET_EXPIRY면 자산 id) |
| dismissed_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 확인처리 시각 |
| created_by | VARCHAR(100) | NOT NULL | |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | |
| | | UNIQUE(user_id, notification_type, source_id) | 동일 알림 중복 확인처리 방지(API-COM-001 멱등 처리 근거) |

## 5. RBAC · 화면 관리 테이블

RBAC/화면 매핑 테이블(`screen`, `user_role`, `screen_role`)은 [auth.md](auth.md) 5절 참조(단일 원천). 승인 프로세스 정의 CRUD(SCR-ADMIN-007)는 SYSTEM_ADMIN 전용이며 `screen_role` 매핑도 SYSTEM_ADMIN 역할에만 부여한다.

## 6. 관계 · 제약조건 요약

- comment.author_id → app_user.id (FK)
- ticket_link: 다형 참조라 DB FK 대신 애플리케이션 레벨에서 대상 존재 검증. UNIQUE(source_type, source_id, target_type, target_id)
- (ticket_type, ticket_id) 조합에 조회 인덱스 권장: comment, timeline_event, approval_request
- approval_process_requester_role.approval_process_id → approval_process.id, role_id → role.id (FK), UNIQUE(approval_process_id, role_id)
- approval_process_step.approval_process_id → approval_process.id (FK), UNIQUE(approval_process_id, step_no), CHECK(step_no BETWEEN 1 AND 10)
- approval_process_step_role.step_id → approval_process_step.id, role_id → role.id (FK), UNIQUE(step_id, role_id)
- approval_process: 부분 유니크(tier=0 전체 1개, tier=11 도메인당 1개, tier=23 도메인+요청유형당 1개, 모두 `is_deleted=false` 조건 포함), tier=14/25/37 역할 조합 중복은 애플리케이션 검증(4절 approval_process 상세 참조)
- approval_request.approval_process_id → approval_process.id (FK), (ticket_type, ticket_id) 인덱스 권장
- approval_request_step.approval_request_id → approval_request.id (FK), UNIQUE(approval_request_id, step_no)
- approval_request_step_role.step_id → approval_request_step.id, role_id → role.id (FK), UNIQUE(step_id, role_id)
- approval_decision.step_id → approval_request_step.id, role_id → role.id, decided_by_id → app_user.id (FK), UNIQUE(step_id, role_id)
- notification_dismissal.user_id → app_user.id (FK), UNIQUE(user_id, notification_type, source_id). (user_id, notification_type, source_id) 조합에 조회 인덱스 권장(API-COM-002 확인처리 이력 조회)
