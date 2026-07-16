# 테이블 정의서 — 지식 관리 (Knowledge)

> 도메인: knowledge · 버전: 0.1 · 작성일: 2026-07-09

## 변경 이력

| 날짜 | 요약 |
|------|------|
| 2026-07-09 | 최초 작성 |

지식 기사, 카테고리/라벨, 유용성 피드백, 검토 이력, 검색 로그(무결과 지표)를 정의한다. 티켓 연계(KCS)는 [common.md](common.md)의 `ticket_link`(target_type='KNOWLEDGE')를 사용한다.

## 1. 정규화 방침

| 대상 | 적용 정규화 | 근거 (왜 필요한가) |
|------|-------------|--------------------|
| knowledge_article ↔ label (다대다) | 3NF | 기사-라벨은 다대다라 `article_label` 매핑 테이블로 분리. |
| knowledge_category | 3NF | 카테고리를 참조로 분리해 중복 제거·분류별 조회 지원. |
| helpful_count / not_helpful_count | 비정규 집계 | 유용성 평점 조회 성능을 위해 기사에 집계값 캐시(원 피드백은 knowledge_feedback 보관). |
| search_log | 1NF | 무결과 검색·사용량 지표 산출을 위한 append-only 로그. |

## 2. 공통 컬럼 규칙

[auth.md](auth.md) 2절과 동일.

## 3. 테이블 목록

| 테이블명 | 설명 | 관련 요구사항 |
|----------|------|---------------|
| knowledge_category | 기사 카테고리 | REQ-KM-007 |
| knowledge_label | 라벨 | REQ-KM-007 |
| knowledge_article | 지식 기사 | REQ-KM-001~005 |
| article_label | 기사-라벨 매핑 | REQ-KM-007 |
| knowledge_feedback | 유용성 평가 | REQ-KM-006 |
| knowledge_review | 검토·게시 이력 | REQ-KM-003 |
| search_log | 검색 로그(무결과 지표) | REQ-KM-004/009 |

## 4. 테이블 상세

### knowledge_category

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(100) | UNIQUE, NOT NULL | 카테고리명 |
| ...공통 컬럼... | | | |

### knowledge_label

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(100) | UNIQUE, NOT NULL | 라벨명 |
| ...공통 컬럼... | | | |

### knowledge_article

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| title | VARCHAR(300) | NOT NULL | 제목(필수) |
| body | TEXT | NOT NULL | 본문(필수) |
| status | VARCHAR(15) | NOT NULL, DEFAULT 'DRAFT' | DRAFT/IN_REVIEW/PUBLISHED |
| category_id | BIGINT | FK → knowledge_category.id, NULL | 카테고리 |
| author_id | BIGINT | FK → app_user.id, NOT NULL | 기여자 |
| helpful_count | INT | NOT NULL, DEFAULT 0 | 도움됨 집계 |
| not_helpful_count | INT | NOT NULL, DEFAULT 0 | 도움안됨 집계 |
| view_count | INT | NOT NULL, DEFAULT 0 | 사용량(열람 수) |
| published_at | TIMESTAMPTZ | NULL | 게시 시각 |
| ...공통 컬럼... | | | |

### article_label

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| article_id | BIGINT | FK → knowledge_article.id, NOT NULL | 기사 |
| label_id | BIGINT | FK → knowledge_label.id, NOT NULL | 라벨 |
| | | UNIQUE(article_id, label_id) | |
| ...공통 컬럼... | | | |

### knowledge_feedback

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| article_id | BIGINT | FK → knowledge_article.id, NOT NULL | 대상 기사 |
| user_id | BIGINT | FK → app_user.id, NOT NULL | 평가자 |
| helpful | BOOLEAN | NOT NULL | 도움됨 여부 |
| comment | VARCHAR(500) | NULL | 피드백 |
| ...공통 컬럼... | | | |

### knowledge_review

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| article_id | BIGINT | FK → knowledge_article.id, NOT NULL | 대상 기사 |
| gatekeeper_id | BIGINT | FK → app_user.id, NOT NULL | 게이트키퍼 |
| decision | VARCHAR(10) | NOT NULL | APPROVE/REJECT |
| reason | VARCHAR(500) | NULL | 반려 사유 |
| ...공통 컬럼... | | | |

### search_log

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| keyword | VARCHAR(255) | NOT NULL | 검색어 |
| result_count | INT | NOT NULL | 결과 건수(0=무결과) |
| user_id | BIGINT | FK → app_user.id, NULL | 검색 사용자 |
| searched_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 검색 시각 |
| created_by | VARCHAR(100) | NOT NULL | |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | |

## 5. RBAC · 화면 관리 테이블

[auth.md](auth.md) 5절 참조. 관련 화면: SCR-KM-001~005.

## 6. 관계 · 제약조건 요약

- knowledge_article.category_id → knowledge_category.id, author_id → app_user.id (FK)
- article_label.article_id → knowledge_article.id, label_id → knowledge_label.id (FK), UNIQUE(article_id, label_id)
- knowledge_feedback / knowledge_review.article_id → knowledge_article.id (FK)
- KCS 티켓 연계는 common.ticket_link(target_type='KNOWLEDGE'), 미게시 기사 접근 통제는 status로 판정
