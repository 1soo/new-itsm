# 테이블 정의서 — IT 인프라 모니터링 & 용량관리 (Infra Monitoring & Capacity Management)

> 도메인: infra-monitoring · 버전: 0.1 · 작성일: 2026-07-10

인프라 지표, 지표 항목별 임계치·초과 알림, 자산 가동률 목표, 팀/서비스 용량 계획을 정의한다. 대상 자산은 [asset.md](asset.md)의 `asset`을 참조한다.

**DB 접근 방식**: JPA(Spring Data JPA) — 기존 코어 도메인과 동일.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| infra_metric | 3NF | 자산별·측정 시각별 지표가 시계열로 계속 누적되므로 자산과 분리한 append 테이블. |
| infra_metric_threshold | 3NF | 지표 항목(전역) 단위 설정이라 자산 테이블과 무관하게 독립 관리. |
| infra_metric_alert | 3NF | 임계치 초과 이벤트는 지표 등록 시점에 발생하는 별도 이력이라 `infra_metric`과 분리(자산 `expiry_alert`와 동일 패턴, [asset.md](asset.md)). |
| uptime_target / capacity_plan | 3NF | 목표값·계획값은 실제 지표와 별개 개념이라 독립 테이블로 분리, 실제값·활용률은 조회 시점 계산(캐시하지 않음). |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| infra_metric | 인프라 지표 레코드 | REQ-IOM-001/002 |
| infra_metric_threshold | 지표 항목별 임계치 | REQ-IOM-003 |
| infra_metric_alert | 임계치 초과 알림 | REQ-IOM-003 |
| uptime_target | 자산별 가동률 목표(SLA) | REQ-IOM-005 |
| capacity_plan | 팀/서비스별 용량 계획 | REQ-IOM-004 |

## 4. 테이블 상세

### infra_metric

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| asset_id | BIGINT | FK → asset.id, NOT NULL | 대상 자산 |
| metric_type | VARCHAR(20) | NOT NULL | UPTIME/CPU/MEMORY/RESPONSE_TIME |
| value | NUMERIC(10,2) | NOT NULL | 측정값 |
| measured_at | TIMESTAMPTZ | NOT NULL | 측정 시각 |
| ...공통 컬럼... | | | |

### infra_metric_threshold

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| metric_type | VARCHAR(20) | UNIQUE, NOT NULL | UPTIME/CPU/MEMORY/RESPONSE_TIME |
| upper_limit | NUMERIC(10,2) | NULL | 상한(미설정 가능) |
| lower_limit | NUMERIC(10,2) | NULL | 하한(미설정 가능) |
| ...공통 컬럼... | | | |

### infra_metric_alert

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| metric_id | BIGINT | FK → infra_metric.id, NOT NULL | 원인 지표 레코드 |
| asset_id | BIGINT | FK → asset.id, NOT NULL | 대상 자산(조회 성능용 비정규화) |
| metric_type | VARCHAR(20) | NOT NULL | 지표 항목(비정규화) |
| breached_value | NUMERIC(10,2) | NOT NULL | 초과 당시 값 |
| threshold_type | VARCHAR(10) | NOT NULL | UPPER/LOWER |
| acknowledged | BOOLEAN | NOT NULL, DEFAULT false | 확인 처리 여부 |
| ...공통 컬럼... | | | |

### uptime_target

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| asset_id | BIGINT | FK → asset.id, UNIQUE, NOT NULL | 대상 자산(1:1) |
| target_percentage | NUMERIC(5,2) | NOT NULL | 가동률 목표(%) |
| ...공통 컬럼... | | | |

### capacity_plan

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| team_or_service | VARCHAR(150) | NOT NULL | 팀/서비스명 |
| capacity | NUMERIC(12,2) | NOT NULL | 처리 역량 |
| demand | NUMERIC(12,2) | NOT NULL | 예상 수요 |
| ...공통 컬럼... | | | |

## 5. RBAC · 화면 관리 테이블

[auth.md](auth.md) 5절 참조(단일 원천). 관련 화면: SCR-IOM-001~005.

## 6. 관계 · 제약조건 요약

- infra_metric.asset_id, infra_metric_alert.asset_id, uptime_target.asset_id → asset.id (FK, [asset.md](asset.md))
- infra_metric_alert.metric_id → infra_metric.id (FK)
- (asset_id, metric_type, measured_at) 조회 인덱스 권장(시계열 대시보드·가동률 평균 계산)
- 실제 가동률·용량 활용률은 저장하지 않고 `infra_metric`/`capacity_plan` 조회 시점 계산값([api_spec/infra-monitoring.md](../api_spec/infra-monitoring.md) 0절)
