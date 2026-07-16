# 테이블 정의서 — IT 자산 관리 / CMDB (Asset)

> 도메인: asset · 버전: 0.1 · 작성일: 2026-07-09

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |

자산, 유형별 속성, 생애주기 이력, 만료 알림, 구성 항목(CI)과 CI 관계(CMDB)를 정의한다. 티켓 연계는 [common.md](common.md)의 `ticket_link`를 사용한다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| asset_attribute | 1NF·EAV | HW/SW/클라우드 유형별 표준 속성이 상이·가변이라 (자산, 키, 값) EAV로 저장. |
| asset_lifecycle_history | 3NF | 생애주기 단계 전이 이력을 별도 테이블에 append. |
| ci_relation (자기참조 다대다) | 3NF | CI 간 의존 관계는 CI 자기참조 다대다라 관계 테이블로 분리(영향 범위 그래프 탐색). |
| expiry_alert | 3NF | 라이선스·보증·계약별 만료 알림을 자산과 1:N으로 분리. |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| asset | 자산 | REQ-ITAM-001~004 |
| asset_attribute | 유형별 속성 | REQ-ITAM-003 |
| asset_lifecycle_history | 생애주기 이력 | REQ-ITAM-002 |
| expiry_alert | 만료 임박 알림 | REQ-ITAM-004 |
| configuration_item | 구성 항목(CI) | REQ-ITAM-005 |
| ci_relation | CI 간 의존 관계(CMDB) | REQ-ITAM-005/007 |

## 4. 테이블 상세

### asset

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| asset_key | VARCHAR(20) | UNIQUE, NOT NULL | AST-#### |
| name | VARCHAR(200) | NOT NULL | 자산명(필수) |
| type | VARCHAR(15) | NOT NULL | HARDWARE/SOFTWARE/CLOUD |
| status | VARCHAR(15) | NOT NULL, DEFAULT 'PLANNING' | PLANNING/PROCUREMENT/OPERATION/MAINTENANCE/RETIREMENT |
| owner | VARCHAR(100) | NULL | 소유자 |
| location | VARCHAR(150) | NULL | 위치 |
| purchase_date | DATE | NULL | 구매일 |
| cost | NUMERIC(15,2) | NULL | 비용 |
| license_expiry | DATE | NULL | 라이선스 만료 |
| warranty_expiry | DATE | NULL | 보증 만료 |
| contract_expiry | DATE | NULL | 계약 만료 |
| ...공통 컬럼... | | | |

### asset_attribute

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| asset_id | BIGINT | FK → asset.id, NOT NULL | 소속 자산 |
| attr_key | VARCHAR(50) | NOT NULL | 속성 키 |
| attr_value | VARCHAR(500) | NULL | 속성 값 |
| | | UNIQUE(asset_id, attr_key) | |
| ...공통 컬럼... | | | |

### asset_lifecycle_history

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| asset_id | BIGINT | FK → asset.id, NOT NULL | 소속 자산 |
| stage | VARCHAR(15) | NOT NULL | 전이 단계 |
| changed_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 전이 시각 |
| ...공통 컬럼... | | | |

### expiry_alert

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| asset_id | BIGINT | FK → asset.id, NOT NULL | 소속 자산 |
| expiry_type | VARCHAR(15) | NOT NULL | LICENSE/WARRANTY/CONTRACT |
| due_date | DATE | NOT NULL | 만료일 |
| notified | BOOLEAN | NOT NULL, DEFAULT false | 알림 발송 여부 |
| ...공통 컬럼... | | | |

### configuration_item

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(200) | NOT NULL | CI명 |
| type | VARCHAR(50) | NULL | CI 유형 |
| asset_id | BIGINT | FK → asset.id, NULL | 연결 자산(선택) |
| ...공통 컬럼... | | | |

### ci_relation

CI 자기참조 의존 관계. 영향 범위 조회 시 그래프 탐색.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| source_ci_id | BIGINT | FK → configuration_item.id, NOT NULL | 출발 CI |
| target_ci_id | BIGINT | FK → configuration_item.id, NOT NULL | 대상 CI |
| relation_type | VARCHAR(20) | NOT NULL | DEPENDS_ON/RUNS_ON/CONNECTS_TO |
| | | UNIQUE(source_ci_id, target_ci_id, relation_type) | |
| | | CHECK (source_ci_id <> target_ci_id) | 자기참조 금지 |
| ...공통 컬럼... | | | |

## 5. RBAC · 화면 관리 테이블

[auth.md](auth.md) 5절 참조. 관련 화면: SCR-ITAM-001~005.

## 6. 관계 · 제약조건 요약

- asset_attribute / asset_lifecycle_history / expiry_alert.asset_id → asset.id (FK)
- asset_attribute UNIQUE(asset_id, attr_key)
- configuration_item.asset_id → asset.id (FK, nullable)
- ci_relation.source_ci_id / target_ci_id → configuration_item.id (FK), UNIQUE(source_ci_id, target_ci_id, relation_type), CHECK 자기참조 금지
- 티켓 연계는 common.ticket_link(source_type='ASSET' 또는 'CI')
