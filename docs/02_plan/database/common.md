# 테이블 정의서 — 공통 (Common)

> 도메인: common · 버전: 0.1 · 작성일: 2026-07-09

여러 도메인이 공유하는 교차 관심사 테이블을 정의한다. 티켓 간 링크(인시던트↔문제↔변경, 자산/CI↔티켓, 지식↔티켓), 코멘트, 타임라인 이벤트, 승인(서비스요청·변경 공용)을 다형(polymorphic) 구조로 관리한다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| ticket_link | 3NF·다형 참조 | 도메인마다 링크 테이블을 두면 중복되므로 `(source_type, source_id)`~`(target_type, target_id)` 다형 참조로 단일화. DB FK 대신 애플리케이션에서 대상 존재 검증(400 처리). |
| comment / timeline_event | 3NF·다형 참조 | 코멘트·타임라인은 티켓 유형 무관 동일 구조라 공용 테이블로 정규화. |
| approval | 3NF | 서비스요청·변경이 동일한 승인 개념을 공유하므로 공용 테이블로 분리. |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일(id, created_by/at, updated_by/at, is_deleted). 이하 도메인 컬럼만 기술.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| ticket_link | 티켓/자산/지식 간 다형 링크 | REQ-INC-009, REQ-PRB-007/008, REQ-CHG-009, REQ-ITAM-006, REQ-KM-008 |
| comment | 티켓 공용 코멘트 | REQ-SRM-009 등 |
| timeline_event | 티켓 공용 타임라인 이벤트 | REQ-INC-006 등 |
| approval | 승인(서비스요청·변경 공용) | REQ-SRM-005, REQ-CHG-005 |

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

### approval

서비스요청·변경 승인 공용. **역할 기반 승인** 모델: 승인 담당 역할(`approver_role`)을 보유한 사용자라면 공유 대기함에서 처리하며, 먼저 처리한 사용자가 결정한다(`decided_by_id`에 기록). 인가 검증(403)은 요청 사용자의 role claim에 `approver_role` 포함 여부로 판정.

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| ticket_type | VARCHAR(20) | NOT NULL | SERVICE_REQUEST / CHANGE |
| ticket_id | BIGINT | NOT NULL | 대상 티켓 id |
| approver_role | VARCHAR(50) | NOT NULL | 승인 담당 역할(role.role_code). SRM=요청 유형의 approver_role(기본 APPROVER), CHANGE=승인 경로(CAB/동료검토→APPROVER) |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| decided_by_id | BIGINT | FK → app_user.id, NULL | 실제 결정한 사용자(먼저 처리) |
| decision_reason | VARCHAR(500) | NULL | 승인/반려 의견·사유 |
| decided_at | TIMESTAMPTZ | NULL | 결정 시각 |
| ...공통 컬럼... | | | |

## 5. RBAC · 화면 관리 테이블

RBAC/화면 매핑 테이블(`screen`, `user_role`, `screen_role`)은 [auth.md](auth.md) 5절 참조(단일 원천).

## 6. 관계 · 제약조건 요약

- comment.author_id → app_user.id (FK), approval.decided_by_id → app_user.id (FK, nullable)
- approval.approver_role → role.role_code (논리 참조, 인가 판정에 사용)
- ticket_link: 다형 참조라 DB FK 대신 애플리케이션 레벨에서 대상 존재 검증. UNIQUE(source_type, source_id, target_type, target_id)
- (ticket_type, ticket_id) 조합에 조회 인덱스 권장: comment, timeline_event, approval
