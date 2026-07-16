# 요구사항 정의서 — 인시던트 관리 (Incident Management)

> 도메인: incident · 버전: 0.1 · 작성일: 2026-07-09

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |

## 1. 개요

계획되지 않은 서비스 중단·저하 이벤트를 신속히 탐지·대응·복구·학습하는 프로세스를 지원한다. 인시던트 등록, 심각도(SEV)/우선순위, 담당자·역할 배정, 에스컬레이션, 상태 커뮤니케이션, 해결, 포스트모템, 시간 지표(MTTA/MTTD/MTTR)를 제공한다. (출처: `incident-management.md`)

## 2. 범위

### 포함 (In Scope)

- 인시던트 등록(탐지·수동 생성)과 핵심 필드(요약·설명·심각도·영향 서비스·영향 제품)
- 심각도(SEV 1~3) 및 우선순위 관리
- 라이프사이클 상태 전이(신규→대응중→해결→종료)
- 역할 배정(Incident Manager, Tech Lead, Communications Manager, Scribe 등)
- 에스컬레이션(계층적·기능적·자동)
- 상태 업데이트/커뮤니케이션(내부·외부 구분) 및 타임라인 기록
- 해결 처리 및 시간 지표 계산(탐지·영향시작·영향종료 → MTTD/MTTA/MTTR)
- 포스트모템(blameless, 5 Whys, 근본원인·조치항목) 작성
- 문제 관리 연계(인시던트→문제 등록)

### 미포함 (Out of Scope)

- 외부 모니터링/알림 도구 연동(OpsGenie 등)·자동 페이징
- Status Page·SMS·소셜미디어 등 외부 채널 자동 발송
- 재해 복구(DR)·ITSCM·SLA/SLO/Error Budget 정책 관리

## 3. 요구사항 목록

| ID | 유형 | 요구사항 | 우선순위 | 출처 |
|----|------|----------|----------|------|
| REQ-INC-001 | 기능 | 사용자는 인시던트를 등록하고 핵심 필드를 기입할 수 있다 | High | Detect/Raise 단계, 필드(Summary·Severity·Faulty service 등) |
| REQ-INC-002 | 기능 | 시스템은 인시던트에 심각도(SEV 1~3)와 우선순위를 부여·변경할 수 있다 | High | Severity Levels, 심각도 vs 우선순위 |
| REQ-INC-003 | 기능 | 인시던트는 정의된 라이프사이클 상태로 전이된다 | High | 대응 라이프사이클(Detect→Resolve) |
| REQ-INC-004 | 기능 | Incident Manager는 대응 역할을 배정할 수 있다 | High | 역할과 책임(IM·Tech Lead·Comms·Scribe) |
| REQ-INC-005 | 기능 | 사용자는 인시던트를 에스컬레이션할 수 있다 | High | 에스컬레이션 3경로 |
| REQ-INC-006 | 기능 | 사용자는 상태 업데이트를 등록하고 타임라인에 기록한다 | High | 소통 개시·후속 공지, 타임라인 |
| REQ-INC-007 | 기능 | 시스템은 해결 시각을 기록하고 시간 지표(MTTD/MTTA/MTTR)를 계산한다 | High | Resolve 단계, TTR/TTD, 공통 시간 지표 |
| REQ-INC-008 | 기능 | 사용자는 인시던트에 대한 포스트모템을 작성할 수 있다 | High | 포스트모템(blameless·5 Whys·근본원인·조치) |
| REQ-INC-009 | 기능 | 사용자는 인시던트를 문제(Problem)로 연결·등록할 수 있다 | Med | 인시던트↔문제 관계, 포스트모템→문제관리 |
| REQ-INC-010 | 비기능 | 시스템은 인시던트 지표(건수·MTTR 등)를 리포팅한다 | Med | KPI/Common Metrics |

## 4. 인수 기준 (Acceptance Criteria · EARS)

### REQ-INC-001

- (Event-driven) **WHEN** 사용자가 요약·설명·심각도·영향 서비스를 포함해 인시던트를 등록하면, 시스템은 인시던트를 생성하고 식별키를 반환해야 한다.
- (Unwanted) **IF** 필수 필드(요약·심각도)가 누락되면, **THEN** 시스템은 생성을 거부해야 한다.

### REQ-INC-002

- (Event-driven) **WHEN** 사용자가 심각도/우선순위를 지정·변경하면, 시스템은 값을 갱신하고 이력에 남겨야 한다.
- (Ubiquitous) 시스템은 심각도(SEV 1~3)와 우선순위를 독립적으로 관리해야 한다.

### REQ-INC-003

- (Event-driven) **WHEN** 담당자가 상태를 전이(예: 신규→대응중)하면, 시스템은 허용된 전이만 반영해야 한다.
- (Unwanted) **IF** 허용되지 않은 상태 전이를 시도하면, **THEN** 시스템은 이를 거부해야 한다.

### REQ-INC-004

- (Event-driven) **WHEN** Incident Manager가 특정 사용자에게 역할(Tech Lead·Comms·Scribe 등)을 배정하면, 시스템은 배정을 기록·표시해야 한다.
- (Unwanted) **IF** Incident Manager 권한이 없는 사용자가 역할을 배정하려 하면, **THEN** 시스템은 403을 반환해야 한다.

### REQ-INC-005

- (Event-driven) **WHEN** 사용자가 인시던트를 상위/전문 담당자에게 에스컬레이션하면, 시스템은 대상에게 배정·통지하고 이력에 기록해야 한다.

### REQ-INC-006

- (Event-driven) **WHEN** 사용자가 상태 업데이트(내부/외부 구분)를 등록하면, 시스템은 타임라인에 타임스탬프와 함께 기록해야 한다.

### REQ-INC-007

- (Event-driven) **WHEN** 인시던트가 해결되면, 시스템은 해결 시각을 기록하고 영향시작·탐지·영향종료 시각으로 MTTD/MTTA/MTTR를 계산해야 한다.
- (Unwanted) **IF** 필요한 시각 정보(영향시작 등)가 없으면, **THEN** 시스템은 계산 불가 항목을 공란/미산정으로 표시해야 한다.

### REQ-INC-008

- (Event-driven) **WHEN** 사용자가 포스트모템(요약·타임라인·5 Whys·근본원인·조치항목)을 작성·제출하면, 시스템은 이를 인시던트에 연결·저장해야 한다.
- (State-driven) **WHILE** SEV 1·2 인시던트가 해결되었으나 포스트모템이 없는 동안, 시스템은 포스트모템 필요 상태로 표시해야 한다.

### REQ-INC-009

- (Event-driven) **WHEN** 사용자가 인시던트에서 문제 등록/연결을 실행하면, 시스템은 인시던트와 문제를 링크해야 한다.

### REQ-INC-010

- (Ubiquitous) 시스템은 기간별 인시던트 건수·심각도 분포·평균 MTTR를 집계·조회할 수 있어야 한다.
