# 테이블 정의서 — 컴플라이언스 관리 (Compliance Management)

> 도메인: compliance · 버전: 0.1 · 작성일: 2026-07-10

컴플라이언스 요구사항, 시정조치를 정의한다. 변경 요청 연계는 [common.md](common.md)의 `ticket_link`, 감사 로그는 [auth.md](auth.md)의 `audit_log`를 재사용한다.

**DB 접근 방식**: JPA(Spring Data JPA) — 기존 코어 도메인과 동일.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| corrective_action | 3NF | 요구사항(1) : 시정조치(N) — 요구사항당 시정조치가 가변 개수라 별도 테이블로 분리. |
| compliance_status(준수 상태) | 비저장 계산값 | `corrective_action`에 미해결(DETECTED/IN_PROGRESS) 건이 있는지로 조회 시점에 산정하며 컬럼으로 저장하지 않는다(동기화 불일치 방지, [api_spec/compliance.md](../api_spec/compliance.md) 0절). |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| compliance_requirement | 컴플라이언스 요구사항 | REQ-COMP-001/002 |
| corrective_action | 시정조치 항목 | REQ-COMP-003 |

## 4. 테이블 상세

### compliance_requirement

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| requirement_key | VARCHAR(20) | UNIQUE, NOT NULL | COMP-YYYY-#### |
| name | VARCHAR(200) | NOT NULL | 이름(필수) |
| basis | VARCHAR(500) | NOT NULL | 근거(규제 조항/내부 정책, 필수) |
| scope | VARCHAR(500) | NULL | 적용 범위 |
| owner_id | BIGINT | FK → app_user.id, NULL | 책임자(오너), NULL이면 "책임자 미지정" |
| ...공통 컬럼... | | | |

### corrective_action

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| requirement_id | BIGINT | FK → compliance_requirement.id, NOT NULL | 소속 요구사항 |
| description | VARCHAR(500) | NOT NULL | 시정조치 내용 |
| status | VARCHAR(15) | NOT NULL, DEFAULT 'DETECTED' | DETECTED/IN_PROGRESS/RESOLVED |
| ...공통 컬럼... | | | |

## 5. RBAC · 화면 관리 테이블

[auth.md](auth.md) 5절 참조(단일 원천). 관련 화면: SCR-COMP-001~004.

## 6. 관계 · 제약조건 요약

- compliance_requirement.owner_id → app_user.id (FK, nullable)
- corrective_action.requirement_id → compliance_requirement.id (FK)
- 변경 요청 연계는 common.ticket_link(source_type='COMPLIANCE_REQUIREMENT', target_type='CHANGE') — [change.md](change.md) API-CHG-003 응답에도 `COMPLIANCE_REQUIREMENT` 타입으로 노출
- 감사 로그는 auth.audit_log(event_type='COMPLIANCE_REQ_CREATE'/'COMPLIANCE_REQ_UPDATE'/'COMPLIANCE_ACTION_STATUS_CHANGE', target='COMPLIANCE_REQUIREMENT:{id}' 또는 'CORRECTIVE_ACTION:{id}')
- (requirement_id, status) 조회 인덱스 권장(corrective_action, 미해결 건수 집계용)
